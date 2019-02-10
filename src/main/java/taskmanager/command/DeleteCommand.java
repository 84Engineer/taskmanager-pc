package taskmanager.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;

public class DeleteCommand implements Command {

    private BlockingQueue<Path> in;
    private final Path POISON;

    DeleteCommand(BlockingQueue<Path> in, Path poison) {
        this.in = in;
        this.POISON = poison;
    }

    @Override
    public void execute() throws Exception {
        Path input;
        while (!(input = in.take()).equals(POISON)) {
            Files.delete(input);
        }
    }
}
