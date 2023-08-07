package com.icatchtek.nadk.show.wakeup;

import com.icatchtek.nadk.show.ssdp.SSDPConstants;

/**
 * Created by sha.liu on 2023/8/3.
 */
public class WakeUpResponse {
    private String response;
    private String mIP;

    public WakeUpResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public String getIP() {
        return mIP;
    }

    public void setIP(String mIP) {
        this.mIP = mIP;
    }

}
