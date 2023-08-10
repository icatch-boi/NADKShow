package com.icatchtek.nadk.show.sdk.datachannel;

import com.alibaba.fastjson.JSON;

/**
 * Created by sha.liu on 2021/10/28.
 */
public class FileListRequest {
    private long beginTimestamp;
    private long endTimestamp;
    private int maxItemQuantity;

    public FileListRequest() {
    }

    public FileListRequest(long beginTimestamp, long endTimestamp, int maxItemQuantity) {
        this.beginTimestamp = beginTimestamp;
        this.endTimestamp = endTimestamp;
        this.maxItemQuantity = maxItemQuantity;
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

    public int getMaxItemQuantity() {
        return maxItemQuantity;
    }

    public void setMaxItemQuantity(int maxItemQuantity) {
        this.maxItemQuantity = maxItemQuantity;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
