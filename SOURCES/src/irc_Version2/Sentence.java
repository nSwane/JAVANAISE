/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc_Version2;

public class Sentence implements java.io.Serializable, ItfSentence {
	String 		data;
 
	public Sentence() {
		data = new String("");
	}

	public void write(String text) {
		data = text;
	}
	
	public String read() {
		return data;	
	}
	
}