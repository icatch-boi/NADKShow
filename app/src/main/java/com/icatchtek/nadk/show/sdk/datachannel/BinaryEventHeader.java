package com.icatchtek.nadk.show.sdk.datachannel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by sha.liu on 2021/10/21.
 */

//struct eventHeader {
//        uint16_t eventid;
//        uint16_t index;
//        uint16_t width;
//        uint16_t height;
//        uint32_t fps:8;
//        uint32_t isKeyFrame:1;
//        uint32_t endflag:1;
//        uint32_t framesize:20;
//        uint32_t reserved：2;
//        };

public class BinaryEventHeader {
    private static final int PACKET_VERSION = 1;
    public static final int HEADER_SIZE = 12;

    private short eventid; //16bit
    private short index; //16bit
    private short width; //16bit
    private short height; //16bit
    private int fps; //8bit
    private int isKeyFrame; //1bit
    private int endFlag; //1bit
    private int frameSize; // 20bit
    private int reserved;//2bit

    private byte[] headerByte;


    public BinaryEventHeader(short eventid, short index, short width, short height, short fps, short isKeyFrame, short endFlag, int frameSize, short reserved) {
        this.eventid = eventid;
        this.index = index;
        this.width = width;
        this.height = height;
        this.fps = fps;
        this.isKeyFrame = isKeyFrame;
        this.endFlag = endFlag;
        this.frameSize = frameSize;
        this.reserved = reserved;

        byte[] headerByte = new byte[HEADER_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(headerByte);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putShort(eventid);
        buffer.putShort(index);
        buffer.putShort(width);
        buffer.putShort(height);

//        int tmp = ((this.fps & 0x000000FF) << 24) | ((this.isKeyFrame & 0x00000001) << 23)  | ((this.endFlag & 0x00000001) << 22) | (this.reserved & 0x003FFFFF);

        int tmp = (this.fps & 0x000000FF) | ((this.isKeyFrame & 0x00000001) << 8)  | ((this.endFlag & 0x00000001) << 9) | ((this.frameSize & 0x00FFFFF) << 10) | ((this.reserved & 0x3) << 30);
        buffer.putInt(tmp);
    }

    public BinaryEventHeader(byte[] headerByte) {
        ByteBuffer buffer = ByteBuffer.wrap(headerByte, 0, HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.headerByte = buffer.array();

        this.eventid = buffer.getShort();
        this.index = buffer.getShort();
        this.width = buffer.getShort();
        this.height = buffer.getShort();
        int tmp = buffer.getInt();
        this.fps = tmp & 0x000000FF;
        this.isKeyFrame = (tmp & 0x00000100) >> 8;
        this.endFlag = (tmp & 0x00000200) >> 9;
        this.frameSize = (tmp & 0x3FFFFC00) >> 10;
        this.reserved = tmp & 0xC0000000 >> 30;
    }


    public short getEventid() {
        return eventid;
    }

    public short getIndex() {
        return index;
    }

    public short getWidth() {
        return width;
    }

    public short getHeight() {
        return height;
    }

    public int getFps() {
        return fps;
    }

    public int getIsKeyFrame() {
        return isKeyFrame;
    }

    public int getEndFlag() {
        return endFlag;
    }

    public int getReserved() {
        return reserved;
    }

    public byte[] getHeaderByte() {
        return headerByte;
    }

    @Override
    public String toString() {
        return "BinaryEventHeader{" +
                "eventid=" + eventid +
                ", index=" + index +
                ", width=" + width +
                ", height=" + height +
                ", fps=" + fps +
                ", isKeyFrame=" + isKeyFrame +
                ", endFlag=" + endFlag +
                ", frameSize=" + frameSize +
                ", reserved=" + reserved +
                ", headerByte=" + headerByte +
                '}';
    }
}
