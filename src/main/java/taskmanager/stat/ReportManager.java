package taskmanager.stat;

import java.util.function.Supplier;

public class ReportManager {

    private Supplier<String> report;
    private long interval;

    public ReportManager(Supplier<String> report, long interval) {
        this.report = report;
        this.interval = interval;
    }

    public void printReport() {
        System.out.println(report.get());
    }

    public void startReportDaemon() {
        Thread stat = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                printReport();
                //Uncomment to simulate JVM down
//                System.exit(0);
            }
        });
        stat.setDaemon(true);
        stat.start();
    }

}
