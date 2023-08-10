package com.icatchtek.nadk.show.sdk.datachannel;//package com.tinyai.bpscam.webrtc.datachannel;

import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.reliant.datachannel.NADKDataChannel;
import com.icatchtek.nadk.reliant.datachannel.NADKDataChannelMessageObserver;


/**
 * Created by sha.liu on 2022/3/28.
 */
public class NADKWebRtcDataChannel implements IDataChannel {
    private String TAG = NADKWebRtcDataChannel.class.getSimpleName();
    private NADKDataChannel dataChannel;
    private NADKDataChannelMessageObserver dataChannelMessageObserver;

    public NADKWebRtcDataChannel(NADKDataChannel dataChannel) {
        this.dataChannel = dataChannel;
    }

    @Override
    public String getChannelName() {
        try {
            return dataChannel.getChannelName();
        } catch (NADKException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void registerObserver(Observer observer) {
        try {
            dataChannelMessageObserver = new NADKDataChannelMessageObserver() {

                @Override
                public void onMessage(String s) {
                    observer.onMessage(s);

                }

                @Override
                public void onMessage(byte[] bytes, int i) {
                    observer.onMessage(bytes, i);
                }
            };
            dataChannel.setChannelMessageObserver(dataChannelMessageObserver);
        } catch (NADKException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void unregisterObserver() {
        try {
            dataChannel.removeChannelMessageObserver(dataChannelMessageObserver);
        } catch (NADKException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean sendData(String message) {
        int ret = -1;
        try {
            dataChannel.sendData(message);
        } catch (NADKException e) {
            e.printStackTrace();
            ret = e.getErrCode();
        }
        AppLog.d(TAG, "sendData: " + ret);
        return ret >= 0;
    }

    @Override
    public boolean sendData(byte[] data, int dataSize) {
        int ret = -1;
        try {
            dataChannel.sendData(data, dataSize);
        } catch (NADKException e) {
            e.printStackTrace();
            ret = e.getErrCode();
        }
        AppLog.d(TAG, "sendData: " + ret);
        return ret >= 0;
    }
}
