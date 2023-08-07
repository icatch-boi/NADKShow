package com.icatchtek.nadk.show.ssdp;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by sha.liu on 2023/8/4.
 */
public class SearchDeviceThread extends Thread {
    private boolean flag;
    private SSDPSocket socket;
    private List<SSDPSearchResponse> responseList;
    private SSDPSearchResponseListener responseListener;


    public SearchDeviceThread() throws IOException {
        this.socket = new SSDPSocket(SSDPConstants.ADDRESS, 2000);
        responseList = new LinkedList<>();
        SSDPSearchResponse response = new SSDPSearchResponse("ST: urn:icatch-upnp:device:camera:1\r\nUSN: uuid:30-7B-C9-24-AB-31::urn:icatch-upnp:device:camera:1\r\n");
        response.setIP("192.168.0.114");
        responseList.add(response);
    }

    public void setStopFlag(){
        flag = false;
    }

    public List<SSDPSearchResponse> getResponseMap() {
        return responseList;
    }

    public void sendRequest() {
        try {
            socket.send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setResponseListener(SSDPSearchResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    @Override
    public void run(){
        flag = true;

        try {
            socket.send();

            while(flag){
                try {
                    SSDPSearchResponse response = socket.receive();
                    if (response != null) {
                        responseList.add(response);
                        if (responseListener != null) {
                            responseListener.notify(response);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface SSDPSearchResponseListener {
        void notify(SSDPSearchResponse response);
    }
}
