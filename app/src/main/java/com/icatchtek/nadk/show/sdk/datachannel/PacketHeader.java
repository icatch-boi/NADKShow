package com.icatchtek.nadk.show.sdk.datachannel;

import java.nio.ByteBuffer;

/**
 * Created by sha.liu on 2021/10/21.
 */
public class PacketHeader {
    private static final int PACKET_VERSION = 1;
    public static final int HEADER_SIZE = 8;
    public static final int DATA_TYPE_STRING = 0;
    public static final int DATA_TYPE_BINARY = 1;
    public static final int END_FLAG_NO = 0;
    public static final int END_FLAG_YES = 1;
    private int version; //4bit
    private int transactionId; //12bit
    private int packetIndex; //16bit
    private int packetSize; //16bit
    private int endFlag; //1bit
    private int dataType; //1bit
    private int reserved; //14bit

    private byte[] headerByte;
    private int dataSize;

    public PacketHeader(int transactionId, int packetIndex, int dataSize, int endFlag, int dataType, int reserved) {
        this.version = PACKET_VERSION;
        this.transactionId = transactionId;
        this.packetIndex = packetIndex;
        this.dataSize = dataSize;
        this.packetSize = dataSize + HEADER_SIZE;
        this.endFlag = endFlag;
        this.dataType = dataType;
        this.reserved = reserved;
        short[] shorts = new short[HEADER_SIZE / 2];
//        shorts[0] = (short) ((this.version & 0x0000000F) | ((this.transactionId & 0x00000FFF) << 4));
        shorts[0] = (short) (((this.version & 0x0000000F) << 12) | (this.transactionId & 0x00000FFF));
        shorts[1] = (short) packetIndex;
        shorts[2] = (short) packetSize;
//        shorts[3] = (short) ((this.endFlag & 0x00000001) | ((this.dataType & 0x00000001) << 1) | ((this.reserved & 0x000003FF) << 2));
        shorts[3] = (short) (((this.endFlag & 0x00000001) << 15) | ((this.dataType & 0x00000001) << 14) | (this.reserved & 0x00003FFF));

//        headerByte = shortToByte(shorts, false);
        headerByte = shortToByte(shorts, true);

    }

    public PacketHeader(byte[] headerByte) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(headerByte, 0, HEADER_SIZE);
        this.headerByte = byteBuffer.array();

//        version = getBits(getShort(headerByte, 0, false), 0, 4);
//        transactionId = getBits(getShort(headerByte, 0, false), 4, 12);
//        packetIndex = getBits(getShort(headerByte, 2, false), 0, 16);
//        packetSize = getBits(getShort(headerByte, 4, false), 0, 16);
//        endFlag = getBits(getShort(headerByte, 6, false), 0, 1);
//        dataType = getBits(getShort(headerByte, 6, false), 1, 1);
//        reserved = getBits(getShort(headerByte, 6, false), 2, 14);

        version = getBits(getShort(headerByte, 0, true), 12, 4);
        transactionId = getBits(getShort(headerByte, 0, true), 0, 12);
        packetIndex = getBits(getShort(headerByte, 2, true), 0, 16);
        packetSize = getBits(getShort(headerByte, 4, true), 0, 16);
        endFlag = getBits(getShort(headerByte, 6, true), 15, 1);
        dataType = getBits(getShort(headerByte, 6, true), 14, 1);
        reserved = getBits(getShort(headerByte, 6, true), 0, 14);
        dataSize = packetSize - HEADER_SIZE;
    }

    public int getVersion() {
        return version;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getPacketIndex() {
        return packetIndex;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getEndFlag() {
        return endFlag;
    }

    public int getDataType() {
        return dataType;
    }

    public int getReserved() {
        return reserved;
    }

    public byte[] getHeaderByte() {
        return headerByte;
    }

    public int getDataSize() {
        return dataSize;
    }

    //b为传入的字节，start是起始位，length是长度，如要获取bit0-bit4的值，则start为0，length为5
    private int getBits(short b,int start, int length) {
        int bit = (int)((b >>> start) & (0xFFFF >>> (16 - length)));
        return bit;
    }

    private short getShort(byte[] buf, int post, boolean bBigEnding) {
        if (buf == null) {
            throw new IllegalArgumentException("byte array is null!");
        }

        short r = 0;
        if (bBigEnding) {
            for (int i = 0; i < 2; i++) {
                r <<= 8;
                r |= (buf[post + i] & 0x00FF);
            }
        } else {
            for (int i = 2 - 1; i >= 0; i--) {
                r <<= 8;
                r |= (buf[post + i] & 0x00FF);
            }
        }

        return r;
    }

    private byte[] shortToByte(short[] shorts, boolean bBigEnding) {
        byte[] bytes = new byte[shorts.length * 2];
        for(int i = 0; i < shorts.length; i++) {
            if (bBigEnding) {
                bytes[2 * i] = (byte) ((shorts[i] & 0xFF00) >>> 8);
                bytes[2 * i + 1] = (byte) (shorts[i] & 0x00FF);
            } else {
                bytes[2 * i] = (byte) (shorts[i] & 0x00FF);
                bytes[2 * i + 1] = (byte) ((shorts[i] & 0xFF00) >>> 8);
            }

        }

        return bytes;
    }

    @Override
    public String toString() {
        return "PacketHeader{" +
                "version=" + version +
                ", transactionId=" + transactionId +
                ", packetIndex=" + packetIndex +
                ", packetSize=" + packetSize +
                ", endFlag=" + endFlag +
                ", dataType=" + dataType +
                ", reserved=" + reserved +
                ", dataSize=" + dataSize +
                '}';
    }
}
