package com.icatchtek.nadk.show.sdk;

import com.icatchtek.nadk.playback.NADKPlaybackClient;
import com.icatchtek.nadk.playback.file.NADKFileTransferListener;
import com.icatchtek.nadk.playback.type.NADKDateTime;
import com.icatchtek.nadk.playback.type.NADKMediaFile;
import com.icatchtek.nadk.playback.type.NADKThumbnail;
import com.icatchtek.nadk.reliant.NADKException;
import com.tinyai.libmediacomponent.components.filelist.FileItemInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by sha.liu on 2021/11/3.
 */
public class DeviceLocalFileListInfo {
    private static final String TAG = DeviceLocalFileListInfo.class.getSimpleName();
    private static final int STORAGE_INFO_MAX_DAYS = 365;
    private static final int MAX_FILE_COUNT = 300;
    private static final int FIRST_GET_FILE_COUNT = 20;
    private static final String THUMBNAIL_PREFIX = "datachannel://";
    private Date startTimeOfList;
    private Date endTimeOfList;
    private List<FileItemInfo> fileInfoList = new LinkedList<>();
    private NADKPlaybackClient playbackClient;

    public DeviceLocalFileListInfo(NADKPlaybackClient playbackClient) {
        this.playbackClient = playbackClient;
        this.endTimeOfList = setEndTimeOfDay(new Date());
        this.startTimeOfList = new Date(endTimeOfList.getTime() - (long) STORAGE_INFO_MAX_DAYS * 24 * 60 * 60 * 1000);

    }

    public List<FileItemInfo> getDeviceFileInfoList() {
        return fileInfoList;
    }

    public List<FileItemInfo> pullDownToRefresh() throws NADKException {
        if (this.fileInfoList.isEmpty()) {
            if (playbackClient != null) {
                NADKDateTime headTime = convertToNADKDateTime(startTimeOfList);
                NADKDateTime tailTime = convertToNADKDateTime(endTimeOfList);;
                List<NADKMediaFile> deviceFileInfoList = playbackClient.getMediaFiles(headTime, tailTime, 0, FIRST_GET_FILE_COUNT);
                if (deviceFileInfoList != null) {
                    List<FileItemInfo> fileItemInfoList = convertToFileItemInfoList(deviceFileInfoList);
                    this.fileInfoList.addAll(fileItemInfoList);
                    return fileItemInfoList;
                }
                return null;
            }

        } else {
            if (playbackClient != null) {
                NADKDateTime headTime = convertToNADKDateTime(new Date(fileInfoList.get(0).getTime()  + 1000));
                NADKDateTime tailTime = convertToNADKDateTime(setEndTimeOfDay(new Date()));;
                List<NADKMediaFile> deviceFileInfoList = playbackClient.getMediaFiles(headTime, tailTime);
                if (deviceFileInfoList != null) {
                    List<FileItemInfo> fileItemInfoList = convertToFileItemInfoList(deviceFileInfoList);
                    this.fileInfoList.addAll(0, fileItemInfoList);
                    return fileItemInfoList;
                }
                return null;
            }

        }
        return null;
    }

    public List<FileItemInfo> pullUpToRefresh() throws NADKException {
        if (this.fileInfoList.isEmpty()) {
            if (playbackClient != null) {
                NADKDateTime headTime = convertToNADKDateTime(startTimeOfList);
                NADKDateTime tailTime = convertToNADKDateTime(endTimeOfList);;
                List<NADKMediaFile> deviceFileInfoList = playbackClient.getMediaFiles(headTime, tailTime, 0, FIRST_GET_FILE_COUNT);
                if (deviceFileInfoList != null) {
                    List<FileItemInfo> fileItemInfoList = convertToFileItemInfoList(deviceFileInfoList);
                    this.fileInfoList.addAll(fileItemInfoList);
                    return fileItemInfoList;
                }
                return null;
            }

        } else {
            if (playbackClient != null) {
                NADKDateTime headTime = convertToNADKDateTime(startTimeOfList);
                int listSize = fileInfoList.size();
                NADKDateTime tailTime = convertToNADKDateTime(new Date(fileInfoList.get(listSize - 1).getTime() - 1000));;
                List<NADKMediaFile> deviceFileInfoList = playbackClient.getMediaFiles(headTime, tailTime, 0, FIRST_GET_FILE_COUNT);
                if (deviceFileInfoList != null) {
                    List<FileItemInfo> fileItemInfoList = convertToFileItemInfoList(deviceFileInfoList);
                    this.fileInfoList.addAll(fileItemInfoList);
                    return fileItemInfoList;
                }
                return null;
            }

        }
        return null;

    }

