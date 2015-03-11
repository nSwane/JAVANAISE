package irc_Version2;

import jvn.utils.MethodType;

public interface ItfSentence {

	@MethodType(type = "read")
	public String read();

	@MethodType(type = "write")
	public void write(String text);
}
