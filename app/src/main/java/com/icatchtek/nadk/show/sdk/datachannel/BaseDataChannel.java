package com.icatchtek.nadk.show.sdk.datachannel;

import com.icatchtek.baseutil.log.AppLog;

/**
 * Created by sha.liu on 2021/10/21.
 */
public class BaseDataChannel implements IDataChannel.Observer{
    private String TAG = BaseDataChannel.class.getSimpleName();
    private final int MAX_TRANS_ID = 0xFFF;
    private final int MAX_PACKET_DATA_SIZE = (60 * 1000) - PacketHeader.HEADER_SIZE;
    private String deviceId;
    private IDataChannel dataChannel;
    private int sendTransId = 0;
    private Observer observer;
    private String channelName;
    private int channelId;

    public BaseDataChannel(String deviceId, IDataChannel dataChannel) {
        this.deviceId = deviceId;
        this.dataChannel = dataChannel;
        channelName = dataChannel.getChannelName();
//        channelId = dataChannel.id();
        TAG += "["+ channelName + "]";
    }

    public void init(Observer observer) {
        if (dataChannel != null) {
            dataChannel.registerObserver(this);
        }
        this.observer = observer;
    }

    public void unInit() {
        if (dataChannel != null) {
            dataChannel.unregisterObserver();
//            dataChannel.close();
        }
        this.observer = null;
    }

    public String getChannelName() {
        return channelName;
    }

    public int getChannelId() {
        return channelId;
    }

    public boolean sendData(byte[] data, int dataSize) {
//        if (!dataChannel.isConnected()) {
//            AppLog.e(TAG, "sendData: dataChannel.isConnected() =" + dataChannel.isConnected());
//            return false;
//        }

        if (dataSize < MAX_PACKET_DATA_SIZE) {
            PacketHeader header = new PacketHeader(sendTransId, 0, dataSize, PacketHeader.END_FLAG_YES, PacketHeader.DATA_TYPE_STRING, 0);
//            PacketHeader header1 = new PacketHeader(header.getHeaderByte());
//            AppLog.e(TAG, "sendData: header1 = " + header1.toString());
            PacketData packetData = new PacketData(header, data, 0);
//            boolean ret = dataChannel.send(new DataChannel.Buffer(ByteBuffer.wrap(packetData.getPacketByte()), true));
            boolean ret = dataChannel.sendData(packetData.getPacketByte(), packetData.getHeader().getPacketSize());
            AppLog.d(TAG, "sendData: header = " + header.toString() + ", ret = " + ret);
            addSendTransId();
            return ret;
        } else {

            int packetNumber = dataSize / MAX_PACKET_DATA_SIZE;
            int lastPacketSize = dataSize % MAX_PACKET_DATA_SIZE;
            int offset = 0;

            if (lastPacketSize > 0) {
                packetNumber += 1;
            }

            boolean ret = false;

            for(int packetId = 0; packetId < packetNumber; packetId++) {
                int packetDataSize = MAX_PACKET_DATA_SIZE;
                int endFlag = PacketHeader.END_FLAG_NO;

                if (packetId == (packetNumber - 1)) {
                    packetDataSize = lastPacketSize;
                    endFlag = PacketHeader.END_FLAG_YES;
                }

                PacketHeader header = new PacketHeader(sendTransId, packetId, packetDataSize, endFlag, PacketHeader.DATA_TYPE_STRING, 0);
                PacketData packetData = new PacketData(header, data, offset);
//                ret = dataChannel.send(new DataChannel.Buffer(ByteBuffer.wrap(packetData.getPacketByte()), true));
                ret = dataChannel.sendData(packetData.getPacketByte(), packetData.getHeader().getPacketSize());
                AppLog.d(TAG, "sendData: header = " + header.toString() + ", ret = " + ret);
                offset += packetDataSize;
            }
            addSendTransId();

            return ret;
        }
    }

//    @Override
//    public void onBufferedAmountChange(long previousAmount) {
//        AppLog.d(TAG, "onBufferedAmountChange: previousAmount = " + previousAmount);
//
//    }

    @Override
    public void onStateChange(IDataChannel.State state) {
        AppLog.d(TAG, "onStateChange IDataChannel.State: " + state.name());
        if (observer != null) {
            observer.onStateChange(state);
        }

    }

    @Override
    public void onMessage(String message) {
        AppLog.d(TAG, "onMessage: message = " + message);

    }

    @Override
    public void onMessage(byte[] data, int dataSize) {
        AppLog.e(TAG, "onMessage: data = " + data + ", dataSize = " + dataSize);

//        StringBuilder tmp = new StringBuilder();
//        for (int i = 0; i < 8; i++) {
//            tmp.append(String.format("%02X ", data[i]));
//        }
//        AppLog.e(TAG, "onMessage header dump: " + tmp.toString());
//
//        PacketData packetData = new PacketData(data);
//        PacketHeader header = packetData.getHeader();
//
//        AppLog.e(TAG, "onMessage: " + header.toString());
//
//
//        if (observer != null) {
//            observer.onPacketArrived(packetData);
//        }

        if (observer != null) {
            observer.onRawDataArrived(data, dataSize);
        }

    }

//    @Override
//    public void onMessage(DataChannel.Buffer buffer) {
//        if (buffer.binary) {
//            byte[] bytes;
//            if (buffer.data.hasArray()) {
//                bytes = buffer.data.array();
//            } else {
//                bytes = new byte[buffer.data.remaining()];
//                buffer.data.get(bytes);
//            }
//
//            AppLog.d(TAG, "onMessage: buffer.binary = " + buffer.binary + ", bytes = " + bytes.length);
//
//            PacketData packetData = new PacketData(bytes);
//            PacketHeader header = packetData.getHeader();
//
//            AppLog.e(TAG, "onMessage: " + header.toString());
//
//            if (observer != null) {
//                observer.onPacketArrived(packetData);
//            }
//
//        } else {
//            Charset charset = Charset.forName("utf-8");
//            String s1 =  charset.decode(buffer.data).toString();
//            AppLog.d(TAG, "onMessage: buffer.binary = " + buffer.binary + ", buffer.data = " + s1);
//        }
//
//    }

    private void addSendTransId() {
        ++sendTransId;
        if (sendTransId > MAX_TRANS_ID) {
            sendTransId = 0;
        }
    }

    public interface Observer {
        public void onStateChange(IDataChannel.State state);
        public void onPacketArrived(PacketData packetData);
        public void onRawDataArrived(byte[] data, int dataSize);
    }
}
