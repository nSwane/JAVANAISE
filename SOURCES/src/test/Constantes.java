package test;

/**
 * Contains necessaries to run java application (classpath...).
 * @author nawaouis
 *
 */
public class Constantes {

	public String separator;
	public String classpath;
	public String javaBin;
	
	public Constantes(){
		separator = System.getProperty("file.separator");
		classpath = System.getProperty("java.class.path");
		javaBin = System.getProperty("java.home") + separator + "bin" + separator;
	}
	
}
