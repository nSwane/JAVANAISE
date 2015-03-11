/**
 * Log class
 * Allow to display log if it's activated
 * @author Marc
 *
 */

package jvn.utils;

import java.util.ArrayList;
import java.util.Arrays;

public class Log {
	
	public static boolean activated = false;
	public static ArrayList<String> types;
	
	/**
	 * Activate the logs which are of the type t. It can be a set of types.
	 * @param t the types of message to activate. Enter "all" to activate all logs.
	 */
	public static void activate(String... t){
		activated = true;
		types = new ArrayList<String>(Arrays.asList(t));
	}
	
	/**
	 * Display the message of type t if this type is activated.
	 * @param msg the message to display
	 * @param t the message's type
	 */
	public static void display(String msg, String t){
		if(activated){
			if( types.contains("all") || types.contains(t) ){
				System.out.println("log ["+t+"]: "+msg);
			}
		}
	}
}
