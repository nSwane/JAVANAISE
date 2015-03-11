/**
 * JvnCoordStarter class
 * Starts the coordinator (centralized server)
 *
 */

package cs;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import jvn.implementations.JvnCoordImpl;
import jvn.utils.JvnException;

/**
 * Start the JVN server.
 * 
 * @author ray
 *
 */
public class JvnCoordStarter {

	public static void main(String [] args){
		
		Registry rmiRegistry = null;
		JvnCoordImpl coordinator = null;
		
		String hostName = "localhost";
		int port = 1024;
		String coordName = "COORDINATOR";
		String url = "//"+hostName+":"+port+"/"+coordName;
		
		try {
			
			// Initialize connection parameters
			System.out.println("Creating registry...");
	    	rmiRegistry = LocateRegistry.createRegistry(port);
	    	System.out.println("Registry created.\n");
	    	
	        System.out.println("Initializing coordinator...");
	        coordinator = new JvnCoordImpl();
	        System.out.println("Coordinator initialized.\n");
	        
	        System.out.println("Binding coordinator...");
			Naming.rebind(url, coordinator);
			System.out.println("Coordinator bound.\n");
			
			// Start the JVN service
			coordinator.start();
			
		} catch (RemoteException e) {
			System.out.println(e);
			System.exit(-1);
		} catch (AlreadyBoundException e) {
			System.out.println(e);
			System.exit(-1);
		} catch (Exception e) {
			System.out.println(e);
			System.exit(-1);
		}
		
        System.out.println("Coordinator ready\n");
        
        System.out.println("Enter 's' to stop the server properly.");
        
        try{
        	System.out.println("Wating for command 's' to terminate JVN service...");
            char stop = (char) System.in.read();
            while(stop != 's'){
            	System.out.println("Wating for command 's' to terminate JVN service...");
            	stop = (char) System.in.read();
            }
            
	        coordinator.stop();
			
			// Stop rmi and force close
			UnicastRemoteObject.unexportObject(rmiRegistry, true);
			
        } catch(JvnException e){
        	System.out.println("A problem occured while stopping the JVN service: "+e);
        } catch (NoSuchObjectException e) {
        	System.out.println("A problem occured while stopping the JVN service: "+e);
		} catch (IOException e) {
			System.out.println("A problem occured while stopping the JVN service: "+e);
		}
        			
	}
}
