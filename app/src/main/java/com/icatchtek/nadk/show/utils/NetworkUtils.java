package com.icatchtek.nadk.show.utils;

import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.reliant.NADKNetAddress;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sha.liu on 2023/7/25.
 */
public class NetworkUtils {

    public static List<NADKNetAddress> getNetworkAddress()
    {
        List<NADKNetAddress> netAddresses = new LinkedList<>();

        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                AppLog.i("ipAddress", "info: " + networkInterface.getName());
                if (!networkInterface.getName().contains("wlan")) {
                    continue;
                }

                AppLog.i("ipAddress", "found valid wlan interface: " + networkInterface.getName());
                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (address instanceof Inet4Address) {
                        AppLog.i("ipAddress", "valid inet4 address: " + address.getHostAddress());
                        NADKNetAddress netAddress = new NADKNetAddress(true, address.getHostAddress());
                        netAddresses.add(netAddress);
                    }
                    if (address instanceof Inet6Address) {
                        String hostAddr = address.getHostAddress();
                        if (hostAddr.startsWith("fe80")) {
                            continue;
                        }
                        AppLog.i("ipAddress", "valid inet6 address: " + address.getHostAddress());
                        NADKNetAddress netAddress = new NADKNetAddress(false, address.getHostAddress());
                        netAddresses.add(netAddress);
                    }
                }
            }
        } catch (Exception e) {
            AppLog.e("getIPAddress", e.toString());
        }
        return netAddresses;
    }
}
