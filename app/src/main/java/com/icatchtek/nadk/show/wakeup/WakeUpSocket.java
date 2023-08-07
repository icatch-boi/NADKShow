package com.icatchtek.nadk.show.wakeup;

import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.show.ssdp.SSDPSearchResponse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by sha.liu on 2023/8/3.
 */
public class WakeUpSocket {
    private DatagramSocket socket;
    private InetAddress inetAddress;
    private int port;
    private WOLMagicPacket magicPacket;

    public WakeUpSocket(String ip, String mac, int timeout_ms) throws IOException {
        inetAddress = InetAddress.getByName(ip);
        port = WakeUpConstants.PORT;
        magicPacket = new WOLMagicPacket(mac);
        socket = new DatagramSocket(port);
        socket.setSoTimeout(timeout_ms);

    }

    /* Used to send SSDP packet */
    public void send() throws IOException {
        byte[] data = magicPacket.getMagicPacket();
        DatagramPacket dp = new DatagramPacket(data, data.length, inetAddress, port);
        socket.send(dp);
    }

    /* Used to receive SSDP packet */
    public WakeUpResponse receive() throws IOException {
        byte[] buf = new byte[4096];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);
        socket.receive(dp);

        String data = new String(dp.getData()).trim();
        String ip = dp.getAddress().getHostAddress();
        int port = dp.getPort();
        WakeUpResponse response = new WakeUpResponse(data);
        response.setIP(ip);
        AppLog.e("WakeUpSocket", "接收到的消息为：\n" + data + "\n来源IP地址：" + ip+ "\n来源port：" + port);

        return response;
    }

    public void close() {
        if (socket != null) {
            socket.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
