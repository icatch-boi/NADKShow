package com.icatchtek.nadk.show.sdk.datachannel;

/**
 * Created by sha.liu on 2023/5/11.
 */
public class BinaryEvent {
    private BinaryEventHeader header;
    private int eventDataSize;
    private byte[] eventData;
    private byte[] packetData;

    public BinaryEvent(byte[] packetData, int dataSize) {
        this.packetData = packetData;
        header = new BinaryEventHeader(this.packetData);
        eventDataSize = dataSize - BinaryEventHeader.HEADER_SIZE;
        eventData = new byte[eventDataSize];
        System.arraycopy(this.packetData, BinaryEventHeader.HEADER_SIZE, eventData, 0, eventDataSize);

    }

    public BinaryEvent(BinaryEventHeader header, byte[] eventData, int eventDataSize, int offset) {
        this.header = header;
        this.eventData = eventData;
        this.eventDataSize = eventDataSize;
        packetData = new byte[eventDataSize + BinaryEventHeader.HEADER_SIZE];
        System.arraycopy(header.getHeaderByte(), 0, packetData, 0, BinaryEventHeader.HEADER_SIZE);
        System.arraycopy(eventData, offset, packetData, BinaryEventHeader.HEADER_SIZE, this.eventDataSize);
    }

    public BinaryEventHeader getHeader() {
        return header;
    }

    public byte[] getEventData() {
        return eventData;
    }

    public byte[] getPacketData() {
        return packetData;
    }

    public int getEventDataSize() {
        return eventDataSize;
    }
}
