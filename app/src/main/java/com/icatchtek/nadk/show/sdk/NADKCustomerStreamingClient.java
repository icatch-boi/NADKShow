package com.icatchtek.nadk.show.sdk;

import com.icatchtek.nadk.streaming.NADKStreamingClient;

/**
 * Created by sha.liu on 2023/8/18.
 */
public interface NADKCustomerStreamingClient extends NADKStreamingClient {

    void initialize(NADKCustomerStreamingClientObserver observer);

    void prepare();

    void destroy();

}
