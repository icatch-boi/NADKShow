package com.icatchtek.nadk.show.sdk.datachannel;

/**
 * Created by sha.liu on 2021/10/21.
 */
public class CmdResponse {
    private long transid;
    private String msgType;
    private int cmd;
    private int err;
    private Object ret;

    public CmdResponse() {
    }

    public CmdResponse(long transid, String msgType, int cmd, int err, Object ret) {
        this.transid = transid;
        this.msgType = msgType;
        this.cmd = cmd;
        this.err = err;
        this.ret = ret;
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

    public int getErr() {
        return err;
    }

    public void setErr(int err) {
        this.err = err;
    }

    public Object getRet() {
        return ret;
    }

    public void setRet(Object ret) {
        this.ret = ret;
    }
}
