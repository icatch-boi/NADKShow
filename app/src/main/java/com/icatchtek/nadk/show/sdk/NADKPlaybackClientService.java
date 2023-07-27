package com.icatchtek.nadk.show.sdk;

import com.icatchtek.nadk.playback.NADKPlaybackClient;
import com.icatchtek.nadk.playback.NADKPlaybackClientListener;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NADKPlaybackClientService implements NADKPlaybackClientListener
{
    boolean masterRole;
	private final Lock connectedClientsMutex = new ReentrantLock();
	private final Condition connectedClientsCondV = connectedClientsMutex.newCondition();
    NADKPlaybackClient playbackClient;
	private NADKPlaybackClientListener playbackClientListener;

	public NADKPlaybackClientService(boolean masterRole, NADKPlaybackClientListener playbackClientListener)
	{
		this.masterRole = masterRole;
		this.playbackClientListener = playbackClientListener;
	}

	public NADKPlaybackClient getPlaybackClient(int timeout)
	{
		/* Using master role as server
		 * App do not need this playbackClient. */
		if (this.masterRole) {
			return null;
		}

		if (this.playbackClient != null) {
			return this.playbackClient;
		}

		connectedClientsMutex.lock();
		try {
			connectedClientsCondV.await(33, TimeUnit.MILLISECONDS);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return this.playbackClient;
	}

	public void connected(NADKPlaybackClient playbackClient)
	{
		if (this.playbackClient != null) {
			return;
		}

		this.connectedClientsMutex.lock();
		this.playbackClient = playbackClient;
		this.connectedClientsCondV.signalAll();
		this.connectedClientsMutex.unlock();
		if (playbackClientListener != null) {
			playbackClientListener.connected(this.playbackClient);
		}
	}

	public void disconnected(NADKPlaybackClient playbackClient)
	{
		if (this.playbackClient != null &&
			this.playbackClient == playbackClient)
		{
			if (playbackClientListener != null) {
				playbackClientListener.disconnected(this.playbackClient);
			}
			this.playbackClient = null;
		}
	}
}
