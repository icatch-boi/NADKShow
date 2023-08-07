package com.icatchtek.nadk.show.wakeup;

/**
 * Created by sha.liu on 2023/8/3.
 */
public class WOLMagicPacket {
    private static final String WOL_MAGIC_PACKET_HEADER = "FFFFFFFFFFFF";
    private String mac;
    private byte[] packetByte;

    public WOLMagicPacket(String mac) {
        this.mac = mac.replace("-", "").replace("::", "");
        createMagicPacket();
    }

    public byte[] getMagicPacket() {
        return packetByte;
    }

    private void createMagicPacket() {
        StringBuilder builder = new StringBuilder(WOL_MAGIC_PACKET_HEADER);
        for (int i = 0; i < 16; i++) {
            builder.append(mac);
        }
        String packet =  builder.toString();

        packetByte = hexStringToByteArray(packet);

        String tmp = bytesToHexString(packetByte);

    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return b;
    }

    private String bytesToHexString(byte[] bArray) {
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
