package com.icatchtek.nadk.show.sdk.datachannel;

/**
 * Created by sha.liu on 2021/10/21.
 */
public class PacketData {
    private PacketHeader header;
    private byte[] data;
    private byte[] packetByte;

    public PacketData(byte[] packetByte) {
        this.packetByte = packetByte;
        header = new PacketHeader(packetByte);
        data = new byte[header.getDataSize()];
        System.arraycopy(packetByte, PacketHeader.HEADER_SIZE, data, 0, header.getDataSize());
//        ByteBuffer byteBuffer = ByteBuffer.wrap(packetByte, PacketHeader.HEADER_SIZE, header.getDataSize());
//        data = byteBuffer.array();
    }

    public PacketData(PacketHeader header, byte[] data, int offset) {
        this.header = header;
        this.data = data;
        packetByte = new byte[header.getPacketSize()];
        System.arraycopy(header.getHeaderByte(), 0, packetByte, 0, PacketHeader.HEADER_SIZE);
        System.arraycopy(data, offset, packetByte, PacketHeader.HEADER_SIZE, header.getDataSize());
    }

    public PacketHeader getHeader() {
        return header;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getPacketByte() {
        return packetByte;
    }
}
