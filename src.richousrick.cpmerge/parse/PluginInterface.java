package parse;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;

import costmodel.CostModel;
import dif.ASTNode;
import distance.APTED;
import merge.ClassNodeSkeleton;
import merge.FunctionMappings;
import merge.IntermdiateAST;
import merge.MergeGroup;
import merge.MergeThread;
import merge.MergedFunction;
import ref.FunctionPos;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public interface PluginInterface<D> {

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
	 * @return a list of {@link ASTNode}'s
	 */
	public ArrayList<? extends ASTNode<D>> getClasses();

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

	public PluginInterface<D> generateInstance();

	public ArrayList<ArrayList<Integer>> validateMergeGroup(ArrayList<ArrayList<Integer>> groups,
			FunctionMappings<D> mappings, ArrayList<? extends ASTNode<D>> functions);

	/**
	 * ran before the {@link MergeGroup#buildMergeFunction()} processes the
	 * unique sets.
	 * This can be used to validate and or tidy up the structure of the unique
	 * set's before processing.
	 *
	 * @param root
	 *            node of the {@link IntermdiateAST}
	 */
	public void preMerge(ClassNodeSkeleton<D> root);

	public void postMerge(ClassNodeSkeleton<D> root);

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
	public FunctionPos getPositionInFile(ASTNode<D> funcHead, BufferedReader in, int startPos);

	/**
	 * Pretty print the {@link MergedFunction} into code, that will be inserted into
	 * the file
	 *
	 * @param root
	 *            to convert to code
	 * @return a textual representation of the AST inside the {@link MergedFunction}
	 */
	public String prettyPrint(MergedFunction<D> root);

	public int getClassStartLine(ASTNode<D> classRoot, BufferedReader in);

	public String genFunctionName(ArrayList<ASTNode<D>> functions);

	public void startWriting();

	public String insertFunction(MergedFunction<D> function, String fileContent);

	public String removeFunctions(MergedFunction<D> function, String fileContent);

	public String updateReferences(String currFileRep, ArrayList<MergedFunction<D>> mergedFunctios);

	/**
	 * TODO Annotate method
	 *
	 * @param currFileRep
	 * @param updatedFunctions
	 * @return
	 */
	public String initUpdateReferences(String currFileRep, ArrayList<MergedFunction<D>> updatedFunctions);

	/**
	 * TODO Annotate method
	 *
	 * @param currFileRep
	 * @param updatedFunctions
	 * @return
	 */
	public String destroyUpdateReferences(String currFileRep, ArrayList<MergedFunction<D>> updatedFunctions);

	public void setMergeThread(MergeThread<D> thread);

	public ASTNode<?> copyNode(ASTNode<?> node, boolean copyChildren);

	public APTED<? extends CostModel<D>, D> getApted();

	/**
	 * TODO Annotate method
	 *
	 * @param c
	 * @param currFileRep
	 * @return
	 */
	public String updateFunctionBodies(MergedFunction<D> c, String currFileRep);

	/**
	 * TODO Annotate method
	 *
	 * @param currFileRep
	 * @return
	 */
	public String postProcessFile(String currFileRep);
}