package com.icatchtek.nadk.show.ssdp;

import java.util.List;

/**
 * Created by sha.liu on 2023/8/3.
 */
public class SSDPSearchResponse {
    private String mST;
    private String mUSN;
    private String mMac;
    private String mIP;
    private int mPort;


    public SSDPSearchResponse(String response) {
        parse(response);
    }

    public String getST() {
        return mST;
    }

    public String getUSN() {
        return mUSN;
    }

    public String getMac() {
        return mMac;
    }

    public String getIP() {
        return mIP;
    }

    public void setIP(String mIP) {
        this.mIP = mIP;
    }

    public int getPort() {
        return mPort;
    }

    public void setPort(int mPort) {
        this.mPort = mPort;
    }

    private void parse(String response) {
        String[] infoList = response.split(SSDPConstants.NEWLINE);

        for(int i = 0; i < infoList.length; i++) {
            String info = infoList[i];
            if (info.startsWith("ST:")) {
                mST = info.replace(" ", "");
            } else if (info.startsWith("USN:")) {
                mUSN = info.replace(" ", "");
                int startIndex = 9;
                mMac = mUSN.substring(startIndex, startIndex + 17);
            }
        }

    }
}
