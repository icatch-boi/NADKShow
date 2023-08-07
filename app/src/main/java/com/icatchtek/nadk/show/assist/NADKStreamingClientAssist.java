package com.icatchtek.nadk.show.assist;

import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.reliant.NADKLogger;
import com.icatchtek.nadk.reliant.event.NADKEventHandler;
import com.icatchtek.nadk.reliant.parameter.NADKAudioParameter;
import com.icatchtek.nadk.reliant.parameter.NADKVideoParameter;
import com.icatchtek.nadk.streaming.render.NADKStreamingRender;
import com.icatchtek.nadk.streaming.NADKStreamingClient;
import com.icatchtek.nadk.streaming.NADKStreamingClientListener;
import com.icatchtek.nadk.streaming.producer.NADKStreamingProducer;
import com.icatchtek.nadk.show.assist.impl.NADKStreamingClientAssistImpl;

public class NADKStreamingClientAssist implements NADKStreamingClientListener
{
    private final NADKStreamingClientAssistImpl clientAssistImpl;
    private NADKStreamingClientListener streamingClientListener;

    public NADKStreamingClientAssist(
            NADKLogger logger,
            NADKEventHandler eventHandler,
            NADKStreamingRender streamingRender,
            NADKStreamingProducer streamingProducer, NADKStreamingClientListener nadkStreamingClientListener) {
        this.clientAssistImpl = new NADKStreamingClientAssistImpl(
                logger, eventHandler, streamingRender, streamingProducer);
        this.streamingClientListener = nadkStreamingClientListener;
    }

    public void created(NADKStreamingClient streamingClient) {
        try {
            if (streamingClientListener != null) {
                streamingClientListener.created(streamingClient);
            }
            this.clientAssistImpl.created(streamingClient);
        } catch (NADKException e) {
            e.printStackTrace();
        }
    }

    public void destroyed(NADKStreamingClient streamingClient) {
        try {
            if (streamingClientListener != null) {
                streamingClientListener.destroyed(streamingClient);
            }
            this.clientAssistImpl.destroyed(streamingClient);
        } catch (NADKException e) {
            e.printStackTrace();
        }
    }

    public void connected(NADKStreamingClient streamingClient) {
        try {
            if (streamingClientListener != null) {
                streamingClientListener.connected(streamingClient);
            }
            this.clientAssistImpl.connected(streamingClient);
        } catch (NADKException e) {
            e.printStackTrace();
        }
    }

    public void disconnected(NADKStreamingClient streamingClient) {
        try {
            if (streamingClientListener != null) {
                streamingClientListener.disconnected(streamingClient);
            }
            this.clientAssistImpl.disconnected(streamingClient);
        } catch (NADKException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void streamingEnabled(NADKStreamingClient streamingClient, NADKAudioParameter audioParameter, NADKVideoParameter videoParameter) {
        try {
            if (streamingClientListener != null) {
                streamingClientListener.streamingEnabled(streamingClient, audioParameter, videoParameter);
            }
            this.clientAssistImpl.streamingEnabled(streamingClient, audioParameter, videoParameter);
        } catch (NADKException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void streamingDisabled(NADKStreamingClient streamingClient) {
        try {
            if (streamingClientListener != null) {
                streamingClientListener.streamingDisabled(streamingClient);
            }
            this.clientAssistImpl.streamingDisabled(streamingClient);
        } catch (NADKException e) {
            e.printStackTrace();
        }
    }
}