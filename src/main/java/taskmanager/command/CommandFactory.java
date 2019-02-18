package taskmanager.command;

import taskmanager.data.ProgressData;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.toList;
import static taskmanager.data.ProgressData.Status.INITIAL;

public class CommandFactory {

    private static final CommandFactory instance = new CommandFactory();

    private static final String progressFile = "saved.dat";

    static final ProgressData POISON = new ProgressData("POISON");

    private final BlockingQueue<ProgressData> initialQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ProgressData> downloadedQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ProgressData> processedQueue = new LinkedBlockingQueue<>();
    private final AtomicLong downloadCounter = new AtomicLong();
    private final AtomicLong countWordsCounter = new AtomicLong();
    private final AtomicLong deleteCounter = new AtomicLong();
    private List<ProgressData> data = new ArrayList<>();

    public static CommandFactory getInstance() {
        return instance;
    }

    public List<Command> initiateCommands(List<String> cmdLines) throws InterruptedException {
        List<Command> commands = cmdLines.stream().map(this::parseCommand).collect(toList());
        initiateProgressSaver();
        for (ProgressData progressData : data) {
            initialQueue.put(progressData);
        }
        return commands;
    }

    public List<Command> restoreCommands() throws IOException, ClassNotFoundException, InterruptedException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(progressFile))) {
            this.data = (List<ProgressData>) in.readObject();
        }
        List<Command> commands = new ArrayList<>();
        for (ProgressData progressData: data) {
            if (progressData.getStatus() == INITIAL) {
                commands.add(new DownloadCommand(initialQueue, downloadedQueue, downloadCounter));
                initialQueue.add(progressData);
            } else {
                downloadedQueue.put(progressData);
            }
        }
        return commands;
    }

    public List<Command> getProcessingCommands(int processingCmdsCount) {
        List<Command> commands = new ArrayList<>();
        for (int i = 0; i < processingCmdsCount; i++) {
            commands.add(new CountWordsCommand(downloadedQueue, processedQueue, countWordsCounter, POISON));
            commands.add(new DeleteCommand(processedQueue, deleteCounter, POISON));
        }
        return commands;
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

    public void startStatDaemon(int intervalSeconds) {
        Thread stat = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(intervalSeconds * 1000);
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

    private void initiateProgressSaver() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(progressFile))) {
                out.writeObject(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

}
