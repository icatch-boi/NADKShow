package com.icatchtek.nadk.show;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.icatchtek.baseutil.file.FileUtil;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.baseutil.permission.PermissionTools;
import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.reliant.NADKSignalingType;
import com.icatchtek.nadk.show.utils.NADKConfig;
import com.icatchtek.nadk.show.utils.NADKShowLog;
import com.icatchtek.nadk.webrtc.NADKWebrtc;
import com.icatchtek.nadk.webrtc.assist.NADKAuthorization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class LanModeActivity extends AppCompatActivity {
    private static final String TAG = LanModeActivity.class.getSimpleName();
    private ImageButton back_btn;
    private Button live_view_btn;
    private Button local_playback_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan_mode);
        back_btn = findViewById(R.id.back_btn);
        live_view_btn = findViewById(R.id.live_view_btn);
        local_playback_btn = findViewById(R.id.local_playback_btn);


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        live_view_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLanModeAuthorization();
                Intent intent = new Intent(LanModeActivity.this, LiveViewActivity.class);
                intent.putExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_BASE_TCP);
                startActivity(intent);

            }
        });


        local_playback_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLanModeAuthorization();
                Intent intent = new Intent(LanModeActivity.this, LocalPlaybackActivity.class);
                intent.putExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_BASE_TCP);
                startActivity(intent);

            }
        });

        getLanModeAuthorization();
    }


    public void getLanModeAuthorization() {
        EditText channelname_edt = findViewById(R.id.channelname_edt);
        EditText endpoint_edt = findViewById(R.id.endpoint_edt);
        EditText clientid_edt = findViewById(R.id.clientid_edt);

        NADKAuthorization auth = NADKConfig.getInstance().getLanModeAuthorization();
        channelname_edt.setText(auth.getChannelName());
        endpoint_edt.setText(auth.getEndpoint());
        clientid_edt.setText(auth.getClientId());
    }

    public void setLanModeAuthorization() {
        EditText channelname_edt = findViewById(R.id.channelname_edt);
        EditText endpoint_edt = findViewById(R.id.endpoint_edt);
        EditText clientid_edt = findViewById(R.id.clientid_edt);

        NADKAuthorization auth = new NADKAuthorization();
        auth.setChannelName(channelname_edt.getText().toString());
        auth.setEndpoint(endpoint_edt.getText().toString());
        auth.setClientId(clientid_edt.getText().toString());
        NADKConfig.getInstance().setLanModeAuthorization(auth);
        NADKConfig.getInstance().serializeConfig();
    }


}