package parse;

import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public interface PluginInterface {

	/**
	 * Used to identify the language used by the plugin.
	 * e.g. Java, Python
	 * @return the language the plugin adds support for
	 */
	public String getLanguageName();
	
	/**
	 * 
	 * @return a unique identifier of the version of the plugin
	 */
	public String getPluginVersion();
	
	/**
	 * 
	 * @param stream
	 */
	public void parse(CharStream stream);
	
	
	/**
	 * @return a list of methods read in
	 */
	public ArrayList<ArrayList<ParserRuleContext>> getMethods();
	
	
	/**
	 * Validates a given file on the system
	 * @param filename path to the file
	 * @return true if the file is able to be parsed by this plugin
	 */
	public boolean validfile(String filename);
	

	public ParserRuleContext getParsedCode();
	
	public HashMap<Class<? extends ParserRuleContext>, ResoultionPattern> getPatterns();
	
	public PluginInterface generateInstance();
	
}
