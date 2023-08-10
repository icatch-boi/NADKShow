package com.icatchtek.nadk.show.utils;

import com.icatchtek.nadk.reliant.NADKSignalingType;
import com.icatchtek.nadk.reliant.NADKWebrtcAuthentication;
import com.icatchtek.nadk.reliant.NADKWebrtcSetupInfo;
import com.icatchtek.nadk.webrtc.assist.NADKAuthorization;
import com.icatchtek.nadk.webrtc.assist.NADKAuthorizationConfig;

/**
 * Created by sha.liu on 2023/7/11.
 */
public class NADKConfig {
    private static final String TAG = NADKConfig.class.getSimpleName();
    private static NADKConfig instance;
    private final String AWS_CA = "cert.pem";
    private final String TINYAI_CA = "digcert_ca.pem";
    private final String NADK_CA_FILE_PATH = "/storage/self/primary/certs";
    private final String NADK_CONFIG_FILE_PATH = "/storage/self/primary";
    private NADKAuthorizationConfig authorizationConfig;

    public static NADKConfig getInstance() {
        if (instance == null) {
            instance = new NADKConfig();
        }
        return instance;
    }

    public static void release() {
        instance = null;
    }

    private NADKConfig() {
        authorizationConfig = new NADKAuthorizationConfig(NADK_CONFIG_FILE_PATH);
    }

    public void loadConfig() {
        authorizationConfig.loadConfig(NADK_CONFIG_FILE_PATH);
    }

    public boolean serializeConfig() {
        return authorizationConfig.serializeConfig(NADK_CONFIG_FILE_PATH);
    }

    public void setAWSKVSWebrtcAuthorization(NADKAuthorization auth) {
        authorizationConfig.setAWSKVSWebrtcAuthorization(auth);
    }

    public NADKAuthorization getAWSKVSWebrtcAuthorization() {
        return authorizationConfig.getAWSKVSWebrtcAuthorization();
    }

    public void setAWSKVSStreamAuthorization(NADKAuthorization  auth) {
        authorizationConfig.setAWSKVSStreamAuthorization(auth);
    }

    public NADKAuthorization getAWSKVSStreamAuthorization() {
        return authorizationConfig.getAWSKVSStreamAuthorization();
    }

    public void setTinyaiRtcAuthorization(NADKAuthorization  auth) {
        authorizationConfig.setTinyaiRtcAuthorization(auth);
    }

    public NADKAuthorization getTinyaiRtcAuthorization() {
        return authorizationConfig.getTinyaiRtcAuthorization();
    }

    public void setLanModeAuthorization(NADKAuthorization  auth) {
        authorizationConfig.setLanModeAuthorization(auth);
    }

    public NADKAuthorization getLanModeAuthorization() {
        return authorizationConfig.getLanModeAuthorization();
    }

    public void setSrtp(boolean srtp)
    {
        authorizationConfig.setSrtp(srtp);
    }
    public boolean getSrtp()
    {
        return authorizationConfig.getSrtp();
    }

    public void setRtcpTwcc(boolean rtcpTwcc)
    {
        authorizationConfig.setRtcpTwcc(rtcpTwcc);
    }
    public boolean getRtcpTwcc()
    {
        return authorizationConfig.getRtcpTwcc();
    }

    public NADKWebrtcAuthentication createNADKWebrtcAuthentication(boolean isMaster, int signalingType) {
        NADKWebrtcAuthentication authentication = authorizationConfig.createWebrtcAuthentication(isMaster, signalingType);
        String cafile = TINYAI_CA;
        if (signalingType == NADKSignalingType.NADK_SIGNALING_TYPE_KVS) {
            cafile = AWS_CA;
        }
        authentication.setCertFile(NADK_CA_FILE_PATH + "/" + cafile);
        return authentication;
    }

    public NADKWebrtcSetupInfo createNADKWebrtcSetupInfo(boolean isMaster, int signalingType) {
        NADKWebrtcSetupInfo setupInfo = new NADKWebrtcSetupInfo(isMaster);
        setupInfo.setTrickleIce(true);
        setupInfo.setUseTurn(true);
        setupInfo.setMaxLatency(500);
        setupInfo.setLocalAddresses(NetworkUtils.getNetworkAddress());

        if (signalingType == NADKSignalingType.NADK_SIGNALING_TYPE_BASE_TCP) {
            setupInfo.setSrtp(false);
            setupInfo.setRtcpTwcc(true);
        } else {
            setupInfo.setSrtp(getSrtp());
            setupInfo.setRtcpTwcc(getRtcpTwcc());
        }
        return setupInfo;
    }


}
