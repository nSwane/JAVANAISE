/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn.implementations;

import jvn.interfaces.JvnLocalServer;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.io.*;

import jvn.interfaces.JvnObject;
import jvn.interfaces.JvnRemoteCoord;
import jvn.interfaces.JvnRemoteServer;
import jvn.utils.JvnException;
import jvn.utils.Lock;
import jvn.utils.Log;



public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer{
	
	// A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;
		
	// Location of rmiregistry
	private String hostName = "//localhost:1024/COORDINATOR";
		
	// Location of The coordinator in the rmiregistry
	private JvnRemoteCoord coordinator;
	
	// Contains the jvnObject associated to an id
	private Hashtable<Integer, JvnObject> objectCache;
	
	// Maximum cache size
	private final int maxCacheSize = 5;
	
	/**
	* Default constructor
	* @throws JvnException
	**/
	private JvnServerImpl() throws Exception {
		super();
		
		// Get the coordinator reference
		this.coordinator = (JvnRemoteCoord) Naming.lookup(hostName);
		
		// Create the object and lock cache
		this.objectCache = new Hashtable<Integer, JvnObject>();
	}
	
	/**
    * Static method allowing an application to get a reference to 
    * a JVN server instance
    * @throws JvnException
    **/
	public static JvnServerImpl jvnGetServer() throws JvnException {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				throw new JvnException("Un probleme de connexion est survenue. Verifier que le coordinateur est lance et accessible.\n"+e);
			}
		}
		return js;
	}
	
	/**
	 * Method allowing the JVN server to signal a client that the coordinator restarted.
	 * It updates the instance of the coordinator the client had.
	 * @param int : JVN Object id
	 * @throws JvnException
	 */
	public void jvnUpdateServer(int joi) throws RemoteException, JvnException {
		try{
			
			Log.display("Retreive JVN server instance after connection lost", "appels");
			
			// Get the coordinator reference
			this.coordinator = (JvnRemoteCoord) Naming.lookup(hostName);
			
			// Reset JVN object 's lock
			jvnInvalidateWriter(joi);
			
		}catch(Exception e){
			throw new JvnException(""+e);
		}
		
	}
	
	/**
	* The JVN service is not used anymore
	* @throws JvnException
	**/
	public  void jvnTerminate() throws jvn.utils.JvnException {
		try {
			this.coordinator.jvnTerminate(js);
		} catch (Exception e) {
			throw new JvnException("jvnTerminate: "+e);
		}
	} 
	
	/**
	* creation of a JVN object
	* @param Serializable o : the JVN object state
	* @return JvnObject : a JVN object which could be null if the coordinator cannot allocate any memory for a new object
	* @throws JvnException
	**/
	public  JvnObject jvnCreateObject(Serializable o) throws jvn.utils.JvnException { 
		int id;
		JvnObject jo = null;

		if(objectCache.size() >= maxCacheSize){
			throw new JvnException("Maximum cache size achieved");
		}
		
		try {
			// If we get an id, create a new jvnObject with write lock AND
			// create a new entry in the lock and object cache 
			id = this.coordinator.jvnGetObjectId();
			if(id != -1){	
				jo = new JvnObjectImpl(id, o, Lock.W);
				objectCache.put(id,  jo);
			}
		} catch (Exception e) {
			throw new JvnException("jvnCreateObject: "+e);
		}
		return jo; 
	}
	
	/**
	*  Associate a symbolic name with a JVN object
	* @param jon : the JVN object name
	* @param jo : the JVN object 
	* @throws JvnException
	**/
	public  void jvnRegisterObject(String jon, JvnObject jo) throws jvn.utils.JvnException {
		try {
			// create the shared object in the coordinator store
			this.coordinator.jvnRegisterObject(jon, jo, js);
		} catch (Exception e) {
			throw new JvnException("jvnRegisterObject: "+e);
		}
	}
	
	/**
	* Provide the reference of a JVN object being given its symbolic name
	* @param jon : the JVN object name
	* @return the JVN object 
	* @throws JvnException
	**/
	public  JvnObject jvnLookupObject(String jon) throws jvn.utils.JvnException {
		JvnObject jo = null;
		
		try {
			// Ask the coordinator for the JVN object
			// Then create a new entry in the object cache if the object is already shared and not present in the object cache
			jo = this.coordinator.jvnLookupObject(jon, js);

			if(jo!=null){
				if(objectCache.size() >= maxCacheSize){
					throw new JvnException("Maximum cache size achieved");
				}
				
				Integer id = jo.jvnGetObjectId();
				if(objectCache.get(id)==null){
					objectCache.put(jo.jvnGetObjectId(), jo);
				}
			}
		} catch (Exception e) {
			throw new JvnException("jvnLookupObject: "+e);
		}
		return jo;
	}	
	
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	 * @throws RemoteException 
	**/
	public Serializable jvnLockRead(int joi) throws JvnException, RemoteException {
		Serializable objectState = objectCache.get(joi);
		
		// Ask the coordinator for a read lock and update lock cache
		objectState = this.coordinator.jvnLockRead(joi, js);
		return objectState;
	}
	
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	 * @throws RemoteException 
	**/
   public Serializable jvnLockWrite(int joi) throws JvnException, RemoteException {
	   Serializable objectState = objectCache.get(joi);

	   // Ask the coordinator for a write lock and update lock cache
	   objectState = this.coordinator.jvnLockWrite(joi, js);
	   return objectState;
	}	

	
  /**
	* Invalidate the Read lock of the JVN object identified by id 
	* called by the JvnCoord
	* @param joi : the JVN object id
	* @return void
	* @throws java.rmi.RemoteException,JvnException
	**/
   	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException,jvn.utils.JvnException {
		objectCache.get(joi).jvnInvalidateReader();
	}
   	
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException,jvn.utils.JvnException { 
		return objectCache.get(joi).jvnInvalidateWriter();
	}
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException,jvn.utils.JvnException { 
	   return objectCache.get(joi).jvnInvalidateWriterForReader();
   }

   /**
    * Empty the object cache and remove locks in the coordinator
    */
	public void jvnFlush() throws JvnException {
		jvnTerminate();
		objectCache.clear();
	}
}

 
