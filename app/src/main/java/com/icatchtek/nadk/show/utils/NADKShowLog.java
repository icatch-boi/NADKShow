package com.icatchtek.nadk.show.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.icatchtek.baseutil.info.AppInfo;
import com.icatchtek.baseutil.info.SystemInfo;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.baseutil.log.Logger;
import com.icatchtek.nadk.applog.NADKAppLog;
import com.icatchtek.nadk.applog.NADKAppLogLevel;
import com.icatchtek.nadk.applog.NADKAppLogModule;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sha.liu on 2022/8/18.
 */
public class NADKShowLog implements Logger {
    private static final String TAG = AppLog.class.getSimpleName();
    private boolean enableLog = false;
    private static Context context;

    @Override
    synchronized public void initLog(Context mcontext) {
        Log.i("NADKShowLog", "enableAppLog :" + enableLog);
        if(enableLog){
            return;
        }
        context = mcontext.getApplicationContext();
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            AppInfo.APP_VERSION = packageInfo.versionName;
        }

        AppInfo.SDK_VERSION = "3.0.0";

        initNADKAppLog(context);

    }

    @Override
    synchronized public void reInitLog() {
        if(!enableLog){
            return;
        }
        NADKAppLog.setFileLog(false, false);
        NADKAppLog.setFileLog(true, true);
        NADKAppLog.writeLog(NADKAppLogModule.NADK_LOG_MODULE_APP, NADKAppLogLevel.NADK_LOG_LEVEL_INFO, TAG, "reInitNADKAppLog");
        printfAppInfo();
    }

    @Override
    public void unInitLog() {
        if(!enableLog){
            return;
        }
        NADKAppLog.cleanup();
    }

    @Override
    public void i(String tag, String message) {
        if(!enableLog){
            return;
        }
        writeLog(NADKAppLogLevel.NADK_LOG_LEVEL_INFO, tag, message);
    }

    @Override
    public void w(String tag, String message) {
        if(!enableLog){
            return;
        }
        writeLog(NADKAppLogLevel.NADK_LOG_LEVEL_WARN, tag, message);
    }

    @Override
    public void e(String tag, String message) {
        if(!enableLog){
            return;
        }
        writeLog(NADKAppLogLevel.NADK_LOG_LEVEL_ERROR, tag, message);
    }

    @Override
    public void d(String tag, String message) {
        if(!enableLog){
            return;
        }
        writeLog(NADKAppLogLevel.NADK_LOG_LEVEL_DEBUG, tag, message);
    }

    @Override
    public String getRelativeLogFileName() {
        if(!enableLog){
            return null;
        }
        return NADKAppLog.getRelativeFileName();
    }

    @Override
    public String getAbsoluteLogFileName() {
        if(!enableLog){
            return null;
        }
        return NADKAppLog.getAbsoluteFileName();
    }

    @Override
    public String getUniqueID() {
        return NADKAppLog.getUniqueID();
    }

    @Override
    public void checkLogFileExist() {
        String filePath = getAbsoluteLogFileName();
        if (filePath != null && !filePath.isEmpty()) {
            File file = new File(filePath);
            if (!file.exists()) {
//                Log.i("BPSCamLog", "File :" + filePath + " not exist, reInitLog");
                reInitLog();
                NADKAppLog.writeLog(NADKAppLogModule.NADK_LOG_MODULE_APP, NADKAppLogLevel.NADK_LOG_LEVEL_INFO, TAG, "File :" + filePath + " not exist, reInitLog");
            }
        }
    }

    private void writeLog(int level, String tag, String message) {
        NADKAppLog.writeLog(NADKAppLogModule.NADK_LOG_MODULE_APP, level, tag, message);
    }

    private void initNADKAppLog(Context context) {
        String deviceId = SystemInfo.getAndroidId(context);
        String application = "NADKShow_App_SDK";
        NADKAppLog.initialize(deviceId, application);
        NADKAppLog.setDevcieID(deviceId);
        NADKAppLog.setApplication(application);
        NADKAppLog.setThreadInfo(true);
        NADKAppLog.setCachedInfo(false);
        NADKAppLog.setDebugMode(true);
        NADKAppLog.setCachingMode(false);
        NADKAppLog.setSystemLog(true);
        NADKAppLog.setRelativeTime(false);
        String path = Environment.getExternalStorageDirectory().getPath() + "/NADKShow_APP_Log";;
        createDirectory(path);
        NADKAppLog.setFileLogPath(path);
        NADKAppLog.setFileLog(true, true);


        NADKAppLog.setModule(NADKAppLogModule.NADK_LOG_MODULE_COMMON, true);
        NADKAppLog.setModuleLevel(NADKAppLogModule.NADK_LOG_MODULE_COMMON, NADKAppLogLevel.NADK_LOG_LEVEL_VERB);
        NADKAppLog.setModule(NADKAppLogModule.NADK_LOG_MODULE_PLAYBACK, true);
        NADKAppLog.setModuleLevel(NADKAppLogModule.NADK_LOG_MODULE_PLAYBACK, NADKAppLogLevel.NADK_LOG_LEVEL_VERB);
        NADKAppLog.setModule(NADKAppLogModule.NADK_LOG_MODULE_NADK_P2P, true);
        NADKAppLog.setModuleLevel(NADKAppLogModule.NADK_LOG_MODULE_NADK_P2P, NADKAppLogLevel.NADK_LOG_LEVEL_VERB);

        NADKAppLog.setModule(NADKAppLogModule.NADK_LOG_MODULE_STREAM, true);
        NADKAppLog.setModuleLevel(NADKAppLogModule.NADK_LOG_MODULE_STREAM, NADKAppLogLevel.NADK_LOG_LEVEL_VERB);
        NADKAppLog.setModule(NADKAppLogModule.NADK_LOG_MODULE_RENDER, true);
        NADKAppLog.setModuleLevel(NADKAppLogModule.NADK_LOG_MODULE_RENDER, NADKAppLogLevel.NADK_LOG_LEVEL_VERB);
        NADKAppLog.setModule(NADKAppLogModule.NADK_LOG_MODULE_APP, true);
        NADKAppLog.setModuleLevel(NADKAppLogModule.NADK_LOG_MODULE_APP, NADKAppLogLevel.NADK_LOG_LEVEL_VERB);
        NADKAppLog.writeLog(NADKAppLogModule.NADK_LOG_MODULE_APP, NADKAppLogLevel.NADK_LOG_LEVEL_INFO, TAG, "initNADKAppLog");
//        com.tinyai.nadk.applog.NADKAppLog.setLog(14, true);
//        com.tinyai.nadk.applog.NADKAppLog.setLogLevel(14, NADKAppLogLevel.NADK_LOG_LEVEL_VERB);
        enableLog = true;
        printfAppInfo();

    }

    private void printfAppInfo() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.CHINA);
        i("AppInfo", "========================================================");
        i("AppInfo", "-- Date Time: " + sdf.format(date) + " --");
        i("AppInfo", "-- APP Version: " + AppInfo.APP_VERSION  + " --");
        i("AppInfo", "-- Android OS: " + Build.VERSION.RELEASE + " (" + Build.BRAND + " " + Build.MODEL + ")"  + " --");
        i("AppInfo", "-- SDK Version: " + AppInfo.SDK_VERSION + " --");
        i("AppInfo", "========================================================");
    }

    private void createDirectory(String directoryPath) {
        if (directoryPath != null) {
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                boolean ret = directory.mkdirs();
                Log.d("BPSCamLog", "createDirectory: " + directoryPath + ", ret = " + ret);
            } else {
                Log.d("BPSCamLog", "createDirectory: " + directoryPath + ", directory exists");
            }
        }
    }
}
