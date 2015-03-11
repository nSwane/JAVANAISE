package test_Version2;

import irc_Version2.ItfSentence;
import irc_Version2.Sentence;

import java.util.Random;

import jvn.implementations.JvnProxy;
import jvn.implementations.JvnServerImpl;
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
		
		ItfSentence s = null;
		try{
			// initialize JVN
			// Create (or get if it already exists) a shared object named IRC
			s = (ItfSentence) JvnProxy.newInstance(new Sentence(), "IRC");	

		}
		catch(JvnException e){
			System.out.println(e);
			System.exit(1);
		}
		
		try{
			// Starting requests
			for(int i = 1; i <= maxRequests; i++){
				if(randomRW.nextBoolean()){
					
					// Read request
					s.read();
				}
				else{
					
					// Write request
					s.write("");
				}
				
				// Simulate treatment with duration between 100ms and 300ms.
				try {
					Thread.sleep(randomSleep.nextInt(201)+100);
				} catch (InterruptedException e) {
					System.out.println("This is not normal!! o_O");
				}
				
			}
			
			// End Requests
			JvnServerImpl.jvnGetServer().jvnTerminate();
			System.exit(0);
		
		} catch(JvnException e){
			System.out.println(e);
			System.exit(-1);
		}
	}

}
