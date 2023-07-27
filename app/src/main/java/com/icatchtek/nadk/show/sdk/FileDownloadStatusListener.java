package com.icatchtek.nadk.show.sdk;

import android.util.Log;

import com.icatchtek.nadk.playback.file.NADKFileTransferStatusListener;

import java.util.Locale;

public class FileDownloadStatusListener implements NADKFileTransferStatusListener
{
    public void transferNotify(long transferedSize, long fileSize)
    {
        Log.i("fileStatus", String.format(Locale.getDefault(),
                "download %d of %d, percent: %d%%",
            transferedSize, fileSize, transferedSize * 100 / fileSize));
    }
}
