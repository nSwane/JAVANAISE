/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors: 
 */

package jvn.interfaces;

import java.io.*;
import jvn.utils.JvnException;

/**
 * Interface of a JVN object. 
 * The serializable property is required in order to be able to transfer 
 * a reference to a JVN object remotely
 */

public interface JvnObject extends Serializable {

	/**
	* Get a Read lock on the object 
	* @throws JvnException
	**/
	public void jvnLockRead()
	throws jvn.utils.JvnException; 

	/**
	* Get a Write lock on the object 
	* @throws JvnException
	**/
	public void jvnLockWrite()
     	throws jvn.utils.JvnException; 

	/**
	* Unlock  the object 
	* @throws JvnException
	**/
	public void jvnUnLock()
	throws jvn.utils.JvnException; 
	
	
	/**
	* Get the object identification
	* @throws JvnException
	**/
	public int jvnGetObjectId()
	throws jvn.utils.JvnException; 
	
	/**
	* Get the object state
	* @throws JvnException
	**/
	public Serializable jvnGetObjectState()
	throws jvn.utils.JvnException; 
	
	
	/**
	* Invalidate the Read lock of the JVN object 
	* @throws JvnException
	**/
  public void jvnInvalidateReader()
	throws jvn.utils.JvnException;
	    
	/**
	* Invalidate the Write lock of the JVN object  
	* @return the current JVN object state
	* @throws JvnException
	**/
  public Serializable jvnInvalidateWriter()
	throws jvn.utils.JvnException;
	
	/**
	* Reduce the Write lock of the JVN object 
	* @return the current JVN object state
	* @throws JvnException
	**/
   public Serializable jvnInvalidateWriterForReader()
	 throws jvn.utils.JvnException;
}
