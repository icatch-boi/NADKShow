package com.icatchtek.nadk.show.ssdp;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by sha.liu on 2023/8/4.
 */
public class SearchUtils {

    public static String getMac(String ip) {
        SSDPSocket socket = null;
        try {
            socket = new SSDPSocket(ip, 2000);
            socket.send();
            SSDPSearchResponse response = socket.receive();
            if (response != null) {
                socket.close();
                return response.getMac();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (socket != null) {
            socket.close();
        }
        return null;
    }

    public static List<SSDPSearchResponse> searchDevice(long timeout_ms) {
        try {
            SearchDeviceThread thread = new SearchDeviceThread();
            thread.start();
            thread.join(timeout_ms);
            thread.setStopFlag();
            return thread.getResponseMap();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static List<SSDPSearchResponse> searchDevice(long timeout_ms, SearchDeviceThread.SSDPSearchResponseListener responseListener) {
        try {
            SearchDeviceThread thread = new SearchDeviceThread();
            thread.setResponseListener(responseListener);
            thread.start();
            thread.join(timeout_ms);
            thread.setStopFlag();
            return thread.getResponseMap();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }
}
