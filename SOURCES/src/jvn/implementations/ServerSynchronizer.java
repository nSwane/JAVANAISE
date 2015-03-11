package jvn.implementations;

/**
 * Synchronize access to JVN object 's lock.
 * @author nawaouis
 *
 */
public class ServerSynchronizer {
	private boolean invalidateReceived;
	private boolean invalidateDone;
	
	private boolean invalidationAvailable;
	
	public ServerSynchronizer(){
		this.invalidateReceived = false;
		this.invalidateDone = true;
		
		this.invalidationAvailable = false;
	}
	
	public synchronized boolean isInvalidateReceived() {
		return invalidateReceived;
	}


	public synchronized void setInvalidateReceived(boolean invalidateReceived) {
		this.invalidateReceived = invalidateReceived;
	}


	public synchronized boolean isInvalidateDone() {
		return invalidateDone;
	}


	public synchronized void setInvalidateDone(boolean invalidateDone) {
		this.invalidateDone = invalidateDone;
	}

	public synchronized boolean isInvalidationAvailable() {
		return invalidationAvailable;
	}

	public synchronized void setInvalidationAvailable(boolean invalidationAvailable) {
		this.invalidationAvailable = invalidationAvailable;
	}

	/**
	 * Wait for the end of invalidating request if received.
	 */
	public synchronized void waitForInvalidation(){
		while(invalidateReceived && !this.invalidateDone){
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void notifyInvalidation(){
		notifyAll();
	}
	
	/**
	 * Wait for being allowed to invalidate JVN object lock.
	 */
	public synchronized void waitForAuthorization(){
		while(!invalidationAvailable){
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void notifyAuthorization(){
		notifyAll();
	}
}
