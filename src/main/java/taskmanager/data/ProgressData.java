package taskmanager.data;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ProgressData implements Serializable {

    static final long serialVersionUID = 1L;

    private final String url;
    private String downloadedFile;
    private String processedFile;
    private String deletedFile;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ProgressData(String url) {
        this.url = url;
    }

    public String getUrl() {
        lock.readLock().lock();
        try {
            return url;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getDownloadedFile() {
        lock.readLock().lock();
        try {
            return downloadedFile;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setDownloadedFile(String downloadedFile) {
        lock.writeLock().lock();
        try {
            this.downloadedFile = downloadedFile;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getProcessedFile() {
        lock.readLock().lock();
        try {
            return processedFile;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setProcessedFile(String processedFile) {
        lock.writeLock().lock();
        try {
            this.processedFile = processedFile;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getDeletedFile() {
        lock.readLock().lock();
        try {
            return deletedFile;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setDeletedFile(String deletedFile) {
        lock.writeLock().lock();
        try {
            this.deletedFile = deletedFile;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String toString() {
        return "ProgressData{" +
                "url='" + url + '\'' +
                ", downloadedFile='" + downloadedFile + '\'' +
                ", processedFile='" + processedFile + '\'' +
                ", deletedFile='" + deletedFile + '\'' +
                '}';
    }
}
