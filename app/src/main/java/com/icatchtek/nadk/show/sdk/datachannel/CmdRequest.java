package com.icatchtek.nadk.show.sdk.datachannel;

import com.alibaba.fastjson.JSON;

/**
 * Created by sha.liu on 2021/10/21.
 */
public class CmdRequest {
    private long transid;
    private String msgType;
    private int cmd;
    private int operation;
    private Object value;

    public CmdRequest() {
    }

    public CmdRequest(String msgType, int cmd, int operation, Object value) {
        this.transid = System.currentTimeMillis() / 1000;
        this.msgType = msgType;
        this.cmd = cmd;
        this.operation = operation;
        this.value = value;
    }

    public long getTransid() {
        return transid;
    }

    public void setTransid(long transid) {
        this.transid = transid;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
