/***
 * Store class
 * Allows to store shared objects. Contains a StoreEntry Table.
 */

package jvn.implementations;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import jvn.interfaces.JvnObject;
import jvn.interfaces.JvnRemoteServer;
import jvn.utils.JvnException;
import jvn.utils.Lock;
import jvn.utils.Log;
import jvn.utils.Server;
import jvn.utils.StoreEntry;

public class Store {

	private StoreEntry [] store;
	private int maxEntry;
	
	private int maxAccess;
	private Semaphore semStore;
	
	public Store(){
				
		this.maxEntry = 5;
		this.store = new StoreEntry [this.maxEntry];
		
		this.maxAccess = 1;
		this.semStore = new Semaphore(this.maxAccess);
	}

	public void setEntry(int index, StoreEntry entry){
		if(index < this.maxEntry){
			this.store[index] = entry;
		}
	}
	
	/**
	 * Allocate a NEW JVN object id.
	 * @return : The JVN object id on success or -1 otherwise.
	 */
	public int allocateId() {
		int id;
		
		Log.display("Entree allocateId", "appels");
		
		// Ensure that no one else is trying to modify the store state.
		Log.display("en attente d'acces au store pour selection d'un ID ...", "synchro");
		try {
			
			this.semStore.acquire();
			
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			return -1;
		}
		
		Log.display("verrou obtenu", "synchro");
		
		// Look for a free entry.
		id = 0;
		while(id < this.maxEntry && this.store[id] != null){
			id++;
		}
		
		if(id == this.maxEntry){ // No entry has been found
			id = -1;
			
			Log.display("Allocation impossible, store plein.", "appels");
			
		}else{
			
			Log.display("Allocation reussite.", "appels");
			this.store[id] = new StoreEntry(id);
		}
		
		// Release access to the store entry.
		this.semStore.release();
		
		Log.display("verrou relache", "synchro");
		
		Log.display("Sortie allocateId", "appels");
		
		return id;
	}
	
