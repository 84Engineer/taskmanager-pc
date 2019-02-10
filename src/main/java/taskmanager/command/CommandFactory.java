package taskmanager.command;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.stream.Collectors.toList;

public class CommandFactory {

    private static final CommandFactory instance = new CommandFactory();

    private final BlockingQueue<Path> downloadedQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Path> processedQueue = new LinkedBlockingQueue<>();
    private final Path POISON = Paths.get("POISON");

    public static CommandFactory getInstance() {
        return instance;
    }

    public List<Command> initiateCommands(List<String> cmdLines) {
        List<Command> commands = cmdLines.stream().map(this::parseCommand).collect(toList());
//        for (int i = 0; i < processingCmdsCount; i++) {
//            commands.add(new CountWordsCommand(downloadedQueue, processedQueue));
//            commands.add(new DeleteCommand(processedQueue));
//        }
        return commands;
    }

    public List<Command> getProcessingCommands(int processingCmdsCount) {
        List<Command> commands = new ArrayList<>();
        for (int i = 0; i < processingCmdsCount; i++) {
            commands.add(new CountWordsCommand(downloadedQueue, processedQueue, POISON));
            commands.add(new DeleteCommand(processedQueue, POISON));
        }
        return commands;
    }


    private Command parseCommand(String cmdLine) {

        String[] cmd = cmdLine.split("\\s+");

        switch (cmd[0].toLowerCase()) {
            case "d":
            case "download":
            case "dw":
                return new DownloadCommand(cmd[1], downloadedQueue);
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


}
