package com.icatchtek.nadk.show.sdk.datachannel;

//import com.icatchtek.nadk.reliant.datachannel.NADKDataChannelObserver;

/**
 * Created by sha.liu on 2022/3/28.
 */
public interface IDataChannel {
    String getChannelName();

    boolean isConnected();

    void registerObserver(Observer observer);

    void unregisterObserver();

    boolean sendData(String message);

    boolean sendData(byte[] data, int dataSize);

    public enum State {
        CONNECTING,
        OPEN,
        CLOSING,
        CLOSED;
    }

    public interface Observer {
        void onStateChange(State state);

        void onMessage(String message);

        void onMessage(byte[] data, int dataSize);
    }
}
