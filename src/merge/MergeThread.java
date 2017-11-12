/**
 * 
 */
package merge;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ParserRuleContext;

import com.sun.javafx.scene.paint.GradientUtils.Parser;
import com.sun.prism.impl.VertexBuffer;

import dif.APTEDCostModel;
import dif.ParserRuleContextNode;
import distance.APTED;
import parse.PluginInterface;
import parse.ResoultionPattern;
import parse.parser.java.comp.JavaParser.ClassDeclarationContext;
import parse.parser.java.comp.JavaParser.MethodDeclarationContext;
import ref.Helper;

/**
 * @author Rikkey Paal
 *
 */
public class MergeThread implements Runnable{
	
	private PluginInterface plugin;
	private File f;
	private String ThreadName;
	private int minMatch;
	private double minPer;
	private double minPerDelta;
	
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
	
	private ArrayList<ParserRuleContext[]> getCopies(ArrayList<ParserRuleContext> cst){
		
		return null;
	}
	
	private boolean areMatch(ParserRuleContext p1, ParserRuleContext p2){
		APTED<APTEDCostModel, ParserRuleContext> apted = new APTED<APTEDCostModel, ParserRuleContext>(new APTEDCostModel());
		apted.computeEditDistance(new ParserRuleContextNode(p1), new ParserRuleContextNode(p2));
		LinkedList<int[]> list = apted.computeEditMapping();
		System.out.println(list.toString());
		
		return list.size()==0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		Helper.printToSTD("Reading file",f.getName());
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
		ArrayList<ArrayList<ParserRuleContext>> classMethods = plugin.getMethods();
		
		if(Helper.test){
			for(ArrayList<ParserRuleContext> methods:classMethods){
				String functionName = ((ClassDeclarationContext)methods.get(0).getParent().getParent().getParent().getParent()).IDENTIFIER().getText();
				String functions = "";
				for(ParserRuleContext p : methods){
					functions += ((MethodDeclarationContext)p).IDENTIFIER().toString()+",";
				}
				Helper.printToSTD("Found class \""+functionName +"\" containing methods "+functions.substring(0, functions.length()-1));
			}
		}
		
		Helper.printToSTD("Methods Extracted, Searching for matches", f.getName());
		//HashMap<Class<? extends ParserRuleContext>, ResoultionPattern> rules = plugin.getPatterns();
		for(ArrayList<ParserRuleContext> methods: classMethods){
			
		}
	}
}
