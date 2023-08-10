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
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.icatchtek.nadk.playback.NADKPlayback;
import com.icatchtek.nadk.playback.NADKPlaybackAssist;
import com.icatchtek.nadk.playback.NADKPlaybackClient;
import com.icatchtek.nadk.playback.NADKPlaybackClientListener;
import com.icatchtek.nadk.playback.file.NADKFileStatusListener;
import com.icatchtek.nadk.playback.type.NADKMediaFile;
import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.reliant.NADKSignalingType;
import com.icatchtek.nadk.reliant.NADKWebrtcAuthentication;
import com.icatchtek.nadk.reliant.NADKWebrtcSetupInfo;
import com.icatchtek.nadk.reliant.datachannel.NADKDataChannel;
import com.icatchtek.nadk.reliant.datachannel.NADKDataChannelListener;
import com.icatchtek.nadk.reliant.event.NADKEvent;
import com.icatchtek.nadk.reliant.event.NADKEventID;
import com.icatchtek.nadk.reliant.event.NADKEventListener;
import com.icatchtek.nadk.reliant.parameter.NADKAudioParameter;
import com.icatchtek.nadk.reliant.parameter.NADKVideoParameter;
import com.icatchtek.nadk.reliant.parameter.NADKWebrtcStreamParameter;
import com.icatchtek.nadk.show.assist.NADKStreamingClientAssist;
import com.icatchtek.nadk.show.assist.WebrtcLogStatusListener;
import com.icatchtek.nadk.show.device.DeviceManager;
import com.icatchtek.nadk.show.device.NADKLocalDevice;
import com.icatchtek.nadk.show.sdk.NADKPlaybackClientService;
import com.icatchtek.nadk.show.utils.NADKConfig;
import com.icatchtek.nadk.show.utils.NADKShowLog;
import com.icatchtek.nadk.show.utils.NADKWebRtcAudioRecord;
import com.icatchtek.nadk.show.wakeup.WakeUpThread;
import com.icatchtek.nadk.show.wakeup.WakeupUtils;
import com.icatchtek.nadk.streaming.NADKStreamingClient;
import com.icatchtek.nadk.streaming.render.NADKStreamingRender;
import com.icatchtek.nadk.streaming.NADKStreaming;
import com.icatchtek.nadk.streaming.NADKStreamingAssist;
import com.icatchtek.nadk.streaming.NADKStreamingClientListener;
import com.icatchtek.nadk.streaming.producer.NADKStreamingProducer;
import com.icatchtek.nadk.streaming.render.gl.surface.NADKSurfaceContext;
import com.icatchtek.nadk.streaming.render.gl.type.NADKGLColor;
import com.icatchtek.nadk.streaming.render.gl.type.NADKGLDisplayPPI;
import com.icatchtek.nadk.webrtc.NADKWebrtc;
import com.icatchtek.nadk.webrtc.NADKWebrtcClient;
import com.icatchtek.nadk.webrtc.NADKWebrtcClientStatusListener;
import com.icatchtek.nadk.webrtc.NADKWebrtcControl;
import com.icatchtek.nadk.webrtc.assist.NADKAuthorization;

import java.io.File;
import java.io.IOException;


public class LiveViewActivity extends NADKShowBaseActivity
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
    private ImageView talk_imv;
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

    private NADKPlayback playback;
    private NADKPlaybackClient playbackClient;
    private NADKPlaybackClientService playbackClientService;
    private Button playback_btn;
    private ImageView playback_imv;

    private NADKWebrtcClient webrtcClient;
    private NADKWebrtcControl webrtcControl;

    private NADKLocalDevice nadkLocalDevice;
    private NADKStreamingClientListener clientListener;
    private boolean isPlayback = false;


    protected boolean surfaceReady = false;
    protected NADKSurfaceContext surfaceContext;
    protected RelativeLayout live_view_control_layout;
    protected SurfaceView surfaceView;
    private SurfaceHolder.Callback callback;

    private RelativeLayout connect_loading_layout;
    private ProgressBar connect_loading_bar;
    private TextView connect_loading_txt;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_view);

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

        playback_btn = findViewById(R.id.playb_btn);
        playback_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (isPlayback) {
//                    enableStreaming();
//                    isPlayback = false;
//                } else {
//                    disableStreaming();
//                    isPlayback = true;
//                }
                setTalk(false);
                disableStreaming();
                Intent intent = new Intent(LiveViewActivity.this, LocalPlaybackActivity.class);
                intent.putExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_BASE_TCP);
                intent.putExtra("isFromPV", true);
                startActivity(intent);
            }
        });
        playback_btn.setEnabled(false);

        playback_imv = findViewById(R.id.playback_imv);
        playback_imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (isPlayback) {
//                    enableStreaming();
//                    isPlayback = false;
//                } else {
//                    disableStreaming();
//                    isPlayback = true;
//                }
                setTalk(false);
                disableStreaming();
                Intent intent = new Intent(LiveViewActivity.this, LocalPlaybackActivity.class);
                intent.putExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_BASE_TCP);
                intent.putExtra("isFromPV", true);
                startActivity(intent);
            }
        });
        playback_imv.setEnabled(false);

        connect_loading_layout = (RelativeLayout) findViewById(R.id.connect_loading_layout);
        connect_loading_bar = (ProgressBar) findViewById(R.id.connect_loading_bar);
        connect_loading_txt = (TextView) findViewById(R.id.connect_loading_txt);

        showConnectLoading("Connecting...");


        initView();

        Intent intent = getIntent();
        signalingType = intent.getIntExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_KVS);