    public String downloadThumbnail(NADKMediaFile mediaFile) {
        if (playbackClient == null) {
            return null;
        }

        try {
            NADKThumbnail thumbnail = playbackClient.getThumbnail(mediaFile);
            NADKFileTransferListener Listener = new FileDownloadStatusListener(null);
            return this.playbackClient.downloadThumbnail(thumbnail, Listener);
        } catch (NADKException e) {
            e.printStackTrace();
        }

        return null;
//        return "/storage/self/primary/NADKWebrtcResources/media/cache/20230217_000428.thm_1690193125_614327.tmp.jpg";
    }

    public synchronized String downloadMediaFile(NADKMediaFile mediaFile, NADKFileTransferListener listener) {
        if (playbackClient == null) {
            return null;
        }

        try {

            return this.playbackClient.downloadMediaFile(mediaFile, listener);
        } catch (NADKException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<FileItemInfo> convertToFileItemInfoList(List<NADKMediaFile> mediaFiles) {
        List<FileItemInfo> fileItemInfoList = new LinkedList<>();
        for (NADKMediaFile file : mediaFiles) {
            FileItemInfo itemInfo = convertToFileItemInfo(file);
            fileItemInfoList.add(itemInfo);
        }
        return fileItemInfoList;
    }

    public static List<NADKMediaFile> convertToNADKMediaFileList(List<FileItemInfo> itemInfo) {
        List<NADKMediaFile> mediaFileList = new LinkedList<>();
        for (FileItemInfo item : itemInfo) {
            NADKMediaFile file = convertToNADKMediaFile(item);
            if (file != null) {
                mediaFileList.add(file);
            }
        }
        return mediaFileList;
    }

    public static FileItemInfo convertToFileItemInfo(NADKMediaFile mediaFile) {
        FileItemInfo itemInfo = new FileItemInfo((int) mediaFile.getFileHandle(), 2,
                "", mediaFile.getFileName(), mediaFile.getFileSize(), mediaFile.getFileTime() * 1000,
                30, 1920, 1080, mediaFile.isTriggerMode() ? 1 : 2, (int)mediaFile.getDuration() * 1000, THUMBNAIL_PREFIX + mediaFile.toString());
        return itemInfo;
    }

    public static NADKMediaFile convertToNADKMediaFile(FileItemInfo itemInfo) {
        int size = THUMBNAIL_PREFIX.length();
        String thumbnailPath = itemInfo.getThumbPath();
        return convertToNADKMediaFile(thumbnailPath);
    }

    public static NADKMediaFile convertToNADKMediaFile(String thumbnailPath) {
        int size = THUMBNAIL_PREFIX.length();
        if (thumbnailPath.length() > size) {
            String fileInfo = thumbnailPath.substring(size);
            return NADKMediaFile.fromString(fileInfo);
        }
        return null;
    }

    public static NADKDateTime convertToNADKDateTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return new NADKDateTime(calendar.get(Calendar.YEAR)
                , calendar.get(Calendar.MONTH) + 1
                , calendar.get(Calendar.DAY_OF_MONTH)
                , calendar.get(Calendar.HOUR_OF_DAY)
                , calendar.get(Calendar.MINUTE)
                , calendar.get(Calendar.SECOND));

    }

    public static Date setEndTimeOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }


}
