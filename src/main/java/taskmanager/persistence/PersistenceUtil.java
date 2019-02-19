package taskmanager.persistence;

import java.io.*;

public class PersistenceUtil {

    public static void persistObject(Object o, String filePath) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(o);
        }
    }

    public static Object restoreObject(String fileName) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            return in.readObject();
        }
    }

}
