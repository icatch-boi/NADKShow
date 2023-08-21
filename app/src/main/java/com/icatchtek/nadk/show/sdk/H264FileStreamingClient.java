package com.icatchtek.nadk.show.sdk;

import static com.icatchtek.nadk.reliant.NADKCodec.NADK_CODEC_H264;
import static com.icatchtek.nadk.reliant.NADKCodec.NADK_CODEC_OPUS;

import android.os.Environment;

import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.reliant.NADKCodec;
import com.icatchtek.nadk.reliant.NADKError;
import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.reliant.NADKFrameBuffer;
import com.icatchtek.nadk.reliant.parameter.NADKAudioParameter;
import com.icatchtek.nadk.reliant.parameter.NADKVideoParameter;
import com.icatchtek.nadk.streaming.NADKStreamingClient;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by sha.liu on 2023/8/10.
 */
public class H264FileStreamingClient implements NADKCustomerStreamingClient {
    private final static String TAG = H264FileStreamingClient.class.getSimpleName();
    private int width = 1280;
    private int height = 720;
    private int fps = 30;
    private int frameCount = 0;
    private int interval;
    private NADKCustomerStreamingClientObserver clientObserver;

    public H264FileStreamingClient() {
        interval = 1000 / fps;
    }

    @Override
    public long getClientID() {
        return 0;
    }

    @Override
    public NADKAudioParameter getAudioParameter() throws NADKException {
//        return new NADKAudioParameter(
//                NADK_CODEC_OPUS, 16000, 16, 1);
        throw new NADKException(NADKError.NADK_NULL_ARG);

//        return null;
    }

    @Override
    public NADKVideoParameter getVideoParameter() throws NADKException {
        return new NADKVideoParameter(
                NADK_CODEC_H264, 1280, 720, 50000, 30);
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
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int index = frameCount % 1500 + 1;
        String fileName = String.format("frame-%04d.h264", index);
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/samples/h264_720p_25fps_1500kbps/" + fileName;

        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = readByChannelTest3(filePath);
        } catch (IOException e) {
            throw new NADKException(NADKError.NADK_TRY_AGAIN);
        }

        if (byteBuffer == null) {
            throw new NADKException(NADKError.NADK_TRY_AGAIN);
        }

        byte[] buffer = byteBuffer.array();
        int frameSize = buffer.length;
        long pts = (long) frameCount * interval;
        frameBuffer.setPresentationTime(pts);
        frameBuffer.setFrameType(NADKCodec.NADK_CODEC_H264);
        frameBuffer.setFrameSize(frameSize);
        frameBuffer.setIndex(frameCount);
        boolean keyFrame = isKeyFrame(buffer);
        frameBuffer.setKeyFrame(keyFrame);
        AppLog.e(TAG, "getNextVideoFrame frameCount: " + frameCount + ", pts: " + pts + ", frameSize: " + frameSize + ", keyFrame: " + keyFrame);
        System.arraycopy(buffer, 0, frameBuffer.getBuffer(), 0, frameSize);
        ++frameCount;

    }

    @Override
    public void sendNextAudioFrame(NADKFrameBuffer frameBuffer) throws NADKException {
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sendNextVideoFrame(NADKFrameBuffer frameBuffer) throws NADKException {
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 通过 FileChannel.map()拿到MappedByteBuffer
     * 使用内存文件映射，速度会快很多
     *
     * @throws IOException
     */
    public static ByteBuffer readByChannelTest3(String file) throws IOException {
        long start = System.currentTimeMillis();

        RandomAccessFile fis = new RandomAccessFile(new File(file), "rw");
        FileChannel channel = fis.getChannel();
        long size = channel.size();
        int allocate = (int)size;
        System.out.println(String.format("\n===>文件%s大小：%d 字节", file, size));

        if (allocate == 0) {
            return null;
        }

        // 构建一个只读的MappedByteBuffer
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);

        // 如果文件不大,可以选择一次性读取到数组
        // byte[] all = new byte[(int)size];
        // mappedByteBuffer.get(all, 0, (int)size);
        // 打印文件内容
        // System.out.println(new String(all));

        // 如果文件内容很大,可以循环读取,计算应该读取多少次
        byte[] bytes = new byte[allocate];
        long cycle = size / allocate;
        int mode = (int)(size % allocate);
        //byte[] eachBytes = new byte[allocate];
        for (int i = 0; i < cycle; i++) {
            // 每次读取allocate个字节
            mappedByteBuffer.get(bytes);

            // 打印文件内容,关闭打印速度会很快
            // System.out.print(new String(eachBytes));
        }
        if(mode > 0) {
            bytes = new byte[mode];
            mappedByteBuffer.get(bytes);

            // 打印文件内容,关闭打印速度会很快
            // System.out.print(new String(eachBytes));
        }

        // 关闭通道和文件流
        channel.close();
        fis.close();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        long end = System.currentTimeMillis();
//        System.out.println(String.format("\n===>文件大小：%s 字节", size));
//        System.out.println(String.format("===>读取并打印文件耗时：%s毫秒", end - start));

        return byteBuffer;
    }

    private boolean isKeyFrame(byte[] frame) {
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            tmp.append(String.format("%02X ", frame[i]));
        }
        AppLog.e(TAG, "isKeyFrame dump: " + tmp.toString());
        int offset = 0;
        while (offset < frame.length - 4) {
            // 找到 NALU 起始码
            if (frame[offset] == 0x00 && frame[offset + 1] == 0x00  && frame[offset + 2] == 0x00 &&
                    frame[offset + 3] == 0x01) {

                int type = frame[offset + 10] & 0x1F;
                if (type == 7) {
                    // 帧类型为 I 帧，即关键帧
                    return true;
                } else {
                    // 帧类型为 P 帧或 B 帧
                    return false;
                }
            }

            offset++;
        }

        return false;
    }

    @Override
    public void initialize(NADKCustomerStreamingClientObserver observer) {
        this.clientObserver = observer;
    }

    @Override
    public void prepare() {
        if (clientObserver != null) {
            clientObserver.onPrepare(true);
        }

    }

    @Override
    public void destroy() {
        if (clientObserver != null) {
            clientObserver.onDestroy();
        }
    }
}
