package taskmanager.data;

import com.sun.istack.internal.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static taskmanager.data.ProgressData.Status.*;

public class ProgressData implements Serializable {

    static final long serialVersionUID = 1L;

    private final String url;
    private String downloadedFile;
    private String processedFile;
    private String deletedFile;
    private Status status;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public enum Status {
        INITIAL, FILE_DOWNLOADED, FILE_PROCESSED, FILE_DELETED;
    }

    public ProgressData(@NotNull String url) {
        Objects.requireNonNull(url);
        this.url = url;
        this.status = INITIAL;
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
            this.status = FILE_DOWNLOADED;
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
            this.status = FILE_PROCESSED;
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
            this.status = FILE_DELETED;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Status getStatus() {
        lock.readLock().lock();
        try {
            return status;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProgressData)) {
            return false;
        }
        return url.equals(((ProgressData) obj).getUrl());
    }
}
