package com.icatchtek.nadk.show.sdk.datachannel;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * Created by sha.liu on 2021/10/28.
 */
public class StorageInfoOfOneDay {
    private String date;
    private int timezone;
    private int count;
    private List<LocalFileInfo> list;


    public StorageInfoOfOneDay() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<LocalFileInfo> getList() {
        return list;
    }

    public void setList(List<LocalFileInfo> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
