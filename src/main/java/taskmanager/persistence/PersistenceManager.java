package taskmanager.persistence;

import java.io.*;

public class PersistenceManager<T> {

    private String filePath;

    public PersistenceManager(String filePath) {
        this.filePath = filePath;
    }

    public void persistObject(T obj) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(obj);
        }
    }

    @SuppressWarnings("unchecked")
    public T restoreObject() throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            return (T) in.readObject();
        }
    }

    public void addPersistenceHook(T obj) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                persistObject(obj);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

}
