package com.icatchtek.nadk.show.sdk.datachannel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.icatch.smarthome.am.utils.ThreadPoolUtil;
import com.icatchtek.baseutil.log.AppLog;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sha.liu on 2021/10/28.
 */
public class CmdDataChannel implements BaseDataChannel.Observer {
    private final String TAG = CmdDataChannel.class.getSimpleName();
    public final int MAX_DATA_SIZE = 200 * 1024; //200KB
    private BaseDataChannel baseDataChannel;
    private String deviceId;
    private String channelName;
    private String channelId;
    private Map<Long, Cmd> sendCmdMap;
    private int packetTransId = 0;
    private byte[] receivedData;
    private int offset = 0;
    private int packetId = 0;
    private DownloadDataChannel thumbnailChannel;
    private DownloadDataChannel downloadChannel;


    public CmdDataChannel(String deviceId, IDataChannel dataChannel) {
        this.deviceId = deviceId;
        baseDataChannel = new BaseDataChannel(deviceId, dataChannel);
        sendCmdMap = new HashMap<>();
        receivedData = new byte[MAX_DATA_SIZE];

        baseDataChannel.init(this);

    }

    public void getStorageInfo(long beginTime, long endTime, int timeZone, CmdResponseCallback<StorageInfoList> cmdResponseCallback) {
        StorageInfoRequest request = new StorageInfoRequest(beginTime, endTime, timeZone);
        CmdRequest cmdRequest = new CmdRequest(CmdMessageType.PLAYBACK, LocalPBCmdID.CMD_GET_STORAGE_INFO, CmdOperationType.GET, request);
        sendCmd(cmdRequest, new CmdResponseCallback<Object>() {
            @Override
            public void onResponse(Object response) {
                if (response instanceof JSONObject) {
                    StorageInfoList storageInfoList = JSON.parseObject(response.toString(), StorageInfoList.class);
                    AppLog.d(TAG, "storageInfoList: " + storageInfoList);
                    cmdResponseCallback.onResponse(storageInfoList);

                }
            }

            @Override
            public void onError(int err, String msg) {
                cmdResponseCallback.onError(err, msg);

            }
        });

    }

    public void getFileList(long beginTime, long endTime, int maxItemQuantity, CmdResponseCallback<StorageInfoOfOneDay> cmdResponseCallback) {
        FileListRequest request = new FileListRequest(beginTime, endTime, maxItemQuantity);
        CmdRequest cmdRequest = new CmdRequest(CmdMessageType.PLAYBACK, LocalPBCmdID.CMD_GET_FILE_LIST, CmdOperationType.GET, request);
        sendCmd(cmdRequest, new CmdResponseCallback<Object>() {
            @Override
            public void onResponse(Object response) {
                if (response instanceof JSONObject) {
                    StorageInfoOfOneDay storageInfoOfOneDay = JSON.parseObject(response.toString(), StorageInfoOfOneDay.class);
                    AppLog.d(TAG, "storageInfoOfOneDay: " + storageInfoOfOneDay);
                    cmdResponseCallback.onResponse(storageInfoOfOneDay);

                }
            }

            @Override
            public void onError(int err, String msg) {
                cmdResponseCallback.onError(err, msg);

            }
        });

    }

    public InputStream getThumbnail(int fileHandle) {
        DownloadFileRequest request = new DownloadFileRequest(fileHandle, LocalFileType.FILE_TYPE_THUMBNAIL, 0);
        CmdRequest cmdRequest = new CmdRequest(CmdMessageType.PLAYBACK, LocalPBCmdID.CMD_DOWNLOAD_FILE, CmdOperationType.GET, request);

        sendCmd(cmdRequest, new CmdResponseCallback<Object>() {
            @Override
            public void onResponse(Object response) {

            }

            @Override
            public void onError(int err, String msg) {

            }
        });

        if (thumbnailChannel != null) {
            return thumbnailChannel.getThumbnail(cmdRequest, 30 * 1000);
        } else {
            return null;
        }

    }

    public void downloadFile(int fileHandle, int fileType, int offset, String filePath, DownloadProgressCallback downloadProgressCallback) {
        DownloadFileRequest request = new DownloadFileRequest(fileHandle, fileType, offset);
        CmdRequest cmdRequest = new CmdRequest(CmdMessageType.PLAYBACK, LocalPBCmdID.CMD_DOWNLOAD_FILE, CmdOperationType.GET, request);

        ThreadPoolUtil.getInstance().executorNetThread(new Runnable() {
            @Override
            public void run() {
                if (fileType == LocalFileType.FILE_TYPE_THUMBNAIL) {
                    if (thumbnailChannel != null) {
                        thumbnailChannel.downloadFile(cmdRequest, filePath, downloadProgressCallback);
                    }
                } else {
                    if (downloadChannel != null) {
                        downloadChannel.downloadFile(cmdRequest, filePath, downloadProgressCallback);
                    }
                }
            }
        });

        sendCmd(cmdRequest, new CmdResponseCallback<Object>() {
            @Override
            public void onResponse(Object response) {

            }

            @Override
            public void onError(int err, String msg) {
                downloadChannel.cancelDownloadFile(cmdRequest);
                downloadProgressCallback.onStop(err, msg);
            }
        });

    }

    public void setDori(String area, CmdResponseCallback<Object> cmdResponseCallback) {
        CmdRequest cmdRequest = new CmdRequest(CmdMessageType.DORI, LocalPBCmdID.CMD_SET_DORI, CmdOperationType.SET, area);
        sendCmd(cmdRequest, new CmdResponseCallback<Object>() {
            @Override
            public void onResponse(Object response) {
                cmdResponseCallback.onResponse(response);
            }

            @Override
            public void onError(int err, String msg) {
                cmdResponseCallback.onError(err, msg);

            }
        }, 0);

    }

