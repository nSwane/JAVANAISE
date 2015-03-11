/***
 * Server class
 * Store the lock associated to a remoteServer
 */

package jvn.utils;

import java.io.Serializable;

import jvn.interfaces.JvnRemoteServer;

/**
 * Remote server 's lock manager
 * @author Ray
 *
 */

public class Server implements Serializable {
	JvnRemoteServer remoteServer;
	Lock lock;
	
	public Server(JvnRemoteServer js){
		this.remoteServer = js;
		this.lock = Lock.NL;
	}

	public Server(JvnRemoteServer js, Lock lock){
		this.remoteServer = js;
		this.lock = lock;
	}
	
	public JvnRemoteServer getRemoteServer() {
		return remoteServer;
	}

	public void setRemoteServer(JvnRemoteServer js) {
		this.remoteServer = js;
	}

	public Lock getLock() {
		return lock;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}
}