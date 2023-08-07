package com.icatchtek.nadk.show.ssdp;

import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.show.wakeup.WakeUpConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by sha.liu on 2023/8/3.
 */
public class SSDPSocket {
    private DatagramSocket socket;
    private InetAddress inetAddress;
    private int port;

    public SSDPSocket(String ip, int timeout_ms) throws IOException {
        //默认地址和端口：port： 1900,  address：239.255.255.250
//        socket = new DatagramSocket(SSDPConstants.PORT); // Bind some random port for receiving datagram
//        inetAddress = InetAddress.getByName(SSDPConstants.ADDRESS);
//        socket.joinGroup(inetAddress);
        inetAddress = InetAddress.getByName(ip);
        port = SSDPConstants.PORT;
        socket = new DatagramSocket(port);
        socket.setSoTimeout(timeout_ms);

    }

    /* Used to send SSDP packet */
    public void send() throws IOException {
        SSDPSearchRequest request = new SSDPSearchRequest();
        byte[] data = request.toString().getBytes();
        DatagramPacket dp = new DatagramPacket(data, data.length, inetAddress, port);
        socket.send(dp);
    }

    /* Used to receive SSDP packet */
    public SSDPSearchResponse receive() throws IOException {
        byte[] buf = new byte[4096];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);
        socket.receive(dp);

        String data = new String(dp.getData()).trim();
        String ip = dp.getAddress().getHostAddress();
        int port = dp.getPort();
        SSDPSearchResponse response = new SSDPSearchResponse(data);
        response.setIP(ip);
        response.setPort(port);
        String mac = response.getMac();
        AppLog.e("SSDPSocket", "接收到的消息为：\n" + data + "\n来源IP地址：" + ip+ "\n来源port：" + port+ "\n来源mac：" + mac);

        return response;
    }

    public void close() {
        if (socket != null) {
            socket.close();
        }
    }

}
