package com.icatchtek.nadk.show.utils;

import static com.icatchtek.nadk.reliant.NADKCodec.NADK_CODEC_OPUS;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Process;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.icatchtek.baseutil.log.AppLog;
import com.icatchtek.nadk.reliant.NADKCodec;
import com.icatchtek.nadk.reliant.NADKError;
import com.icatchtek.nadk.reliant.NADKException;
import com.icatchtek.nadk.reliant.NADKFrameBuffer;
import com.icatchtek.nadk.reliant.parameter.NADKAudioParameter;
import com.icatchtek.nadk.streaming.NADKStreamingClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by sha.liu on 2022/3/29.
 */
public class NADKWebRtcAudioRecord {
    private static final String TAG = NADKWebRtcAudioRecord.class.getSimpleName();

    // Requested size of each recorded buffer provided to the client.
    private static final int CALLBACK_BUFFER_SIZE_MS = 10;

    // Average number of callbacks per second.
    private static final int BUFFERS_PER_SECOND = 1000 / CALLBACK_BUFFER_SIZE_MS;

    // We ask for a native buffer size of BUFFER_SIZE_FACTOR * (minimum required
    // buffer size). The extra space is allocated to guard against glitches under
    // high load.
    private static final int BUFFER_SIZE_FACTOR = 2;

    // The AudioRecordJavaThread is allowed to wait for successful call to join()
    // but the wait times out afther this amount of time.
    private static final long AUDIO_RECORD_THREAD_JOIN_TIMEOUT_MS = 2000;

    public static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_COMMUNICATION;

    // Default audio data format is PCM 16 bit per sample.
    // Guaranteed to be supported by all devices.
    public static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // Indicates AudioRecord has started recording audio.
    private static final int AUDIO_RECORD_START = 0;

    // Indicates AudioRecord has stopped recording audio.
    private static final int AUDIO_RECORD_STOP = 1;

    // Time to wait before checking recording status after start has been called. Tests have
    // shown that the result can sometimes be invalid (our own status might be missing) if we check
    // directly after start.
    private static final int CHECK_REC_STATUS_DELAY_MS = 100;

    public static final int DEFAULT_AUDIO_SAMPLE_RATE = 16000;

    public static final int DEFAULT_AUDIO_CHANNEL = 1;

    public static final int DEFAULT_AUDIO_SAMPLE_BITS = 16;

    private final Context context;
    private final AudioManager audioManager;
    private final int audioSource;
    private final int audioFormat;



    private @Nullable
    ByteBuffer byteBuffer;

    private @Nullable
    AudioRecord audioRecord;
    private @Nullable
    AudioRecordThread audioThread;
    private @Nullable
    AudioDeviceInfo preferredDevice;

    private volatile boolean microphoneMute = true;
    private byte[] emptyBytes;

    private NADKStreamingClient streamingClient;
    private long frameSize_ms = 0;
    private FileOutputStream outputStream;
    private boolean save_raw_data = false;
    private MediaCodecAudioEncoder audioEncoder;

    public NADKWebRtcAudioRecord(Context context, AudioManager audioManager, NADKStreamingClient streamingClient) {
        this(context, audioManager, DEFAULT_AUDIO_SOURCE, DEFAULT_AUDIO_FORMAT, streamingClient);
    }

    public NADKWebRtcAudioRecord(Context context, AudioManager audioManager, int audioSource,
                                 int audioFormat, NADKStreamingClient streamingClient) {
        this.context = context;
        this.audioManager = audioManager;
        this.audioSource = audioSource;
        this.audioFormat = audioFormat;
        this.streamingClient = streamingClient;
    }

    public int initRecording(int sampleRate, int channels) {
        AppLog.d(TAG, "initRecording(sampleRate=" + sampleRate + ", channels=" + channels + ")");
        if (audioRecord != null) {
            AppLog.e(TAG,"InitRecording called twice without StopRecording.");
            return -1;
        }
        NADKAudioParameter nadkAudioFormat = new NADKAudioParameter(NADKCodec.NADK_CODEC_PCMA, sampleRate, channels, DEFAULT_AUDIO_SAMPLE_BITS);
        int frameSize = 3200;
        final int bytesPerSample = channels * getBytesPerSample(audioFormat);
        frameSize_ms = frameSize  * 1000 / bytesPerSample / sampleRate;
        AppLog.d(TAG, "required frameSize: " + frameSize + ", frameSize_ms: " + frameSize_ms);
        int bitrate = 16000;

//        try {
//            audioEncoder = new MediaCodecAudioEncoder(MediaCodecAudioEncoder.MIME_TYPE_OPUS, sampleRate, channels, bitrate, frameSize);
//        } catch (IOException e) {
//            e.printStackTrace();
//            AppLog.e(TAG, "MediaCodecAudioEncoder exception: " + e.getMessage());
//        }


        byteBuffer = ByteBuffer.allocateDirect(frameSize);
        if (!(byteBuffer.hasArray())) {
            AppLog.e(TAG, "ByteBuffer does not have backing array.");
            return -1;
        }
        AppLog.d(TAG, "byteBuffer.capacity: " + byteBuffer.capacity());
        emptyBytes = new byte[byteBuffer.capacity()];
        // Rather than passing the ByteBuffer with every callback (requiring
        // the potentially expensive GetDirectBufferAddress) we simply have the
        // the native class cache the address to the memory once.


        // Get the minimum buffer size required for the successful creation of
        // an AudioRecord object, in byte units.
        // Note that this size doesn't guarantee a smooth recording under load.
        final int channelConfig = channelCountToConfiguration(channels);
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            AppLog.d(TAG, "AudioRecord.getMinBufferSize failed: " + minBufferSize);
            return -1;
        }
        AppLog.d(TAG, "AudioRecord.getMinBufferSize: " + minBufferSize);

