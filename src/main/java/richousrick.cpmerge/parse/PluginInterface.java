package richousrick.cpmerge.parse;

import costmodel.CostModel;
import distance.APTED;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import richousrick.cpmerge.dif.ASTNode;
import richousrick.cpmerge.merge.*;
import richousrick.cpmerge.ref.FunctionPos;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

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
    String getLanguageName();

    /**
     * @return a unique identifier of the version of the plugin
     */
    String getPluginVersion();

    /**
     * @param stream
     */
    void parse(CharStream stream);

    /**
     * @return a list of {@link ASTNode}'s
     */
    ArrayList<? extends ASTNode<D>> getClasses();

    /**
     * Validates a given file on the system
     *
     * @param filename path to the file
     * @return true if the file is able to be parsed by this plugin
     */
    boolean validfile(String filename);

    ParserRuleContext getParsedCode();

    HashMap<Class<? extends ParserRuleContext>, ResoultionPattern> getPatterns();

    PluginInterface<D> generateInstance();

    ArrayList<ArrayList<Integer>> validateMergeGroup(ArrayList<ArrayList<Integer>> groups,
                                                     FunctionMappings<D> mappings, ArrayList<? extends ASTNode<D>> functions);

    /**
     * ran before the {@link MergeGroup#buildMergeFunction()} processes the
     * unique sets.
     * This can be used to validate and or tidy up the structure of the unique
     * set's before processing.
     *
     * @param root node of the {@link IntermdiateAST}
     */
    void preMerge(ClassNodeSkeleton<D> root);

    void postMerge(ClassNodeSkeleton<D> root);

    /**
     * Gets the position of a function in a file
     *
     * @param funcHead  function to find
     * @param in,       stream to use to find the file
     * @param startPos, the start of the enclosing type in the file
     * @return the position the function appears in the file
     */
    FunctionPos getPositionInFile(ASTNode<D> funcHead, BufferedReader in, int startPos);

    /**
     * Pretty print the {@link MergedFunction} into code, that will be inserted into
     * the file
     *
     * @param root to convert to code
     * @return a textual representation of the AST inside the {@link MergedFunction}
     */
    String prettyPrint(MergedFunction<D> root);

    int getClassStartLine(ASTNode<D> classRoot, BufferedReader in);

    String genFunctionName(ArrayList<ASTNode<D>> functions);

    void startWriting();

    String insertFunction(MergedFunction<D> function, String fileContent);

    String removeFunctions(MergedFunction<D> function, String fileContent);

    String updateReferences(String currFileRep, ArrayList<MergedFunction<D>> mergedFunctios);

    /**
     * TODO Annotate method
     *
     * @param currFileRep
     * @param updatedFunctions
     * @return
     */
    String initUpdateReferences(String currFileRep, ArrayList<MergedFunction<D>> updatedFunctions);

    /**
     * TODO Annotate method
     *
     * @param currFileRep
     * @param updatedFunctions
     * @return
     */
    String destroyUpdateReferences(String currFileRep, ArrayList<MergedFunction<D>> updatedFunctions);

    void setMergeThread(MergeThread<D> thread);

    ASTNode<?> copyNode(ASTNode<?> node, boolean copyChildren);

    APTED<? extends CostModel<D>, D> getApted();

    /**
     * TODO Annotate method
     *
     * @param c
     * @param currFileRep
     * @return
     */
    String updateFunctionBodies(MergedFunction<D> c, String currFileRep);

    /**
     * TODO Annotate method
     *
     * @param currFileRep
     * @return
     */
    String postProcessFile(String currFileRep);
}