    public void switchSensor(CmdResponseCallback<Object> cmdResponseCallback) {
        CmdRequest cmdRequest = new CmdRequest(CmdMessageType.CUSTOMER, LocalPBCmdID.CMD_SWITCH_SENSOR, CmdOperationType.SET, null);
        sendCmd(cmdRequest, new CmdResponseCallback<Object>() {
            @Override
            public void onResponse(Object response) {
                cmdResponseCallback.onResponse(response);
            }

            @Override
            public void onError(int err, String msg) {
                cmdResponseCallback.onError(err, msg);

            }
        }, 0);

    }

    private void sendCmd(CmdRequest request, CmdResponseCallback<Object> responseCallback) {
        sendCmd(request, responseCallback, 20 * 1000);
    }

    private void sendCmd(CmdRequest request, CmdResponseCallback<Object> responseCallback, long timeout_ms) {

        String cmdStr = JSON.toJSONString(request);
        AppLog.e(TAG, "sendCmd: " + cmdStr);
        boolean ret = baseDataChannel.sendData(cmdStr.getBytes(), cmdStr.getBytes().length);

        if (!ret) {
//            CmdResponseCallback<Object> callback = sendCmdMap.get(request.getTransid()).getResponseCallback();
//            if (callback != null) {
//                 callback.onError(-1, "send cmd error");
//            }

            responseCallback.onError(-1, "send cmd error");
//            sendCmdMap.remove(request.getTransid());
        } else {

            if (timeout_ms <= 0) {
                responseCallback.onResponse(null);
                return;
            }

            Object lock = new Object();
            SendCmdResult cmdResult = new SendCmdResult();
            CmdResponseCallback<Object> callback = new CmdResponseCallback<Object>() {
                @Override
                public void onResponse(Object response) {
                    responseCallback.onResponse(response);
                    synchronized (lock) {
                        cmdResult.responseTimeout = false;
                        lock.notifyAll();
                    }
                }

                @Override
                public void onError(int err, String msg) {
                    responseCallback.onError(err, msg);
                    synchronized (lock) {
                        cmdResult.responseTimeout = false;
                        lock.notifyAll();
                    }

                }
            };
            Cmd cmd = new Cmd(request, callback);


            sendCmdMap.put(request.getTransid(), cmd);
            try {
                synchronized (lock) {
                    lock.wait(timeout_ms);
                }

                if (cmdResult.responseTimeout) {
                    responseCallback.onError(-1, "response timeout");
                    sendCmdMap.remove(request.getTransid());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (cmdResult.responseTimeout) {
                    responseCallback.onError(-1, "response timeout");
                    sendCmdMap.remove(request.getTransid());
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
            packetTransId = receivedTransId;
            offset = 0;
            packetId = 0;
        }

        System.arraycopy(packetData.getData(), 0, receivedData, offset, header.getDataSize());
        offset += header.getDataSize();
        if (header.getPacketIndex() - packetId > 1) {
            AppLog.e(TAG, "onPacketArrived: packet lost " + (header.getPacketIndex() - packetId) );
        }
        packetId = header.getPacketIndex();

        if (header.getEndFlag() == PacketHeader.END_FLAG_YES) {
            if (header.getDataType() == PacketHeader.DATA_TYPE_STRING) {
                Charset charset = Charset.forName("utf-8");
                String responseStr =  charset.decode(ByteBuffer.wrap(receivedData, 0, offset)).toString();
//                String responseStr = new String(receivedData);
                AppLog.e(TAG, "responseStr: " + responseStr);
                CmdResponse cmdResponse = null;
                try {
                    cmdResponse = JSON.parseObject(responseStr, CmdResponse.class);

                } catch (Exception e) {
                    e.printStackTrace();
                    AppLog.e(TAG, "parseObject responseStr: " + e.getMessage());
                }

                if (cmdResponse != null) {
                    Cmd cmd = sendCmdMap.get(cmdResponse.getTransid());
                    if (cmd != null) {
                        if (cmd.getRequest().getCmd() == cmdResponse.getCmd() && cmd.getRequest().getMsgType().equals(cmdResponse.getMsgType())) {
                            if (cmdResponse.getErr() == 0) {
                                cmd.getResponseCallback().onResponse(cmdResponse.getRet());
                            } else {
                                cmd.getResponseCallback().onError(cmdResponse.getErr(), "");
                            }
                        } else {
                            cmd.getResponseCallback().onError(-2, "cmd response error");
                        }
                        sendCmdMap.remove(cmdResponse.getTransid());
                    }

                }
            }
        }


    }

    @Override
    public void onRawDataArrived(byte[] data, int dataSize) {

    }

    public void setThumbnailChannel(DownloadDataChannel thumbnailChannel) {
        this.thumbnailChannel = thumbnailChannel;
    }

    public void setDownloadChannel(DownloadDataChannel downloadChannel) {
        this.downloadChannel = downloadChannel;
    }

    private class SendCmdResult {
        public int error = -1;
        public String msg;
        public boolean responseTimeout = true;
    }

    private class Cmd {
        private CmdRequest request;
        private CmdResponseCallback responseCallback;

        public Cmd(CmdRequest request, CmdResponseCallback responseCallback) {
            this.request = request;
            this.responseCallback = responseCallback;
        }

        public CmdRequest getRequest() {
            return request;
        }

        public void setRequest(CmdRequest request) {
            this.request = request;
        }

        public CmdResponseCallback getResponseCallback() {
            return responseCallback;
        }

        public void setResponseCallback(CmdResponseCallback responseCallback) {
            this.responseCallback = responseCallback;
        }
    }
}
