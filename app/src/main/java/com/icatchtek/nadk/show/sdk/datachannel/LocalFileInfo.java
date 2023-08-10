package com.icatchtek.nadk.show.sdk.datachannel;

/**
 * Created by sha.liu on 2021/10/28.
 */
public class LocalFileInfo {
    private int fileHandle;
    private long time;
    private long size;
    private int duration;
    private int type;
    private int triggerType;
    private int attachment;

    public LocalFileInfo() {
    }

    public int getFileHandle() {
        return fileHandle;
    }

    public void setFileHandle(int fileHandle) {
        this.fileHandle = fileHandle;
    }

    public long getTime() {
        return time * 1000;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(int triggerType) {
        this.triggerType = triggerType;
    }

    public int getAttachment() {
        return attachment;
    }

    public void setAttachment(int attachment) {
        this.attachment = attachment;
    }
}
