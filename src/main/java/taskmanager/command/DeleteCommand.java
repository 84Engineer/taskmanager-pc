package taskmanager.command;

import taskmanager.data.ProgressData;
import taskmanager.persistence.PersistenceManager;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import static taskmanager.data.ProgressData.Status.FILE_PROCESSED;

public class DeleteCommand extends Command {

    private BlockingQueue<ProgressData> in;
    private final ProgressData poison;
    private final AtomicLong counter;

    DeleteCommand(PersistenceManager persistenceManager, BlockingQueue<ProgressData> in, AtomicLong counter, ProgressData poison) {
        super(persistenceManager);
        this.in = in;
        this.counter = counter;
        this.poison = poison;
    }

    @Override
    public void execute() throws Exception {
        while (true) {
            ProgressData progress = in.take();
            if (progress.equals(poison)) {
                System.out.println("DELETE POISONED");
                break;
            } else if (progress.getStatus() == FILE_PROCESSED) {
                Files.delete(Paths.get(progress.getDownloadedFile()));
                progress.setDeletedFile(progress.getDownloadedFile());
                clearProgress(progress);
                counter.incrementAndGet();
            }
        }
    }
}
