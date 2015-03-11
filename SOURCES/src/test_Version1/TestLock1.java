package test_Version1;

import java.io.Serializable;
import java.util.Random;

import test.EmptyClass;
import jvn.implementations.JvnServerImpl;
import jvn.interfaces.JvnObject;
import jvn.utils.JvnException;
import jvn.utils.Log;

/**
 * This client sends lock requests randomly and sleep between each acquired lock to simulate a treatment.
 * This client always leave the JVN service properly.
 * 
 * @author nawaouis
 *
 */
public class TestLock1 {

	public static void main(String[] args) {
		
		// activate log
		Log.activate("verrous", "synchro", "appels");
				
		// This client will send at last maxRequests requests to the coordinator. 
		int maxRequests;
		Random randomReq = new Random();
		maxRequests = randomReq.nextInt(91)+10; // maxRequests between 10 and 100
		System.out.println("MaxRequests : "+maxRequests+".");
		
		// random boolean generator: true for Read and false for write request.
		Random randomRW;
		randomRW = new Random();
		
		// random long generator: simulate treatment duration.
		Random randomSleep;
		randomSleep = new Random();
		
		try {
			// initialize JVN
			JvnServerImpl js = JvnServerImpl.jvnGetServer();
			
			// look up the IRC object in the JVN server
			// if not found, create it, and register it in the JVN server
			JvnObject jo = js.jvnLookupObject("IRC");
			
			if (jo == null) {
				jo = js.jvnCreateObject((Serializable) new EmptyClass());
				if(jo == null){
					System.out.println("Cannot create JVN object");
					System.exit(-1);
				}
				// after creation, I have a write lock on the object
				jo.jvnUnLock();
				js.jvnRegisterObject("IRC", jo);
			}
			
			// Starting requests
			for(int i = 1; i <= maxRequests; i++){
				if(randomRW.nextBoolean()){
					
					// Read request
					jo.jvnLockRead();
					
				}
				else{
					
					// Write request
					jo.jvnLockWrite();
					
				}
				
				// Simulate treatment with duration between 100ms and 300ms.
				try {
					Thread.sleep(randomSleep.nextInt(201)+100);
				} catch (InterruptedException e) {
					System.out.println("That is not normal!! o_O");
				}
				
				// End read request
				jo.jvnUnLock();
			}
			
			// End Requests
			js.jvnTerminate();
			
			System.exit(0);
			
		} catch (JvnException e) {
			System.out.println(e);
			System.exit(-1);
		}
	}

}
