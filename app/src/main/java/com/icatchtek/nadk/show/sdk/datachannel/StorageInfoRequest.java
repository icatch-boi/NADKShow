package com.icatchtek.nadk.show.sdk.datachannel;

import com.alibaba.fastjson.JSON;

/**
 * Created by sha.liu on 2021/10/21.
 */
public class StorageInfoRequest {
    private long beginTimestamp;
    private long endTimestamp;
    private int timezone;

    public StorageInfoRequest() {
    }

    public StorageInfoRequest(long beginTimestamp, long endTimestamp, int timezone) {
        this.beginTimestamp = beginTimestamp;
        this.endTimestamp = endTimestamp;
        this.timezone = timezone;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public void setBeginTimestamp(long beginTimestamp) {
        this.beginTimestamp = beginTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
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
