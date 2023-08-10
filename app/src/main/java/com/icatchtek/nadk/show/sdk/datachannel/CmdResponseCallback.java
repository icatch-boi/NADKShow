package com.icatchtek.nadk.show.sdk.datachannel;

/**
 * Created by sha.liu on 2021/11/3.
 */
public interface CmdResponseCallback<T> {
    public void onResponse(T response);

    public void onError(int err, String msg);
}
