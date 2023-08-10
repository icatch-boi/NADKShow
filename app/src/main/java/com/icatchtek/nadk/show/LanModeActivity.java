package com.icatchtek.nadk.show;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.icatchtek.baseutil.ThreadPoolUtils;
import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.reliant.NADKSignalingType;
import com.icatchtek.nadk.show.device.DeviceManager;
import com.icatchtek.nadk.show.device.NADKLocalDevice;
import com.icatchtek.nadk.show.ssdp.SSDPSearchResponse;
import com.icatchtek.nadk.show.ssdp.SearchUtils;
import com.icatchtek.nadk.show.utils.NADKConfig;
import com.icatchtek.nadk.show.wakeup.WOLMagicPacket;
import com.icatchtek.nadk.webrtc.assist.NADKAuthorization;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class LanModeActivity extends NADKShowBaseActivity {
    private static final String TAG = LanModeActivity.class.getSimpleName();
    private ImageButton back_btn;
    private Button live_view_btn;
    private Button local_playback_btn;

    private DatagramSocket socket;
    private UDPReceiveThread udpReceiveThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan_mode);
        back_btn = findViewById(R.id.back_btn);
        live_view_btn = findViewById(R.id.live_view_btn);
        local_playback_btn = findViewById(R.id.local_playback_btn);


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        live_view_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLanModeAuthorization();
                Intent intent = new Intent(LanModeActivity.this, LiveViewActivity.class);
                intent.putExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_BASE_TCP);
                startActivity(intent);

            }
        });


        local_playback_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLanModeAuthorization();
                Intent intent = new Intent(LanModeActivity.this, LocalPlaybackActivity.class);
                intent.putExtra("signalingType", NADKSignalingType.NADK_SIGNALING_TYPE_BASE_TCP);
                startActivity(intent);

            }
        });

        Button wakeup_btn = findViewById(R.id.wakeup_btn);
        wakeup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
                    @Override
                    public void run() {
                        wakeup();
//                        serch();
                    }
                }, 200);
            }
        });

        Button search_btn = findViewById(R.id.search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadPoolUtils.getInstance().executorNetThread(new Runnable() {
                    @Override
                    public void run() {
//                        wakeup();
                        serch();
                    }
                }, 200);
            }
        });

        getLanModeAuthorization();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (udpReceiveThread != null) {
            udpReceiveThread.setStopFlag();
        }
    }


    public void getLanModeAuthorization() {
        EditText channelname_edt = findViewById(R.id.channelname_edt);
        EditText endpoint_edt = findViewById(R.id.endpoint_edt);
        EditText clientid_edt = findViewById(R.id.clientid_edt);

        NADKAuthorization auth = NADKConfig.getInstance().getLanModeAuthorization();
        channelname_edt.setText(auth.getChannelName());
        endpoint_edt.setText(auth.getEndpoint());
        clientid_edt.setText(auth.getClientId());
    }

    public void setLanModeAuthorization() {
        EditText channelname_edt = findViewById(R.id.channelname_edt);
        EditText endpoint_edt = findViewById(R.id.endpoint_edt);
        EditText clientid_edt = findViewById(R.id.clientid_edt);

        NADKAuthorization auth = new NADKAuthorization();
        auth.setChannelName(channelname_edt.getText().toString());
        auth.setEndpoint(endpoint_edt.getText().toString());
        auth.setClientId(clientid_edt.getText().toString());
        String endpoint = auth.getEndpoint();
        String[] list = endpoint.split("//");
        if (list.length >= 2) {
            String[] tmpList = list[1].split(":");
            if (tmpList.length > 0) {
                String ip = tmpList[0];
                auth.setAccessKey(ip);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String mac = SearchUtils.getMac(ip);
                        auth.setSecretKey(mac);
                    }
                });
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        NADKConfig.getInstance().setLanModeAuthorization(auth);
        NADKConfig.getInstance().serializeConfig();
        DeviceManager.getInstance().addDevice(new NADKLocalDevice(auth.getAccessKey(), auth.getAccessKey(), auth.getAccessKey(), NADKLocalDevice.LOCAL_SIGNALING_DEFAULT_PORT, auth.getSecretKey(), auth.getChannelName()));
    }


    private void wakeup() {
        if (udpReceiveThread != null) {
            udpReceiveThread.setStopFlag();
            try {
                udpReceiveThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        byte[] packet = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A,
//                0x44, (byte)0xEF, (byte)0xBF, 0x4B, 0x6C, 0x5A};

        byte[] packet = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31,
                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0xAB, 0x31};

//        byte[] packet = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F,
//                0x30, (byte)0x7B, (byte)0xC9, 0x24, (byte)0x83, 0x7F};

        String ip = "192.168.0.101";
        int port = 59001;

        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            DatagramSocket client = new DatagramSocket();
            // 设置接收数据时阻塞的最长时间
            client.setSoTimeout(5000);

            udpReceiveThread = new UDPReceiveThread(client);
            udpReceiveThread.start();
            // 发送数据包
            DatagramPacket dataPacket = new DatagramPacket(packet, packet.length, inetAddress, port);
            client.send(dataPacket);

//            client.close();
        } catch (IOException e) {
            e.printStackTrace();
            AppLog.e(TAG, "IOException: " + e.getMessage());
        }


    }

    private void serch() {

        byte[] mac = hexStringToByteArray("307BC924AB31");
        String macStr = bytesToHexString(mac);

        if (udpReceiveThread != null) {
            udpReceiveThread.setStopFlag();
            try {
                udpReceiveThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String packet = "M-SEARCH * HTTP/1.1\r\n" +
                "Host: 239.255.255.250:19000\r\n" +
                "Man: \"ssdp:discover\"\r\n" +
                "MX: 2\r\n" +
                "ST: urn:icatch-upnp:device:camera:1\r\n";
        String ip = "239.255.255.250";
//        String ip = "192.168.93.255";
        int port = 59000;

        List<String> listReceive = new ArrayList<String>();

        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            DatagramSocket socket = new DatagramSocket();
            // 设置接收数据时阻塞的最长时间
            socket.setSoTimeout(5000);
            udpReceiveThread = new UDPReceiveThread(socket);
            udpReceiveThread.start();
            // 发送数据包
            DatagramPacket dataPacket = new DatagramPacket(packet.getBytes(StandardCharsets.UTF_8), packet.length(), inetAddress, port);
            socket.send(dataPacket);

        } catch (IOException e) {
            e.printStackTrace();
            AppLog.e(TAG, "IOException: " + e.getMessage());
        }

    }


    private class UDPReceiveThread extends Thread {
        private boolean flag;
        private DatagramSocket socket;


        public UDPReceiveThread(DatagramSocket socket) {
            this.socket = socket;
        }

        public void setStopFlag(){
            flag = false;
        }

        @Override
        public void run(){
            flag = true;
            try {


                while(flag){
                    try {
                        byte[] buf = new byte[4096];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        String c = new String(packet.getData()).trim();
                        String remoteip = packet.getAddress().getHostAddress();
                        int remoteport = packet.getPort();
                        SSDPSearchResponse response = new SSDPSearchResponse(c);
                        String mac = response.getMac();
                        AppLog.e(TAG, "接收到的消息为：\n" + c + "\n来源IP地址：" + remoteip+ "\n来源port：" + remoteport+ "\n来源mac：" + mac);

                        if (mac == null || mac.isEmpty()) {
                            continue;
                        }
                        WOLMagicPacket magicPacket = new WOLMagicPacket(mac);
                        byte[] tmp = magicPacket.getMagicPacket();
                        if (tmp != null) {
                            InetAddress inetAddress = InetAddress.getByName(remoteip);


                            // 发送数据包
                            DatagramPacket dataPacket = new DatagramPacket(tmp, tmp.length, inetAddress, 59001);
                            socket.send(dataPacket);
                        }

                    } catch(SocketTimeoutException e){
                        e.printStackTrace();
                    }	// 为了能结束线程，每秒钟退出receive一次，检查flag
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return b;
    }

    public String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }


}