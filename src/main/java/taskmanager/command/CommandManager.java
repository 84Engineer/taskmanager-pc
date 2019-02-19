package taskmanager.command;

import taskmanager.data.ProgressData;
import taskmanager.persistence.PersistenceManager;
import taskmanager.stat.ReportManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class CommandManager {

    private static final String progressFile = "saved.dat";

    static final ProgressData POISON = new ProgressData("POISON");

    private final BlockingQueue<ProgressData> initialQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ProgressData> downloadedQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ProgressData> processedQueue = new LinkedBlockingQueue<>();

    private final AtomicLong downloadCounter = new AtomicLong();
    private final AtomicLong countWordsCounter = new AtomicLong();
    private final AtomicLong deleteCounter = new AtomicLong();

    private final List<String> downloadCmdNames = asList("d", "download", "dw");

    private final ReportManager reportManager;
    private final PersistenceManager<List<ProgressData>> persistenceManager;

    private long timeout;

    public CommandManager(long timeout, long reportPeriodicity) {
        this.timeout = timeout;
        this.reportManager = new ReportManager(
                () -> "******Application report******" + lineSeparator()
                        + "Files downloaded: " + downloadCounter.get() + lineSeparator()
                        + "Files processed: " + countWordsCounter.get() + lineSeparator()
                        + "Files deleted: " + deleteCounter.get() + lineSeparator()
                        + "******************************",
                reportPeriodicity);
        this.persistenceManager = new PersistenceManager<>(progressFile);
    }

    public void processCommands(List<String> cmdLines) throws InterruptedException {
        process(parseCommands(cmdLines));
    }

    public void restoreCommands() throws IOException, ClassNotFoundException, InterruptedException {
        process(persistenceManager.restoreObject());
    }


    private void process(List<ProgressData> progressData) throws InterruptedException {
        ExecutorService service = initiateCommands(Runtime.getRuntime().availableProcessors());
        List<ProgressData> data = new ArrayList<>(progressData);
        data.addAll(getPoison(Runtime.getRuntime().availableProcessors()));

        persistenceManager.addPersistenceHook(data);
        reportManager.startReportDaemon();

        for (ProgressData pd : data) {
            initialQueue.put(pd);
        }

        service.awaitTermination(timeout, MILLISECONDS);
        reportManager.printReport();
    }

    private ExecutorService initiateCommands(int processorsCount) {
        List<Command> commands = new ArrayList<>();
        for (int i = 0; i < processorsCount; i++) {
            commands.add(new DownloadCommand(initialQueue, downloadedQueue, downloadCounter, POISON));
            commands.add(new CountWordsCommand(downloadedQueue, processedQueue, countWordsCounter, POISON));
            commands.add(new DeleteCommand(processedQueue, deleteCounter, POISON));
        }
        ExecutorService cmdsService = newFixedThreadPool(commands.size());
        commands.forEach(cmdsService::execute);
        cmdsService.shutdown();
        return cmdsService;
    }

    private List<ProgressData> parseCommands(List<String> cmdLines) {
        return cmdLines.stream().map(l -> {
            String[] cmd = l.split("\\s+");
            if (downloadCmdNames.contains(cmd[0].toLowerCase())) {
                return new ProgressData(cmd[1]);
            } else {
                throw new IllegalStateException("Unknown command " + cmd[0]);
            }

        }).collect(toList());
    }

    private List<ProgressData> getPoison(int processorsCount) {
        return range(0, processorsCount).mapToObj(i -> POISON).collect(toList());
    }

}
