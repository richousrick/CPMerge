/**
 * 
 */
package merge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
		for (ClassNode classNode : classMethods) {
			processClass(classNode);
		}

	}

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
			}
		}

	}

	/**
	 * TODO Annotate method
	 * 
	 * @param parserRuleContext
	 * @param parserRuleContext2
	 * @return
	 */
	private List<int[]> compareMethods(ClassNode method1, ClassNode method2) {
		// optimise
		float editDistance = apted.computeEditDistance(method1, method2);
		int maxSize = Math.max(method1.getSize(), method2.getSize());
		float diffValue = (maxSize - editDistance) / maxSize;
		if (diffValue < minPer) {
			return null;
		}
		Helper.printToSTD(method1.getIdentifier() + " " + method2.getIdentifier() + ": " + diffValue + "%",
				"\t" + f.getName());
		LinkedList<int[]> list = apted.computeEditMapping();
		Helper.printToSTD("\t" + Arrays.deepToString(list.toArray()));
		Helper.printToSTD(method1.print("\t", true));
		Helper.printToSTD(method2.print("\t", true));
		return list;
	}
}