//        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        enableStreaming();
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

        live_view_control_layout = findViewById(R.id.live_view_control_layout);
        surfaceView = findViewById(R.id.surfaceView1);
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (live_view_control_layout.getVisibility() == View.VISIBLE) {
                    live_view_control_layout.setVisibility(View.GONE);
                } else {
                    live_view_control_layout.setVisibility(View.VISIBLE);
                }
            }
        });

        initSurface(null);

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
                if (enableTalk) {
                    setTalk(false);
                } else {
                    setTalk(true);
                }
            }
        });

        talk_imv = findViewById(R.id.talk_imv);
        talk_imv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClickUtils.isFastDoubleClick(v)){
                    AppLog.i(TAG, "isFastDoubleClick the v.id=" + v.getId());
                    return;
                }
                if (enableTalk) {
                    setTalk(false);
                } else {
                    setTalk(true);
                }
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

    public void showConnectLoading(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connect_loading_txt.setText(msg);
                connect_loading_layout.setVisibility(View.VISIBLE);
            }
        });
    }

    public void closeConnectLoading() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                connect_loading_layout.setVisibility(View.GONE);
            }
        });

    }

    protected void initSurface(NADKStreamingRender render) {
        Log.i("__render__", "initSurface: ");
        if (callback != null) {
            surfaceView.getHolder().removeCallback(callback);
        }

        callback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                AppLog.i("LiveView", "Flow, surfaceReady");
                surfaceContext = new NADKSurfaceContext(surfaceView.getHolder().getSurface());
                if (!surfaceReady) {
                    surfaceReady = true;
                    ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
                        @Override
                        public void run() {
                            initWebrtc();
                        }
                    }, 200);

                }

                if (render != null) {
                    try {
                        render.changeSurfaceContext(surfaceContext);
                    } catch (NADKException e) {
                        e.printStackTrace();
                    }
                }
                enableStreaming();
                Log.i("__render__", "surface available: " + surfaceContext);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {
                try {
                    surfaceContext.setViewPort(0, 0, width, height);
                    Log.i("__render__", "surface changed: " + surfaceContext);
                } catch (NADKException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
//                surfaceReady = false;
//                disableStreaming();
                Log.i("__render__", "surface destroyed: ");
            }
        };
        surfaceView.getHolder().addCallback(callback);

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
        if (signalingType != NADKSignalingType.NADK_SIGNALING_TYPE_BASE_TCP) {
            return;
        }
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

    private boolean initWebrtc() {
        if (signalingType == NADKSignalingType.NADK_SIGNALING_TYPE_BASE_TCP) {
            nadkLocalDevice = DeviceManager.getInstance().getDevice(NADKConfig.getInstance().getLanModeAuthorization().getAccessKey());
            if (nadkLocalDevice != null) {
                nadkLocalDevice.setPlaybackClient(null);
            }
        }

        try
        {
            AppLog.i("LiveView", "Flow, createWebrtcStreaming start");
            /* create webrtc */
            this.webrtc = NADKWebrtc.create(this.masterRole);

            /* create streaming based on webrtc */
            this.streaming = NADKStreamingAssist.createWebrtcStreaming(this.webrtc);
            registerStreamingListener();


            /* create playback based on webrtc */
            String path = getExternalCacheDir().toString() + "/NADK";
            createDirectory(path);
            createDirectory(path);


            this.playback = NADKPlaybackAssist.createWebrtcPlayback(
                    masterRole, path, path, this.webrtc);


            webrtc.addClientStatusListener(new WebrtcStatusListener());

            /* init logger */
//            this.initLogger(this.webrtc.getLogger(), masterRole);

            /* add a status listener, we want to upload file log after all
             * webrtc session disconnected. */
            webrtc.addActiveClientListener(
                    new WebrtcLogStatusListener(webrtc.getLogger(),
                            "android", masterRole ? "master" : "viewer"));
            AppLog.i("LiveView", "Flow, createWebrtcStreaming end");

            setWebrtc();
        }
        catch(Exception ex) {
            ex.printStackTrace();
            AppLog.e("LiveView", "Flow, createWebrtcStreaming end, Exception: " + ex.getMessage());
        }


        return true;
    }

    private void prepareRender() {
        try {

            if (streamingRender == null) {
//                if (this.surfaceContext != null)
//                {
                    DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
                    NADKGLDisplayPPI displayPPI = new NADKGLDisplayPPI(displayMetrics.xdpi, displayMetrics.ydpi);
                    streamingRender = NADKStreamingRender.createTextureRender(
                            webrtc.getLogger(),
                            webrtc.getEventHandler(),
                            NADKGLColor.BLACK, displayPPI, this.surfaceContext);
//                }
//                else
//                {
//                    streamingRender = NADKStreamingRender.createConsoleRender(
//                            webrtc.getLogger(),
//                            webrtc.getEventHandler());
//                }
            }
            AppLog.i("LiveView", "Flow, prepareRender end");

            /* create a streaming client listener,
             * the streaming client will be used to send/receive media frames */
            if (clientListener == null) {
                NADKStreamingProducer streamingProducer = NADKStreamingProducer.createFileStreamingProducer();
                clientListener = new NADKStreamingClientAssist(
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
            }

            if (streamParameter == null) {
                this.streamParameter = new NADKWebrtcStreamParameter();
            }

            this.streaming.prepare(this.streamParameter, clientListener);
        } catch (NADKException e) {
            e.printStackTrace();
        }
    }

    private boolean prepareWebrtc()
    {
        startWakeup();

        try
        {
            AppLog.i("LiveView", "Flow, prepareRender start");
            prepareRender();

            /* create a playback client listener,
             * the playback client will be used to send/receive media frames */
            playbackClientService = new NADKPlaybackClientService(this.masterRole, new LocalPlaybackClientListener());
            this.playback.prepare(playbackClientService);


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
            closeConnectLoading();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LiveViewActivity.this, "NADK_EVENT_WEBRTC_SIGNALING_PEER_DISCONNECTED", Toast.LENGTH_SHORT).show();

                }
            });

        }

        return true;
    }

    private boolean destroyWebrtc()
    {
        AppLog.i("LiveView", "Flow, disconnect");
        AppLog.i(TAG, "stop viewer");

        stopWakeup();

        if (nadkLocalDevice != null) {
            nadkLocalDevice.setPlaybackClient(null);
        }

        enableTalk = false;
        talk_btn.setText("Enable Talk");

        prepareWebrtc = false;
        webrtc_btn.setText("Prepare Webrtc");

        try {
            if (streamingRender != null) {
                this.streamingRender.destroyRender();
                this.streamingRender.stopStreaming();
                streamingRender = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        disableStreaming();


        /* destroy playback */
        try {
            if (playback != null) {
                this.playback.destroy();
            }

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

        this.playbackClientService = null;
        this.playbackClient = null;

        AppLog.reInitLog();

        return true;
    }

    private void enableStreaming() {
        if (clientListener != null) {
            prepareRender();
        }
    }

    private void disableStreaming() {

        /* destroy streaming */
        try {
            if (streaming != null) {
                streaming.destroy();
            }
        } catch (NADKException e) {
            e.printStackTrace();
        }

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
                        showConnectLoading("Receiving Frame");

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
                closeConnectLoading();


            } else if (event.getEventID() == NADKEventID.NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED) {
                status = "NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED";
                AppLog.i("LiveView", "Flow, NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED");
                closeConnectLoading();


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

            destroyWebrtc();
            prepareWebrtc = false;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    setTalk(false);
                    webrtc_btn.setText("Prepare Webrtc");
                }
            });

        } else {
            AppLog.i("LiveView", "Flow, Click Play");
            /* surface must be available */
//            if (!surfaceReady) {
//                Toast.makeText(LiveViewActivity.this,
//                        "The surface unavailable, please wait", Toast.LENGTH_LONG).show();
//                return;
//            }

            /* prepare viewer */
            boolean retVal = prepareWebrtc();
            AppLog.i("main", "prepare webrtc: " + retVal);
            if (retVal) {
                prepareWebrtc = true;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        webrtc_btn.setText("Destroy Webrtc");
                    }
                });
            }
        }
    }

    private void setTalk(boolean enableTalk) {
        if (!enableTalk) {
            if (nadkWebRtcAudioRecord != null) {
                nadkWebRtcAudioRecord.setMicrophoneMute(true);
                this.enableTalk = false;
                talk_btn.setText("Enable Talk");
                talk_imv.setImageResource(R.drawable.selector_mic_off_btn);
            }

        } else {
            if (nadkWebRtcAudioRecord != null) {
                nadkWebRtcAudioRecord.setMicrophoneMute(false);
                this.enableTalk = true;
                talk_btn.setText("Disable Talk");
                talk_imv.setImageResource(R.drawable.selector_mic_on_btn);
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


    private class LocalPlaybackClientListener implements NADKPlaybackClientListener {

        @Override
        public void connected(NADKPlaybackClient mplaybackClient) {
            playbackClient = mplaybackClient;
            AppLog.d(TAG, "LocalPlaybackClientListener connected: " + playbackClient);
            try {
                playbackClient.setFileStatusListener(new NADKFileStatusListener() {
                    @Override
                    public void fileAdded(NADKMediaFile mediaFile) {
                        AppLog.d(TAG, "NADKFileStatusListener fileAdded: " + mediaFile.toString());

                    }

                    @Override
                    public void fileRemoved(NADKMediaFile mediaFile) {
                        AppLog.d(TAG, "NADKFileStatusListener fileRemoved: " + mediaFile.toString());

                    }
                });
            } catch (NADKException e) {
                e.printStackTrace();
            }
            nadkLocalDevice.setPlaybackClient(playbackClient);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    playback_btn.setEnabled(true);
                    playback_imv.setEnabled(true);
                }
            });

        }

        @Override
        public void disconnected(NADKPlaybackClient playbackClient) {
            AppLog.d(TAG, "LocalPlaybackClientListener disconnected: " + playbackClient);
            nadkLocalDevice.setPlaybackClient(null);
        }
    }

    private class WebrtcStatusListener implements NADKWebrtcClientStatusListener {

        @Override
        public void created(NADKWebrtcClient webrtcClient) {
            initWebrtcControl(webrtcClient);

        }

        @Override
        public void destroyed(NADKWebrtcClient webrtcClient) {


        }

        @Override
        public void connected(NADKWebrtcClient webrtcClient) {
            stopWakeup();

//            initWebrtcControl(webrtcClient);
        }

        @Override
        public void disconnected(NADKWebrtcClient webrtcClient) {


        }

    }

    private void initWebrtcControl(NADKWebrtcClient webrtcClient) {
        this.webrtcClient = webrtcClient;
        try {
            webrtcControl= NADKWebrtcControl.createControl(webrtcClient);
            AppLog.d(TAG, "createControl succeed");
            webrtcControl.addDataChannelListener("USER_DC_0", new NADKDataChannelListener() {
                @Override
                public void onDataChannelCreated(NADKDataChannel dataChannel) {
                    try {
                        AppLog.d(TAG, "onDataChannelCreated: " + dataChannel.getChannelName());
                    } catch (NADKException e) {
                        e.printStackTrace();
                    }

                }
            });
            AppLog.d(TAG, "addDataChannelListener succeed");
        } catch (NADKException e) {
            e.printStackTrace();
            AppLog.d(TAG, "initWebrtcControl: " + e.getMessage());
        }

    }


    private void createDirectory(String directoryPath) {
        if (directoryPath != null) {
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                boolean ret = directory.mkdirs();
                AppLog.d(TAG, "createDirectory: " + directoryPath + ", ret = " + ret);
            } else {
                AppLog.d(TAG, "createDirectory: " + directoryPath + ", directory exists");
            }
        }
    }
}
