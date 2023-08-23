package com.icatchtek.nadk.show.timeline;

import java.util.Date;

/**
 * Created by sha.liu on 2023/8/21.
 */
public class VideoInfo {
    private String fileName;
    private Date startTime;
    private Date endTime;

    public VideoInfo(String fileName, Date startTime, Date endTime) {
        this.fileName = fileName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