	/**
	 * Associate a symbolic name with a JVN object.
	 * @param String jon : JVN object name
	 * @param JvnObject jo : JVN object
	 * @param JvnRemoteServer js : JVN remote server claimer
	 */
	public void registerObject(String jon, JvnObject jo, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.utils.JvnException {
		int objectId;
		
		Log.display("Entree registerObject", "appels");
		
		try {
			
			// Ensure that no one else is trying to modify the store state.
			Log.display("en attente d'acquisition du verrou pour enregister un objet...", "synchro");
			this.semStore.acquire();
			
			Log.display("verrou obtenu", "synchro");
			
			// Retrieve the corresponding store entry reference.
			objectId = jo.jvnGetObjectId();
			StoreEntry se = this.store[objectId];
			
			// Initialize the store entry
			se.setObject(jo);
			se.setObjectName(jon);
			se.addServer(new Server(js, Lock.W));
			
		} catch (JvnException | InterruptedException e) {
			// Release access to the store.
			this.semStore.release();
			
			throw new JvnException("Cannot register the JVN object: "+e);
			
		}
		
		// Release access to the store.
		this.semStore.release();
		
		Log.display("verrou relache", "synchro");
		
		Log.display("Sortie registerObject", "appels");
	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server
	 * @param String jon : JVN object name
	 * @param JvnRemoteServer js : JVN remote server claimer
	 * @return JvnObject : null if the object named jon doesn't exist, else return the reference to the object jon 
	 */
	public JvnObject lookUpObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.utils.JvnException {
		
		Log.display("Entree lookUpObject", "appels");
		
		// Ensure that no one else is trying to modify the store state.
		Log.display("en attente d'acces au store...", "synchro");
		try {
			this.semStore.acquire();
		} catch (InterruptedException e) {
			throw new JvnException("Cannot look up JVN object.");
		}
		
		Log.display("verrou obtenu", "synchro");
		
		int i = 0;
		while(i < this.maxEntry && (this.store[i] == null || this.store[i].getObjectName() == null || this.store[i].getObjectName().compareTo(jon) != 0)){
			i++;
		}
		
		// Release access to the store.
		this.semStore.release();
		Log.display("verrou relache", "synchro");
		
		if(i == this.maxEntry){	// Object not found
			
			Log.display("Objet non trouve", "appels");
			Log.display("Sortie lookUpObject", "appels");
			return null;
		}else{
			
			// Ensure that no one else is accessing the store entry i.
			Log.display("en attente d'acces a une entree...", "synchro");
			
			this.store[i].acquire();
			
			// The object has been found => Register the client and send him the object.
			this.store[i].addServer(new Server(js, Lock.NL));
			
			Log.display("verrou relache", "synchro");
			Log.display("Objet trouve", "appels");
			Log.display("Sortie lookUpObject", "appels");
			
			this.store[i].release();
			
			return this.store[i].getObject();
		}
	}

	/**
	 * Remove information about the remote server js from the store.
	 * If the remote server is the last client using the JVN object, the store entry is removed.
	 * @param JvnRemoteServer js : JVN remote server claimer
	 */
	public void leave(JvnRemoteServer js) {
		
		Log.display("Entree leave", "appels");
		
		for(int i = 0; i < this.maxEntry; i++){
			
    		if(this.store[i] != null){
    			
    			this.store[i].acquire();
    			
    			Log.display("en attente d'acces a une entree...", "synchro");
    			
    			Log.display("verrou obtenu", "synchro");
    			
    			this.store[i].removeServer(js);
    			
    			//If js was the last client linked to the object then free the store entry.
    			if(!this.store[i].existServer()){
    				
    				// Ensure that no one else is trying to modify the store state
    				Log.display("en attente d'acces au store...", "synchro");
        			try {
    					this.semStore.acquire();

            			Log.display("verrou obtenu", "synchro");
            			
        				this.store[i] = null;
        				
        				Log.display("verrou detruit", "synchro");
        				
    				} catch (InterruptedException e) {
    					Log.display("Prise de verrou impossible", "appels");
    				}
    				
    				// Release access to the store.
        			this.semStore.release();
    			}
    			
    			// Release access to the store entry i.
    			if(this.store[i] != null){
    				
    				// Persistent data
    				this.store[i].storeState();
    				
    				this.store[i].release();
    			}
    			
    		}
    		
    	}

		Log.display("Sortie leave", "appels");
	}

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server.
	 * @param int joi : JVN Object Identifier
	 * @param JvnRemoteServer js : JVN remote server claimer
	 * @return Serializable : JVN Object State
	 * @throws JvnException 
	 * @throws RemoteException 
	 */
	public Serializable lockRead(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		
		Log.display("Entree lockRead", "appels");
		
		Log.display("en attente d'acces a l'entree "+joi+"...", "synchro");
		
		// Ensure that no one else is requesting a lock on the store entry.
		this.store[joi].acquire();
		
		Log.display("verrou obtenu", "synchro");
		
		// Get a read lock on the JVN object identified by "joi"
		JvnObject object = this.store[joi].getObject();
		
		// Get the JVN object State
		Serializable objectState = object.jvnGetObjectState();
		
		// Ensure that the remote server can read the JVN object.
		Log.display("Recherche d'un writer", "appels");
		JvnRemoteServer rs = this.store[joi].freeToRead(js);
		
		if(rs != null){ // The remote server cannot read the JVN object
			
			Log.display("Writer trouve", "appels");
			
			// If the remote server cannot access the JVN object for read,
			// the one who has the write lock has to invalidate it.
			try{
				Log.display("Invalidation du writer", "appels");
				
				objectState = rs.jvnInvalidateWriterForReader(joi);
				
				Log.display("Mise a jour de l'etat de l'objet", "appels");
				
				// update the object state
				((JvnObjectImpl)object).jvnSetObjetState(objectState);
				
				// Update the remote server' s lock state: WRITE to READ
				this.store[joi].setServerLock(rs, Lock.R);
			}
			catch(JvnException e){
				
				// Cannot invalidate writer for reader and retrieve the updated JVN object.
				// Then an unupdated JVN object will be send to the remote server.
				
				Log.display("Cannot invalidate writer for reader", "appels");
				
				// Update the remote server' s lock state: WRITE to NOLOCK
				this.store[joi].setServerLock(rs, Lock.NL);
			}
			catch(RemoteException e){
				
				// Cannot join the remote server who is supposed to deliver the new JVN object state.
				// Then an unupdated JVN object will be send to the requester.
				
				Log.display("Connexion failed on invalidating writer for reader lock","appels");
				
				// Since the remote server cannot be joined, we have to have to remove him from the store. 
				this.store[joi].removeServer(rs);
				
				//TODO if he was the last one who used this JVN object, the entry must be unallocated.
			}
			
		}
		
		Log.display("Mise a jour du lock", "verrou");
		
		// Update the corresponding store entry. The remote server "js" has a lock read.
		this.store[joi].setServerLock(js, Lock.R);
		
		Log.display("Sauvegarde de l'entree", "appels");
		
		// Persistent data
		this.store[joi].storeState();
				
		Log.display("verrou relache", "synchro");
		
		Log.display("Sortie lockRead", "appels");
		
		// Release access to the store entry.
		this.store[joi].release();
		
		return objectState;
	}

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server.
	 * @param int joi : JVN Object Identifier
	 * @param JvnRemoteServer js : JVN remote server claimer
	 * @return Serializable: JVN Object State
	 * @throws JvnException 
	 * @throws RemoteException 
	 */
	public Serializable lockWrite(int joi, JvnRemoteServer js) throws JvnException, RemoteException {
		
		Log.display("Entree lockWrite", "appels");
		
		Log.display("en attente d'acces a l'entree "+joi+"...", "synchro");
		
		// Ensure that no one else is requesting a lock on the store entry.
		this.store[joi].acquire();
		
		// Get a write lock on the JVN object
		JvnObject object = this.store[joi].getObject();

		// Get the JVN object State
		Serializable objectState = object.jvnGetObjectState();
		
		// Ensure that the remote server can modifies the JVN object
		
		Log.display("Recherche de clients possedant un lock", "appels");
		ArrayList<Server> list = this.store[joi].freeToWrite(js);
		
		if(list != null){ // The JVNobject cannot be modified cause the lock is already taken
			// If the remote server cannot access the JVN object for write,
			// the others have to invalidate their lock.
			JvnRemoteServer rs;
			
			for(Server s: list){				
				rs = s.getRemoteServer();
				if(s.getLock() == Lock.R){
					
					try{
						Log.display("Invalidation d'un reader", "appels");
						
						rs.jvnInvalidateReader(joi);
						
					}
					catch(JvnException e){
						
						// Cannot invalidate reader and retrieve the updated JVN object.
						// Then an unupdated JVN object will be send to the remote server.
						
						Log.display("Cannot invalidate reader","appels");
						
						// Update the remote server' s lock state: READ to NOLOCK
						// Since the remote server cannot deliver the object state, he will have to send a new request to get the JVN object.
						this.store[joi].setServerLock(rs, Lock.NL);
						
						Log.display("Cannot invalidate reader","appels");
					}
					catch(RemoteException e){

						// Cannot join the remote server who is supposed to deliver the new JVN object state.
						// Then an unupdated JVN object will be send to the requester.
						
						
						Log.display("Connexion failed on invalidating read lock", "appels");
						
						// Since the remote server cannot be joined, we have to remove him from the store. 
						this.store[joi].removeServer(rs);
					}
					
					
				}else{
					
					try{
						Log.display("Invalidation d'un writer", "appels");
						
						objectState = rs.jvnInvalidateWriter(joi);
						
					}
					catch(JvnException e){
						
						// Cannot invalidate writer and retrieve the updated JVN object.
						// Then an unupdated JVN object will be send to the remote server.
						
						Log.display("Cannot invalidate writer","appels");
						
						// Update the remote server' s lock state: WRITE to NOLOCK
						this.store[joi].setServerLock(rs, Lock.NL);
						
					}
					catch(RemoteException e){

						// Cannot join the remote server who is supposed to deliver the new JVN object state.
						// Then an unupdated JVN object will be send to the requester.
						
						
						Log.display("Connexion failed on invalidating write lock", "appels");
						
						// Since the remote server cannot be joined, we have to remove him from the store. 
						this.store[joi].removeServer(rs);
					}
					
					Log.display("Mise a jour de l'etat de l'objet", "appels");
					
					// update the object state
					((JvnObjectImpl)object).jvnSetObjetState(objectState);
					
				}
				
				// Update the remote server 's lock state: WRITE to NOLOCK or READ to NOLOCK
				this.store[joi].setServerLock(rs, Lock.NL);
				
			}
		}
		// Update the corresponding store entry. The remote server has a write lock.
		this.store[joi].setServerLock(js, Lock.W);
		
		// Persistent data
		this.store[joi].storeState();
		
		Log.display("verrou relache", "synchro");
		
		Log.display("Sortie lockWrite", "appels");
		
		// Release access to the store entry.
		this.store[joi].release();
		
		return objectState;
	}
}
