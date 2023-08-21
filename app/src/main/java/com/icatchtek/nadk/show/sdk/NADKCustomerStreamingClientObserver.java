package com.icatchtek.nadk.show.sdk;

/**
 * Created by sha.liu on 2023/8/18.
 */
public interface NADKCustomerStreamingClientObserver {
    void onPrepare(boolean succeed);

    void onDestroy();
}
