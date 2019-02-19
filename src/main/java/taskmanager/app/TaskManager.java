package taskmanager.app;

import taskmanager.command.CommandManager;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TaskManager {

    public static final String RESTORE_OPTION = "--restore";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Application parameters not specified.");
        }

        CommandManager commandManager = new CommandManager(
                5 * 60 * 1000,
                5 * 1000
        );

        if (args[0].equalsIgnoreCase(RESTORE_OPTION)) {
            commandManager.restoreCommands();
        } else {
            commandManager.processCommands(Files.readAllLines(Paths.get(args[0])));
        }
    }


}
