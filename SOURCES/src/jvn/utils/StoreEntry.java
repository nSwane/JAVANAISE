/***
 * StoreEntry class
 * Entry of the Store object which keep information about a shared object (id, name, remote servers)
 */

package jvn.utils;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import jvn.interfaces.JvnObject;
import jvn.interfaces.JvnRemoteServer;

public class StoreEntry implements Serializable {

	transient private DataManager dataManager;
	
	private int objectId;
	private JvnObject object;
	private String objectName;
	
	private ArrayList<Server> servers;
	
	private int maxAccess;
	private Semaphore semList;
	
	public StoreEntry(int objectId) {
		
		// Initialize data manager.
		try {
			this.dataManager = new DataManager(objectId);
		} catch (IOException e) {
			Log.display("Cannot Initialize data manager: "+e, "appels");
		}
		
		this.objectId = objectId;
		this.object = null;
		this.objectName = null;
		this.servers = new ArrayList<Server>();
		
		this.maxAccess = 1;
		this.semList = new Semaphore(this.maxAccess);
		
	}
	
	/**
	 * Method called to initialize JVN object ID
	 * @param objectId
	 */
	public void setObjectId(int objectId){
		this.objectId = objectId;
	}

	/**
	 * Method called to initialize JVN object name
	 * @param jon
	 */
	public void setObjectName(String jon){
		this.objectName = jon;
	}
	
	/**
	 * Method called when a remote server first consult a JVN object
	 * @param jon
	 */
	public void addServer(Server s){
		this.servers.add(s);
	}
	
	/**
	 * Remove the remote server referenced by "js" from the list.
	 * @param js
	 */
	public void removeServer(JvnRemoteServer js){
		
		int i = find(js);
		if(i != -1){
			Log.display("Serveur distant supprime", "appels");
			this.servers.remove(i);
		}
		
	}
	
	/**
	 * JVN object id getter
	 * @return
	 */
	public int getObjectId(){
		return this.objectId;
	}
	
	/**
	 * Find the remote server "js" into the list "servers".
	 * @param js
	 * @return : the index of the reference "js" if found or -1 otherwise.
	 */
	private int find(JvnRemoteServer js) {
		
		int i = 0;
		
		while( i < this.servers.size() && !this.servers.get(i).remoteServer.equals(js)){
			i++;
		}
		
		if(i < this.servers.size()){
			return i;
		}
		else{
			return -1;
		}
	}
	
	/**
	 * Return true if the list contains the remote server referenced by "js", false otherwise.
	 * @param js
	 * @return
	 */
	public boolean contains(JvnRemoteServer js){
		int i = find(js);
		if(i != -1){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Return true if the remote servers list is not empty.
	 * @return
	 */
	public boolean existServer(){
		return !this.servers.isEmpty();
	}
	
	public JvnObject getObject(){
		return this.object;
	}
	
	public void setObject(JvnObject jo){
		this.object = jo;
	}
	
	public String getObjectName(){
		return this.objectName;
	}

	/**
	 * Update the remote server 's lock.
	 * @param js
	 * @param lock
	 */
	public void setServerLock(JvnRemoteServer js, Lock lock) {
		int i = find(js);
		
		if(i != -1){
			this.servers.get(i).lock = lock;
		}
		
	}
	
	/**
	 * Return null if the JVN Object can be read or the remote server who has the write lock.
	 * @param js: JvnRemoteServer: the remote server 's read request.
	 * @return JvnRemoteServer: The remote server who has a write lock on this JVN object.
	 */
	public JvnRemoteServer freeToRead(JvnRemoteServer js){
		
		int i;
		JvnRemoteServer writer;
		
		// If a remote server has a write lock on the JVN object, the JVN object cannot be read.
		i = 0;
		writer = null;
		while(i < this.servers.size() && writer == null){
			
			// There is no need for the remote server to test his own lock.
			if(!this.servers.get(i).remoteServer.equals(js) && this.servers.get(i).lock == Lock.W){
				writer = this.servers.get(i).remoteServer;
			}
			
			i++;
		}
		
		return writer;
	}
	
	/**
	 * Return null if the JVN Object can be modified or the remote servers who has a lock.
	 * @param js: JvnRemoteServer: the remote server 's read request.
	 * @return ArrayList<Server>: The remote servers who have a lock on this JVN object.
	 */
	public ArrayList<Server> freeToWrite(JvnRemoteServer js){
		ArrayList<Server> list = new ArrayList<>();
		int i;
		
		// If a remote server has a lock on the JVN object, the JVN object cannot be modified.
		i = 0;
		while(i < this.servers.size()){
			
			// There is no need for the remote server to test his own lock.
			if(!this.servers.get(i).remoteServer.equals(js) && this.servers.get(i).lock != Lock.NL){ // the remote server has a lock
				list.add(this.servers.get(i));
			}
			
			i++;
		}
		
		if(!list.isEmpty()){
			
			// At least one remote server has a lock hence the JVN object cannot be modified
			return list;
		}
		
		// None of the remote servers has a lock on the JVN object
		return null;
	}
	
	/**
	 * Acquire access to this store entry.
	 */
	public void acquire(){
		
		try {
			
			this.semList.acquire();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Release access to this store entry.
	 */
	public void release(){
		int r, w, nl;
		r = w = nl = 0;
		
		for(Server s: servers){
			if(s.lock == Lock.R){
				r++;
			}
			else{
				if(s.lock == Lock.W){
					w++;
				}
				else{
					nl++;
				}
			
			}
		}
		
		Log.display("Readers: "+r, "appels");
		Log.display("Writers: "+w, "appels");
		Log.display("NL: "+nl, "appels");
		this.semList.release();
	}
	
	/**
	 * Save this store entry state.
	 */
	public void storeState(){
		
		try {
			if(this.dataManager == null){
				this.dataManager = new DataManager(this.objectId);
			}
			
			this.dataManager.storeEntry(this);
		} catch (IOException e) {
			Log.display("Cannot save entry state: "+e, "appels");
		}
		
	}
	
	/**
	 * Signal all remote servers that the coordinator restarted.
	 */
	public void broadcast(){
		ArrayList<Server> toBeRemoved = new ArrayList<Server>();
		
		for(Server s: servers){
			try{
				s.remoteServer.jvnUpdateServer(this.objectId);
			} catch(JvnException | RemoteException e){
				
				// Unable to communicate with the remote server.
				// The server must then be removed.
				toBeRemoved.add(s);
			}
		}
		
		// Remove not valid references to remote servers
		for(Server s: toBeRemoved){
			servers.remove(s);
		}
		
		toBeRemoved.clear();
	}
	
}
