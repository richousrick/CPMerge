/**
 * 
 */
package merge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

	private PluginInterface plugin;
	private File f;
	private String ThreadName;
	private int minMatch;
	private double minPer;
	private double minPerDelta;
	private APTED<APTEDCostModel, ParserRuleContext> apted;
	private int totalComparisons = 0;
	private int comparisonsDone = 0;
	private double perShown = 0;

	/**
	 * 
	 */
	public MergeThread(File f, PluginInterface pluginInterface, int minMatch, double minPer, double minPerDelta) {
		this.plugin = pluginInterface;
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
		Helper.printToSTD("Reading file", f.getName());
		CharStream contents;
		try {
			contents = Helper.readFile(f);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Helper.printToSTD("Read file, Parsing contents", f.getName());

		plugin.parse(contents);
		Helper.printToSTD("File Parsed, Extracting methods", f.getName());
		ArrayList<ClassNode> classMethods = plugin.getClasses();

		if (Helper.test) {
			for (ClassNode methods : classMethods) {
				String functions = "";
				for (ClassNode n : methods.getChildrenAsCN()) {
					functions += n.getIdentifier() + ",";
				}
				Helper.printToSTD("Found class \"" + methods.getIdentifier() + "\" containing methods "
						+ functions.substring(0, functions.length() - 1));
			}
		}

		Helper.printToSTD("Building costModel", f.getName());
		// HashMap<Class<? extends ParserRuleContext>, ResoultionPattern> rules
		// = plugin.getPatterns();
		apted = new APTED<APTEDCostModel, ParserRuleContext>(new APTEDCostModel());

		Helper.printToSTD("Methods Extracted, Searching for matches", f.getName());
		int numMethods = 0;
		for (ClassNode classNode : classMethods) {
			int size = classNode.getChildrenAsCN().size();
			numMethods += size;
			totalComparisons += (size * (size - 1)) / 2;
		}
		System.out.println(totalComparisons);
		for (ClassNode classNode : classMethods) {
			processClass(classNode);
		}
	}

	/**
	 * Compares all the methods in the class with one another, merging them into
	 * one method if possible. TODO modify implementation to group methods into
	 * larger than binary pairs.
	 * 
	 * @param classNode
	 *            containing the class to be processed
	 */
	private void processClass(ClassNode classNode) {
		ArrayList<ArrayList<List<int[]>>> comps = new ArrayList<>();
		ArrayList<ClassNode> methods = classNode.getChildrenAsCN();
		if (Helper.verbose) {
			Helper.printToSTD("Comparing methods from " + classNode.getIdentifier(), "\n" + f.getName());
		}
		for (int i = 0; i < methods.size() - 1; i++) {
			comps.add(new ArrayList<>());
			for (int j = i + 1; j < methods.size(); j++) {
				List<int[]> comp = compareMethods(methods.get(i), methods.get(j));
				if (comp != null) {
					comps.get(i).add(comp);
				}
				comparisonsDone++;
				double perDone = ((double) comparisonsDone) / ((double) totalComparisons);
				if (perDone > perShown + 0.01) {
					perShown = Math.floor(perDone * 100) / 100;
					System.out.printf("%.2f%%\n", perShown);
				}
			}
		}

	}

	/**
	 * Compares two classNodes. This returns a mapping between the two methods.
	 * Or null if they are not sufficiently similar. The mapping [a,b] states
	 * that the a'th node in the smaller method is the same as the b'th node in
	 * the larger. The positioning uses post order staring at 1. If a node is
	 * mapped with 0 as a pair then it is unique to that method.
	 * 
	 * @param method1
	 *            first method to compare
	 * @param method2
	 *            second method to be compared
	 * @return the difference mapping between the two nodes, Or null if they are
	 *         too different.
	 */
	private List<int[]> compareMethods(ClassNode method1, ClassNode method2) {
		int s1 = method1.getSize();
		int s2 = method2.getSize();
		// if one method is too large for minPer to be reached don't compute
		if (s1 < s2 * minPer || s2 < s1 * minPer) {
			return null;
		}
		float editDistance;
		if (s1 > s2) {
			editDistance = apted.computeEditDistance(method2, method1);
		} else {
			editDistance = apted.computeEditDistance(method1, method2);
		}

		int maxSize = Math.max(method1.getSize(), method2.getSize());
		float diffValue = (maxSize - editDistance) / maxSize;
		if (diffValue < minPer) {
			return null;
		}
		Helper.printToSTD(method1.getIdentifier() + " " + method2.getIdentifier() + ": " + diffValue + "%",
				"\t" + f.getName());
		LinkedList<int[]> list = apted.computeEditMapping();
		Helper.printToSTD("\t" + Arrays.deepToString(list.toArray()));
		if (s1 > s2) {
			Helper.printToSTD(method2.print("\t", true));
			Helper.printToSTD(method1.print("\t", true));
			// Helper.printToSTD(mergeMethods(method2, method1,
			// list).print("\t", true));
			mergeMethods(method2, method1, list);
		} else {
			Helper.printToSTD(method1.print("\t", true));
			Helper.printToSTD(method2.print("\t", true));
			// Helper.printToSTD(mergeMethods(method1, method2,
			// list).print("\t", true));
			mergeMethods(method1, method2, list);
		}

		return list;
	}

	/**
	 * Merges two methods into one using the specified mapping.
	 * 
	 * TODO complete implementation
	 * 
	 * @param method1
	 *            the smaller method to be mapped
	 * @param method2
	 *            the larger method to be mapped
	 * @param mapping
	 *            between the two methods
	 * @return a new {@link ClassNode} that behaves the same as
	 */
	private ClassNode mergeMethods(ClassNode method1, ClassNode method2, List<int[]> mapping) {
		ClassNode shared = new ClassNode(method1);
		ArrayList<ClassNode>[] mergeCandidates = getMergeCandidates(shared, method2, mapping);
		ArrayList<ClassNode> candidatesFrom1 = mergeCandidates[0];
		ArrayList<ClassNode> candidatesFrom2 = mergeCandidates[1];

		Helper.printToSTD("\tShared Code");
		Helper.printToSTD(shared.print("\t\t", true));
		Helper.printToSTD("\tcandidates from 1");
		for (ClassNode c : candidatesFrom1) {
			Helper.printToSTD(c.print("\t\t", true));
		}
		Helper.printToSTD("\tcandidates from 2");

		for (ClassNode c : candidatesFrom2) {
			Helper.printToSTD(c.print("\t\t", true));
		}

		return null;
	}

	/**
	 * Retrieves the root nodes of subtrees that are unique to each method.
	 * method 1 is also converted into a tree containing the shared nodes.
	 * 
	 * @param method1
	 *            the smaller method to get the merge candidates out of. Also
	 *            all unique nodes will be removed.
	 * @param method2
	 *            the larger method to get the merge candidates from.
	 * @param mapping
	 *            between the two
	 * @return an array containing the roots of the unique subtrees.
	 */
	private ArrayList<ClassNode>[] getMergeCandidates(ClassNode method1, ClassNode method2, List<int[]> mapping) {
		HashSet<Integer> set1 = new HashSet<>();
		HashSet<Integer> set2 = new HashSet<>();
		ArrayList<ClassNode>[] mergeCandidates = new ArrayList[2];
		for (int[] map : mapping) {
			if (map[0] == 0) {
				set2.add(map[1]);
			} else if (map[1] == 0) {
				set1.add(map[0]);
			}
		}
		mergeCandidates[0] = method1.getMinimalNodesFromPostOrder(set1, true);
		mergeCandidates[1] = method2.getMinimalNodesFromPostOrder(set2, false);

		return mergeCandidates;
	}

}
