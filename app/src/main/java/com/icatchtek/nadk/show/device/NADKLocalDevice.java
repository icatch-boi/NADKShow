package com.icatchtek.nadk.show.device;

import com.icatchtek.baseutil.RandomString;
import com.icatchtek.nadk.playback.NADKPlaybackClient;
import com.icatchtek.nadk.webrtc.assist.NADKAuthorization;

/**
 * Created by sha.liu on 2023/8/4.
 */
public class NADKLocalDevice {
    public static final String DEFAULT_CHANNEL_NAME = "DefaultChannel";
    public static final int LOCAL_SIGNALING_DEFAULT_PORT = 8007;
    private String deviceId;
    private String deviceName;
    private String ip;
    private int port;
    private String mac;
    private String channelName;

    private NADKPlaybackClient playbackClient;


    public NADKLocalDevice(String deviceId, String deviceName, String ip, int port, String mac, String channelName) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.ip = ip;
        this.port = port;
        this.mac = mac;
        this.channelName = channelName;
    }


    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getMac() {
        return mac;
    }

    public String getChannelName() {
        return channelName;
    }

    public NADKAuthorization getNADKAuthorization() {
        NADKAuthorization authorization = new NADKAuthorization();
        authorization.setChannelName(channelName);
        authorization.setEndpoint(String.format("tcp://%s:%d", ip, port));
        authorization.setClientId(new RandomString().nextString());
        authorization.setAccessKey(ip);
        authorization.setSecretKey(mac);
        return authorization;
    }

    public NADKPlaybackClient getPlaybackClient() {
        return playbackClient;
    }

    public void setPlaybackClient(NADKPlaybackClient playbackClient) {
        this.playbackClient = playbackClient;
    }
}
