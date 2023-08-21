package com.icatchtek.nadk.show.sdk;

import static com.icatchtek.nadk.reliant.NADKCodec.NADK_CODEC_H264;

import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.reliant.NADKCodec;
import com.icatchtek.nadk.reliant.NADKError;
import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.reliant.NADKFrameBuffer;
import com.icatchtek.nadk.reliant.parameter.NADKAudioParameter;
import com.icatchtek.nadk.reliant.parameter.NADKVideoParameter;
import com.icatchtek.nadk.show.sdk.datachannel.BinaryEvent;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by sha.liu on 2023/8/18.
 */
public class NADKPreRollingStreamingClient implements NADKCustomerStreamingClient, Observer {
    private final static String TAG = NADKPreRollingStreamingClient.class.getSimpleName();
    private final static int MAX_LOOP_COUNT = 3;
    private int width = 1280;
    private int height = 720;
    private int fps = 30;
    private int frameCount = 0;
    private int getFrameCount = 0;
    private int interval;

    private NADKCustomerStreamingClientObserver clientObserver;
    private int totalFrameCount = 0;
    private ArrayList<BinaryEvent> cachedFrameList = null;
    private LinkedBlockingQueue<BinaryEvent> receivedFrameQueue;
    private boolean receivedFirstFrame = false;
    private boolean receivedLastFrame = false;
    private boolean isDestroy = false;
    private boolean isPrepare = false;
    private boolean firstPlay = false;


    public NADKPreRollingStreamingClient() {
        interval = 1000 / fps;
        receivedFrameQueue = new LinkedBlockingQueue<>(500);
    }

    @Override
    public void initialize(NADKCustomerStreamingClientObserver observer) {
        this.clientObserver = observer;
    }

    @Override
    public void prepare() {

        getFrameCount = 0;
        frameCount = 0;
        if (cachedFrameList == null) {
            cachedFrameList = new ArrayList<>();
            firstPlay = true;
        } else {
            firstPlay = false;
        }

        isDestroy = false;
        isPrepare = true;

        if (clientObserver != null) {
            clientObserver.onPrepare(true);
        }

        AppLog.e(TAG, "prepare: width: " + this.width + ", height: " + this.height + ", fps: " + this.fps + ", firstPlay: " + firstPlay);

    }

    @Override
    public void destroy() {
        isDestroy = true;
        isPrepare = false;

        if (receivedFrameQueue != null) {
            receivedFrameQueue.clear();
//            receivedFrameQueue = null;
        }

        if (clientObserver != null) {
            clientObserver.onDestroy();
        }
    }

    @Override
    public long getClientID() {
        return 0;
    }

    @Override
    public NADKAudioParameter getAudioParameter() throws NADKException {
        throw new NADKException(NADKError.NADK_NULL_ARG);
    }

    @Override
    public NADKVideoParameter getVideoParameter() throws NADKException {
        return new NADKVideoParameter(NADK_CODEC_H264, width, height, 50000, fps);
    }

