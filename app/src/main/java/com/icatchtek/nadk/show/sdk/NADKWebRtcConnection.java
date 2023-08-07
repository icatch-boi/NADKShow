package com.icatchtek.nadk.show.sdk;

import android.util.Log;

import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.playback.NADKPlayback;
import com.icatchtek.nadk.playback.NADKPlaybackAssist;
import com.icatchtek.nadk.playback.NADKPlaybackClient;
import com.icatchtek.nadk.playback.NADKPlaybackClientListener;
import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.reliant.NADKNetAddress;
import com.icatchtek.nadk.reliant.NADKSignalingType;
import com.icatchtek.nadk.reliant.NADKWebrtcAuthentication;
import com.icatchtek.nadk.reliant.NADKWebrtcSetupInfo;
import com.icatchtek.nadk.reliant.parameter.NADKAudioParameter;
import com.icatchtek.nadk.reliant.parameter.NADKVideoParameter;
import com.icatchtek.nadk.reliant.parameter.NADKWebrtcStreamParameter;
import com.icatchtek.nadk.show.assist.WebrtcLogStatusListener;
import com.icatchtek.nadk.show.utils.NetworkUtils;
import com.icatchtek.nadk.webrtc.NADKWebrtc;
import com.icatchtek.nadk.webrtc.assist.NADKWebrtcAppConfig;
import com.icatchtek.nadk.webrtc.assist.NADKWebrtcServiceRoutines;

import java.io.File;
import java.util.List;

/**
 * Created by sha.liu on 2023/7/25.
 */
public class NADKWebRtcConnection {
    private static final String TAG = NADKWebRtcConnection.class.getSimpleName();
    private static final String MEDIA_PATH = "/storage/self/primary/NADKWebrtcResources/media";
    private static final String CACHE_PATH = "/storage/self/primary/NADKWebrtcResources/media/cache";

    private boolean masterRole = true;
    private NADKWebrtc webrtc;
    private NADKPlayback playback;
    private NADKWebrtcStreamParameter streamParameter;
    private NADKPlaybackClientService playbackClientService;

    private boolean initWebrtc() {
        List<NADKNetAddress> localAddresses = NetworkUtils.getNetworkAddress();
        this.streamParameter = new NADKWebrtcStreamParameter();

        try
        {
            /* create webrtc */
            this.webrtc = NADKWebrtc.create(masterRole);

            /* init logger */
//            this.initLogger(this.webrtc.getLogger(), masterRole);

            /* create playback based on webrtc */
            this.playback = NADKPlaybackAssist.createWebrtcPlayback(
                    masterRole, MEDIA_PATH, CACHE_PATH, this.webrtc);


            /* add a status listener, we want to upload file log after all
             * webrtc session disconnected. */
            webrtc.addActiveClientListener(
                    new WebrtcLogStatusListener(webrtc.getLogger(),
                            "android", masterRole ? "master" : "viewer"));
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    private boolean prepareWebrtc()
    {
        try
        {
            /* create a playback client listener,
             * the playback client will be used to send/receive media frames */
            playbackClientService = new NADKPlaybackClientService(this.masterRole, new NADKPlaybackClientListener() {
                @Override
                public void connected(NADKPlaybackClient playbackClient) {

                }

                @Override
                public void disconnected(NADKPlaybackClient playbackClient) {

                }
            });
            this.playback.prepare(playbackClientService);

            /* prepare webrtc client*/
            NADKWebrtcAppConfig appConfig = new NADKWebrtcAppConfig("/storage/self/primary");
            NADKWebrtcSetupInfo setupInfo = appConfig.createWebrtcSetupInfo(this.masterRole);

            /* create webrtc authentication */
            NADKWebrtcAuthentication authentication = appConfig.createWebrtcAuthentication(this.masterRole);
            if (authentication == null) {
                return false;
            }

            /* for aiot wss signaling, we need to get the signaling url & ice server info */
            if (authentication.getSignalingType() == NADKSignalingType.NADK_SIGNALING_TYPE_AIOT_WSS)
            {
                if (masterRole) {
                    NADKWebrtcServiceRoutines.applyTinyAiSettings(appConfig.getDeviceID(), authentication);
                }
                else {
                    NADKWebrtcServiceRoutines.applyTinyAiSettings(
                            appConfig.getAccountID(), appConfig.getPassword(), appConfig.getDeviceID(), authentication);
                }
            }

            /* prepare the webrtc client, connect to the signaling */
            this.webrtc.prepareWebrtc(setupInfo, authentication);
        }
        catch(NADKException ex) {
            ex.printStackTrace();
        }

        return true;
    }

    private boolean destroyWebrtc()
    {
        Log.i(TAG, "stop viewer");
        /* destroy playback */
        try {
            this.playback.destroy();
        } catch (NADKException e) {
            e.printStackTrace();
        }
        /* destroy webrtc */
        try {
            this.webrtc.destroyWebrtc();
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }

        this.playbackClientService = null;
        return true;
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
