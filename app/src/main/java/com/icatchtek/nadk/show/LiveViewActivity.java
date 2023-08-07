package com.icatchtek.nadk.show;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.icatchtek.basecomponent.utils.ClickUtils;
import com.icatchtek.baseutil.ThreadPoolUtils;
import com.icatchtek.baseutil.device.MyOrientationEventListener;
import com.icatchtek.baseutil.device.ScreenUtils;
import com.icatchtek.baseutil.info.SystemInfo;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.reliant.NADKNetAddress;
import com.icatchtek.nadk.reliant.NADKSignalingType;
import com.icatchtek.nadk.reliant.NADKWebrtcAuthentication;
import com.icatchtek.nadk.reliant.NADKWebrtcSetupInfo;
import com.icatchtek.nadk.reliant.event.NADKEvent;
import com.icatchtek.nadk.reliant.event.NADKEventHandler;
import com.icatchtek.nadk.reliant.event.NADKEventID;
import com.icatchtek.nadk.reliant.event.NADKEventListener;
import com.icatchtek.nadk.reliant.parameter.NADKAudioParameter;
import com.icatchtek.nadk.reliant.parameter.NADKVideoParameter;
import com.icatchtek.nadk.reliant.parameter.NADKWebrtcStreamParameter;
import com.icatchtek.nadk.show.assist.NADKStreamingClientAssist;
import com.icatchtek.nadk.show.assist.WebrtcLogStatusListener;
import com.icatchtek.nadk.show.utils.NADKConfig;
import com.icatchtek.nadk.show.utils.NADKShowLog;
import com.icatchtek.nadk.show.utils.NADKWebRtcAudioRecord;
import com.icatchtek.nadk.show.utils.NetworkUtils;
import com.icatchtek.nadk.show.wakeup.WakeUpThread;
import com.icatchtek.nadk.show.wakeup.WakeupUtils;
import com.icatchtek.nadk.streaming.NADKStreamingClient;
import com.icatchtek.nadk.streaming.render.NADKStreamingRender;
import com.icatchtek.nadk.streaming.NADKStreaming;
import com.icatchtek.nadk.streaming.NADKStreamingAssist;
import com.icatchtek.nadk.streaming.NADKStreamingClientListener;
import com.icatchtek.nadk.streaming.producer.NADKStreamingProducer;
import com.icatchtek.nadk.streaming.render.gl.type.NADKGLColor;
import com.icatchtek.nadk.streaming.render.gl.type.NADKGLDisplayPPI;
import com.icatchtek.nadk.webrtc.NADKWebrtc;
import com.icatchtek.nadk.webrtc.assist.NADKAuthorization;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LiveViewActivity extends BaseWebrtcActivity
{
    private static final String TAG = LiveViewActivity.class.getSimpleName();

    private boolean masterRole = false;
    private NADKWebrtc webrtc;
    private NADKStreaming streaming;
    private NADKWebrtcStreamParameter streamParameter;
    private NADKStreamingRender streamingRender;
    private NADKEventListener nadkStreamingEventListener;
    private int signalingType;

    private RelativeLayout live_view_layout;
    private RelativeLayout control_btn_layout;
    private ImageButton fullscreen_switch;
    private RelativeLayout topbar;
    private ImageView topbar_back;
    private TextView topbar_title;
    private RelativeLayout top_bar_layout;
    private ImageButton back_btn;
    private Button webrtc_btn;
    private Button talk_btn;
    private boolean prepareWebrtc = false;
    private boolean enableTalk = false;
    private boolean orientationIsVertical = true;

    private MyOrientationEventListener orientationEventListener;
    private Handler handler = new Handler();

    private AudioManager audioManager;
    private int originalAudioMode;
    private boolean originalSpeakerphoneOn;
    private NADKWebRtcAudioRecord nadkWebRtcAudioRecord;

    private WakeUpThread wakeUpThread;


    @Override
    protected void setContentViewWhichHasSurfaceView1()
    {
        setContentView(R.layout.activity_live_view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int checkedID)
//            {
//                if (checkedID == R.id.radio_master) {
//                   masterRole = true;
//                }
//                else {
//                    masterRole = false;
//                }
//            }
//        });

        /* viewer checked as default */
        RadioButton radioViewer = findViewById(R.id.radio_viewer);
        radioViewer.setChecked(true);

        Button btnPermission = findViewById(R.id.btnPermission);
        btnPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (!Environment.isExternalStorageManager())
                {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        });

        Button btnPrepareWebrtc = findViewById(R.id.btnPrepareWebrtc);
        btnPrepareWebrtc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                /* surface must be available */
                if (!surfaceReady) {
                    Toast.makeText(LiveViewActivity.this,
                            "The surface unavailable, please wait", Toast.LENGTH_LONG).show();
                    return;
                }

                /* prepare viewer */
                boolean retVal = prepareWebrtc();
                AppLog.i("main", "prepare webrtc: " + retVal);
            }
        });

        Button btnDestroyWebrtc = findViewById(R.id.btnDestroyWebrtc);
        btnDestroyWebrtc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean retVal = destroyWebrtc();
                AppLog.i("main", "destroy webrtc: " + retVal);
            }
        });

        Button wakeup_btn = findViewById(R.id.wakeup_btn);
        wakeup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
                    @Override
                    public void run() {
                        NADKAuthorization authorization = NADKConfig.getInstance().getLanModeAuthorization();
                        WakeupUtils.wakeup(authorization.getAccessKey(), authorization.getSecretKey());
                    }
                }, 200);
            }
        });

        initView();

        Intent intent = getIntent();
        signalingType = intent.getIntExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_KVS);

        try
        {
            AppLog.i("LiveView", "Flow, createWebrtcStreaming start");
            /* create webrtc */
            this.webrtc = NADKWebrtc.create(this.masterRole);

            NADKEventHandler eventHandler = webrtc.getEventHandler();

            /* create streaming based on webrtc */
            this.streaming = NADKStreamingAssist.createWebrtcStreaming(this.webrtc);
            registerStreamingListener();

            /* init logger */
//            this.initLogger(this.webrtc.getLogger(), masterRole);

            /* add a status listener, we want to upload file log after all
             * webrtc session disconnected. */
            webrtc.addActiveClientListener(
                new WebrtcLogStatusListener(webrtc.getLogger(),
               "android", masterRole ? "master" : "viewer"));
            AppLog.i("LiveView", "Flow, createWebrtcStreaming end");
        }
        catch(Exception ex) {
            ex.printStackTrace();
            AppLog.e("LiveView", "Flow, createWebrtcStreaming end, Exception: " + ex.getMessage());
        }

        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSurface(streamingRender);
    }

    private void initActivityCfg() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        if (isLocked) {
//            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//        } else {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        }
    }

    private void initView() {
        AppLog.enableAppLog(this, new NADKShowLog());
        initActivityCfg();
        top_bar_layout = findViewById(R.id.toolbar_layout);
        control_btn_layout = findViewById(R.id.control_btn_layout);
        back_btn = findViewById(R.id.back_btn);
        webrtc_btn = findViewById(R.id.webrtc_btn);
        talk_btn = findViewById(R.id.talk_btn);
        live_view_layout = findViewById(R.id.live_view_layout);
        fullscreen_switch = findViewById(R.id.exo_fullscreen);
        topbar = findViewById(R.id.exo_top_bar);
        topbar_back = findViewById(R.id.exo_top_bar_back);

        fullscreen_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickUtils.isFastDoubleClick(v)){
                    AppLog.i(TAG, "isFastDoubleClick the v.id=" + v.getId());
                    return;
                }
                if (!orientationIsVertical) {
                    setPvLayout(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setPvLayout(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });

        topbar_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickUtils.isFastDoubleClick(v)){
                    AppLog.i(TAG, "isFastDoubleClick the v.id=" + v.getId());
                    return;
                }
                if (!orientationIsVertical) {
                    setPvLayout(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });

        talk_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickUtils.isFastDoubleClick(v)){
                    AppLog.i(TAG, "isFastDoubleClick the v.id=" + v.getId());
                    return;
                }
                setTalk();
            }
        });

        webrtc_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickUtils.isFastDoubleClick(v)){
                    AppLog.i(TAG, "isFastDoubleClick the v.id=" + v.getId());
                    return;
                }
                setWebrtc();
            }
        });

        orientationEventListener = new MyOrientationEventListener(this, new MyOrientationEventListener.OnOrientationChangedCallback() {
            @Override
            public void onOrientationChanged(int orientation) {
                setPvLayout(orientation);
            }
        });

        if (orientationEventListener != null) {
            orientationEventListener.enable();
        }

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setPvLayout(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        exit();

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                AppLog.d(TAG, "home");
                exit();
                break;
            case KeyEvent.KEYCODE_BACK:
                AppLog.d(TAG, "back");
                if (!orientationIsVertical) {
                    setPvLayout(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    finish();
                }
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    /* Add code there to make ensure that the stream is closed */
    private void exit()
    {
        try {
            unRegisterStreamingListener();
            if (streamingRender != null) {
                boolean retVal = destroyWebrtc();
                AppLog.i(TAG, "destroy webrtc: " + retVal);
            }
            AppLog.i(TAG, "exit activity");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startWakeup() {
        NADKAuthorization authorization = NADKConfig.getInstance().getLanModeAuthorization();

        try {
            wakeUpThread = new WakeUpThread(authorization.getAccessKey(), authorization.getSecretKey());
            wakeUpThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopWakeup() {
        if (wakeUpThread != null) {
            wakeUpThread.setStopFlag();
            wakeUpThread = null;
        }
    }

    private boolean prepareWebrtc()
    {
        startWakeup();

        try
        {
            AppLog.i("LiveView", "Flow, prepareRender start");
            if (this.surfaceContext != null)
            {
                DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
                NADKGLDisplayPPI displayPPI = new NADKGLDisplayPPI(displayMetrics.xdpi, displayMetrics.ydpi);
                streamingRender = NADKStreamingRender.createTextureRender(
                        webrtc.getLogger(),
                        webrtc.getEventHandler(),
                        NADKGLColor.BLACK, displayPPI, this.surfaceContext);
            }
            else
            {
                streamingRender = NADKStreamingRender.createConsoleRender(
                        webrtc.getLogger(),
                        webrtc.getEventHandler());
            }
            AppLog.i("LiveView", "Flow, prepareRender end");

            /* create a streaming client listener,
             * the streaming client will be used to send/receive media frames */
            NADKStreamingProducer streamingProducer = NADKStreamingProducer.createFileStreamingProducer();
            NADKStreamingClientListener clientListener = new NADKStreamingClientAssist(
                    webrtc.getLogger(),
                    webrtc.getEventHandler(),
                    (masterRole) ? null : streamingRender,
                    (masterRole) ? streamingProducer : null, new NADKStreamingClientListener() {
                @Override
                public void created(NADKStreamingClient streamingClient) {

                }

                @Override
                public void destroyed(NADKStreamingClient streamingClient) {

                }

                @Override
                public void connected(NADKStreamingClient streamingClient) {
                    initTalk(streamingClient);
                }

                @Override
                public void disconnected(NADKStreamingClient streamingClient) {

                }

                @Override
                public void streamingEnabled(NADKStreamingClient streamingClient, NADKAudioParameter audioParameter, NADKVideoParameter videoParameter) {

                }

                @Override
                public void streamingDisabled(NADKStreamingClient streamingClient) {

                }

            });

            this.streamParameter = new NADKWebrtcStreamParameter();
            this.streaming.prepare(this.streamParameter, clientListener);

            /* prepare webrtc client*/

            AppLog.i("LiveView", "Flow, getWebRtcServerInfo start");
            NADKWebrtcSetupInfo setupInfo = NADKConfig.getInstance().createNADKWebrtcSetupInfo(this.masterRole, signalingType);
            /* create webrtc authentication */
            NADKWebrtcAuthentication authentication = NADKConfig.getInstance().createNADKWebrtcAuthentication(this.masterRole, signalingType);
            if (authentication == null) {
                AppLog.e("LiveView", "Flow, getWebRtcServerInfo end");
                return false;
            }
            AppLog.i("LiveView", "Flow, getWebRtcServerInfo end");

            AppLog.i("LiveView", "Flow, startWebrtcStreaming start");
            /* prepare the webrtc client, connect to the signaling */
            this.webrtc.prepareWebrtc(setupInfo, authentication);
            AppLog.i("LiveView", "Flow, startWebrtcStreaming end");
        }
        catch(NADKException ex) {
            ex.printStackTrace();
            AppLog.e("LiveView", "Flow, startWebrtcStreaming end, Exception: " + ex.getClass().getSimpleName() + ", error: " + ex.getMessage());

        }

        return true;
    }

    private boolean destroyWebrtc()
    {
        AppLog.i("LiveView", "Flow, disconnect");
        AppLog.i(TAG, "stop viewer");

        stopWakeup();

        enableTalk = false;
        talk_btn.setText("Enable Talk");

        prepareWebrtc = false;
        webrtc_btn.setText("Prepare Webrtc");


        try
        {
            if (streamingRender != null) {
                this.streamingRender.destroyRender();
                this.streamingRender.stopStreaming();
                streamingRender = null;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
//            return false;
        }

        /* destroy streaming */
        try {
            streaming.destroy();

        } catch (NADKException e) {
            e.printStackTrace();
        }
        /* destroy webrtc */
        try {
            webrtc.destroyWebrtc();
        } catch (Exception ex)
        {
            ex.printStackTrace();
//            return false;
        }

        if (nadkWebRtcAudioRecord != null) {
            nadkWebRtcAudioRecord.stopRecording();
            nadkWebRtcAudioRecord = null;
        }

        if (audioManager != null) {
            audioManager.setMode(originalAudioMode);
            audioManager.setSpeakerphoneOn(originalSpeakerphoneOn);
            audioManager = null;
        }

        AppLog.reInitLog();

        return true;
    }

    private void registerStreamingListener() {
        if (streaming != null) {
            nadkStreamingEventListener = new NADKStreamingListener();
            try {
                streaming.addEventListener(NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECTED, nadkStreamingEventListener);
            } catch (Exception e) {
                e.printStackTrace();
                AppLog.d(TAG, "registerStreamingListener NADK_EVENT_WEBRTC_PEER_CONNECTED Exception： " + e + ", " + e.getMessage());
            }

            try {
                streaming.addEventListener(NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECT_FAILED, nadkStreamingEventListener);
            } catch (Exception e) {
                e.printStackTrace();
                AppLog.d(TAG, "registerStreamingListener NADK_EVENT_WEBRTC_PEER_CONNECT_FAILED Exception： " + e + ", " + e.getMessage());
            }

            try {
                streaming.addEventListener(NADKEventID.NADK_EVENT_WEBRTC_PEER_CLOSED, nadkStreamingEventListener);
            } catch (Exception e) {
                e.printStackTrace();
                AppLog.d(TAG, "registerStreamingListener NADK_EVENT_WEBRTC_PEER_CLOSED Exception： " + e + ", " + e.getMessage());
            }

            try {
                streaming.addEventListener(NADKEventID.NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED, nadkStreamingEventListener);
            } catch (Exception e) {
                e.printStackTrace();
                AppLog.d(TAG, "registerStreamingListener NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED Exception： " + e + ", " + e.getMessage());
            }

            try {
                streaming.addEventListener(NADKEventID.NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED, nadkStreamingEventListener);
            } catch (Exception e) {
                e.printStackTrace();
                AppLog.d(TAG, "registerStreamingListener NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED Exception： " + e + ", " + e.getMessage());
            }
        }
    }

    private void unRegisterStreamingListener() {
        if (streaming != null && nadkStreamingEventListener != null) {
            try {
                streaming.removeEventListener(NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECTED, nadkStreamingEventListener);
            } catch (Exception e) {
                e.printStackTrace();
                AppLog.d(TAG, "unRegisterStreamingListener NADK_EVENT_WEBRTC_PEER_CONNECTED Exception： " + e + ", " + e.getMessage());
            }

            try {
                streaming.removeEventListener(NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECT_FAILED, nadkStreamingEventListener);
            } catch (Exception e) {
                e.printStackTrace();
                AppLog.d(TAG, "unRegisterStreamingListener NADK_EVENT_WEBRTC_PEER_CONNECT_FAILED Exception： " + e + ", " + e.getMessage());
            }

            try {
                streaming.removeEventListener(NADKEventID.NADK_EVENT_WEBRTC_PEER_CLOSED, nadkStreamingEventListener);
            } catch (Exception e) {
                e.printStackTrace();
                AppLog.d(TAG, "unRegisterStreamingListener NADK_EVENT_WEBRTC_PEER_CLOSED Exception： " + e + ", " + e.getMessage());
            }

            try {
                streaming.removeEventListener(NADKEventID.NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED, nadkStreamingEventListener);
            } catch (Exception e) {
                e.printStackTrace();
                AppLog.d(TAG, "unRegisterStreamingListener NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED Exception： " + e + ", " + e.getMessage());
            }

            try {
                streaming.removeEventListener(NADKEventID.NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED, nadkStreamingEventListener);
            } catch (Exception e) {
                e.printStackTrace();
                AppLog.d(TAG, "unRegisterStreamingListener NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED Exception： " + e + ", " + e.getMessage());
            }
            nadkStreamingEventListener = null;
        }
    }

    private class NADKStreamingListener implements NADKEventListener {

        @Override
        public void notify(NADKEvent event) {
            String status = "";
            if (event.getEventID() == NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECTING) {
                status = "NADK_EVENT_WEBRTC_PEER_CONNECTING";
                AppLog.i(TAG, "WEBRTC_PEER Status, NADK_EVENT_WEBRTC_PEER_CONNECTED");
                AppLog.i("LiveView", "Flow, deviceReady");


            } else if (event.getEventID() == NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECTED) {
                status = "NADK_EVENT_WEBRTC_PEER_CONNECTED";
                AppLog.i("LiveView", "Flow, NADK_EVENT_WEBRTC_PEER_CONNECTED");
                stopWakeup();
                final String finalStatus = status;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LiveViewActivity.this, finalStatus, Toast.LENGTH_SHORT).show();
                    }
                });


            } else if (event.getEventID() == NADKEventID.NADK_EVENT_WEBRTC_PEER_CLOSED || event.getEventID() == NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECT_FAILED) {
                String iceConnectionState = "NADK_EVENT_WEBRTC_PEER_CONNECT_FAILED";
                if (event.getEventID() == NADKEventID.NADK_EVENT_WEBRTC_PEER_CLOSED) {
                    iceConnectionState = "NADK_EVENT_WEBRTC_PEER_CLOSED";
                }
                status = iceConnectionState;

                AppLog.i("LiveView", "Flow, " + iceConnectionState);
                final String finalStatus = status;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LiveViewActivity.this, finalStatus, Toast.LENGTH_SHORT).show();
                    }
                });


            } else if (event.getEventID() == NADKEventID.NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED) {
                status = "NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED";
                AppLog.i("LiveView", "Flow, NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED");


            } else if (event.getEventID() == NADKEventID.NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED) {
                status = "NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED";
                AppLog.i("LiveView", "Flow, NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED");
            }

//            final String finalStatus = status;
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(LiveViewActivity.this, finalStatus, Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }

    private void initTalk(NADKStreamingClient streamingClient) {

        /* enableTalk */
        try {
            AppLog.i("LiveView", "Flow, enableTalk start");
            if (streaming == null) {
                AppLog.e(TAG, "initTalk streaming == null");
                return;
            }

            if (streamingClient == null) {
                AppLog.e("LiveView", "Flow, enableTalk end, initTalk == null");
                return;
            }

            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            originalAudioMode = audioManager.getMode();
            originalSpeakerphoneOn = audioManager.isSpeakerphoneOn();
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(true);

            nadkWebRtcAudioRecord = new NADKWebRtcAudioRecord(this, audioManager, streamingClient);
            if (nadkWebRtcAudioRecord.initRecording(NADKWebRtcAudioRecord.DEFAULT_AUDIO_SAMPLE_RATE, NADKWebRtcAudioRecord.DEFAULT_AUDIO_CHANNEL) > 0) {

                if (nadkWebRtcAudioRecord == null) {
                    AppLog.e(TAG, "initTalk nadkWebRtcAudioRecord == null");
                    return;
                }
                nadkWebRtcAudioRecord.startRecording();
            }
            AppLog.i("LiveView", "Flow, enableTalk end");

        } catch(Exception ex) {
            ex.printStackTrace();
            AppLog.e("LiveView", "Flow, enableTalk end, Exception: " + ex.getClass().getSimpleName() + ", error: " + ex.getMessage());
        }
    }

    private void setWebrtc() {
        if (prepareWebrtc) {
            if (nadkWebRtcAudioRecord != null) {
                nadkWebRtcAudioRecord.setMicrophoneMute(true);
            }
            enableTalk = false;
            talk_btn.setText("Enable Talk");

            destroyWebrtc();
            prepareWebrtc = false;
            webrtc_btn.setText("Prepare Webrtc");

        } else {
            AppLog.i("LiveView", "Flow, Click Play");
            /* surface must be available */
            if (!surfaceReady) {
                Toast.makeText(LiveViewActivity.this,
                        "The surface unavailable, please wait", Toast.LENGTH_LONG).show();
                return;
            }

            /* prepare viewer */
            boolean retVal = prepareWebrtc();
            AppLog.i("main", "prepare webrtc: " + retVal);
            if (retVal) {
                prepareWebrtc = true;
                webrtc_btn.setText("Destroy Webrtc");
            }
        }
    }

    private void setTalk() {
        if (enableTalk) {
            if (nadkWebRtcAudioRecord != null) {
                nadkWebRtcAudioRecord.setMicrophoneMute(true);
                enableTalk = false;
                talk_btn.setText("Enable Talk");
            }

        } else {
            if (nadkWebRtcAudioRecord != null) {
                nadkWebRtcAudioRecord.setMicrophoneMute(false);
                enableTalk = true;
                talk_btn.setText("Disable Talk");
            }

        }
    }


    public void setPvLayout(int requestedOrientation) {
        //竖屏
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            fullscreen_switch.setImageResource(R.drawable.exo_icon_fullscreen_enter);
            ViewGroup.LayoutParams params = live_view_layout.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            int screenWidth;
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                screenWidth = SystemInfo.getScreenHeight(this);
            } else {
                screenWidth = SystemInfo.getScreenWidth(this);
            }

            /* width : height = 4 : 3 */
//            params.height = screenWidth * 3 / 4;

            /* width : height = 16 : 9 */
            params.height = screenWidth * 9 / 16;


            AppLog.d(TAG, " screenWidth =" + screenWidth);
            AppLog.d(TAG, " params.width =" + params.width);
            AppLog.d(TAG, " params.height =" + params.height);
            live_view_layout.setLayoutParams(params);


            top_bar_layout.setVisibility(View.VISIBLE);
            control_btn_layout.setVisibility(View.VISIBLE);
            topbar.setVisibility(View.GONE);
            orientationIsVertical = true;
            live_view_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            ScreenUtils.setPortrait(this);
        } else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            fullscreen_switch.setImageResource(R.drawable.exo_icon_fullscreen_exit);
            //横屏
            ViewGroup.LayoutParams params = live_view_layout.getLayoutParams();

            /* width : height = 4 : 3 */
//            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
//            int screenWidth = SystemInfo.getScreenWidth(this);
//            params.width = screenWidth * 4 / 3;

            /* width : height = 16 : 9 */
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            int screenWidth = SystemInfo.getScreenHeight(this);
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
//            params.height = screenWidth * 9 / 16;



            AppLog.d(TAG, " screenWidth =" + screenWidth);
            AppLog.d(TAG, " params.width =" + params.width);
            AppLog.d(TAG, " params.height =" + params.height);


            live_view_layout.setLayoutParams(params);
//            remoteView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
            ScreenUtils.setLandscape(this, requestedOrientation);
            topbar.setVisibility(View.VISIBLE);
            top_bar_layout.setVisibility(View.GONE);
            control_btn_layout.setVisibility(View.GONE);
            live_view_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            orientationIsVertical = false;
            RelativeLayout.LayoutParams remoteViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);

        }
    }

}
