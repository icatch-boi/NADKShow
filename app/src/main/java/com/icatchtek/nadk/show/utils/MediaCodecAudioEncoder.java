package com.icatchtek.nadk.show.utils;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.icatchtek.baseutil.log.AppLog;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by sha.liu on 2023/7/6.
 */
public class MediaCodecAudioEncoder {
    private static final String TAG = MediaCodecAudioEncoder.class.getSimpleName();
    public static final String MIME_TYPE_OPUS = MediaFormat.MIMETYPE_AUDIO_OPUS;
    private static final int TIMEOUT_US = 100000;

    private MediaCodec mediaCodec;
    private MediaCodec.BufferInfo bufferInfo;

    public MediaCodecAudioEncoder(String mimeType, int sampleRate, int channelCount, int bitrate, int bufferSize) throws IOException {
        try {
            // 创建 MediaCodec 实例
            mediaCodec = MediaCodec.createEncoderByType(mimeType);

            // 配置 MediaCodec
            MediaFormat format = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount);
            format.setString(MediaFormat.KEY_MIME, mimeType);
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount);
            format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
//            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 14000000);
//            byte[] csd0bytes = {
//                    // Opus
//                    0x4f, 0x70, 0x75, 0x73,
//                    // Head
//                    0x48, 0x65, 0x61, 0x64,
//                    // Version
//                    0x01,
//                    // Channel Count
//                    0x01,
//                    // Pre skip
//                    0x00, 0x00,
//                    // Input Sample Rate (Hz), eg: 16000
//                    (byte) 0x80, (byte) 0x3e, 0x00, 0x00,
//                    // Output Gain (Q7.8 in dB)
//                    0x00, 0x00,
//                    // Mapping Family
//                    0x00};
//            byte[] csd1bytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//            byte[] csd2bytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//            ByteBuffer csd0 = ByteBuffer.wrap(csd0bytes);
//            format.setByteBuffer("csd-0", csd0);
//            ByteBuffer csd1 = ByteBuffer.wrap(csd1bytes);
//            format.setByteBuffer("csd-1", csd1);
//            ByteBuffer csd2 = ByteBuffer.wrap(csd2bytes);
//            format.setByteBuffer("csd-2", csd2);
            format.setInteger(MediaFormat.KEY_COMPLEXITY, 1);
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            bufferInfo = new MediaCodec.BufferInfo();

            // 启动编码器
            mediaCodec.start();
            MediaFormat outputFormat = mediaCodec.getOutputFormat();
            AppLog.e(TAG, "MediaCodecAudioEncoder getOutputFormat KEY_SAMPLE_RATE: " + outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) + ", KEY_CHANNEL_COUNT: " + outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG, "MediaCodecAudioEncoder create exception: " + e.getMessage());
            throw e;
        }
    }


    public byte[] encode(byte[] data, int dataSize, long pts) {
        byte[] encodedData = null;

        try {
            // 将数据填充到输入缓冲区
            int inputBufferIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_US);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                inputBuffer.clear();
                inputBuffer.rewind();
                inputBuffer.put(data, 0, dataSize);
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, dataSize, pts * 1000, 0);
            } else {
                AppLog.e(TAG, "encode dequeueInputBuffer failed: inputBufferIndex = " + inputBufferIndex);
            }

            // 获取输出缓冲区索引
            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
            if (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                encodedData = new byte[bufferInfo.size];
                outputBuffer.get(encodedData);
                outputBuffer.clear();

                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            } else {
                AppLog.e(TAG, "encode dequeueOutputBuffer failed: inputBufferIndex = " + outputBufferIndex);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat outputFormat = mediaCodec.getOutputFormat();
                    AppLog.e(TAG, "encode dequeueOutputBuffer failed INFO_OUTPUT_FORMAT_CHANGED getOutputFormat KEY_SAMPLE_RATE: " + outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE) + ", KEY_CHANNEL_COUNT: " + outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e(TAG, "encode exception: " + e.getClass().getSimpleName() + ", " + e.getMessage());
        }

        return encodedData;
    }

    public void release() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}