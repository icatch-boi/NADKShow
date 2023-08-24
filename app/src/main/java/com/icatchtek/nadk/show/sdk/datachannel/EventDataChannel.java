package com.icatchtek.nadk.show.sdk.datachannel;

import com.alibaba.fastjson.JSON;
import com.icatch.smarthome.am.aws.iot.DeviceConnectedClientStatus;
import com.icatch.smarthome.am.aws.iot.DeviceStreamStatus;
import com.icatch.smarthome.am.aws.iot.DeviceUpgradeStatus;
import com.icatch.smarthome.am.aws.iot.IotEventType;
import com.icatch.smarthome.am.aws.iot.MqttObservable;
import com.icatch.smarthome.am.entity.DeviceExternalEvent;
import com.icatch.smarthome.am.utils.DebugLogger;
import com.icatchtek.baseutil.log.AppLog;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Observer;


/**
 * Created by sha.liu on 2021/11/17.
 */
public class EventDataChannel implements BaseDataChannel.Observer {
    private final static String TAG = EventDataChannel.class.getSimpleName();
    private final int MAX_DATA_SIZE = 500 * 1024; //200KB
    private String deviceId;
    private long receivedSessionId = 0;



    private BaseDataChannel baseDataChannel;
    private String channelName;
    private String channelId;
    private int packetTransId = 0;
    private byte[] receivedData;
    private int offset = 0;
    private int packetId = 0;

    private Map<String, MqttObservable> eventObservableMap = new HashMap<>();
    private Map<Integer, MqttObservable> externalEventObservableMap = new HashMap<>();
    public static final String IOT_EVENT_TYPE_CUSTOMER = "CUSTOMER_EVENT";
    public static final String IOT_EVENT_TYPE_BINARY = "BINARY_EVENT";


    public EventDataChannel(String deviceId, IDataChannel dataChannel) {
        this.deviceId = deviceId;
        baseDataChannel = new BaseDataChannel(deviceId, dataChannel);
        receivedData = new byte[MAX_DATA_SIZE];
        baseDataChannel.init(this);
        eventObservableMap.put(IotEventType.IOT_EVENT_TYPE_EXTERNAL, new MqttObservable());
    }



    public void addEventObserver(String eventType, Observer observer) {
        if (eventType == null || observer == null) {
            return;
        }
        if (!eventObservableMap.containsKey(eventType)) {
            eventObservableMap.put(eventType, new MqttObservable());
        }
        Objects.requireNonNull(eventObservableMap.get(eventType)).addObserver(observer);
    }

    public void deleteEventObserver(String eventType, Observer observer) {
        if (eventType == null || observer == null) {
            return;
        }

        if (eventObservableMap.containsKey(eventType)) {
            Objects.requireNonNull(eventObservableMap.get(eventType)).deleteObserver(observer);
        }
    }

    public void addExternalEventObserver(int eventId, Observer observer) {
        if (observer == null) {
            return;
        }
        if (!externalEventObservableMap.containsKey(eventId)) {
            externalEventObservableMap.put(eventId, new MqttObservable());
        }
        Objects.requireNonNull(externalEventObservableMap.get(eventId)).addObserver(observer);
    }

