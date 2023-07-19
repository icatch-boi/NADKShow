package com.icatchtek.nadk.show.kvsarchivedmedialibextend;

/**
 * Created by sha.liu on 2021/1/6.
 */
public interface DownloadingProgressListen {
    void updateTotalSize(long totalSize);
    void updateDownloadSize(long totalSize, long downloadSize);
}
