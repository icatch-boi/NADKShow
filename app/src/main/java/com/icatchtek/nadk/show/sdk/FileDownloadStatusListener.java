package com.icatchtek.nadk.show.sdk;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.playback.file.NADKFileTransferListener;


public class FileDownloadStatusListener implements NADKFileTransferListener
{
    private static final String TAG = FileDownloadStatusListener.class.getSimpleName();
    private String fileName;
    private long fileSize;
    private final Object lock = new Object();
    private NADKFileTransferListener nadkFileTransferListener;

    public FileDownloadStatusListener(NADKFileTransferListener nadkFileTransferListener) {
        this.nadkFileTransferListener = nadkFileTransferListener;

    }

    @Override
    public void transferStarted(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        synchronized (lock) {
            lock.notifyAll();
        }
        if (nadkFileTransferListener != null) {
            nadkFileTransferListener.transferStarted(fileName, fileSize);
        }
        AppLog.i(TAG, "transferStarted, fileName: " + fileName + ", fileSize: " + fileSize);
    }

    @Override
    public void transferFinished(long transferedSize) {
        if (nadkFileTransferListener != null) {
            nadkFileTransferListener.transferFinished(transferedSize);
        }
        AppLog.i(TAG, "transferFinished, transferedSize: " + transferedSize);

    }

    @Override
    public void transferInformation(long transferedSize) {
        if (nadkFileTransferListener != null) {
            nadkFileTransferListener.transferInformation(transferedSize);
        }
        AppLog.i(TAG, "transferInformation, transferedSize: " + transferedSize);

    }

    public String getFileName() {
        synchronized (lock) {
            try {
                lock.wait(5000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }
}
