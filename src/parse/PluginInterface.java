package parse;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;

import dif.ClassNode;
import merge.ClassNodeSkeleton;
import merge.IntermdiateAST;
import merge.MergeGroup;
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

	/**
	 * ran before the {@link MergeGroup#buildMergeFunction()} processes the
	 * unique sets.
	 * This can be used to validate and or tidy up the structure of the unique
	 * set's before processing.
	 *
	 * @param root
	 *            node of the {@link IntermdiateAST}
	 */
	public void preMerge(ClassNodeSkeleton root);

	public void postMerge(ClassNodeSkeleton root);

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
	public String prettyPrint(ClassNodeSkeleton functionStructure);

	public int getClassStartLine(ClassNode classRoot, BufferedReader in);

	public String genFunctionName(ArrayList<ClassNode> functions);

	public String updateFunctionName(ClassNodeSkeleton originalRoot, ClassNodeSkeleton newRoot, int fID, String code);
}