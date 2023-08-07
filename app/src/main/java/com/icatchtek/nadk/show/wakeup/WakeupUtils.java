package com.icatchtek.nadk.show.wakeup;
import java.io.IOException;

/**
 * Created by sha.liu on 2023/8/4.
 */
public class WakeupUtils {

    public static boolean wakeup(String ip, String mac) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        if (mac == null || mac.isEmpty()) {
            return false;
        }

        WakeUpSocket socket = null;
        boolean ret = false;
        try {
            socket = new WakeUpSocket(ip, mac, 2000);
            socket.send();
            WakeUpResponse response = socket.receive();
            if (response.getResponse().equals(WakeUpConstants.WAKEUP_RESPONSE)) {
                ret =  true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (socket != null) {
            socket.close();
        }

        return ret;

    }

    public static boolean wakeup(String ip, String mac, long timeout_ms) {
        try {
            WakeUpThread thread = new WakeUpThread(ip, mac);
            thread.start();
            thread.join(timeout_ms);
            thread.setStopFlag();
            return thread.isWakeup();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }



}
