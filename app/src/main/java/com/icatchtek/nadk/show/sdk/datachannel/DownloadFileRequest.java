package com.icatchtek.nadk.show.sdk.datachannel;

import com.alibaba.fastjson.JSON;

/**
 * Created by sha.liu on 2021/10/28.
 */
public class DownloadFileRequest {
    private int fileHandle;
    private int fileType;
    private long offset;

    public DownloadFileRequest() {
    }

    public DownloadFileRequest(int fileHandle, int fileType, long offset) {
        this.fileHandle = fileHandle;
        this.fileType = fileType;
        this.offset = offset;
    }

    public int getFileHandle() {
        return fileHandle;
    }

    public void setFileHandle(int fileHandle) {
        this.fileHandle = fileHandle;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