    public void deleteExternalEventObserver(int eventId, Observer observer) {
        if (observer == null) {
            return;
        }

        if (externalEventObservableMap.containsKey(eventId)) {
            Objects.requireNonNull(externalEventObservableMap.get(eventId)).deleteObserver(observer);
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
                String eventStr =  charset.decode(ByteBuffer.wrap(receivedData, 0, offset)).toString();
//                String eventStr = new String(receivedData);
                AppLog.e(TAG, "eventStr: " + eventStr);
                try {

                    JSONObject eventJson = new JSONObject(eventStr);
                    if (eventJson.has("sessionid")) {
                        receivedSessionId = eventJson.getLong("sessionid");
                    }

                    if (eventJson.has("eventtype")) {
                        String eventType = eventJson.getString("eventtype");

                        if (eventObservableMap.containsKey(eventType)) {
                            if (eventType.equals(IotEventType.IOT_EVENT_TYPE_STREAM_STATUS)) {
                                DeviceStreamStatus streamStatus = JSON.parseObject(eventStr, DeviceStreamStatus.class);
                                DebugLogger.e(TAG, "eventType: " + eventType + ", streamStatus: " + streamStatus);
                                MqttObservable mqttObservable = eventObservableMap.get(eventType);
                                if (mqttObservable != null) {
                                    mqttObservable.notifyChanged(streamStatus);
                                } else {
                                    DebugLogger.e(TAG, "eventType: " + eventType + ", mqttObservable == null");
                                }
//                                        Objects.requireNonNull(eventObservableMap.get(eventType)).notifyChanged(streamStatus);
                            } else if (eventType.equals(IotEventType.IOT_EVENT_TYPE_UPGRADE_STATUS)) {
                                DeviceUpgradeStatus upgradeStatus = JSON.parseObject(eventStr, DeviceUpgradeStatus.class);
                                DebugLogger.e(TAG, "eventType: " + eventType + ", upgradeStatus: " + upgradeStatus);
                                MqttObservable mqttObservable = eventObservableMap.get(eventType);
                                if (mqttObservable != null) {
                                    mqttObservable.notifyChanged(upgradeStatus);
                                } else {
                                    DebugLogger.e(TAG, "eventType: " + eventType + ", mqttObservable == null");
                                }
//                                        Objects.requireNonNull(eventObservableMap.get(eventType)).notifyChanged(upgradeStatus);
                            } else if (eventType.equals(IotEventType.IOT_EVENT_TYPE_CLIENT_STATUS)) {
                                DeviceConnectedClientStatus clientStatus = JSON.parseObject(eventStr, DeviceConnectedClientStatus.class);
                                DebugLogger.e(TAG, "eventType: " + eventType + ", clientStatus: " + clientStatus);
                                MqttObservable mqttObservable = eventObservableMap.get(eventType);
                                if (mqttObservable != null) {
                                    mqttObservable.notifyChanged(clientStatus);
                                } else {
                                    DebugLogger.e(TAG, "eventType: " + eventType + ", mqttObservable == null");
                                }
//                                        Objects.requireNonNull(eventObservableMap.get(eventType)).notifyChanged(clientStatus);
                            } else if (eventType.equals(IotEventType.IOT_EVENT_TYPE_RADAR)) {
                                JSONObject data = null;
                                if (eventJson.has("data")) {
                                    data = eventJson.getJSONObject("data");
                                }
                                DebugLogger.e(TAG, "eventType: " + eventType + ", data: " + data);

                                if (data != null) {
                                    MqttObservable mqttObservable = eventObservableMap.get(eventType);
                                    if (mqttObservable != null) {
                                        mqttObservable.notifyChanged(data.toString());
                                    } else {
                                        DebugLogger.e(TAG, "eventType: " + eventType + ", mqttObservable == null");
                                    }
                                } else {
                                    DebugLogger.e(TAG, "eventType: " + eventType + ", data == null");
                                }
                            } else if (eventType.equals(IotEventType.IOT_EVENT_TYPE_EXTERNAL)) {
                                int eventId = 0;
                                Object param =  null;
                                if (eventJson.has("id")) {
                                    eventId = eventJson.getInt("id");
                                }

                                if (eventJson.has("param")) {
                                    param = eventJson.get("param");
                                }
                                DebugLogger.e(TAG, "eventType: " + eventType + ", eventId: " + eventId + ", param: " + param);
                                DeviceExternalEvent externalEvent = JSON.parseObject(eventStr, DeviceExternalEvent.class);
                                DebugLogger.e(TAG, "eventType: " + eventType + ", externalEvent: " + externalEvent);

                                if (externalEvent != null) {
                                    MqttObservable mqttObservable = externalEventObservableMap.get(externalEvent.getId());
                                    if (mqttObservable != null) {
                                        mqttObservable.notifyChanged(externalEvent);
                                    } else {
                                        DebugLogger.e(TAG, "eventType: " + eventType + ", eventId: " + eventId + ", mqttObservable == null");
                                    }
                                    MqttObservable externalMqttObservable = eventObservableMap.get(eventType);
                                    if (externalMqttObservable != null) {
                                        externalMqttObservable.notifyChanged(externalEvent);
                                    } else {
                                        DebugLogger.e(TAG, "eventType: " + eventType + ", externalMqttObservable == null");
                                    }
                                } else {
                                    DebugLogger.e(TAG, "eventType: " + eventType + ", externalEvent == null");
                                }

                            } else {
                                MqttObservable mqttObservable = eventObservableMap.get(eventType);
                                if (mqttObservable != null) {
                                    mqttObservable.notifyChanged(eventStr);
                                } else {
                                    DebugLogger.e(TAG, "eventType: " + eventType + ", mqttObservable == null");
                                }
//                                        Objects.requireNonNull(eventObservableMap.get(eventType)).notifyChanged(msgData);
                            }

                        } else {
                            if (!eventStr.isEmpty()) {
                                MqttObservable mqttObservable = eventObservableMap.get(IOT_EVENT_TYPE_CUSTOMER);
                                if (mqttObservable != null) {
                                    mqttObservable.notifyChanged(eventStr);
                                } else {
                                    DebugLogger.e(TAG, "IOT_EVENT_TYPE_CUSTOMER, mqttObservable == null");
                                }

                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    DebugLogger.e(TAG, deviceId + " event Exception= " + e.getMessage());
                    if (!eventStr.isEmpty()) {
                        MqttObservable mqttObservable = eventObservableMap.get(IOT_EVENT_TYPE_CUSTOMER);
                        if (mqttObservable != null) {
                            mqttObservable.notifyChanged(eventStr);
                        } else {
                            DebugLogger.e(TAG, "IOT_EVENT_TYPE_CUSTOMER, mqttObservable == null");
                        }

                    }
                }

            } else {

                AppLog.d(TAG, "onMessage: receivedData = " + receivedData + ", dataSize = " + offset);
                if (offset < BinaryEventHeader.HEADER_SIZE) {
                    DebugLogger.e(TAG, "IOT_EVENT_TYPE_BINARY, offset < BinaryEventHeader.HEADER_SIZE");
                    return;
                }

                StringBuilder tmp = new StringBuilder();
                for (int i = 0; i < BinaryEventHeader.HEADER_SIZE; i++) {
                    tmp.append(String.format("%02X ", receivedData[i]));
                }
                AppLog.e(TAG, "onMessage BinaryEventHeader dump: " + tmp.toString());
                BinaryEvent binaryEvent = new BinaryEvent(receivedData, offset);
                BinaryEventHeader binaryEventHeader = binaryEvent.getHeader();

                AppLog.e(TAG, "onMessage: " + binaryEventHeader.toString());

                MqttObservable mqttObservable = eventObservableMap.get(IOT_EVENT_TYPE_BINARY);
                if (mqttObservable != null) {
                    mqttObservable.notifyChanged(binaryEvent);
                } else {
                    DebugLogger.e(TAG, "IOT_EVENT_TYPE_BINARY, mqttObservable == null");
                }

            }
        }


    }

    @Override
    public void onRawDataArrived(byte[] data, int dataSize) {
        offset = 0;
        packetId = 0;

        System.arraycopy(data, 0, receivedData, offset, dataSize);
        AppLog.d(TAG, "onMessage: receivedData = " + receivedData + ", dataSize = " + dataSize + ", frameSize = " + (dataSize - BinaryEventHeader.HEADER_SIZE));

        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < BinaryEventHeader.HEADER_SIZE * 3; i++) {
            tmp.append(String.format("%02X ", receivedData[i]));
        }
        AppLog.e(TAG, "onMessage BinaryEventHeader dump: " + tmp.toString());
        BinaryEvent binaryEvent = new BinaryEvent(receivedData, dataSize);
        BinaryEventHeader binaryEventHeader = binaryEvent.getHeader();

        AppLog.e(TAG, "onMessage: " + binaryEvent);

        MqttObservable mqttObservable = eventObservableMap.get(IOT_EVENT_TYPE_BINARY);
        if (mqttObservable != null) {
            mqttObservable.notifyChanged(binaryEvent);
        } else {
            DebugLogger.e(TAG, "IOT_EVENT_TYPE_BINARY, mqttObservable == null");
        }

    }


}