        // Use a larger buffer size than the minimum required when creating the
        // AudioRecord instance to ensure smooth recording under load. It has been
        // verified that it does not increase the actual recording latency.
        int bufferSizeInBytes = Math.max(BUFFER_SIZE_FACTOR * minBufferSize, byteBuffer.capacity());
        AppLog.d(TAG, "bufferSizeInBytes: " + bufferSizeInBytes);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Use the AudioRecord.Builder class on Android M (23) and above.
                // Throws IllegalArgumentException.
                audioRecord = createAudioRecordOnMOrHigher(
                        audioSource, sampleRate, channelConfig, audioFormat, bufferSizeInBytes);
                if (preferredDevice != null) {
                    setPreferredDevice(preferredDevice);
                }
            } else {
                // Use the old AudioRecord constructor for API levels below 23.
                // Throws UnsupportedOperationException.
                audioRecord = createAudioRecordOnLowerThanM(
                        audioSource, sampleRate, channelConfig, audioFormat, bufferSizeInBytes);
            }
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            // Report of exception message is sufficient. Example: "Cannot create AudioRecord".
            AppLog.e(TAG, e.getClass().getSimpleName() + ": " + e.getMessage());
            releaseAudioResources();
            return -1;
        }
        if (audioRecord == null || audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            AppLog.e(TAG,"Creation or initialization of audio recorder failed.");
            releaseAudioResources();
            return -1;
        }
        logMainParameters();
        logMainParametersExtended();

        return frameSize;
    }

    public boolean startRecording() {
        AppLog.d(TAG, "startRecording");
        try {
            audioRecord.startRecording();
            if (save_raw_data) {
                try {
                    outputStream = new FileOutputStream(new File("/mnt/sdcard/talk_test.pcm"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IllegalStateException e) {
            AppLog.e(TAG, "AudioRecord.startRecording failed: " + e.getMessage());
            return false;
        }

        audioThread = new AudioRecordThread("NADKAudioRecordThread");
        audioThread.start();
        return true;
    }

    public boolean stopRecording() {
        AppLog.d(TAG, "stopRecording");

        releaseAudioResources();

//        if (nadkStreamingLiveTalk != null) {
//            try {
////                nadkStreamingLiveTalk.disableTalk();
//            } catch (NADKException e) {
//                e.printStackTrace();
//            }
//            nadkStreamingLiveTalk = null;
//        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }

        if(audioThread != null) {
            audioThread.stopThread();
            if (audioThread.isAlive()) {
                try {
                    audioThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    AppLog.e(TAG, "Join of AudioRecordJavaThread InterruptedException: " + e.getMessage());
                }
            }
            audioThread = null;
            AppLog.e(TAG, "stop AudioRecordThread succeed");
        }

        if (audioEncoder != null) {
            audioEncoder.release();
            audioEncoder = null;
        }

        return true;
    }

    // Sets all recorded samples to zero if |mute| is true, i.e., ensures that
    // the microphone is muted.
    public void setMicrophoneMute(boolean mute) {
        AppLog.w(TAG, "setMicrophoneMute(" + mute + ")");
        microphoneMute = mute;
    }

    /**
     * Prefer a specific {@link AudioDeviceInfo} device for recording. Calling after recording starts
     * is valid but may cause a temporary interruption if the audio routing changes.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    @TargetApi(Build.VERSION_CODES.M)
    void setPreferredDevice(@Nullable AudioDeviceInfo preferredDevice) {
        AppLog.d(
                TAG, "setPreferredDevice " + (preferredDevice != null ? preferredDevice.getId() : null));
        this.preferredDevice = preferredDevice;
        if (audioRecord != null) {
            if (!audioRecord.setPreferredDevice(preferredDevice)) {
                AppLog.e(TAG, "setPreferredDevice failed");
            }
        }
    }



    @TargetApi(Build.VERSION_CODES.M)
    private static AudioRecord createAudioRecordOnMOrHigher(
            int audioSource, int sampleRate, int channelConfig, int audioFormat, int bufferSizeInBytes) {
        AppLog.d(TAG, "createAudioRecordOnMOrHigher");
        return new AudioRecord.Builder()
                .setAudioSource(audioSource)
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelConfig)
                        .build())
                .setBufferSizeInBytes(bufferSizeInBytes)
                .build();
    }

    private static AudioRecord createAudioRecordOnLowerThanM(
            int audioSource, int sampleRate, int channelConfig, int audioFormat, int bufferSizeInBytes) {
        AppLog.d(TAG, "createAudioRecordOnLowerThanM");
        return new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSizeInBytes);
    }

    private void logMainParameters() {
        AppLog.d(TAG,
                "AudioRecord: "
                        + "session ID: " + audioRecord.getAudioSessionId() + ", "
                        + "channels: " + audioRecord.getChannelCount() + ", "
                        + "sample rate: " + audioRecord.getSampleRate());
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void logMainParametersExtended() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AppLog.d(TAG,
                    "AudioRecord: "
                            // The frame count of the native AudioRecord buffer.
                            + "buffer size in frames: " + audioRecord.getBufferSizeInFrames());
        }
    }



    // Helper method which throws an exception  when an assertion has failed.
//    private static void assertTrue(boolean condition) {
//        if (!condition) {
//            throw new AssertionError("Expected condition to be true");
//        }
//    }

    private int channelCountToConfiguration(int channels) {
        return (channels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO);
    }

    // Releases the native AudioRecord resources.
    private void releaseAudioResources() {
        AppLog.d(TAG, "releaseAudioResources");
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
    }

    // Reference from Android code, AudioFormat.getBytesPerSample. BitPerSample / 8
    // Default audio data format is PCM 16 bits per sample.
    // Guaranteed to be supported by all devices
    private static int getBytesPerSample(int audioFormat) {
        switch (audioFormat) {
            case AudioFormat.ENCODING_PCM_8BIT:
                return 1;
            case AudioFormat.ENCODING_PCM_16BIT:
            case AudioFormat.ENCODING_IEC61937:
            case AudioFormat.ENCODING_DEFAULT:
                return 2;
            case AudioFormat.ENCODING_PCM_FLOAT:
                return 4;
            case AudioFormat.ENCODING_INVALID:
            default:
                throw new IllegalArgumentException("Bad audio format " + audioFormat);
        }
    }

    private class AudioRecordThread extends Thread {
        private volatile boolean keepAlive = true;

        public AudioRecordThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            AppLog.d(TAG, "AudioRecordThread" + "@[name=" + Thread.currentThread().getName() + ", id=" + Thread.currentThread().getId() + "]");
            long pts_ms = 0;

            long lastTime = System.nanoTime();
            while (keepAlive) {
                if (audioRecord == null) {
                    keepAlive = false;
                    AppLog.e(TAG, "audioRecord == null, set keepAlive = false, stop AudioRecordThread");
                    break;
                }
                int bytesRead = audioRecord.read(byteBuffer, byteBuffer.capacity());
//                AppLog.d(TAG, "AudioRecord.read bytesRead: " + bytesRead);
                if (bytesRead == byteBuffer.capacity()) {
                    pts_ms += frameSize_ms;
                    if (microphoneMute) {
                        byteBuffer.clear();
                        byteBuffer.put(emptyBytes);
                    }
                    // It's possible we've been shut down during the read, and stopRecording() tried and
                    // failed to join this thread. To be a bit safer, try to avoid calling any native methods
                    // in case they've been unregistered after stopRecording() returned.
                    if (keepAlive) {

                    }
                    if (streamingClient != null) {
//                        byte[] data = Arrays.copyOf(byteBuffer.array(), byteBuffer.capacity());
                        byte[] data = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.arrayOffset(),
                                byteBuffer.capacity() + byteBuffer.arrayOffset());
                        try {
                            if (outputStream != null) {
                                outputStream.write(data);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        int rawSize = data.length;

                        if (audioEncoder != null) {
                            data = audioEncoder.encode(data, data.length, pts_ms);
                        }

                        if (data != null) {
                            NADKFrameBuffer nadkFrameBuffer = new NADKFrameBuffer(data);
                            nadkFrameBuffer.setFrameType(NADK_CODEC_OPUS);
                            nadkFrameBuffer.setFrameSize(data.length);
                            nadkFrameBuffer.setPresentationTime(pts_ms);

                            try {
                                AppLog.d(TAG, "AudioRecordThread, talk sendAudioFrame: rawSize = " + rawSize + ", encodeSize = " + data.length + ", pts: " + pts_ms);
                                streamingClient.sendNextAudioFrame(nadkFrameBuffer);

                            } catch (NADKException e) {
                                e.printStackTrace();
                                AppLog.e(TAG, "AudioRecordThread, talk sendAudioFrame NADKException: " + e);
                                if (e.getErrCode() == NADKError.NADK_INVALID_SESSION) {
                                    keepAlive = false;
                                }
                            }

                        }
                    }
                } else {
                    String errorMessage = "AudioRecord.read failed: " + bytesRead;
                    AppLog.e(TAG, errorMessage);
                    if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                        keepAlive = false;
                    }
                }
            }

            try {
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord = null;
                }
            } catch (IllegalStateException e) {
                AppLog.e(TAG, "AudioRecord.stop failed: " + e.getMessage());
            }
        }

        // Stops the inner thread loop and also calls AudioRecord.stop().
        // Does not block the calling thread.
        public void stopThread() {
            AppLog.d(TAG, "stopThread");
            keepAlive = false;
        }
    }
}
