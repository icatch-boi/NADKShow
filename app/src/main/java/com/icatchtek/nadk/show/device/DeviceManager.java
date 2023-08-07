package com.icatchtek.nadk.show.device;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by sha.liu on 2023/8/4.
 */
public class DeviceManager {
    private static DeviceManager instance;
    private Map<String, NADKLocalDevice> deviceMap;

    public static DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }

        return instance;
    }

    private DeviceManager() {
        deviceMap = new HashMap<>();
    }

    public synchronized void addDevice(NADKLocalDevice device) {
        if (deviceMap.containsKey(device.getDeviceId())) {
            return;
        }

        deviceMap.put(device.getDeviceId(), device);
    }

    public synchronized void removeDevice(NADKLocalDevice device) {
        deviceMap.remove(device.getDeviceId());
    }

    public synchronized NADKLocalDevice getDevice(String deviceId) {
        if (deviceMap.containsKey(deviceId)) {
            return deviceMap.get(deviceId);
        }
        return null;
    }

    public synchronized List<NADKLocalDevice> getDeviceList() {
        return new LinkedList<>(deviceMap.values());
    }

    public synchronized void reloadDevice(List<NADKLocalDevice> deviceList) {
        deviceMap.clear();
        for (NADKLocalDevice device : deviceList) {
            deviceMap.put(device.getDeviceId(), device);
        }
    }

}
