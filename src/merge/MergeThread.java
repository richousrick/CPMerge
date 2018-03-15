/**
 *
 */
package merge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;

import dif.APTEDCostModel;
import dif.ClassNode;
import distance.APTED;
import parse.PluginInterface;
import ref.Helper;

/**
 * @author Rikkey Paal
 *
 */
public class MergeThread implements Runnable {


	private final PluginInterface plugin;
	private final File f;
	private String ThreadName;
	private final int minMatch;
	private final double minPer;
	private final double minPerDelta;
	private APTED<APTEDCostModel, ParserRuleContext> apted;
	private int totalComparisons = 0;
	private int comparisonsDone = 0;

	private double perShown = 0;

	String testPrintEnd = "";
	/**
	 *
	 */
	public MergeThread(File f, PluginInterface pluginInterface, int minMatch, double minPer, double minPerDelta) {
		plugin = pluginInterface;
		this.minMatch = minMatch;
		this.minPer = minPer;
		this.minPerDelta = minPerDelta;
		this.f = f;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Read methods from the file
		ArrayList<ClassNode> classMethods;
		try {
			classMethods = parseFile();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// Print the class names and methods
		if (Helper.test) {
			for (ClassNode functions : classMethods) {
				String functionsString = "";
				for (ClassNode n : functions.getChildrenAsCN()) {
					functionsString += n.getIdentifier() + ",";
				}
				if(functionsString.length()>1){
					functionsString = functionsString.substring(0, functionsString.length() - 1);
				}
				Helper.printToSTD("Found class \"" + functions.getIdentifier() + "\" containing functions "
						+ functionsString);
			}
		}


		Helper.printToSTD("Building costModel", f.getName());
		apted = new APTED<APTEDCostModel, ParserRuleContext>(new APTEDCostModel());

		Helper.printToSTD("Methods Extracted, Searching for matches", f.getName());
		// Count number of comparisons to be done - used to estimate percentage
		// complete
		for (ClassNode classNode : classMethods) {
			int size = classNode.getChildrenAsCN().size();
			totalComparisons += size * (size - 1) / 2;
		}
		// process each class sequentially
		System.out.println(totalComparisons);
		for (ClassNode classNode : classMethods) {
			processClass(classNode);
		}
		System.out.println(testPrintEnd);
	}

	/**
	 * Reads in the contents of the file associated with the thread. This file
	 * is then {@link PluginInterface#parse(CharStream) parsed}, and the
	 * {@link PluginInterface#getClasses() classes} are returned.
	 *
	 * @return the classes in the file
	 * @throws IOException
	 */
	private ArrayList<ClassNode> parseFile() throws IOException {
		Helper.printToSTD("Reading file", f.getName());
		CharStream contents;
		contents = Helper.readFile(f);

		Helper.printToSTD("Read file, Parsing contents", f.getName());
		plugin.parse(contents);

		Helper.printToSTD("File Parsed, Extracting functions", f.getName());
		return plugin.getClasses();
	}

	/**
	 * Compares all the functions in the class with one another, merging them
	 * into one function if possible. TODO modify implementation to group
	 * functions into larger than binary pairs.
	 *
	 * @param classNode
	 *            containing the class to be processed
	 */
	private void processClass(ClassNode classNode) {
		testPrintEnd += classNode.getIdentifier() + "\n";
		// Generate a list of mappings between similar methods
		ArrayList<ClassNode> functions = classNode.getChildrenAsCN();
		if (Helper.verbose) {
			Helper.printToSTD("Comparing functions from " + classNode.getIdentifier(), "\n" + f.getName());
		}

		FunctionMappings comps = compareAllMethods(functions);
		testPrintEnd += "\tMatches: " + comps.toString() + "\n";

		// group mappings
		ArrayList<MergeGroup> mergeGroups = generateMergeGroups(comps, functions);
		testPrintEnd += "\tGroups:\n";
		for (MergeGroup mg : mergeGroups) {
			testPrintEnd += "\t\t" + mg.getMethodHeaders() + "\n";
			testPrintEnd += "\t" + mg.printSharedCode().replaceAll("\n", "\n\t") + "\n";

			System.out.println(mg.getMethodHeaders());
			ClassNodeSkeleton root = mg.buildMergeFunction();
			System.out.println(plugin.prettyPrint(root));

			// add code to files
			// log method name changes

		}
		testPrintEnd += "\t";




	}

	/**
	 * Compares all pairs of methods in a class, and will return the mapping of
	 * all pairs that are sufficiently similar
	 *
	 * @param functions
	 *            A list of the functions to compare
	 * @return mapping of the pairs of methods that are sufficiently similar
	 */
	private FunctionMappings compareAllMethods(ArrayList<ClassNode> functions) {
		FunctionMappings mappings = new FunctionMappings(functions);
		// Iterate over every pair of functions in the class
		for (int i = 0; i < functions.size() - 1; i++) {
			for (int j = i + 1; j < functions.size(); j++) {
				// compare the methods, if they are sufficiently similar store
				// thier mapping
				List<int[]> comp = compareMethods(functions.get(i), functions.get(j), false);
				if (comp != null) {
					mappings.addRelations(i, j, comp);
				}
				// Check how many comparisons have been done
				// print current percentage at intervals of 1%, when reached
				comparisonsDone++;
				double perDone = (double) comparisonsDone / (double) totalComparisons;
				if (perDone > perShown + 0.01) {
					perShown = Math.floor(perDone * 100) / 100;
					System.out.printf("%.2f%%\n", perShown);
				}
			}
		}
		return mappings;
	}

	/**
	 * Compares two classNodes. This returns a mapping between the two
	 * functions. Or null if they are not sufficiently similar. The mapping
	 * [a,b] states that the a'th node in the smaller function is the same as
	 * the b'th node in the larger. The positioning uses post order staring at
	 * 1. If a node is mapped with 0 as a pair then it is unique to that
	 * function.
	 *
	 * @param function1
	 *            first function to compare
	 * @param function2
	 *            second function to be compared
	 *
	 * @param forceComputeMapping
	 *            if true, will not skip computing edit mapping
	 * @return the difference mapping between the two nodes, Or null if they are
	 *         too different.
	 */
	private List<int[]> compareMethods(ClassNode function1, ClassNode function2, boolean forceComputeMapping) {
		// check required characteristics match
		if (!function1.compareMustMatch(function2))
			return null;
		int s1 = function1.getSize();
		int s2 = function2.getSize();

		// If one function is too large for minPer to be reached don't compute
		if (s1 < s2 * minPer || s2 < s1 * minPer)
			return null;

		// Get the distance of the smaller function to the larger function
		float editDistance;
		if (s1 > s2) {
			editDistance = apted.computeEditDistance(function2, function1);
		} else {
			editDistance = apted.computeEditDistance(function1, function2);
		}

		// If the distance is not a large enough percentage of the larger method
		// and it is not being forced, don't compute the mapping
		int maxSize = Math.max(function1.getSize(), function2.getSize());
		float diffValue = (maxSize - editDistance) / maxSize;
		if (!forceComputeMapping && diffValue < minPer)
			return null;

		// Compute and return the mapping
		Helper.printToSTD(function1.getIdentifier() + " " + function2.getIdentifier() + ": " + diffValue + "%",
				"\t" + f.getName());


		LinkedList<int[]> list;
		if (s1 > s2) {
			list = flipList(apted.computeEditMapping());
		} else {
			list = apted.computeEditMapping();
		}
		return list;
	}

	private LinkedList<int[]> flipList(List<int[]> list){
		LinkedList<int[]> retList = new LinkedList();
		for(int[] i :list){
			int[] li = new int[2];
			li[0] = i[1];
			li[1] = i[0];
			retList.add(li);
		}
		return retList;
	}




	/**
	 * Identifies groups of methods that are similar and converts them into
	 * merge groups.
	 *
	 *
	 * @param mappings
	 *            comparisons of methods, 2D hashmap, such that x,y refers the
	 *            mapping between method at pos x and method at pos y in
	 *            function methods
	 * @param functions
	 *            parsed functions that have their relationship mapped in comps.
	 * @return
	 */
	private ArrayList<MergeGroup> generateMergeGroups(FunctionMappings mappings,
			ArrayList<ClassNode> functions) {

		ArrayList<MergeGroup> mergeGroups = new ArrayList<>();
		ArrayList<ArrayList<Integer>> groups = mappings.groupFunctions();

		for (ArrayList<Integer> gi : groups) {
			mergeGroups.add(buildMergeGroup(gi, mappings));
		}
		return mergeGroups;
	}

	private MergeGroup buildMergeGroup(ArrayList<Integer> groupPos, FunctionMappings mappings) {
		ArrayList<List<int[]>> relations = new ArrayList<>();
		for (int j = 0; j < groupPos.size(); j++) {
			for (int k = j + 1; k < groupPos.size(); k++) {
				List<int[]> mapping = mappings.getMapping(groupPos.get(j), groupPos.get(k));
				if (mapping == null) {
					mapping = compareMethods(mappings.getFunctionNode(j), mappings.getFunctionNode(k), true);
					mappings.addRelations(j, k, mapping);
				}
				relations.add(mapping);
			}
		}
		ArrayList<ClassNode> classes = new ArrayList<>();
		for (int i : groupPos) {
			classes.add(mappings.getFunctionNode(i));
		}

		return new MergeGroup(classes, relations, plugin);
	}

	private void writeMergedFunction(String function) {

	}

}
