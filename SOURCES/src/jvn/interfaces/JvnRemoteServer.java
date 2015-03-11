/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors: 
 */

package jvn.interfaces;

import java.rmi.*;
import java.io.*;
import jvn.utils.JvnException;

/**
 * Remote interface of a JVN server (used by a remote JvnCoord)
 */

public interface JvnRemoteServer extends Remote {
	    
	/**
	* Invalidate the Read lock of a JVN object 
	* @param joi : the JVN object id
	* @throws java.rmi.RemoteException,JvnException
	**/
  public void jvnInvalidateReader(int joi)
	throws java.rmi.RemoteException,jvn.utils.JvnException;
	    
	/**
	* Invalidate the Write lock of a JVN object 
	* @param joi : the JVN object id
	* @return the current JVN object state 
	* @throws java.rmi.RemoteException,JvnException
	**/
        public Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException,jvn.utils.JvnException;
	
	/**
	* Reduce the Write lock of a JVN object 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
	 throws java.rmi.RemoteException,jvn.utils.JvnException;
   
   /**
	 * Method allowing the JVN server to signal a client that the coordinator restarted.
	 * It updates the instance of the coordinator the client had.
	 * @param int : JVN Object Id
	 * @throws JvnException
	 */
	public void jvnUpdateServer(int joi) throws java.rmi.RemoteException, JvnException;

}

 
