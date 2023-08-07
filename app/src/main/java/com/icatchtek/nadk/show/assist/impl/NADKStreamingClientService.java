package com.icatchtek.nadk.show.assist.impl;

import com.icatchtek.nadk.reliant.NADKError;
import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.reliant.NADKFrameBuffer;
import com.icatchtek.nadk.reliant.NADKLogger;
import com.icatchtek.nadk.reliant.event.NADKEventHandler;
import com.icatchtek.nadk.reliant.parameter.NADKAudioParameter;
import com.icatchtek.nadk.reliant.parameter.NADKVideoParameter;
import com.icatchtek.nadk.streaming.render.NADKStreamingRender;
import com.icatchtek.nadk.streaming.NADKStreamingClient;

import java.util.Locale;

public class NADKStreamingClientService
{
    private final long clientID;
    private final NADKLogger logger;
    private final NADKStreamingClient streamingClient;

    private boolean clientConnected;
    private NADKStreamingRender streamingRender;

    public NADKStreamingClientService(
            NADKLogger logger,
            NADKEventHandler eventHandler,
            NADKStreamingClient streamingClient)
    {
        this.logger = logger;
        this.clientID = streamingClient.getClientID();

        this.clientConnected = false;
        this.streamingClient = streamingClient;
        try {
            this.streamingRender = NADKStreamingRender.createConsoleRender(logger, eventHandler);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public NADKStreamingClientService(
            NADKLogger logger,
            NADKStreamingRender streamingRender,
            NADKStreamingClient streamingClient)
    {
        this.logger = logger;
        this.clientID = streamingClient.getClientID();

        this.clientConnected = false;
        this.streamingClient = streamingClient;
        this.streamingRender = streamingRender;
    }

    protected void finalize()
    {
        if (this.streamingRender != null)
        {
            try {
                this.streamingRender.stopStreaming();
                this.streamingRender.destroyRender();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public long getClientID() {
        return this.clientID;
    }

    public void setConnected()
    {
        this.clientConnected = true;
        logger.writeCommonLogI("StreamingClient:" + this.clientID, "client connected");

        if (this.streamingRender != null)
        {
            try {
                this.streamingRender.startStreaming(this.streamingClient);
                this.streamingRender.prepareRender();
            } catch (NADKException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setDisconnected()
    {
        this.clientConnected = false;
        logger.writeCommonLogI("StreamingClient:" + this.clientID, "client disconnected");

        if (this.streamingRender != null)
        {
            try {
                this.streamingRender.stopStreaming();
            } catch (NADKException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void streamingEnabled(
            NADKAudioParameter audioParameter,
            NADKVideoParameter videoParameter)
    {
        logger.writeCommonLogI("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
                "streamingEnabled, audio: %s, video: %s",
                audioParameter, videoParameter));

        if (audioParameter == null && videoParameter == null) {
            return;
        }

        if (audioParameter != null) {
            logger.writeCommonLogI("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
                    "streamingEnabled audio format changed, type: %d, frequency: %d, sampleBits: %d, sampleChannels: %d",
                    audioParameter.getCodec(), audioParameter.getFrequency(),
                    audioParameter.getSampleBits(), audioParameter.getSampleChannels()));
        }

        if (audioParameter != null) {
            logger.writeCommonLogI("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
                    "streamingEnabled video format changed, type: %d, frameWidth: %d, frameHeight: %d",
                    videoParameter.getCodec(), videoParameter.getWidth(), videoParameter.getHeight()));
        }

        if (this.streamingRender != null) {
            try {
                this.streamingRender.prepareRender();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void streamingDisabled() {
        logger.writeCommonLogI("StreamingClient:" + this.clientID, "streamingDisabled");
        if (this.streamingRender != null) {
            try {
                this.streamingRender.destroyRender();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendAudioFrame(NADKFrameBuffer frameBuffer) throws NADKException
    {
        if (!this.clientConnected) {
            throw new NADKException(NADKError.NADK_NOT_INITIALIZED);
        }

        this.streamingClient.sendNextAudioFrame(frameBuffer);
    }

    public void sendVideoFrame(NADKFrameBuffer frameBuffer) throws NADKException
    {
        if (!this.clientConnected) {
            throw new NADKException(NADKError.NADK_NOT_INITIALIZED);
        }

        this.streamingClient.sendNextVideoFrame(frameBuffer);
    }
}
