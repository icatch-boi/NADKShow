package com.icatchtek.nadk.show.imageloader;

import android.content.Context;

import com.icatch.smarthome.am.aws.AmazonAwsUtil;
import com.icatch.smarthome.am.aws.S3UriUtil;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.playback.type.NADKMediaFile;
import com.icatchtek.nadk.show.sdk.DeviceLocalFileListInfo;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author b.jiang
 * @date 2019/8/29
 * @description
 */
public class CustomImageDownloader extends BaseImageDownloader {
    private final static String TAG = CustomImageDownloader.class.getSimpleName();
    AmazonAwsUtil amazonS3Util;
    DeviceLocalFileListInfo deviceLocalFileListInfo;

    public CustomImageDownloader(DeviceLocalFileListInfo deviceLocalFileListInfo, Context context) {
        super(context);
        this.deviceLocalFileListInfo = deviceLocalFileListInfo;
    }

    public CustomImageDownloader(AmazonAwsUtil amazonS3Util, Context context) {
        super(context);
        this.amazonS3Util = amazonS3Util;
    }

    public CustomImageDownloader(AmazonAwsUtil amazonS3Util, Context context, int connectTimeout, int readTimeout) {
        super(context, connectTimeout, readTimeout);
        this.amazonS3Util = amazonS3Util;
    }

    @Override
    protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
        if (S3UriUtil.isAwsUri(imageUri)) {
            return getStreamFromAWSS3(imageUri, extra);
        } else if (imageUri.startsWith("datachannel://")) {
            return getStreamFromDevice(imageUri, extra);
        }
        return super.getStreamFromOtherSource(imageUri, extra);
    }

    private InputStream getStreamFromAWSS3(String imageUri, Object extra) {
        try {
            return amazonS3Util.getInputStream(imageUri);
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG,"getStreamFromAWSS3 Exception:" + e.getClass().getSimpleName() + " errorMessage:" + e.getMessage() + " imageUri:" + imageUri);
            return null;
        }
    }

    private InputStream getStreamFromDevice(String imageUri, Object extra) {
        try {
            if (deviceLocalFileListInfo == null) {
                return null;
            }

            NADKMediaFile mediaFile = DeviceLocalFileListInfo.convertToNADKMediaFile(imageUri);
            if (mediaFile != null) {
                String thumbnail = deviceLocalFileListInfo.downloadThumbnail(mediaFile);
                if (thumbnail != null) {
                    return new FileInputStream(thumbnail);
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG,"getStreamFromDevice Exception:" + e.getClass().getSimpleName() + " errorMessage:" + e.getMessage() + " imageUri:" + imageUri);
            return null;
        }
    }
}
