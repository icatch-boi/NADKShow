package com.icatchtek.nadk.show.sdk.datachannel;

/**
 * Created by sha.liu on 2021/11/3.
 */
public interface DownloadProgressCallback {
    public void onStart(long totalSize);

    public void onProgress(long downloadSize, long totalSize);

    public void onStop(int error, String msg);
}
