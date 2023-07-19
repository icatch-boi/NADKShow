package com.icatchtek.nadk.show;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.icatchtek.nadk.reliant.NADKLogLevel;
import com.icatchtek.nadk.reliant.NADKLogModule;
import com.icatchtek.nadk.reliant.NADKLogger;

import java.util.Locale;

public abstract class BaseWebrtcActivity extends BaseSurfaceActivity
{
    private static final String TAG = "NADKWebrtcViewer";

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int index = 0; index < permissions.length; index++)
        {
            Log.i(TAG, String.format(Locale.getDefault(),
                    "The permissions: %s, result: %s",
                    permissions[index], grantResults[index] == PackageManager.PERMISSION_GRANTED ? "true" : "false"));
        }
    }

    private static final int ALL_PERMISSIONS = 101;

    public void checkPermission()
    {
        boolean hasAllPermission = true;
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                    PackageManager.PERMISSION_GRANTED))
        {
            hasAllPermission = false;
        }

        if (!hasAllPermission)
        {
            String[] permissions = new String[]{
                    Manifest.permission.RECORD_AUDIO,
            };
            ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);
        }
    }

    protected void initLogger(NADKLogger logger, boolean masterRole)
    {
        Log.i("logger", "logger..");
        logger.setDeviceID("android");
        logger.setApplication(masterRole ? "master" : "viewer");

        logger.setDebugMode(true);
        logger.setCachingMode(true);

        logger.setColor(false);
        logger.setThreadInfo(true);
        logger.setCachedInfo(true);
        logger.setRelativeTime(false);

        String path = Environment.getExternalStorageDirectory().getPath();
        logger.setFileLogPath(path);
        logger.setFileLog(true);
        logger.setSystemLog(true);
        logger.setOutput(true);

        logger.setModule(NADKLogModule.NADK_LOG_MODULE_COMMON, true);
        logger.setModuleLevel(NADKLogModule.NADK_LOG_MODULE_COMMON, NADKLogLevel.NADK_LOG_LEVEL_VERB);
        logger.setModule(NADKLogModule.NADK_LOG_MODULE_NADK, true);
        logger.setModuleLevel(NADKLogModule.NADK_LOG_MODULE_NADK, NADKLogLevel.NADK_LOG_LEVEL_VERB);
        logger.setModule(NADKLogModule.NADK_LOG_MODULE_NADK_P2P, true);
        logger.setModuleLevel(NADKLogModule.NADK_LOG_MODULE_NADK_P2P, NADKLogLevel.NADK_LOG_LEVEL_VERB);

        logger.setModule(NADKLogModule.NADK_LOG_MODULE_STREAM, true);
        logger.setModuleLevel(NADKLogModule.NADK_LOG_MODULE_STREAM, NADKLogLevel.NADK_LOG_LEVEL_VERB);
        logger.setModule(NADKLogModule.NADK_LOG_MODULE_RENDER, true);
        logger.setModuleLevel(NADKLogModule.NADK_LOG_MODULE_RENDER, NADKLogLevel.NADK_LOG_LEVEL_VERB);
    }
}