    @Override
    public void getNextAudioFrame(NADKFrameBuffer frameBuffer) throws NADKException {
        try {
            Thread.sleep(100);
            throw new NADKException(NADKError.NADK_TRY_AGAIN);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void getNextVideoFrame(NADKFrameBuffer frameBuffer) throws NADKException {
        if (!isPrepare) {
            try {
                Thread.sleep(interval);
                throw new NADKException(NADKError.NADK_TRY_AGAIN);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }

        BinaryEvent binaryEvent;
        if (firstPlay) {
            binaryEvent = receivedFrameQueue.poll();
            if (binaryEvent == null) {
                AppLog.e(TAG, "firstPlay: receivedFrameQueue.poll(): binaryEvent == null");
                throw new NADKException(NADKError.NADK_TRY_AGAIN);
            }
            cachedFrameList.add(binaryEvent);
            AppLog.d(TAG, "firstPlay: getFrameSize: " + binaryEvent.getEventDataSize() + ", index: " + binaryEvent.getHeader().getIndex() + ", isKeyFrame: " + binaryEvent.getHeader().getIsKeyFrame());

            if (binaryEvent.getHeader().getEndFlag() == 1 && receivedLastFrame && !isDestroy) {
                firstPlay = false;
            }
        } else {
            int loopCount = frameCount / totalFrameCount;
            if (loopCount >= MAX_LOOP_COUNT) {
                destroy();
                throw new NADKException(NADKError.NADK_TRY_AGAIN);
            }
            int index = frameCount % totalFrameCount;
            if (cachedFrameList.isEmpty() || index >= cachedFrameList.size()) {
                AppLog.e(TAG, "replay["+ loopCount + "]: cachedFrameList.isEmpty() || index >= cachedFrameList.size()");
                throw new NADKException(NADKError.NADK_TRY_AGAIN);
            }

            binaryEvent = cachedFrameList.get(index);
            if (binaryEvent == null) {
                AppLog.e(TAG, "replay["+ loopCount + "]: cachedFrameList.get(" + index + "): binaryEvent == null");
                throw new NADKException(NADKError.NADK_TRY_AGAIN);
            }
            ++frameCount;
            AppLog.d(TAG, "replay["+ loopCount + "] getFrameSize: " + binaryEvent.getEventDataSize() + ", index: " + binaryEvent.getHeader().getIndex() + ", isKeyFrame: " + binaryEvent.getHeader().getIsKeyFrame());
        }

        byte[] frame = binaryEvent.getEventData();
        if (frame == null) {
            throw new NADKException(NADKError.NADK_TRY_AGAIN);
        }

//        ByteBuffer byteBuffer = ByteBuffer.wrap(frame, 0, binaryEvent.getEventDataSize());
//        byte[] buffer = byteBuffer.array();
//        int frameSize = buffer.length;
        byte[] buffer = frame;
        int frameSize = binaryEvent.getEventDataSize();

        long pts = (long) getFrameCount * interval;
        frameBuffer.setPresentationTime(pts);
        frameBuffer.setFrameType(NADKCodec.NADK_CODEC_H264);
        frameBuffer.setFrameSize(frameSize);
        frameBuffer.setIndex(getFrameCount);
        boolean keyFrame = (binaryEvent.getHeader().getIsKeyFrame() == 1);
        frameBuffer.setKeyFrame(keyFrame);
        System.arraycopy(buffer, 0, frameBuffer.getBuffer(), 0, frameSize);
        AppLog.e(TAG, "getNextVideoFrame frameCount: " + getFrameCount + ", pts: " + pts + ", frameSize: " + frameSize + ", keyFrame: " + keyFrame);
        ++getFrameCount;

    }

    @Override
    public void sendNextAudioFrame(NADKFrameBuffer frameBuffer) throws NADKException {

    }

    @Override
    public void sendNextVideoFrame(NADKFrameBuffer frameBuffer) throws NADKException {

    }

    @Override
    public void update(Observable o, Object arg) {
        if (isDestroy) {
            return;
        }

        if (arg instanceof BinaryEvent) {
            BinaryEvent event = (BinaryEvent)arg;
            receivedFrameQueue.offer(event);
            ++totalFrameCount;
            if (!receivedFirstFrame) {
                receivedFirstFrame = true;
                AppLog.e(TAG, "First Frame received: " + receivedFirstFrame);
                width = event.getHeader().getWidth();
                height = event.getHeader().getHeight();
                fps = event.getHeader().getFps();
                interval = 1000 / fps;
                prepare();
            }

            if (event.getHeader().getEndFlag() == 1 && !receivedLastFrame) {
                receivedLastFrame = true;
                AppLog.e(TAG, "Last Frame received: " + receivedLastFrame + ", totalFrameCount = " + totalFrameCount);
            }
        }

    }
}
