/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn.implementations;

import java.rmi.NoSuchObjectException;
import java.rmi.server.UnicastRemoteObject;
import java.io.IOException;
import java.io.Serializable;

import jvn.interfaces.JvnObject;
import jvn.interfaces.JvnRemoteCoord;
import jvn.interfaces.JvnRemoteServer;
import jvn.utils.DataManager;
import jvn.utils.JvnException;
import jvn.utils.Log;


public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord{
	
	private Store store;
	
  /**
  * Default constructor
  * @throws JvnException
  **/
	public JvnCoordImpl() throws Exception {
		super();
		
		// activate log
		//Log.activate("appels", "synchro");
				
	}

	/**
	 * Start the JVN service
	 */
	public void start() throws IOException, ClassNotFoundException{
		
		// Initialize the store
		DataManager.initConfig();
		if(DataManager.recoveryMode()){
			Log.display("Loading store", "appels");
			this.store = DataManager.loadEntries();
		}
		else{
			this.store = new Store();
		}
		DataManager.startManager();
	}
	
  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public int jvnGetObjectId() throws java.rmi.RemoteException,jvn.utils.JvnException {
	// Find a free place in the store
	int id = this.store.allocateId();
	return id;
  } 
  
  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.utils.JvnException{
	  this.store.registerObject(jon, jo, js);
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.utils.JvnException{
	  // Look for the object named "jon" in the store.  
	  return this.store.lookUpObject(jon, js);
  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
	   return this.store.lockRead(joi, js);
   }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
	   return this.store.lockWrite(joi, js);
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
    	// Remove information about the remote server js from the store
    	this.store.leave(js);
    }
    
    /**
     * Terminate the coordinator properly.
     * @throws NoSuchObjectException 
     * @throws JvnException 
     */
    public void stop() throws NoSuchObjectException, JvnException{
    	try {
			DataManager.stopManager();
			UnicastRemoteObject.unexportObject(this, true);
    	} catch (IOException e) {
			throw new JvnException("A problem occured while stopping the data manager: "+e);
		}
    }
}

 
