package com.icatchtek.nadk.show.sdk.datachannel;

/**
 * Created by sha.liu on 2021/10/28.
 */
public class DownloadFileFirstPacket {
    private long transid;
    private String msgType;
    private int cmd;
    private int fileHandle;
    private int fileType;
    private long fileSize;
    private long remainderSize;

    public DownloadFileFirstPacket() {
    }

    public long getTransid() {
        return transid;
    }

    public void setTransid(long transid) {
        this.transid = transid;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getRemainderSize() {
        return remainderSize;
    }

    public void setRemainderSize(long remainderSize) {
        this.remainderSize = remainderSize;
    }
}
