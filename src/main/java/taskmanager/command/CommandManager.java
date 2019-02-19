package taskmanager.command;

import taskmanager.data.ProgressData;
import taskmanager.persistence.PersistenceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.lineSeparator;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static taskmanager.data.ProgressData.Status.INITIAL;

public class CommandManager {

    private static final String progressFile = "saved.dat";

    static final ProgressData POISON = new ProgressData("POISON");

    private final BlockingQueue<ProgressData> initialQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ProgressData> downloadedQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ProgressData> processedQueue = new LinkedBlockingQueue<>();
    private final AtomicLong downloadCounter = new AtomicLong();
    private final AtomicLong countWordsCounter = new AtomicLong();
    private final AtomicLong deleteCounter = new AtomicLong();
    private List<ProgressData> data = new ArrayList<>();

    private long downloadTimeout;
    private long processingTimeout;
    private long reportPeriodicity;

    public CommandManager(long downloadTimeout, long processingTimeout, long reportPeriodicity) {
        this.downloadTimeout = downloadTimeout;
        this.processingTimeout = processingTimeout;
        this.reportPeriodicity = reportPeriodicity;
    }

    public void processCommands(List<String> cmdLines) throws InterruptedException {
        ExecutorService downloadService = initiateDownloadCommands(cmdLines);
        launchPersister();

        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService processingExecutor = initiateProcessingCommands(processors);

        startStatDaemon(reportPeriodicity);

        downloadService.awaitTermination(downloadTimeout, MILLISECONDS);

        sendPoison(processors);

        processingExecutor.awaitTermination(processingTimeout, MILLISECONDS);
        printReport();
    }

    private void process() {

    }


    private List<Command> initiateCommands(int processorsCount) {
        List<Command> commands = new ArrayList<>();
        for (int i = 0; i < processorsCount; i++) {
            commands.add(new DownloadCommand(initialQueue, downloadedQueue, downloadCounter, POISON));
            commands.add(new CountWordsCommand(downloadedQueue, processedQueue, countWordsCounter, POISON));
            commands.add(new DeleteCommand(processedQueue, deleteCounter, POISON));
        }
        return commands;
    }


    private ExecutorService initiateDownloadCommands(List<String> cmdLines) throws InterruptedException {
        List<Command> commands = cmdLines.stream().map(this::parseCommand).collect(toList());
        for (ProgressData progressData : data) {
            initialQueue.put(progressData);
        }

        ExecutorService downloadService = Executors.newCachedThreadPool();
        commands.forEach(downloadService::execute);
        downloadService.shutdown();
        return downloadService;
    }

    public void restoreCommands() throws IOException, ClassNotFoundException, InterruptedException {
        this.data = (List<ProgressData>) PersistenceUtil.restoreObject(progressFile);
        List<Command> commands = new ArrayList<>();
        for (ProgressData progressData : data) {
            if (progressData.getStatus() == INITIAL) {
                commands.add(new DownloadCommand(initialQueue, downloadedQueue, downloadCounter));
                initialQueue.add(progressData);
            } else {
                downloadedQueue.put(progressData);
            }
        }
    }

    public ExecutorService initiateProcessingCommands(int processingCmdsCount) {
        List<Command> commands = new ArrayList<>();
        for (int i = 0; i < processingCmdsCount; i++) {
            commands.add(new CountWordsCommand(downloadedQueue, processedQueue, countWordsCounter, POISON));
            commands.add(new DeleteCommand(processedQueue, deleteCounter, POISON));
        }
        ExecutorService processorService = Executors.newFixedThreadPool(processingCmdsCount * 2);
        commands.forEach(processorService::execute);
        processorService.shutdown();

        return processorService;
    }


    private Command parseCommand(String cmdLine) {

        String[] cmd = cmdLine.split("\\s+");

        switch (cmd[0].toLowerCase()) {
            case "d":
            case "download":
            case "dw":
                data.add(new ProgressData(cmd[1]));
                return new DownloadCommand(initialQueue, downloadedQueue, downloadCounter);
            default:
                throw new IllegalStateException("Unknown command " + cmd[0]);

        }
    }

    public void sendPoison(int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            downloadedQueue.put(POISON);
//            processedQueue.put(POISON);
        }
    }

    public void startStatDaemon(long interval) {
        Thread stat = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                printReport();
//                System.exit(0);
            }
        });
        stat.setDaemon(true);
        stat.start();
    }

    public void printReport() {
        System.out.println("******Application report******" + lineSeparator()
                + "Files downloaded: " + downloadCounter.get() + lineSeparator()
                + "Files processed: " + countWordsCounter.get() + lineSeparator()
                + "Files deleted: " + deleteCounter.get() + lineSeparator()
                + "******************************");
    }

    private void launchPersister() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                PersistenceUtil.persistObject(data, progressFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

}
