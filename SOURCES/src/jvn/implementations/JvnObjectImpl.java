/***
 * JAVANAISE Implementation
 * JvnObjectImpl class
 */

package jvn.implementations;

import java.io.Serializable;
import java.rmi.RemoteException;
import jvn.interfaces.JvnObject;
import jvn.utils.JvnException;
import jvn.utils.Lock;
import jvn.utils.Log;

public class JvnObjectImpl implements JvnObject{

	private int objectId;
	private Serializable objectState = null;
	
	transient private Lock lock; // we don't need to transfer this variable
	transient private ServerSynchronizer synchronizer;
	
	public JvnObjectImpl(int objectId, Serializable objectState, Lock lock) throws InterruptedException{
		this.objectId = objectId;
		this.objectState = objectState;
		
		this.lock = lock;
		this.synchronizer = new ServerSynchronizer();
	}
	
	public void jvnLockRead() throws JvnException {
		Log.display("Entree jvnLockRead", "appels");
		
		if(lock==null){
			lock = Lock.NL;
			this.synchronizer = new ServerSynchronizer();
		}
		
		// Wait for invalidation if received.
		this.synchronizer.waitForInvalidation();
		
		// Forbid invalidating request while modifying the lock state.
		this.synchronizer.setInvalidationAvailable(false);
		
		switch(lock){
			case NL:
				Log.display("demande au serveur un lock lecture", "verrous");
				lock = Lock.R;
				
				// no lock in cache, we ask lock to the server
				
				// Release access to invalidating request.
				this.synchronizer.setInvalidationAvailable(true);
				this.synchronizer.notifyAuthorization();
				
				try{
					this.objectState = JvnServerImpl.jvnGetServer().jvnLockRead(this.objectId);
				}catch(Exception e){
					
					// Reset his lock state
					lock = Lock.NL;
					throw new JvnException(""+e);
				}
				
				break;
				
			case RC:
				Log.display("lock lecture deja en cache", "verrous");
				lock = Lock.R;
				break;
				
			case RWC:
				// nothing to do
				Log.display("lock lecture et ecriture deja en cache", "verrous");
				break;
				
			case WC:
				Log.display("lock ecriture deja en cache, on a donc le lock lecture.", "verrous");
				lock = Lock.RWC;
				break;
				
			default:
				// we can't be here
				throw new JvnException("Un probleme est survenu lors de la prise du verrou en lecture. La valeur du verrou courant est incoherente.");
		}
		
		Log.display("Sortie jvnLockRead", "appels");
		
		// Release access to invalidating request.
		this.synchronizer.setInvalidationAvailable(true);
		this.synchronizer.notifyAuthorization();
		
	}

	public void jvnLockWrite() throws JvnException {
		Log.display("Entree jvnLockWrite", "appels");
		
		if(lock==null){
			lock = Lock.NL;
			this.synchronizer = new ServerSynchronizer();
		}
		
		// Wait for invalidation if received.
		this.synchronizer.waitForInvalidation();
		
		// Forbid invalidating request.
		this.synchronizer.setInvalidationAvailable(false);
		
		switch(lock){
			case NL:
				Log.display("demande au serveur un lock ecriture", "verrous");
				
				// no lock in cache, we ask lock to the server
				
				// Release access to invalidating request.
				this.synchronizer.setInvalidationAvailable(true);
				this.synchronizer.notifyAuthorization();
				
				try{
					this.objectState = JvnServerImpl.jvnGetServer().jvnLockWrite(this.objectId);
				} catch(RemoteException e){
					
					// Reset his lock state
					lock = Lock.NL;
					throw new JvnException(""+e);
				}
				
				// Forbid invalidating request.
				this.synchronizer.setInvalidationAvailable(false);
				
				lock = Lock.W;
				break;
				
			case RC:
				Log.display("possede lock lecture, demande au serveur un lock ecriture", "verrous");
				
				// Release access to invalidating request.
				this.synchronizer.setInvalidationAvailable(true);
				this.synchronizer.notifyAuthorization();
				
				try{
					this.objectState = JvnServerImpl.jvnGetServer().jvnLockWrite(this.objectId);
				} catch(Exception e){
					
					// Reset his lock state
					lock = Lock.NL;
					throw new JvnException(""+e);
				}
				
				// Forbid invalidating request.
				this.synchronizer.setInvalidationAvailable(false);
				
				lock = Lock.W;
				break;
				
			case RWC:
				// nothing to do
				Log.display("lock lecture et ecriture deja en cache.", "verrous");
				break;
				
			case WC:
				Log.display("lock ecriture deja en cache", "verrous");
				lock = Lock.W;
				break;
			
			default:
				// we can't be here
				throw new JvnException("Un probleme est survenu lors de la prise du verrou en ecriture. La valeur du verrou courant est incoherente.");
		}
				
		Log.display("Sortie jvnLockWrite", "appels");
	}

