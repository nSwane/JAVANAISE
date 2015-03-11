package jvn.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import jvn.implementations.Store;

/** The data manager is used to save the store state. It is designed to save each store entry into file system.
 * 
 * @author Ray
 *
 */
public class DataManager {

	private static String projectRoot = System.getProperty("user.dir");
	
	// The recovery file is used to save the store in case of problem from the coordinator.
	private static String recoveryDirectoryName = "recovery";
	private static String recoveryDirectory = projectRoot+"/"+recoveryDirectoryName;
	
	private File recoveryFile;
	private String recoveryFileName;
	
	// The configuration file is used to configure the coordinator start up.
	private static Properties p;
	private static String configFileName = "config.txt";
	
	public DataManager(int storeId) throws IOException{
		
		// Initialize the recovery file
		this.recoveryFileName = "recovery["+storeId+"].ser";
		File dir = new File(DataManager.recoveryDirectory);
		if(!dir.exists()){
			dir.mkdir();
		}
		
		this.recoveryFile = new File(DataManager.recoveryDirectory+"/"+recoveryFileName);
		
	}
	
	/**
	 * Write the store entry into file system. Method startManager() must be called before.
	 * @param store
	 * @throws IOException
	 */
	public void storeEntry(StoreEntry entry) throws IOException{
		
		FileOutputStream recoveryOutput = new FileOutputStream(this.recoveryFile);
		ObjectOutputStream recoveryOut = new ObjectOutputStream(recoveryOutput);
		recoveryOut.writeObject(entry);
		recoveryOutput.close();
		recoveryOut.close();
	}
	
	/**
	 * Read configuration parameters
	 * @throws IOException
	 */
	public static void initConfig() throws IOException{
		p = new Properties();
		
		File fConfig = new File(projectRoot+"/"+DataManager.configFileName);
		
		// Create the config file not exists
		if(!fConfig.exists()){
			if(!fConfig.createNewFile()){
				throw new IOException("Cannot create the config file");
			}
			
			// Initialize parameters
			FileWriter out = new FileWriter(fConfig);
			out.write("recoveryMode=0");
			out.close();
		}
		
		FileInputStream configFileInput = new FileInputStream(fConfig);
		p.load(configFileInput);
		configFileInput.close();
	}
	
	/**
	 * Initialize necessaries to save the store. This method must be called after the store initialization.
	 * @throws IOException
	 */
	public static void startManager() throws IOException{
		
		// delete all previous recovery files
		File folder = new File(DataManager.recoveryDirectory);
		File [] list = folder.listFiles();
		
		if(list != null){
			for(File f: list){
				f.delete();
			}
		}
		
		// The value will pass to 0 if the coordinator has been shut down properly.
		// If it is not the case, the user will be able to load the store from file system.
		p.setProperty("recoveryMode", "1");
		
		// Activate guardian.
		FileOutputStream configFileOutput = new FileOutputStream(projectRoot+"/"+DataManager.configFileName);
		p.store(configFileOutput, null);
		configFileOutput.close();
		
	}
	
	public static void stopManager() throws IOException{
		
		p.setProperty("recoveryMode", "0");
		
		// Release guardian.
		FileOutputStream configFileOutput = new FileOutputStream(projectRoot+"/"+DataManager.configFileName);
		p.store(configFileOutput, null);
		configFileOutput.close();
	}
	
	/**
	 * Return true if the coordinator should be started in recovery mode i.e by loading the store or not.
	 * @return
	 * @throws IOException 
	 */
	public static boolean recoveryMode() throws IOException{
		String mode = p.getProperty("recoveryMode");
		
		if(mode.compareTo("1") == 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Load the store entries from file system.
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Store loadEntries() throws IOException, ClassNotFoundException{
		Store store = new Store();
		File folder = new File(DataManager.recoveryDirectory);
		File [] list = folder.listFiles();
		
		for(File f: list){
			Log.display("Load "+f.getName(), "appels");
			
			FileInputStream fIn = new FileInputStream(f);
			ObjectInputStream in = new ObjectInputStream(fIn);
			StoreEntry storeEntry = (StoreEntry) in.readObject();
			
			// The lock on the store entry is automatically setted after readbject() call.
			
			// Signal all remote servers that the coordinator is back.
			storeEntry.broadcast();
			
			Log.display("Id: "+storeEntry.getObjectId(), "appels");
			
			store.setEntry(storeEntry.getObjectId(), storeEntry);
			
			// Release access to the store entry.
			storeEntry.release();
			
			in.close();
			fIn.close();
		}
		
		return store;
		
	}
}
