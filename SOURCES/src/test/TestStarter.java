package test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Scanner;
import java.util.Timer;

import cs.JvnCoordStarter;

public class TestStarter {

	static Constantes data;
	static ProcessBuilder pb;
	static Process p;
	static int maxProcess = 100;
	static String JvnServiceStarter = JvnCoordStarter.class.getName();
	static long timeLife = 10000;
    static long revivingDelay = 3000;
    
	/**
	 * Start the JVN service using a starter from package cs.
	 * @param name
	 * @throws IOException
	 */
	public static void initializeCoordinator(){
		pb = new ProcessBuilder(data.javaBin+"java", "-cp", data.classpath, JvnServiceStarter);
		
		// Create the log file
		File dir = new File("./logs");
		if(!dir.exists()){
			dir.mkdir();
		}
		
		File log = new File("./logs/log[C].txt");
		if(log.exists()){
			log.delete();
			try {
				log.createNewFile();
			} catch (IOException e) {
				System.out.println("Cannot create log[C].txt file");
				System.exit(-1);
			}
		}
		
		pb.redirectErrorStream(true);
		pb.redirectOutput(Redirect.appendTo(log));
		pb.redirectInput();
	}
	
	public static void startCoordinator(){
		try {
			p = pb.start();
		} catch (IOException e) {
			System.out.println("Cannot start JVN process.");
			System.exit(-1);
		}
		
		// Let time for the coordinator to start.
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			System.out.println("Cannot sleep o_O: "+e1);
		}
		
		System.out.println("JVN service started.");
		
	}
	
	/**
	 * Stop JVN service properly (when b is true) or rudely (when b is false).
	 * @param p
	 * @param b
	 */
	public static void stopCoordinator(boolean b){
		if(p != null){
			if(b){
				String s = "s";
				OutputStream out = null;
				
				try {
					out = p.getOutputStream();
					out.write(s.getBytes());
					
				} catch (IOException e) {
					System.out.println("A problem occured sending signal to JVN servr to stop");
				}
				
				try{
					if(out != null)
						out.close();
				} catch(IOException e){
					System.out.println("A problem occured while stopping JVN server");
				}
				
				System.out.println("JVN service stopped properly.");
			}
			else{
				p.destroy();
				System.out.println("JVN service stopped rudely.");
			}
		}
	}
	
	public static void testStarter(String testName){
		Thread [] clients = new Thread [maxProcess];
		
		// Initialize threads (clients)
		for(int i = 0; i < maxProcess; i++){
			try {
				clients[i] = new Thread(new ThreadClientManager(i, testName, data));
			} catch (IOException e) {
				System.out.println("An error occured during threads initialization");
				System.out.println("Cannot pursue: "+testName);
				System.out.println(e);
				return;
			}
		}
		
		// Start threads
		for(int i = 0; i < maxProcess; i++){
			clients[i].start();
			
			// Let time for others to retrieve the JVN object.
			if(i == 0){
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					System.out.println(("Cannot sleep!"));
				}
			}
		}
		
		// Catch threads
		for(int i = 0; i < maxProcess; i++){
			
			try {
				clients[i].join();
			} catch (InterruptedException e) {
				System.out.println("An error occured while waiting for thread: "+e);
			}
			
		}
		
		System.out.println(testName+" ended!");
	}
	
	public static void main(String[] args){
		Timer timer = null;
		
		// Initialize data environment.
		data = new Constantes();
		
		// Initialize JVN service.
		initializeCoordinator();
		startCoordinator();
		
		System.out.println("Select test to execute: Enter 1 for classic request test, enter 2 for recovery process test)");
		
		Scanner sc = new Scanner(System.in);
		int test = sc.nextInt();
		switch(test){
			
			// Version 1 of JVN service
			case 11:				
				/*
				* PRE CONDITION(S):
				* all remote servers retrieve one JVN object and manipulate it until they terminate properly.
				* The coordinator is already started and never stops.
				* 
				* POST CONDITION(S):
				* The client terminate.
				* There is no deadlocks.
				* The store is relevant.
				* The coordinator is still able to serve.
				 */
				
				testStarter(test_Version1.TestLock1.class.getName());
				break;
				
			case 21:
				/*
				* PRE CONDITION(S):
				* all remote servers retrieve one JVN object and manipulate it until they terminate rudely.
				* The coordinator is already started and never stops.
				* 
				* POST CONDITION(S):
				* The clients terminate.
				* There is no deadlocks.
				* The store is relevant.
				* The coordinator is still able to serve.
				 */
				
				testStarter(test_Version1.TestLock2.class.getName());
				break;
			
			case 1:
			case 31:
				/*
				* PRE CONDITION(S):
				* all remote servers retrieve one JVN object and manipulate it until they terminate either rudely or properly.
				* The coordinator is already started and never stops.
				* 
				* POST CONDITION(S):
				* The clients terminate.
				* There is no deadlocks.
				* The store is relevant.
				* The coordinator is still able to serve.
				 */
				
				testStarter(test_Version1.TestLock3.class.getName());
				break;
			
			case 2:
			case 41:
				/*
				* PRE CONDITION(S):
				* all remote servers retrieve one JVN object and manipulate it until they terminate either rudely or properly.
				* The coordinator is already started.
				* 
				* POST CONDITION(S):
				* The clients terminate.
				* There is no deadlocks.
				* The store is relevant.
				* The coordinator is still able to serve after restarting.
				 */
				
		        System.out.println("Time life set to "+timeLife+" ms from now.");
		        System.out.println("Reviving delay set to "+revivingDelay+" ms after death.");
		        
		        timer = new Timer();
				timer.schedule(new Die(), timeLife);
				timer.schedule(new Revive(), timeLife + revivingDelay);
				
				// Client executing TestLock4 must handle lost of connection
				// with the coordinator.
				testStarter(test_Version1.TestLock4.class.getName());
				
				break;
				
			// Version 2 of JVN service
			case 12:
				testStarter(test_Version2.TestLock1.class.getName());
				break;
			case 32:
				testStarter(test_Version2.TestLock3.class.getName());
				break;
			default:
				System.out.println("Not valide test");
		}
		
		// Stop coordinator
		
		System.out.println("Enter 'true' to stop the JVN service properly, 'false' otherwise.");
		
		boolean end = sc.nextBoolean();
		stopCoordinator(end);
		sc.close();
		
		// Clean timer
		if(timer != null){
	        timer.cancel();
	        timer.purge();
		}
		
		System.exit(0);
	}
	
}
