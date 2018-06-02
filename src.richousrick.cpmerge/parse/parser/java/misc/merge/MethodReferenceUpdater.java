package parse.parser.java.misc.merge;

import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

import merge.MergedFunction;
import parse.parser.java.misc.print.Replacement;
import parse.parser.java.misc.reference.FunctionCall;
import parse.parser.java.misc.reference.scopes.Scope;
import parse.parser.java.visitors.PrettyPrinter;
import ref.Helper;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class MethodReferenceUpdater {

	private static ArrayList<UpdatedMethodSet> updatedFunctions;
	private static HashMap<String, Integer> updatedFunctionPaths;


	public synchronized static boolean initMethodReferenceUpdater(ArrayList<MergedFunction<ParserRuleContext>> nodes) {
		if (updatedFunctions == null) {
			PrettyPrinter p = new PrettyPrinter(false);
			ArrayList<UpdatedMethodSet> set = new ArrayList<>();
			HashMap<String, Integer> functionPaths = new HashMap<>();
			int pos = 0;
			for (MergedFunction<ParserRuleContext> n : nodes) {
				UpdatedMethodSet uM = new UpdatedMethodSet(n);
				set.add(uM);
				// TODO set methodPath to functionPath
				String methodPath = p.getNodePath(n.getOriginalFunctionRoots().get(0).getNodeData());
				for (String s : uM.getFunctionNames()) {
					functionPaths.put(methodPath + "." + s, pos);
				}
				pos++;
			}
			updatedFunctions = set;
			updatedFunctionPaths = functionPaths;
			return true;
		} else
			return false;
	}

	public String updateReferences(ArrayList<FunctionCall> references, String file) {
		ArrayList<Replacement> replacements = new ArrayList<>();
		for (FunctionCall call : references) {
			String fullName = call.getDeclaredFunction().getFullName();


			String nameStart = fullName.substring(0, fullName.indexOf("("));
			nameStart = nameStart.substring(nameStart.lastIndexOf('.')+1);

			String name = nameStart+"(";
			String paramList = fullName.substring(fullName.indexOf('(')+1, fullName.length()-1);
			String paramS = "";
			if(paramList.length()>0){
				for(String paramType : Scope.getParametersAsList(paramList)){
					ArrayList<String> parts = Scope.splitPath(paramType);
					paramS += parts.get(parts.size() - 1).trim() + ", ";
				}
				paramS = paramS.substring(0, paramS.length()-2);
			}
			name+=paramS+")";

			fullName = fullName.substring(0,fullName.indexOf('(')+1) + paramS+")";

			// TODO tweak as includes path
			// e.g. a.function()
			// change to
			// function()
			int start = call.getCtx().start.getStartIndex();
			int end = call.getCtx().stop.getStopIndex();
			String curRep = call.getCtx().start.getInputStream().getText(new Interval(start, end));
			UpdatedMethodSet methSet = null;
			int tmp = -1;
			try {
				tmp = updatedFunctionPaths.get(fullName);
				methSet = updatedFunctions.get(tmp);
			} catch (Exception e) {
				Helper.exitProgram(e);
			}
			String replacementText = (call.hasObjectRef() ? call.getObjectRef() + "." : "")
					+ methSet.getReplacement(name, call.getParameterValues());
			Replacement replacement = new Replacement(start, end, replacementText, curRep,
					call.getEnclosingScope().getPath());
			replacements.add(replacement);

		}
		return Replacement.replace(replacements, file, true);
	}

}
