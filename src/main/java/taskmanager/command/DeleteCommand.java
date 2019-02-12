package taskmanager.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class DeleteCommand implements Command {

    private BlockingQueue<Path> in;
    private final Path POISON;
    private final AtomicLong counter;

    DeleteCommand(BlockingQueue<Path> in, AtomicLong counter, Path poison) {
        this.in = in;
        this.counter = counter;
        this.POISON = poison;
    }

    @Override
    public void execute() throws Exception {
        Path input;
        while (!(input = in.take()).equals(POISON)) {
            Files.delete(input);
            counter.incrementAndGet();
        }
    }
}
