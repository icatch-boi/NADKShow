package com.icatchtek.nadk.show;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.icatchtek.baseutil.file.FileUtil;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.baseutil.permission.PermissionTools;
import com.icatchtek.nadk.show.timeline.TimeLineActivity;
import com.icatchtek.nadk.show.utils.NADKConfig;
import com.icatchtek.nadk.show.utils.NADKShowLog;
import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.reliant.NADKSignalingType;
import com.icatchtek.nadk.webrtc.NADKWebrtc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class MainActivity extends NADKShowBaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ImageButton setting_btn;
    private Button aws_kvs_webrtc_btn;
    private Button aws_kvs_stream_btn;
    private Button tinyai_rtc_btn;
    private Button lan_mode_btn;
    private Button lan_mode_search_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setting_btn = findViewById(R.id.setting_btn);
        aws_kvs_webrtc_btn = findViewById(R.id.aws_kvs_webrtc_btn);
        aws_kvs_stream_btn = findViewById(R.id.aws_kvs_stream_btn);
        tinyai_rtc_btn = findViewById(R.id.tinyai_rtc_btn);
        lan_mode_btn = findViewById(R.id.lan_mode_btn);
        lan_mode_search_btn = findViewById(R.id.lan_mode_search_btn);

        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                startActivity(intent);

            }
        });

        aws_kvs_webrtc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LiveViewActivity.class);
                intent.putExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_KVS);
                startActivity(intent);

            }
        });


        aws_kvs_stream_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoPlaybackActivity.class);
                startActivity(intent);

            }
        });

        tinyai_rtc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LiveViewActivity.class);
                intent.putExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_AIOT_WSS);
                startActivity(intent);

            }
        });

        lan_mode_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LanModeActivity.class);
//                Intent intent = new Intent(MainActivity.this, TimeLineActivity.class);
                startActivity(intent);
            }
        });

        lan_mode_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
//                Intent intent = new Intent(MainActivity.this, VideoFilePlaybackActivity.class);
                startActivity(intent);
            }
        });

        checkPermission();

    }

    @Override
    protected void onResume() {
        super.onResume();
        NADKConfig.getInstance().loadConfig();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                AppLog.d(TAG, "home");
                finish();
                break;
            case KeyEvent.KEYCODE_BACK:
                AppLog.d(TAG, "back");
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean retValue = false;
        switch (requestCode) {
            case PermissionTools.ALL_REQUEST_CODE:
                Log.i(TAG, "permissions.ALL_REQUEST_CODE");
                retValue = true;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        retValue = false;
                        break;
                    }
                }

                if (retValue) {
                    init();
                } else {
                    Toast.makeText(this, "The necessary permissions are denied, the application can not be used normally!", Toast.LENGTH_LONG).show();
                }

                break;
            default:
        }
    }


    public void checkPermission() {
        if(PermissionTools.checkAllSelfPermission(this) ){
            init();
        }else {
            PermissionTools.requestAllPermissions(this);
        }
    }

    public void init() {
        AppLog.enableAppLog(this, new NADKShowLog());
        copyCAFile(this);
        NADKWebrtc webrtc = NADKWebrtc.create(false);
        try {
            webrtc.getEventHandler();
        } catch (NADKException e) {
            e.printStackTrace();
        }
    }

    public void copyCAFile(Context context) {
        String settingFilePath = Environment.getExternalStorageDirectory().getPath() + "/";
        copyDefaultSettingsFile(context, settingFilePath);

        String caFilePath = Environment.getExternalStorageDirectory().getPath() + "/certs";
        copyCAFile(context, caFilePath);
    }

    protected void copyCAFile(Context context, String filePath) {

        File file = new File(filePath);
        if (!file.exists()) {
            FileUtil.copyFolderFromAssets(context, "certs", filePath);
        }
    }

    protected void copyDefaultSettingsFile(Context context, String filePath) {
        String fileName = "DefaultSettings.ini";
        File file = new File(filePath + fileName);
//        if (!file.exists()) {
//            AppLog.d(TAG, "copyDefaultSettingsFile, DefaultSettings.ini not exist, copy default settings file");
//            FileUtil.copyFileFromAssets(context, fileName, filePath + fileName);
//        } else {
//            AppLog.d(TAG, "copyDefaultSettingsFile, DefaultSettings.ini already exist");
//        }
        AppLog.d(TAG, "copyDefaultSettingsFile, copy default settings file");
        FileUtil.copyFileFromAssets(context, fileName, filePath + fileName);
        readFile(filePath, fileName);
    }

    private void readFile(String filePath, String fileName) {
        try {
            FileInputStream fileImputStream = new FileInputStream(filePath + fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileImputStream));
            String line = null;
            long count = 0;
            while ((line = reader.readLine()) != null) {
                ++count;
                AppLog.d(TAG, "copyDefaultSettingsFile, ReadFile " + fileName + ", line "+ count + ": " + line);
            }
            reader.close();
            fileImputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}