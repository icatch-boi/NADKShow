package com.icatchtek.nadk.show.timeline;

import java.util.Date;

/**
 * Created by sha.liu on 2023/8/21.
 */
public class VideoInfo {
    private String fileName;
    private String thumbnailName;
    private Date startTime;
    private Date endTime;
    private boolean sosFile;
    private Object fileInfo;
    private Object fileExtension;

    public VideoInfo(String fileName, String thumbnailName, Date startTime, Date endTime) {
        this.fileName = fileName;
        this.thumbnailName = thumbnailName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public VideoInfo(Object fileInfo, Object fileExtension, String fileName, String thumbnailName, Date startTime, Date endTime, boolean sosFile) {
        this.fileInfo = fileInfo;
        this.fileExtension = fileExtension;
        this.fileName = fileName;
        this.thumbnailName = thumbnailName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sosFile = sosFile;
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

    public String getThumbnailName() {
        return thumbnailName;
    }

    public void setThumbnailName(String thumbnailName) {
        this.thumbnailName = thumbnailName;
    }

    public Object getFileInfo() {
        return fileInfo;
    }

    public boolean isSosFile() {
        return sosFile;
    }

    public Object getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(Object fileExtension) {
        this.fileExtension = fileExtension;
    }
}
