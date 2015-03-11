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
 * This client leaves randomly the JVN service rudely or properly.
 * 
 * @author nawaouis
 *
 */
public class TestLock4 {

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
		
		JvnObject jo = null;
		
		// initialize JVN
		JvnServerImpl js = null;
		
		// look up the IRC object in the JVN server
		// if not found, create it, and register it in the JVN server
		boolean done = false;
		while(!done){
			try{
				js = JvnServerImpl.jvnGetServer();
				jo = js.jvnLookupObject("IRC");
				
				if (jo == null) {
					jo = js.jvnCreateObject((Serializable) new EmptyClass());
					if(jo == null){
						System.out.println("Cannot create JVN object");
						System.exit(0);
					}
					// after creation, I have a write lock on the object
					jo.jvnUnLock();
					js.jvnRegisterObject("IRC", jo);
				}
				
				done = true;
				
			} catch(JvnException e){
				System.out.println(e);
				
				// Release some CPU and try again.
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
				}
			}
		}
	
		// Starting requests
		for(int i = 1; i <= maxRequests; i++){
			
			try{
				if(randomRW.nextBoolean()){
					jo.jvnLockRead();
				}
				else{
					jo.jvnLockWrite();
				}
				
				// Simulate treatment with duration between 100ms and 300ms.
				Thread.sleep(randomSleep.nextInt(201)+100);
				
				jo.jvnUnLock();
			
			} catch (InterruptedException e) {
				System.out.println("That is not normal!! o_O");
			} catch (JvnException e) {
				
				// This must be caused by a connection error between
				// local server and JVN server.
				// This client is waiting for the JVN service to start over.
				System.out.println(e);
				i--;
				
				// Release some CPU
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					System.out.println("That is not normal!! o_O");
				}
			}
			
		}
		
		// End Requests
		if(randomRW.nextBoolean()){
			try {
				JvnServerImpl.jvnGetServer().jvnTerminate();
			} catch (JvnException e) {
				System.out.println("Cannot Terminate properly: "+e);
			}
		}
		System.exit(0);
			
	}

}
