package taskmanager.command;

import taskmanager.data.ProgressData;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadCommand implements Command {

    private final BlockingQueue<ProgressData> initialQueue;
    private final BlockingQueue<ProgressData> queue;
    private final AtomicLong counter;

    DownloadCommand(BlockingQueue<ProgressData> initialQueue, BlockingQueue<ProgressData> queue, AtomicLong counter) {
        this.initialQueue = initialQueue;
        this.queue = queue;
        this.counter = counter;
    }

    @Override
    public void execute() throws Exception {
        ProgressData progress = initialQueue.take();
        String url = progress.getUrl();
        String[] parts = url.split(/*File.separator*/"/");
        Path outFile = Paths.get(parts[parts.length - 1]);
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, outFile, StandardCopyOption.REPLACE_EXISTING);
            progress.setDownloadedFile(outFile.toString());
            queue.put(progress);
            counter.incrementAndGet();
        }
    }
}
