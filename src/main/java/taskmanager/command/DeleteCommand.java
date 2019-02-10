package taskmanager.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;

public class DeleteCommand implements Command {

    private BlockingQueue<Path> in;

    DeleteCommand(BlockingQueue<Path> in) {
        this.in = in;
    }

    @Override
    public void execute() throws Exception {
        while (true) {
            Files.delete(in.take());
        }
    }
}
