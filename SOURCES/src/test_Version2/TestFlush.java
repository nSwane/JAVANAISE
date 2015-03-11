package test_Version2;

import irc_Version2.ItfSentence;
import irc_Version2.Sentence;
import jvn.implementations.JvnProxy;
import jvn.implementations.JvnServerImpl;
import jvn.utils.JvnException;

public class TestFlush {

	public static void main(String argv[]) {
		ItfSentence s=null;
		
		try {
			// Create a set of shared objects 
			for(int i=0; i<5; i++){
				System.out.println("create object");
				s = (ItfSentence) JvnProxy.newInstance(new Sentence(), "var"+i);
				s.write("ecrit");
			}
			
			// Flush all shared objects
			JvnServerImpl.jvnGetServer().jvnFlush();
			System.out.println("flush");
			
			// Create another set of shared objects
			for(int i=0; i<5; i++){
				System.out.println("create object");
				s = (ItfSentence) JvnProxy.newInstance(new Sentence(), "var2"+i);
				s.write("ecrit");
			}
		
		} catch (JvnException e) {
			e.printStackTrace();
		}
	}
}
