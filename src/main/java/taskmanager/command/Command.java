package taskmanager.command;

public interface Command extends Runnable {

    default void run() {
        try {
            execute();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void execute() throws Exception;

}
