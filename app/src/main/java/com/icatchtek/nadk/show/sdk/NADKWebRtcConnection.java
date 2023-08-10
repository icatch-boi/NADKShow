//package com.icatchtek.nadk.show.sdk;
//
//import android.content.Context;
//import android.media.AudioManager;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.icatchtek.baseutil.ThreadPoolUtils;
//import com.icatchtek.baseutil.imageloader.ImageLoaderConfig;
//import com.icatchtek.baseutil.log.AppLog;
//import com.icatchtek.nadk.playback.NADKPlayback;
//import com.icatchtek.nadk.playback.NADKPlaybackAssist;
//import com.icatchtek.nadk.playback.NADKPlaybackClient;
//import com.icatchtek.nadk.playback.NADKPlaybackClientListener;
//import com.icatchtek.nadk.reliant.NADKException;
//import com.icatchtek.nadk.reliant.NADKNetAddress;
//import com.icatchtek.nadk.reliant.NADKSignalingType;
//import com.icatchtek.nadk.reliant.NADKWebrtcAuthentication;
//import com.icatchtek.nadk.reliant.NADKWebrtcSetupInfo;
//import com.icatchtek.nadk.reliant.datachannel.NADKDataChannel;
//import com.icatchtek.nadk.reliant.datachannel.NADKDataChannelListener;
//import com.icatchtek.nadk.reliant.event.NADKEvent;
//import com.icatchtek.nadk.reliant.event.NADKEventHandler;
//import com.icatchtek.nadk.reliant.event.NADKEventID;
//import com.icatchtek.nadk.reliant.event.NADKEventListener;
//import com.icatchtek.nadk.reliant.parameter.NADKAudioParameter;
//import com.icatchtek.nadk.reliant.parameter.NADKVideoParameter;
//import com.icatchtek.nadk.reliant.parameter.NADKWebrtcStreamParameter;
//import com.icatchtek.nadk.show.LiveViewActivity;
//import com.icatchtek.nadk.show.LocalPlaybackActivity;
//import com.icatchtek.nadk.show.R;
//import com.icatchtek.nadk.show.assist.WebrtcLogStatusListener;
//import com.icatchtek.nadk.show.imageloader.CustomImageDownloader;
//import com.icatchtek.nadk.show.utils.NADKConfig;
//import com.icatchtek.nadk.show.utils.NADKWebRtcAudioRecord;
//import com.icatchtek.nadk.show.utils.NetworkUtils;
//import com.icatchtek.nadk.show.wakeup.WakeUpThread;
//import com.icatchtek.nadk.streaming.NADKStreaming;
//import com.icatchtek.nadk.streaming.NADKStreamingAssist;
//import com.icatchtek.nadk.streaming.NADKStreamingClient;
//import com.icatchtek.nadk.streaming.render.NADKStreamingRender;
//import com.icatchtek.nadk.webrtc.NADKWebrtc;
//import com.icatchtek.nadk.webrtc.NADKWebrtcClient;
//import com.icatchtek.nadk.webrtc.NADKWebrtcClientStatusListener;
//import com.icatchtek.nadk.webrtc.NADKWebrtcControl;
//import com.icatchtek.nadk.webrtc.assist.NADKAuthorization;
//import com.icatchtek.nadk.webrtc.assist.NADKWebrtcAppConfig;
//import com.icatchtek.nadk.webrtc.assist.NADKWebrtcServiceRoutines;
//import com.nostra13.universalimageloader.core.ImageLoader;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//
///**
// * Created by sha.liu on 2023/7/25.
// */
//public class NADKWebRtcConnection {
//    private static final String TAG = NADKWebRtcConnection.class.getSimpleName();
//    private static final String MEDIA_PATH = "/storage/self/primary/NADKWebrtcResources/media";
//    private static final String CACHE_PATH = "/storage/self/primary/NADKWebrtcResources/media/cache";
//
//    private Context context;
//
//    private int signalingType;
//    private boolean masterRole = true;
//
//    private boolean prepareWebrtc = false;
//    private NADKWebrtc webrtc;
//    private NADKWebrtcClient webrtcClient;
//
//    private NADKPlayback playback;
//    private NADKPlaybackClient playbackClient;
//    private NADKPlaybackClientService playbackClientService;
//
//
//    private NADKStreaming streaming;
//    private NADKWebrtcStreamParameter streamParameter;
//    private NADKStreamingRender streamingRender;
//    private NADKEventListener nadkStreamingEventListener;
//
//
//    private NADKWebrtcControl webrtcControl;
//
//
//    private boolean enableTalk = false;
//    private AudioManager audioManager;
//    private int originalAudioMode;
//    private boolean originalSpeakerphoneOn;
//    private NADKWebRtcAudioRecord nadkWebRtcAudioRecord;
//
//
//
//    public NADKWebRtcConnection(Context context, int signalingType) {
//        this.context = context;
//        this.signalingType = signalingType;
//    }
//
//    private boolean initWebrtc() {
//
//        this.streamParameter = new NADKWebrtcStreamParameter();
//
//        try
//        {
//            AppLog.i("LiveView", "Flow, createWebrtcStreaming start");
//            /* create webrtc */
//            this.webrtc = NADKWebrtc.create(this.masterRole);
//
//            NADKEventHandler eventHandler = webrtc.getEventHandler();
//
//            /* create streaming based on webrtc */
//            this.streaming = NADKStreamingAssist.createWebrtcStreaming(this.webrtc);
//            registerStreamingListener();
//
//            /* create playback based on webrtc */
//            String path = context.getExternalCacheDir().toString() + "/NADK";
//            createDirectory(path);
//            createDirectory(path);
//
//
//            this.playback = NADKPlaybackAssist.createWebrtcPlayback(
//                    masterRole, path, path, this.webrtc);
//
//
//            webrtc.addClientStatusListener(new WebrtcStatusListener());
//
//            /* init logger */
////            this.initLogger(this.webrtc.getLogger(), masterRole);
//
//            /* add a status listener, we want to upload file log after all
//             * webrtc session disconnected. */
//            webrtc.addActiveClientListener(
//                    new WebrtcLogStatusListener(webrtc.getLogger(),
//                            "android", masterRole ? "master" : "viewer"));
//            AppLog.i("LiveView", "Flow, createWebrtcStreaming end");
//        }        catch(Exception ex) {
//            ex.printStackTrace();
//            AppLog.e("LiveView", "Flow, createWebrtcStreaming end, Exception: " + ex.getMessage());
//        }
//
//        try
//        {
//            /* create webrtc */
//            this.webrtc = NADKWebrtc.create(masterRole);
//
//            /* init logger */
////            this.initLogger(this.webrtc.getLogger(), masterRole);
//
//            /* create playback based on webrtc */
//            String path = context.getExternalCacheDir().toString() + "/NADK";
//            createDirectory(path);
//            createDirectory(path);
//
//
//            this.playback = NADKPlaybackAssist.createWebrtcPlayback(
//                    masterRole, path, path, this.webrtc);
//
//
//            webrtc.addClientStatusListener(new LocalPlaybackActivity.WebrtcStatusListener());
//
//            /* add a status listener, we want to upload file log after all
//             * webrtc session disconnected. */
//            webrtc.addActiveClientListener(
//                    new WebrtcLogStatusListener(webrtc.getLogger(),
//                            "android", masterRole ? "master" : "viewer"));
//        }
//        catch(Exception ex) {
//            ex.printStackTrace();
//            return false;
//        }
//
//        return true;
//
//        return true;
//    }
//
//    private boolean prepareWebrtc()
//    {
//        try
//        {
//            /* create a playback client listener,
//             * the playback client will be used to send/receive media frames */
//            playbackClientService = new NADKPlaybackClientService(this.masterRole, new NADKPlaybackClientListener() {
//                @Override
//                public void connected(NADKPlaybackClient playbackClient) {
//
//                }
//
//                @Override
//                public void disconnected(NADKPlaybackClient playbackClient) {
//
//                }
//            });
//            this.playback.prepare(playbackClientService);
//
//            /* prepare webrtc client*/
//            NADKWebrtcAppConfig appConfig = new NADKWebrtcAppConfig("/storage/self/primary");
//            NADKWebrtcSetupInfo setupInfo = appConfig.createWebrtcSetupInfo(this.masterRole);
//
//            /* create webrtc authentication */
//            NADKWebrtcAuthentication authentication = appConfig.createWebrtcAuthentication(this.masterRole);
//            if (authentication == null) {
//                return false;
//            }
//
//            /* for aiot wss signaling, we need to get the signaling url & ice server info */
//            if (authentication.getSignalingType() == NADKSignalingType.NADK_SIGNALING_TYPE_AIOT_WSS)
//            {
//                if (masterRole) {
//                    NADKWebrtcServiceRoutines.applyTinyAiSettings(appConfig.getDeviceID(), authentication);
//                }
//                else {
//                    NADKWebrtcServiceRoutines.applyTinyAiSettings(
//                            appConfig.getAccountID(), appConfig.getPassword(), appConfig.getDeviceID(), authentication);
//                }
//            }
//
//            /* prepare the webrtc client, connect to the signaling */
//            this.webrtc.prepareWebrtc(setupInfo, authentication);
//        }
//        catch(NADKException ex) {
//            ex.printStackTrace();
//        }
//
//        return true;
//    }
//
//    private boolean destroyWebrtc()
//    {
//        Log.i(TAG, "stop viewer");
//        /* destroy playback */
//        try {
//            this.playback.destroy();
//        } catch (NADKException e) {
//            e.printStackTrace();
//        }
//        /* destroy webrtc */
//        try {
//            this.webrtc.destroyWebrtc();
//        } catch (Exception ex)
//        {
//            ex.printStackTrace();
//            return false;
//        }
//
//        this.playbackClientService = null;
//        return true;
//    }
//
//    private void startWakeup() {
//        NADKAuthorization authorization = NADKConfig.getInstance().getLanModeAuthorization();
//
//        try {
//            wakeUpThread = new WakeUpThread(authorization.getAccessKey(), authorization.getSecretKey());
//            wakeUpThread.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void stopWakeup() {
//        if (wakeUpThread != null) {
//            wakeUpThread.setStopFlag();
//            wakeUpThread = null;
//        }
//    }
//
//    private boolean initWebrtc() {
//
//        try
//        {
//            /* create webrtc */
//            this.webrtc = NADKWebrtc.create(masterRole);
//
//            /* init logger */
////            this.initLogger(this.webrtc.getLogger(), masterRole);
//
//            /* create playback based on webrtc */
//            String path = getExternalCacheDir().toString() + "/NADK";
//            createDirectory(path);
//            createDirectory(path);
//
//
//            this.playback = NADKPlaybackAssist.createWebrtcPlayback(
//                    masterRole, path, path, this.webrtc);
//
//
//            webrtc.addClientStatusListener(new LocalPlaybackActivity.WebrtcStatusListener());
//
//            /* add a status listener, we want to upload file log after all
//             * webrtc session disconnected. */
//            webrtc.addActiveClientListener(
//                    new WebrtcLogStatusListener(webrtc.getLogger(),
//                            "android", masterRole ? "master" : "viewer"));
//        }
//        catch(Exception ex) {
//            ex.printStackTrace();
//            return false;
//        }
//
//        return true;
//    }
//
//    private boolean prepareWebrtc()
//    {
//        startWakeup();
//        try
//        {
//            /* create a playback client listener,
//             * the playback client will be used to send/receive media frames */
//            playbackClientService = new NADKPlaybackClientService(this.masterRole, new LocalPlaybackActivity.LocalPlaybackClientListener());
//            this.playback.prepare(playbackClientService);
//
//            /* prepare webrtc client*/
//
//            NADKWebrtcSetupInfo setupInfo = NADKConfig.getInstance().createNADKWebrtcSetupInfo(masterRole, signalingType);
//
//            /* create webrtc authentication */
//            NADKWebrtcAuthentication authentication = NADKConfig.getInstance().createNADKWebrtcAuthentication(masterRole, signalingType);
//            if (authentication == null) {
//                return false;
//            }
//
//
//            /* prepare the webrtc client, connect to the signaling */
//            this.webrtc.prepareWebrtc(setupInfo, authentication);
//        }
//        catch(NADKException ex) {
//            ex.printStackTrace();
//            return false;
//        }
//
//        return true;
//    }
//
//    private boolean destroyWebrtc()
//    {
//        stopWakeup();
//        AppLog.i(TAG, "stop viewer");
//        /* destroy playback */
//        try {
//            this.playback.destroy();
//        } catch (NADKException e) {
//            e.printStackTrace();
//        }
//        /* destroy webrtc */
//        try {
//            this.webrtc.destroyWebrtc();
//        } catch (Exception ex)
//        {
//            ex.printStackTrace();
//            return false;
//        }
//
//        this.playbackClientService = null;
//        this.localFileListInfo = null;
////        this.fileItemInfoList.clear();
//        this.fileItemInfoList = null;
//        this.webrtcClient = null;
//        this.webrtcControl = null;
//        AppLog.reInitLog();
//        return true;
//    }
//
//    private boolean connect() {
//        setConnectionStatus(R.string.text_connecting);
//        boolean ret = prepareWebrtc();
//        if (!ret) {
//            setConnectionStatus(R.string.text_disconnected);
//        }
//        return true;
//    }
//
//    private boolean disconnect() {
//        releasePlayer();
//        destroyWebrtc();
//        return true;
//    }
//
//    private void initPlayback() {
//        if (localFileListInfo == null) {
////            this.playbackClient = playbackClientService.getPlaybackClient(100);
//            localFileListInfo = new DeviceLocalFileListInfo(playbackClient);
//            try {
//                localFileListInfo.pullDownToRefresh();
//                fileItemInfoList = localFileListInfo.getDeviceFileInfoList();
//            } catch (NADKException e) {
//                e.printStackTrace();
//            }
//
//            CustomImageDownloader downloader = new CustomImageDownloader(localFileListInfo,LocalPlaybackActivity.this);
//            ImageLoaderConfig.initImageLoader(getApplicationContext(), downloader);
//            ImageLoader.getInstance().destroy();
//            ImageLoaderConfig.initImageLoader(getApplicationContext(), downloader);
//
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//
//                    if (fileItemInfoList != null) {
//                        fileListView.renderList(fileItemInfoList);
//                    }
//
//                }
//            });
//        }
//    }
//
//
//    private void createDirectory(String directoryPath) {
//        if (directoryPath != null) {
//            File directory = new File(directoryPath);
//            if (!directory.exists()) {
//                boolean ret = directory.mkdirs();
//                AppLog.d(TAG, "createDirectory: " + directoryPath + ", ret = " + ret);
//            } else {
//                AppLog.d(TAG, "createDirectory: " + directoryPath + ", directory exists");
//            }
//        }
//    }
//
//    private class LocalPlaybackClientListener implements NADKPlaybackClientListener {
//
//        @Override
//        public void connected(NADKPlaybackClient mplaybackClient) {
//            setConnectionStatus(R.string.text_connected);
//            playbackClient = mplaybackClient;
//            ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
//                @Override
//                public void run() {
//                    initPlayback();
//                }
//            }, 200);
//
//
//        }
//
//        @Override
//        public void disconnected(NADKPlaybackClient playbackClient) {
//            setConnectionStatus(R.string.text_disconnected);
//        }
//    }
//
//    private class WebrtcStatusListener implements NADKWebrtcClientStatusListener {
//
//        @Override
//        public void created(NADKWebrtcClient webrtcClient) {
//            initWebrtcControl(webrtcClient);
//
//        }
//
//        @Override
//        public void destroyed(NADKWebrtcClient webrtcClient) {
//            setConnectionStatus(R.string.text_disconnected);
//
//        }
//
//        @Override
//        public void connected(NADKWebrtcClient webrtcClient) {
//            stopWakeup();
//
////            initWebrtcControl(webrtcClient);
//        }
//
//        @Override
//        public void disconnected(NADKWebrtcClient webrtcClient) {
//            setConnectionStatus(R.string.text_disconnected);
//
//        }
//
//    }
//
//    private void initWebrtcControl(NADKWebrtcClient webrtcClient) {
//        this.webrtcClient = webrtcClient;
//        try {
//            webrtcControl= NADKWebrtcControl.createControl(webrtcClient);
//            AppLog.d(TAG, "createControl succeed");
//            webrtcControl.addDataChannelListener(new NADKDataChannelListener() {
//                @Override
//                public void onDataChannelCreated(NADKDataChannel dataChannel) {
//                    try {
//                        AppLog.d(TAG, "onDataChannelCreated: " + dataChannel.getChannelName());
//                    } catch (NADKException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            });
//            AppLog.d(TAG, "addDataChannelListener succeed");
//        } catch (NADKException e) {
//            e.printStackTrace();
//            AppLog.d(TAG, "initWebrtcControl: " + e.getMessage());
//        }
//
//    }
//
//
//    private void registerStreamingListener() {
//        if (streaming != null) {
//            nadkStreamingEventListener = new LiveViewActivity.NADKStreamingListener();
//            try {
//                streaming.addEventListener(NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECTED, nadkStreamingEventListener);
//            } catch (Exception e) {
//                e.printStackTrace();
//                AppLog.d(TAG, "registerStreamingListener NADK_EVENT_WEBRTC_PEER_CONNECTED Exception： " + e + ", " + e.getMessage());
//            }
//
//            try {
//                streaming.addEventListener(NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECT_FAILED, nadkStreamingEventListener);
//            } catch (Exception e) {
//                e.printStackTrace();
//                AppLog.d(TAG, "registerStreamingListener NADK_EVENT_WEBRTC_PEER_CONNECT_FAILED Exception： " + e + ", " + e.getMessage());
//            }
//
//            try {
//                streaming.addEventListener(NADKEventID.NADK_EVENT_WEBRTC_PEER_CLOSED, nadkStreamingEventListener);
//            } catch (Exception e) {
//                e.printStackTrace();
//                AppLog.d(TAG, "registerStreamingListener NADK_EVENT_WEBRTC_PEER_CLOSED Exception： " + e + ", " + e.getMessage());
//            }
//
//            try {
//                streaming.addEventListener(NADKEventID.NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED, nadkStreamingEventListener);
//            } catch (Exception e) {
//                e.printStackTrace();
//                AppLog.d(TAG, "registerStreamingListener NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED Exception： " + e + ", " + e.getMessage());
//            }
//
//            try {
//                streaming.addEventListener(NADKEventID.NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED, nadkStreamingEventListener);
//            } catch (Exception e) {
//                e.printStackTrace();
//                AppLog.d(TAG, "registerStreamingListener NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED Exception： " + e + ", " + e.getMessage());
//            }
//        }
//    }
//
//    private void unRegisterStreamingListener() {
//        if (streaming != null && nadkStreamingEventListener != null) {
//            try {
//                streaming.removeEventListener(NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECTED, nadkStreamingEventListener);
//            } catch (Exception e) {
//                e.printStackTrace();
//                AppLog.d(TAG, "unRegisterStreamingListener NADK_EVENT_WEBRTC_PEER_CONNECTED Exception： " + e + ", " + e.getMessage());
//            }
//
//            try {
//                streaming.removeEventListener(NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECT_FAILED, nadkStreamingEventListener);
//            } catch (Exception e) {
//                e.printStackTrace();
//                AppLog.d(TAG, "unRegisterStreamingListener NADK_EVENT_WEBRTC_PEER_CONNECT_FAILED Exception： " + e + ", " + e.getMessage());
//            }
//
//            try {
//                streaming.removeEventListener(NADKEventID.NADK_EVENT_WEBRTC_PEER_CLOSED, nadkStreamingEventListener);
//            } catch (Exception e) {
//                e.printStackTrace();
//                AppLog.d(TAG, "unRegisterStreamingListener NADK_EVENT_WEBRTC_PEER_CLOSED Exception： " + e + ", " + e.getMessage());
//            }
//
//            try {
//                streaming.removeEventListener(NADKEventID.NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED, nadkStreamingEventListener);
//            } catch (Exception e) {
//                e.printStackTrace();
//                AppLog.d(TAG, "unRegisterStreamingListener NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED Exception： " + e + ", " + e.getMessage());
//            }
//
//            try {
//                streaming.removeEventListener(NADKEventID.NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED, nadkStreamingEventListener);
//            } catch (Exception e) {
//                e.printStackTrace();
//                AppLog.d(TAG, "unRegisterStreamingListener NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED Exception： " + e + ", " + e.getMessage());
//            }
//            nadkStreamingEventListener = null;
//        }
//    }
//
//    private class NADKStreamingListener implements NADKEventListener {
//
//        @Override
//        public void notify(NADKEvent event) {
//            String status = "";
//            if (event.getEventID() == NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECTING) {
//                status = "NADK_EVENT_WEBRTC_PEER_CONNECTING";
//                AppLog.i(TAG, "WEBRTC_PEER Status, NADK_EVENT_WEBRTC_PEER_CONNECTED");
//                AppLog.i("LiveView", "Flow, deviceReady");
//
//
//            } else if (event.getEventID() == NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECTED) {
//                status = "NADK_EVENT_WEBRTC_PEER_CONNECTED";
//                AppLog.i("LiveView", "Flow, NADK_EVENT_WEBRTC_PEER_CONNECTED");
//                stopWakeup();
//                final String finalStatus = status;
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(LiveViewActivity.this, finalStatus, Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//
//            } else if (event.getEventID() == NADKEventID.NADK_EVENT_WEBRTC_PEER_CLOSED || event.getEventID() == NADKEventID.NADK_EVENT_WEBRTC_PEER_CONNECT_FAILED) {
//                String iceConnectionState = "NADK_EVENT_WEBRTC_PEER_CONNECT_FAILED";
//                if (event.getEventID() == NADKEventID.NADK_EVENT_WEBRTC_PEER_CLOSED) {
//                    iceConnectionState = "NADK_EVENT_WEBRTC_PEER_CLOSED";
//                }
//                status = iceConnectionState;
//
//                AppLog.i("LiveView", "Flow, " + iceConnectionState);
//                final String finalStatus = status;
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(LiveViewActivity.this, finalStatus, Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//
//            } else if (event.getEventID() == NADKEventID.NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED) {
//                status = "NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED";
//                AppLog.i("LiveView", "Flow, NADK_EVENT_FIRST_VIDEO_FRAME_RENDERED");
//
//
//            } else if (event.getEventID() == NADKEventID.NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED) {
//                status = "NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED";
//                AppLog.i("LiveView", "Flow, NADK_EVENT_FIRST_AUDIO_FRAME_RENDERED");
//            }
//
////            final String finalStatus = status;
////            handler.post(new Runnable() {
////                @Override
////                public void run() {
////                    Toast.makeText(LiveViewActivity.this, finalStatus, Toast.LENGTH_SHORT).show();
////                }
////            });
//        }
//    }
//
//    private void initTalk(NADKStreamingClient streamingClient) {
//
//        /* enableTalk */
//        try {
//            AppLog.i("LiveView", "Flow, enableTalk start");
//            if (streaming == null) {
//                AppLog.e(TAG, "initTalk streaming == null");
//                return;
//            }
//
//            if (streamingClient == null) {
//                AppLog.e("LiveView", "Flow, enableTalk end, initTalk == null");
//                return;
//            }
//
//            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//            originalAudioMode = audioManager.getMode();
//            originalSpeakerphoneOn = audioManager.isSpeakerphoneOn();
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//            audioManager.setSpeakerphoneOn(true);
//
//            nadkWebRtcAudioRecord = new NADKWebRtcAudioRecord(this, audioManager, streamingClient);
//            if (nadkWebRtcAudioRecord.initRecording(NADKWebRtcAudioRecord.DEFAULT_AUDIO_SAMPLE_RATE, NADKWebRtcAudioRecord.DEFAULT_AUDIO_CHANNEL) > 0) {
//
//                if (nadkWebRtcAudioRecord == null) {
//                    AppLog.e(TAG, "initTalk nadkWebRtcAudioRecord == null");
//                    return;
//                }
//                nadkWebRtcAudioRecord.startRecording();
//            }
//            AppLog.i("LiveView", "Flow, enableTalk end");
//
//        } catch(Exception ex) {
//            ex.printStackTrace();
//            AppLog.e("LiveView", "Flow, enableTalk end, Exception: " + ex.getClass().getSimpleName() + ", error: " + ex.getMessage());
//        }
//    }
//}