	public void jvnUnLock() throws JvnException {
		Log.display("Entree jvnUnLock", "appels");
		
		if(lock==null){
			lock = Lock.NL;
		}
		
		switch(lock){
			case NL:
				// Nothing to do
				break;
			case R:
				Log.display("unlock -> lock lecture en cache", "verrous");
				lock = Lock.RC;
				break;
			case W:
				Log.display("unlock -> lock ecriture en cache", "verrous");
				lock = Lock.WC;
				break;
			case RWC:
				Log.display("unlock -> lock ecriture et lecture en cache", "verrous");
				lock = Lock.RWC;
				break;
			case RC:
				Log.display("unlock -> lock lecture en cache", "verrous");
				lock = Lock.RC;
				break;
			case WC:
				Log.display("unlock -> lock ecriture en cache", "verrous");
				lock = Lock.WC;
				break;
			default:
				// We can't be here
				throw new JvnException("Un probleme est survenu lors de la liberation du verrou. La valeur du verrou courant est incoherente: "+lock.toString());
		}
		
		Log.display("Sortie jvnUnLock", "appels");
		
		// Release access to invalidating request.
		this.synchronizer.setInvalidationAvailable(true);
		this.synchronizer.notifyAuthorization();
		
	}


	public void jvnInvalidateReader() throws JvnException {
		Log.display("Entree jvnInvalidateReader", "appels");
		
		this.synchronizer.setInvalidateReceived(true);
		this.synchronizer.setInvalidateDone(false);
		
		// Wait for being authorized to invalidate this object 's lock.
		this.synchronizer.waitForAuthorization();
		
		Log.display("Quelqu'un prend le verrou ecriture -> lock perdu", "verrous");
		lock = Lock.NL;
		
		Log.display("Sortie jvnInvalidateReader", "appels");
		
		// Release access
		this.synchronizer.setInvalidateDone(true);
		this.synchronizer.setInvalidateReceived(false);
		this.synchronizer.notifyInvalidation();
		
	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		Log.display("Entree jvnInvalidateWriter", "appels");
		
		// Release access
		this.synchronizer.setInvalidateReceived(true);
		this.synchronizer.setInvalidateDone(false);
		
		// Wait for being authorized to invalidate this object 's lock.
		this.synchronizer.waitForAuthorization();
				
		Log.display("Quelqu'un prend le verrou ecriture -> lock perdu", "verrous");
		lock = Lock.NL;
		
		Log.display("Sortie jvnInvalidateWriter", "appels");
		
		// Release access
		this.synchronizer.setInvalidateDone(true);
		this.synchronizer.setInvalidateReceived(false);
		this.synchronizer.notifyInvalidation();
		
		return objectState;
	}
	
	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		Log.display("Entree jvnInvalidateWriterForReader", "appels");
		
		this.synchronizer.setInvalidateReceived(true);
		this.synchronizer.setInvalidateDone(false);
		
		// Wait for being authorized to invalidate this object 's lock.
		this.synchronizer.waitForAuthorization();
		
		Log.display("Quelqu'un prend le verrou lecture -> lock ecriture perdu, prend lock lecture", "verrous");
		lock = Lock.RC;
		
		Log.display("Sortie jvnInvalidateWriterForReader", "appels");
		
		this.synchronizer.setInvalidateDone(true);
		this.synchronizer.setInvalidateReceived(false);
		this.synchronizer.notifyInvalidation();
		
		return objectState;
	}

	public int jvnGetObjectId() throws JvnException {
		return this.objectId;
	}
	
	public Serializable jvnGetObjectState() throws JvnException {
		return this.objectState;
	}
	
	/*
	 * This method is not accessible to the user
	 */
	protected void jvnSetObjetState(Serializable o){
		objectState = o;
	}
}
