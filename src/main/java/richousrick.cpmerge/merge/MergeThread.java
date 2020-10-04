package richousrick.cpmerge.merge;

import costmodel.CostModel;
import distance.APTED;
import org.antlr.v4.runtime.CharStream;
import richousrick.cpmerge.dif.ASTNode;
import richousrick.cpmerge.parse.PluginInterface;
import richousrick.cpmerge.ref.Helper;
import richousrick.cpmerge.ref.Language;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Rikkey Paal
 *
 */
public class MergeThread<D> implements Runnable {


	private final PluginInterface<D> plugin;
	private final File f;
	private final double minPer;
	private final Language<D> lang;
	private APTED<? extends CostModel<D>, D> apted;
	private int totalComparisons = 0;
	private int comparisonsDone = 0;
	private ArrayList<? extends ASTNode<D>> classMethods;

	private double perShown = 0;

	private String currFileRep = null;
	/**
	 *
	 */
	public MergeThread(File f, PluginInterface<D> pluginInterface, double minPer,
			Language<D> lang) {
		plugin = pluginInterface;
		pluginInterface.setMergeThread(this);
		this.minPer = minPer;
		this.lang = lang;
		this.f = f;
		try {
			currFileRep = new String(Files.readAllBytes(f.toPath()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Read methods from the file

		try {
			classMethods = parseFile();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// Print the class names and methods
		if (Helper.test) {
			for (ASTNode<?> functions : classMethods) {
				String functionsString = "";
				for (ASTNode<?> n : functions.getChildrenAsASTNode()) {
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
		apted = plugin.getApted();

		Helper.printToSTD("Methods Extracted, Searching for matches", f.getName());
		// Count number of comparisons to be done - used to estimate percentage
		// complete
		for (ASTNode<D> classNode : classMethods) {
			int size = classNode.getChildrenAsASTNode().size();
			totalComparisons += size * (size - 1) / 2;
		}

		// process each class sequentially
		for (ASTNode<D> classNode : classMethods) {
			processClass(classNode);
		}

		currFileRep = plugin.postProcessFile(currFileRep);

		try {
			Helper.addSourceFIle(f.getPath(), currFileRep);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Reads in the contents of the file associated with the thread. This file
	 * is then {@link PluginInterface#parse(CharStream) parsed}, and the
	 * {@link PluginInterface#getClasses() classes} are returned.
	 *
	 * @return the classes in the file
	 * @throws IOException
	 */
	private ArrayList<? extends ASTNode<D>> parseFile() throws IOException {
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
	private void processClass(ASTNode<D> classNode) {

		// Generate a list of mappings between similar methods
		ArrayList<? extends ASTNode<D>> functions = classNode.getChildrenAsASTNode();
		if (Helper.verbose) {
			Helper.printToSTD("Comparing functions from " + classNode.getIdentifier(), "\n" + f.getName());
		}

		FunctionMappings<D> comps = compareAllMethods(functions);

		// group mappings
		ArrayList<MergeGroup> mergeGroups = generateMergeGroups(comps, functions);
		for (MergeGroup mg : mergeGroups) {

			@SuppressWarnings("unchecked")
			MergedFunction<D> root = mg.buildMergeFunction();

			// add code to files
			currFileRep = plugin.insertFunction(root, currFileRep);
			// log method name changes

			try {
				Helper.addUpdatedFunction(root);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			if (Helper.deleteOldFunctions) {
				currFileRep = plugin.removeFunctions(root, currFileRep);
			} else {
				currFileRep = plugin.updateFunctionBodies(root, currFileRep);
			}

		}



	}

	/**
	 * Compares all pairs of methods in a class, and will return the mapping of
	 * all pairs that are sufficiently similar
	 *
	 * @param functions
	 *            A list of the functions to compare
	 * @return mapping of the pairs of methods that are sufficiently similar
	 */
	private FunctionMappings<D> compareAllMethods(ArrayList<? extends ASTNode<D>> functions) {
		FunctionMappings<D> mappings = new FunctionMappings<D>(functions);
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
	private List<int[]> compareMethods(ASTNode<D> function1, ASTNode<D> function2, boolean forceComputeMapping) {
		// check required characteristics match
		if (!function1.compareCharactersitics(function2))
			return null;
		int s1 = function1.getSize();
		int s2 = function2.getSize();

		// If one function is too large for minPer to be reached don't compute
		if (s1 < s2 * minPer || s2 < s1 * minPer) {
			System.out.println(s1 + "<" + s2 * minPer + " or " + s2 + "<" + s1 * minPer);
			return null;
		}
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
		LinkedList<int[]> retList = new LinkedList<int[]>();
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
	 * @param functions
	 *            parsed functions that have their relationship mapped in comps.
	 * @return
	 */
	private ArrayList<MergeGroup> generateMergeGroups(FunctionMappings<D> mappings,
			ArrayList<? extends ASTNode<D>> functions) {

		ArrayList<MergeGroup> mergeGroups = new ArrayList<>();
		ArrayList<ArrayList<Integer>> groups = mappings.groupFunctions();
		groups = plugin.validateMergeGroup(groups, mappings, functions);
		for (int i = 0; i < groups.size(); i++) {
			mergeGroups.add(buildMergeGroup(groups.get(i), mappings, i));
		}
		return mergeGroups;
	}

	private MergeGroup buildMergeGroup(ArrayList<Integer> groupPos, FunctionMappings<D> mappings, int pos) {
		ArrayList<List<int[]>> relations = new ArrayList<>();
		for (int j = 0; j < groupPos.size(); j++) {
			for (int k = j + 1; k < groupPos.size(); k++) {
				// Try to get mapping if already calculated
				List<int[]> mapping = mappings.getMapping(groupPos.get(j), groupPos.get(k));
				// if mapping has not already been calculate, calculate it
				if (mapping == null) {
					mapping = compareMethods(mappings.getFunctionNode(groupPos.get(j)),
							mappings.getFunctionNode(groupPos.get(k)), true);
					mappings.addRelations(groupPos.get(j), groupPos.get(k), mapping);
				}
				if (mapping != null) {
					relations.add(mapping);
				} else
					throw new InvalidParameterException("trying to merge methods that are not similar");
			}
		}
		ArrayList<ASTNode<?>> classes = new ArrayList<>();
		for (int i : groupPos) {
			classes.add(mappings.getFunctionNode(i));
		}

		return new MergeGroup(classes, relations, plugin, pos);
	}

	public void updateReferences() {
		currFileRep = plugin.updateReferences(currFileRep, lang.getUpdatedFunctions());
		try {
			Helper.addSourceFIle(f.getPath(), currFileRep);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void initUpdateReferences() {
		currFileRep = plugin.initUpdateReferences(currFileRep, lang.getUpdatedFunctions());
		try {
			Helper.addSourceFIle(f.getPath(), currFileRep);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * TODO Annotate method
	 */
	public void postUpdateReferences() {
		currFileRep = plugin.destroyUpdateReferences(currFileRep, lang.getUpdatedFunctions());
		try {
			Helper.addSourceFIle(f.getPath(), currFileRep);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public File getFile() {
		return f;
	}
}
