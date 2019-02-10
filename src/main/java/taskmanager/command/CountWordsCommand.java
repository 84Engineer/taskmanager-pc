package taskmanager.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.joining;

public class CountWordsCommand implements Command {

    private final BlockingQueue<Path> in;
    private final BlockingQueue<Path> out;
    private static final String PROCESSED_SUFFIX = "_processed";

    CountWordsCommand(BlockingQueue<Path> in, BlockingQueue<Path> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void execute() throws Exception {

        while (true) {
            Path input = in.take();
            Map<String, Long> allWords = getAllWords(Files.lines(input));
            String output = toOutputFormat(allWords);
            String[] outFileParts = input.getFileName().toString().split("\\.");
            writeToFile(output, outFileParts[0] + PROCESSED_SUFFIX + "." + outFileParts[1]);
            out.put(input);
        }

    }

    private Map<String, Long> getAllWords(Stream<String> input) {
        return input.map(l -> l.split("\\W+"))
                .flatMap(Stream::of).filter(w -> !w.isEmpty()).map(String::toLowerCase)
                .collect(Collectors.groupingByConcurrent(Function.identity(), counting()));
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
