package taskmanager.command;

import taskmanager.data.ProgressData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.joining;
import static taskmanager.data.ProgressData.Status.FILE_DOWNLOADED;

public class CountWordsCommand implements Command {

    private final BlockingQueue<ProgressData> in;
    private final BlockingQueue<ProgressData> out;
    private final ProgressData POISON;
    private final AtomicLong counter;
    private static final String PROCESSED_SUFFIX = "_processed";

    CountWordsCommand(BlockingQueue<ProgressData> in, BlockingQueue<ProgressData> out, AtomicLong counter,
                      ProgressData poison) {
        this.in = in;
        this.out = out;
        this.counter = counter;
        this.POISON = poison;
    }

    @Override
    public void execute() throws Exception {

        while (true) {
            ProgressData progress = in.take();
            try {
                if (progress.equals(POISON)) {
                    System.out.println("COUNT POISONED");
                    break;
                } else if (progress.getStatus() == FILE_DOWNLOADED) {
                    String downloadedFile = progress.getDownloadedFile();
                    Map<String, Long> allWords = getAllWords(
                            Files.lines(
                                    Paths.get(downloadedFile)));
                    String output = toOutputFormat(allWords);
                    String outFile = getOutFileName(downloadedFile);
                    writeToFile(output, outFile);

                    progress.setProcessedFile(outFile);
                    counter.incrementAndGet();
                }
            } finally {
                out.put(progress);
            }
        }

    }

    private Map<String, Long> getAllWords(Stream<String> input) {
        return input.map(l -> l.split("\\W+"))
                .flatMap(Stream::of).filter(w -> !w.isEmpty()).map(String::toLowerCase)
                .collect(Collectors.groupingByConcurrent(Function.identity(), counting()));
    }

    private String getOutFileName(String inFile) {
        String[] inFileParts = inFile.split("\\.");
        return inFileParts[0] + PROCESSED_SUFFIX + "." + inFileParts[1];
    }

    private String toOutputFormat(Map<String, Long> wordMap) {
        List<Map.Entry<String, Long>> entries = new ArrayList<>(wordMap.entrySet());
        entries.sort(Map.Entry.comparingByValue());
        return entries.stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(joining(lineSeparator()));
    }

    private void writeToFile(String output, String filePath) throws IOException {
        Files.write(Paths.get(filePath),
                output.getBytes());
    }

}
