package test;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

public class ThreadClientManager implements Runnable {

	private int id;
	private Process process;
	private ProcessBuilder processBuilder;
	
	public ThreadClientManager(int id, String testName, Constantes data) throws IOException{
		this.id = id;
		//System.out.println(data.javaBin+"java");
		
		this.processBuilder = new ProcessBuilder(data.javaBin+"java", "-cp", data.classpath, testName);
		
		// Create the log file
		File log = new File("./logs/log["+id+"].txt");
		if(log.exists()){
			log.delete();
			try {
				log.createNewFile();
			} catch (IOException e) {
				throw new IOException("Cannot create log file");
			}
		}
		
		this.processBuilder.redirectErrorStream(true);
		this.processBuilder.redirectOutput(Redirect.appendTo(log));
	}
	
	@Override
	public void run() {
		
		// Starting process
		try {
			
			this.process = this.processBuilder.start();
			
		} catch (IOException e) {
			System.out.println("Cannot start the process: "+e);
			return;
		}
		
		// Waiting processes to terminate
		int status = 2;
		
		try {
			
			status = this.process.waitFor();

			if(status != 0){
				
			}
			
			System.out.println("Thread ["+id+"]: Process exit code --> "+status);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("Thread ["+id+"]: Cannot wait for the process: "+e);
		}
		
	}
	
}
