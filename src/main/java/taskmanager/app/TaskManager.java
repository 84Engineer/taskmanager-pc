package taskmanager.app;

import taskmanager.command.Command;
import taskmanager.command.CommandFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TaskManager {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Commands file not specified.");
        }

//        List<AbstractCommand> commands = args[0].equalsIgnoreCase("--restore")
//                ? CommandFactory.restoreCommands()
//                : extractCommands(args[0]);

        int processorCmdsCount = Runtime.getRuntime().availableProcessors();

        List<Command> commands = CommandFactory.getInstance()
                .initiateCommands(Files.readAllLines(Paths.get(args[0])));

        List<Command> processingCommands = CommandFactory.getInstance().getProcessingCommands(processorCmdsCount);

        ExecutorService processorService = Executors.newFixedThreadPool(processorCmdsCount * 2);
        processingCommands.forEach(processorService::execute);
        processorService.shutdown();
        CommandFactory.getInstance().startStatDaemon(1);

        ExecutorService downloadService = Executors.newCachedThreadPool();
        downloadService.invokeAll(commands.stream().map(Executors::callable).collect(Collectors.toList()));
        downloadService.shutdown();

        CommandFactory.getInstance().sendPoison(processorCmdsCount);

        processorService.awaitTermination(1, TimeUnit.MINUTES);
        CommandFactory.getInstance().printReport();

    }


}
