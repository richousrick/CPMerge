/**
 * 
 */
package parse;

import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Rikkey Paal
 *
 * Used for testing
 */
public class DummyPlugin implements PluginInterface{

	/* (non-Javadoc)
	 * @see parse.PluginInterface#getLanguageName()
	 */
	@Override
	public String getLanguageName() {
		return "Java";
	}

	/* (non-Javadoc)
	 * @see parse.PluginInterface#getPluginVersion()
	 */
	@Override
	public String getPluginVersion() {
		// TODO Auto-generated method stub
		return "tmp";
	}

	/* (non-Javadoc)
	 * @see parse.PluginInterface#validfile(java.lang.String)
	 */
	@Override
	public boolean validfile(String filename) {
		// TODO Auto-generated method stub
		return filename.substring(filename.lastIndexOf('.')+1).equalsIgnoreCase("java");
	}

	/* (non-Javadoc)
	 * @see parse.PluginInterface#getPatterns()
	 */
	@Override
	public HashMap<Class<? extends ParserRuleContext>, ResoultionPattern> getPatterns() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see parse.PluginInterface#parse(org.antlr.v4.runtime.CharStream)
	 */
	@Override
	public void parse(CharStream stream) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see parse.PluginInterface#getMethods()
	 */
	@Override
	public ArrayList<ParserRuleContext> getMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see parse.PluginInterface#getParsedCode()
	 */
	@Override
	public ParserRuleContext getParsedCode() {
		// TODO Auto-generated method stub
		return null;
	}

}
