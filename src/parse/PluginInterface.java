package parse;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;

import dif.ClassNode;
import ref.FunctionPos;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public interface PluginInterface {

	/**
	 * Used to identify the language used by the plugin. e.g. Java, Python
	 *
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
	 * @return a list of {@link ClassNode}'s
	 */
	public ArrayList<ClassNode> getClasses();

	/**
	 * Validates a given file on the system
	 *
	 * @param filename
	 *            path to the file
	 * @return true if the file is able to be parsed by this plugin
	 */
	public boolean validfile(String filename);

	public ParserRuleContext getParsedCode();

	public HashMap<Class<? extends ParserRuleContext>, ResoultionPattern> getPatterns();

	public PluginInterface generateInstance();

	public void preMerge();

	public void postMerge();

	/**
	 * Gets the position of a function in a file
	 *
	 * @param funcHead
	 *            function to find
	 * @param in,
	 *            stream to use to find the file
	 *
	 * @param startPos,
	 *            the start of the enclosing type in the file
	 * @return the position the function appears in the file
	 */
	public FunctionPos getPositionInFile(ClassNode funcHead, BufferedReader in, int startPos);

	/**
	 * Pretty print the {@link ClassNode} into code, that will be inserted into
	 * the file
	 *
	 * @param classNode
	 *            to convert to code
	 * @return a textual representation of the AST inside the {@link ClassNode}
	 */
	public String prettyPrint(ClassNode c);

	public int getClassStartLine(ClassNode classRoot, BufferedReader in);

}
