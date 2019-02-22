package taskmanager.command;

import taskmanager.data.ProgressData;
import taskmanager.persistence.PersistenceManager;

import java.io.IOException;

public abstract class Command implements Runnable {

    private PersistenceManager persistenceManager;

    Command(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    abstract void execute() throws Exception;

    void saveProgress(ProgressData progressData) {
        try {
            persistenceManager.persistProgress(progressData);
        } catch (IOException e) {
            System.out.println("Unable to save progress.");
            e.printStackTrace();
        }
    }

    void clearProgress(ProgressData progressData) {
        try {
            persistenceManager.clearProgress(progressData);
        } catch (IOException e) {
            System.out.println("Unable to clear the progress.");
            e.printStackTrace();
        }
    }

}
