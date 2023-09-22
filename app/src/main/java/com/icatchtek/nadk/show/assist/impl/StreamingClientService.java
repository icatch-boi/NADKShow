package com.icatchtek.nadk.show.assist.impl;

import com.icatchtek.baseutil.log.AppLog;
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

public class StreamingClientService
{
    private final long clientID;
    private final NADKLogger logger;
    private final NADKStreamingClient streamingClient;

    private boolean streamingOn;
    private NADKStreamingRender streamingRender;

    public StreamingClientService(
            NADKLogger logger,
            NADKEventHandler eventHandler,
            NADKStreamingClient streamingClient)
    {
        this.logger = logger;
        this.clientID = streamingClient.getClientID();

        this.streamingOn = false;
        this.streamingClient = streamingClient;
        try {
            this.streamingRender = NADKStreamingRender.createConsoleRender(logger, eventHandler);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public StreamingClientService(
            NADKLogger logger,
            NADKStreamingRender streamingRender,
            NADKStreamingClient streamingClient)
    {
        this.logger = logger;
        this.clientID = streamingClient.getClientID();

        this.streamingOn = false;
        this.streamingClient = streamingClient;
        this.streamingRender = streamingRender;
    }

    protected void finalize()
    {
//        if (this.streamingRender != null)
//        {
//            try {
//                AppLog.d("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
//                        "__flow_debug__, finalize 1"));
//                this.streamingRender.stopStreaming();
//                AppLog.d("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
//                        "__flow_debug__, finalize 2"));
//                this.streamingRender.destroyRender();
//                AppLog.d("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
//                        "__flow_debug__, finalize 3"));
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                AppLog.e("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
//                        "__flow_debug__, finalize 4"));
//            }
//        }
    }

    public long getClientID() {
        return this.clientID;
    }

    public void streamingEnabled(
            NADKAudioParameter audioParameter,
            NADKVideoParameter videoParameter)
    {
        AppLog.d("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
                "__flow_debug__, streaming enabled 1"));

        AppLog.d("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
                "streaming enabled, audio: %s, video: %s",
                audioParameter, videoParameter));
        if (audioParameter == null && videoParameter == null) {
            return;
        }

        if (this.streamingRender != null)
        {
            try {
                AppLog.d("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
                        "__flow_debug__, streaming enabled 2"));
                this.streamingOn = true;
                this.streamingRender.prepareRender();
                this.streamingRender.startStreaming(this.streamingClient);
            } catch (NADKException ex) {
                ex.printStackTrace();
                AppLog.e("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
                        "__flow_debug__, streaming enabled 3: ") + ex.getErrCode());
            }
        }
    }

    public void streamingDisabled()
    {
        AppLog.d("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
                "__flow_debug__, streaming disabled 1"));
        AppLog.d("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
                "streaming disabled"));

        if (this.streamingRender != null) {
            try {
                AppLog.d("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
                        "__flow_debug__, streaming disabled 2"));
                this.streamingOn = false;
                this.streamingRender.stopStreaming();
                this.streamingRender.destroyRender();
            } catch (NADKException e) {
                e.printStackTrace();
                AppLog.e("StreamingClient:" + this.clientID, String.format(Locale.getDefault(),
                        "__flow_debug__, streaming disabled 3: ") + e.getErrCode());
            }
        }
    }

    public void sendAudioFrame(NADKFrameBuffer frameBuffer) throws NADKException
    {
        if (!this.streamingOn) {
            throw new NADKException(NADKError.NADK_NOT_INITIALIZED);
        }

        this.streamingClient.sendNextAudioFrame(frameBuffer);
    }

    public void sendVideoFrame(NADKFrameBuffer frameBuffer) throws NADKException
    {
        if (!this.streamingOn) {
            throw new NADKException(NADKError.NADK_NOT_INITIALIZED);
        }

        this.streamingClient.sendNextVideoFrame(frameBuffer);
    }
}
