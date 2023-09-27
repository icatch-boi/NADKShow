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
import com.icatchtek.nadk.streaming.producer.NADKStreamingProducer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StreamingClientAssistImpl
{
    private final String LOG_TAG = "StreamingClientAssistImpl";

    private final NADKLogger logger;
    private final NADKEventHandler eventHandler;

    private final Lock clientMutex = new ReentrantLock();
    private final Map<Integer, StreamingClientService> streamingClients = new HashMap<>();

    private boolean frameSendRun;
    private AudioFrameSender audioFrameSender;
    private VideoFrameSender videoFrameSender;

    private final NADKStreamingRender streamingRender;
    private final NADKStreamingProducer streamingProducer;

    public StreamingClientAssistImpl(
            NADKLogger logger,
            NADKEventHandler eventHandler,
            NADKStreamingRender streamingRender,
            NADKStreamingProducer streamingProducer) {
        this.logger = logger;
        this.eventHandler = eventHandler;

        this.frameSendRun = false;

        this.streamingRender = streamingRender;
        this.streamingProducer = streamingProducer;
    }

    public void finalize() {
        this.destroySenderFunc();
    }

    public void created(NADKStreamingClient streamingClient)
            throws NADKException {
        /* The first one use app specified render, the other ones use default console render */
        StreamingClientService clientService;
        if (this.streamingClients.size() > 0) {
            clientService = new StreamingClientService(logger, eventHandler, streamingClient);
        } else {
            clientService = new StreamingClientService(logger, this.streamingRender, streamingClient);
        }

        this.clientMutex.lock();
        this.streamingClients.put(streamingClient.hashCode(), clientService);
        this.clientMutex.unlock();
        AppLog.d(LOG_TAG, String.format(Locale.getDefault(), "client[%s] is created", streamingClient.getClientID()));

        /* clients exists, prepare sender func */
        if (!this.frameSendRun) {
            AppLog.d(LOG_TAG, "clients increase from 0 to one or more, create sender func");
            this.prepareSenderFunc();
        }
    }

    public void destroyed(NADKStreamingClient streamingClient)
            throws NADKException {
        StreamingClientService clientService = this.getClientService(streamingClient);
        if (clientService == null) {
            throw new NADKException(NADKError.NADK_INVALID_ARG);
        }

        AppLog.d(LOG_TAG, String.format(Locale.getDefault(), "client[%s] will be destroyed", streamingClient.getClientID()));
        this.clientMutex.lock();
        this.streamingClients.remove(streamingClient.hashCode());
        boolean clientsExists = this.streamingClients.size() > 0;
        this.clientMutex.unlock();

        /* on clients exists, destroy sender func */
        if (!clientsExists) {
            logger.writeCommonLogI(LOG_TAG, "No clients exists, destroy sender func");
            this.destroySenderFunc();
        }
    }

    public void connected(NADKStreamingClient streamingClient)
            throws NADKException {
        StreamingClientService clientService = this.getClientService(streamingClient);
        if (clientService == null) {
            throw new NADKException(NADKError.NADK_INVALID_ARG);
        }

        AppLog.d(LOG_TAG, String.format(Locale.getDefault(), "client[%s] connected", streamingClient.getClientID()));
    }

    public void disconnected(NADKStreamingClient streamingClient)
            throws NADKException {
        StreamingClientService clientService = this.getClientService(streamingClient);
        if (clientService == null) {
            throw new NADKException(NADKError.NADK_INVALID_ARG);
        }

        AppLog.d(LOG_TAG, String.format(Locale.getDefault(), "client[%s] disconnected", streamingClient.getClientID()));
    }

    public void streamingEnabled(
            NADKStreamingClient streamingClient,
            NADKAudioParameter audioParameter, NADKVideoParameter videoParameter) throws NADKException {
        StreamingClientService clientService = this.getClientService(streamingClient);
        if (clientService == null) {
            throw new NADKException(NADKError.NADK_INVALID_ARG);
        }

        AppLog.d(LOG_TAG, String.format(Locale.getDefault(), "reset key frame after client[%s] connected", streamingClient.getClientID()));
        if (this.streamingProducer != null) {
            this.streamingProducer.resetKeyFrame();
        }

        AppLog.d(LOG_TAG, String.format(Locale.getDefault(), "client[%s] streamingEnabled", streamingClient.getClientID()));
        clientService.streamingEnabled(audioParameter, videoParameter);
    }

    public void streamingDisabled(NADKStreamingClient streamingClient)
            throws NADKException
    {
        StreamingClientService clientService = this.getClientService(streamingClient);
        if (clientService == null) {
            throw new NADKException(NADKError.NADK_INVALID_ARG);
        }
        clientService.streamingDisabled();
    }

    void prepareSenderFunc()
    {
        if (this.frameSendRun) {
            return;
        }

        if (this.streamingProducer != null)
        {
            this.frameSendRun = true;

            try {
                this.streamingProducer.prepareStream(null, null);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return;
            }

            this.audioFrameSender = new AudioFrameSender();
            this.audioFrameSender.start();
            this.videoFrameSender = new VideoFrameSender();
            this.videoFrameSender.start();
        }
    }

    void destroySenderFunc()
    {
        if (!this.frameSendRun) {
            return;
        }

        this.frameSendRun = false;
        if (this.audioFrameSender != null && this.audioFrameSender.isAlive())
        {
            try {
                this.audioFrameSender.join();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        if (this.videoFrameSender != null && this.videoFrameSender.isAlive())
        {
            try {
                this.videoFrameSender.join();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        if (this.streamingProducer != null)
        {
            try {
                this.streamingProducer.destroyStream();
            } catch(NADKException ex) {
                ex.printStackTrace();
            }
        }
    }

    class AudioFrameSender extends Thread
    {
        @Override
        public void run() {
            AppLog.d(LOG_TAG, "AudioFrameSender in");

            NADKFrameBuffer frameBuffer = new NADKFrameBuffer(10240);
            while (frameSendRun)
            {
                try {
                    streamingProducer.getAudioFrame(frameBuffer);
                }
                catch (NADKException ex)
                {
                    if (ex.getErrCode() == NADKError.NADK_TRY_AGAIN) {
                        continue;
                    }
                    break;
                }
                sendAudioFrame(frameBuffer);
            }

            AppLog.d(LOG_TAG, "AudioFrameSender out");
        }
    }

    class VideoFrameSender extends Thread
    {
        @Override
        public void run() {
            AppLog.d(LOG_TAG, "VideoFrameSender in");

            NADKFrameBuffer frameBuffer = new NADKFrameBuffer(1920 * 1080 * 2);
            while (frameSendRun)
            {
                try {
                    streamingProducer.getVideoFrame(frameBuffer);
                }
                catch (NADKException ex)
                {
                    if (ex.getErrCode() == NADKError.NADK_TRY_AGAIN) {
                        continue;
                    }
                    break;
                }
                sendVideoFrame(frameBuffer);
            }

            AppLog.d(LOG_TAG, "VideoFrameSender out");
        }
    }

    public void sendAudioFrame(NADKFrameBuffer frameBuffer)
    {
        this.clientMutex.lock();
        Collection<StreamingClientService> clients = this.streamingClients.values();
        this.clientMutex.unlock();

        for (StreamingClientService client : clients)
        {
            try {
                client.sendAudioFrame(frameBuffer);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            AppLog.d(LOG_TAG, String.format(Locale.getDefault(),
                    "send audio frame type: %d, size: %d, pts: %d, through client[%s]",
                    frameBuffer.getFrameType(),
                    frameBuffer.getFrameSize(),
                    frameBuffer.getPresentationTime(),
                    client.getClientID()));
        }
        AppLog.d(LOG_TAG, "sendAudioFrame 44");
    }

    public void sendVideoFrame(NADKFrameBuffer frameBuffer)
    {
        this.clientMutex.lock();
        Collection<StreamingClientService> clients = this.streamingClients.values();
        this.clientMutex.unlock();

        for (StreamingClientService client : clients)
        {
            try {
                client.sendVideoFrame(frameBuffer);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            AppLog.d(LOG_TAG, String.format(Locale.getDefault(),
                    "send video frame type: %d, size: %d, pts: %d, through client[%s]",
                    frameBuffer.getFrameType(),
                    frameBuffer.getFrameSize(),
                    frameBuffer.getPresentationTime(),
                    client.getClientID()));
        }
    }

    private StreamingClientService getClientService(NADKStreamingClient streamingClient)
    {
        this.clientMutex.lock();
        StreamingClientService clientService = this.streamingClients.get(streamingClient.hashCode());
        this.clientMutex.unlock();

        return clientService;
    }
}
