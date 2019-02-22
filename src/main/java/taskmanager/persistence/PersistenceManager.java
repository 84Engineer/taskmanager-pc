package taskmanager.persistence;

import taskmanager.data.ProgressData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PersistenceManager {

    private static final String DIR = "src/main/resources/";

    public void persistProgress(ProgressData progress) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(generateFilePath(progress)))) {
            out.writeObject(progress);
        }
    }

    public void clearProgress(ProgressData progressData) throws IOException {
        Files.deleteIfExists(Paths.get(generateFilePath(progressData)));
    }

    public List<ProgressData> restoreProgress() throws IOException, ClassNotFoundException {
        List<ProgressData> res = new ArrayList<>();
        File[] files = new File(DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                    res.add((ProgressData) in.readObject());
                }
            }
        }
        return res;
    }

    private String generateFilePath(ProgressData progressData) {
        return DIR + progressData.getClass().getSimpleName() + "_" + progressData.getId();
    }

}
