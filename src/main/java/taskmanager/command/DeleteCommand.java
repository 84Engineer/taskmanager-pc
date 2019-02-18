package taskmanager.command;

import taskmanager.data.ProgressData;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import static taskmanager.data.ProgressData.Status.FILE_PROCESSED;

public class DeleteCommand implements Command {

    private BlockingQueue<ProgressData> in;
    private final ProgressData POISON;
    private final AtomicLong counter;

    DeleteCommand(BlockingQueue<ProgressData> in, AtomicLong counter, ProgressData poison) {
        this.in = in;
        this.counter = counter;
        this.POISON = poison;
    }

    @Override
    public void execute() throws Exception {
        ProgressData progress;
        while (!(progress = in.take()).equals(POISON)) {
            if (progress.getStatus() == FILE_PROCESSED) {
                Files.delete(Paths.get(progress.getDownloadedFile()));
                progress.setDeletedFile(progress.getDownloadedFile());
                counter.incrementAndGet();
            }
        }
    }
}
