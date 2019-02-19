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

import static taskmanager.data.ProgressData.Status.INITIAL;

public class DownloadCommand implements Command {

    private final BlockingQueue<ProgressData> in;
    private final BlockingQueue<ProgressData> out;
    private final AtomicLong counter;
    private final ProgressData poison;

    DownloadCommand(BlockingQueue<ProgressData> initialQueue, BlockingQueue<ProgressData> downloadQueue, AtomicLong counter, ProgressData poison) {
        this.in = initialQueue;
        this.out = downloadQueue;
        this.counter = counter;
        this.poison = poison;
    }

    @Override
    public void execute() throws Exception {

        while (true) {
            ProgressData progress = in.take();
            try {
                if (progress.equals(poison)) {
                    System.out.println("DOWNLOAD POISONED");
                    break;
                } else if (progress.getStatus() == INITIAL) {
                    String url = progress.getUrl();
                    String[] parts = url.split("/");
                    Path outFile = Paths.get(parts[parts.length - 1]);
                    try (InputStream in = new URL(url).openStream()) {
                        Files.copy(in, outFile, StandardCopyOption.REPLACE_EXISTING);
                        progress.setDownloadedFile(outFile.toString());
                        counter.incrementAndGet();
                    }
                }
            } finally {
                out.put(progress);
            }
        }

    }
}
