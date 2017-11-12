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

import dif.APTEDCostModel;
import dif.ParserRuleContextNode;
import distance.APTED;
import parse.PluginInterface;
import parse.ResoultionPattern;

/**
 * @author Rikkey Paal
 *
 */
public class MergeThread extends Thread{
	
	PluginInterface plugin;
	
	/**
	 * 
	 */
	public MergeThread(File f, PluginInterface pluginInterface) {
		this.plugin = pluginInterface;
		try {
			plugin.parse(readFile(f));
			ArrayList<ParserRuleContext> cst = plugin.getMethods();
			HashMap<Class<? extends ParserRuleContext>, ResoultionPattern> rules = plugin.getPatterns();
			
		} catch (IOException e) {
			e.printStackTrace();
			this.interrupt();
		}
	}
	
	private CharStream readFile(File f) throws IOException{
		return CharStreams.fromPath(f.toPath());
	}

	
	private ArrayList<ParserRuleContext[]> getCopies(ArrayList<ParserRuleContext> cst){
		
		return null;
	}
	
	private boolean areMatch(ParserRuleContext p1, ParserRuleContext p2){
		APTED apted = new APTED<APTEDCostModel, ParserRuleContextNode>(new APTEDCostModel());
		apted.computeEditDistance(new ParserRuleContextNode(p1), new ParserRuleContextNode(p2));
		LinkedList list = apted.computeEditMapping();
		System.out.println(list.toString());
		
		return list.size()==0;
	}
}
