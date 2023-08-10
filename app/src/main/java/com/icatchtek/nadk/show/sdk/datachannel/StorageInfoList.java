package com.icatchtek.nadk.show.sdk.datachannel;

import com.alibaba.fastjson.JSON;

import java.util.LinkedList;

/**
 * Created by sha.liu on 2021/10/21.
 */
public class StorageInfoList {
    private LinkedList<StorageInfoOfOneDay> storageInfo;
    private int timezone;

    public StorageInfoList() {
    }

    public LinkedList<StorageInfoOfOneDay> getStorageInfo() {
        return storageInfo;
    }

    public void setStorageInfo(LinkedList<StorageInfoOfOneDay> storageInfo) {
        this.storageInfo = storageInfo;
    }

    public int getTimezone() {
        return timezone;
    }

    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
