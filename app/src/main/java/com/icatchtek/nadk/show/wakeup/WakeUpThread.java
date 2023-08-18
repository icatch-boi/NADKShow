package com.icatchtek.nadk.show.wakeup;

import java.io.IOException;

/**
 * Created by sha.liu on 2023/8/4.
 */
public class WakeUpThread extends Thread {
    private boolean flag;
    private WakeUpSocket socket;
    private boolean isWakeup = false;

    public WakeUpThread(String ip, String mac) throws IOException {
        socket = new WakeUpSocket(ip, mac, 2000);
    }

    public void setStopFlag(){
        flag = false;
    }

    public boolean isWakeup() {
        return isWakeup;
    }


    public void sendRequest() {
        try {
            socket.send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        flag = true;
        isWakeup = false;

        while(flag){
            try {
                socket.send();
                WakeUpResponse response = socket.receive();
                if (response.getResponse().equals(WakeUpConstants.WAKEUP_RESPONSE)) {
                    isWakeup = true;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    break;
                }
//                    Thread.sleep(5000);
            } catch (IOException e) {
                e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
            }
        }
        flag = false;
        socket.close();
    }
}
