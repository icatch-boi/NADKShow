package com.icatchtek.nadk.show.sdk.datachannel;

import com.alibaba.fastjson.JSON;
import com.icatchtek.baseutil.log.AppLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sha.liu on 2021/10/28.
 */
public class DownloadDataChannel implements BaseDataChannel.Observer {
    private final String TAG = DownloadDataChannel.class.getSimpleName();
    private BaseDataChannel baseDataChannel;
    private String deviceId;
    private String channelName;
    private String channelId;
    private Map<Long, DownloadRequest> downloadRequestMap;
    private int packetTransId = 0;
    private int binaryDataSize = 0;
    private int packetId = 0;
    private long cmdTransId = 0;


    public DownloadDataChannel(String deviceId, IDataChannel dataChannel) {
        this.deviceId = deviceId;
        baseDataChannel = new BaseDataChannel(deviceId, dataChannel);
        downloadRequestMap = new HashMap<>();

        baseDataChannel.init(this);

    }



    public void downloadFile(CmdRequest cmdRequest, String filePath, DownloadProgressCallback downloadProgressCallback) {

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            Object lock = new Object();
            DownloadResult result = new DownloadResult();
            int timeout = 40 * 1000;
            DownloadRequest request = new DownloadRequest(cmdRequest, fileOutputStream, new DownloadProgressCallback() {
                @Override
                public void onStart(long totalSize) {
                    result.downloadTimeout = false;
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                    downloadProgressCallback.onStart(totalSize);


                }

                @Override
                public void onProgress(long downloadSize, long totalSize) {
                    result.downloadTimeout = false;
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                    downloadProgressCallback.onProgress(downloadSize, totalSize);



                }

                @Override
                public void onStop(int error, String msg) {
                    result.downloadTimeout = false;
                    result.stopDownload = true;
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    downloadProgressCallback.onStop(error, msg);

                }
            });
            downloadRequestMap.put(cmdRequest.getTransid(), request);

            while (!result.stopDownload) {
                synchronized (lock) {
                    lock.wait(timeout);
                    timeout = 20 * 1000;
                    if (result.downloadTimeout) {
                        downloadProgressCallback.onStop(-1, "download timeout");
                        break;
                    } else {
                        result.downloadTimeout = true;
                    }
                }

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            downloadProgressCallback.onStop(-1, "file error: " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            downloadProgressCallback.onStop(-1, "download error: " + e.getMessage());
        } finally {
            downloadRequestMap.remove(cmdRequest.getTransid());
        }


    }

    public void cancelDownloadFile(CmdRequest cmdRequest) {
        downloadRequestMap.remove(cmdRequest.getTransid());
    }



    public InputStream getThumbnail(CmdRequest cmdRequest, long timeout_ms) {
        Object lock = new Object();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(200 * 1024);
        DownloadResult result = new DownloadResult();

        DownloadRequest request = new DownloadRequest(cmdRequest, outputStream, new DownloadProgressCallback() {
            @Override
            public void onStart(long totalSize) {
                result.fileSize = totalSize;
                result.reminderSize = totalSize;

            }

            @Override
            public void onProgress(long downloadSize, long totalSize) {
                result.downloadSize = downloadSize;

            }

            @Override
            public void onStop(int error, String msg) {
                result.error = error;
                result.msg = msg;
                synchronized (lock) {
                    lock.notifyAll();
                }

            }
        });
        downloadRequestMap.put(cmdRequest.getTransid(), request);
        try {
            synchronized (lock) {
                lock.wait(timeout_ms);
            }

            if (result.downloadSize > 0 && result.downloadSize == result.fileSize) {
                return new ByteArrayInputStream(outputStream.toByteArray());
            } else {
                return null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onStateChange(IDataChannel.State state) {

    }

    @Override
    public void onPacketArrived(PacketData packetData) {
        PacketHeader header = packetData.getHeader();
        int receivedTransId = header.getTransactionId();

        if (receivedTransId != packetTransId) {
            DownloadRequest request = downloadRequestMap.get(cmdTransId);
            if (request != null) {
                request.getDownloadProgressCallback().onStop(-1, "stop it");
                downloadRequestMap.remove(cmdTransId);
            }
            packetTransId = receivedTransId;
            binaryDataSize = 0;
            packetId = 0;
            cmdTransId = 0;
        }

        if (header.getPacketIndex() - packetId > 1) {
            AppLog.e(TAG, "onPacketArrived: packet lost " + (header.getPacketIndex() - packetId) );
            DownloadRequest request = downloadRequestMap.get(cmdTransId);
            if (request != null) {
                request.getDownloadProgressCallback().onStop(-1, "stop it");
                downloadRequestMap.remove(cmdTransId);
            }
            return;
        }
        packetId = header.getPacketIndex();

        if (packetId == 0) {
            binaryDataSize = 0;
            if (header.getDataType() == PacketHeader.DATA_TYPE_STRING) {
                Charset charset = Charset.forName("utf-8");
                String responseStr =  charset.decode(ByteBuffer.wrap(packetData.getData(), 0, header.getDataSize())).toString();
//                String responseStr = new String(receivedData);
                AppLog.e(TAG, "firstPacket: " + responseStr);
                DownloadFileFirstPacket fileFirstPacket = null;
                try {
                    fileFirstPacket = JSON.parseObject(responseStr, DownloadFileFirstPacket.class);

                } catch (Exception e) {
                    e.printStackTrace();
                    AppLog.e(TAG, "parseObject responseStr: " + e.getMessage());
                }

                if (fileFirstPacket != null) {
                    cmdTransId = fileFirstPacket.getTransid();
                    DownloadRequest request = downloadRequestMap.get(fileFirstPacket.getTransid());
                    if (request != null) {
                        if (request.getCmdRequest().getCmd() == fileFirstPacket.getCmd() && request.getCmdRequest().getMsgType().equals(fileFirstPacket.getMsgType())) {
                            request.getDownloadProgressCallback().onStart(fileFirstPacket.getFileSize());
                            request.setFileSize(fileFirstPacket.getFileSize());
                            request.setReminderSize(fileFirstPacket.getRemainderSize());
                        } else {
                            request.getDownloadProgressCallback().onStop(-2, "cmd response error");
                            downloadRequestMap.remove(cmdTransId);
                        }
                    }

                }


            }

        } else if (header.getDataType() == PacketHeader.DATA_TYPE_BINARY) {

            DownloadRequest request = downloadRequestMap.get(cmdTransId);
            if (request != null) {
                if (request.getOutputStream() != null) {
                    try {
                        request.getOutputStream().write(packetData.getData(), 0, header.getDataSize());
                        binaryDataSize += header.getDataSize();
                        request.getDownloadProgressCallback().onProgress(binaryDataSize, request.getFileSize());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (header.getEndFlag() == PacketHeader.END_FLAG_YES) {
                    if (binaryDataSize == request.getReminderSize()) {
                        request.getDownloadProgressCallback().onStop(0, "download success");
                    } else {
                        request.getDownloadProgressCallback().onStop(-1, "download failed");
                    }
                    downloadRequestMap.remove(cmdTransId);

                }

            }

        }




    }

    @Override
    public void onRawDataArrived(byte[] data, int dataSize) {

    }

    private class DownloadResult {
        public long fileSize = 0;
        public long offset = 0;
        public long reminderSize = 0;
        public long downloadSize = 0;
        public int error;
        public String msg;
        public boolean stopDownload = false;
        public boolean downloadTimeout = true;
    }

    private class DownloadRequest {
        private CmdRequest cmdRequest;
        private DownloadProgressCallback downloadProgressCallback;
        private OutputStream outputStream;
        private long fileSize;
        private long reminderSize;


        public DownloadRequest(CmdRequest cmdRequest, OutputStream outputStream, DownloadProgressCallback downloadProgressCallback) {
            this.cmdRequest = cmdRequest;
            this.outputStream = outputStream;
            this.downloadProgressCallback = downloadProgressCallback;
        }


        public CmdRequest getCmdRequest() {
            return cmdRequest;
        }

        public DownloadProgressCallback getDownloadProgressCallback() {
            return downloadProgressCallback;
        }

        public OutputStream getOutputStream() {
            return outputStream;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public long getReminderSize() {
            return reminderSize;
        }

        public void setReminderSize(long reminderSize) {
            this.reminderSize = reminderSize;
        }
    }
}
