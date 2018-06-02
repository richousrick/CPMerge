package parse.parser.java.misc.merge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;

import merge.MergedFunction;
import parse.parser.java.comp.JavaParser.FormalParameterContext;
import parse.parser.java.comp.JavaParser.MethodDeclarationContext;
import parse.parser.java.visitors.PrettyPrinter;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class UpdatedMethodSet {
	private final MergedFunction<ParserRuleContext> node;
	// THink about extending key to include parameter types
	// put(int, String)
	private final HashMap<String, UpdatedMethod> updatedMethods;

	/**
	 * Initializes the UpdatedMethod class
	 * TODO Annotate constructor
	 */
	public UpdatedMethodSet(MergedFunction<ParserRuleContext> node) {
		this.node = node;
		updatedMethods = new HashMap<>();

		ArrayList<String[]> parameterList = getParameterList();
		String functionName = node.getStringRepresentation().substring(0,
				node.getStringRepresentation().indexOf('(')).trim();
		functionName = functionName.substring(functionName.lastIndexOf(" ") + 1);
		for(int i = 0; i<node.getOriginalFunctionRoots().size(); i++) {
			ClassNode c = (ClassNode) node.getOriginalFunctionRoots().get(i);
			MethodDeclarationContext ctx = (MethodDeclarationContext) c.getNodeData();
			UpdatedMethod m = new UpdatedMethod(ctx, parameterList, functionName, i);

			String methodName = ctx.IDENTIFIER().getText() + "(";
			for (String s : m.parameterTypes) {
				methodName += s + ", ";
			}
			methodName = removeLastComma(methodName.trim()) + ")";
			updatedMethods.put(methodName, m);
		}

	}


	private ArrayList<String[]> getParameterList() {
		ArrayList<String[]> newFuncParameters = new ArrayList<>();
		String parameterList = node.getStringRepresentation();
		int count = 1;
		int start = -1;
		splitLoop: for (int i = parameterList.length() - 2; i > 0; i--) {
			switch (parameterList.charAt(i)) {
				case ')':
					count++;
					break;
				case '(':
					count--;
					if (count == 0) {
						start = i + 1;
						break splitLoop;
					}
					break;
				default:
					break;
			}
		}

		parameterList = parameterList.substring(start, parameterList.length() - 1);

		for (String parameter : parameterList.split(",")) {
			String[] parts = parameter.trim().split(" ");
			if (parts.length != 2) {
				System.err.println("Parameter \"" + parameter + "\" not in form \"<type> <name>\"");
			} else {
				if(!parts[1].equals("fID")){
					newFuncParameters.add(new String[] { parts[1], parts[0] });
				}
			}
		}
		return newFuncParameters;
	}

	public String getReplacement(String name, ArrayList<String> parameterValues) {
		if (updatedMethods.containsKey(name))
			return updatedMethods.get(name).getReplacement(parameterValues);
		else{
			System.err.println("Method not found");
			return null;
		}}

	private String removeLastComma(String str) {
		if (str.endsWith(","))
			return str.substring(0, str.length() - 1);
		else
			return str;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = node.getStringRepresentation() + " = {";
		for (String s : getFunctionNames()) {
			str += "\n\t" + s;
		}
		str += "}";
		return str;
	}

	/**
	 * TODO Annotate method
	 *
	 * @return
	 */
	public Set<String> getFunctionNames() {
		return updatedMethods.keySet();
	}

	class UpdatedMethod {
		private final ArrayList<String> replacementParts;
		private final ArrayList<Integer> parameterPos;
		private ArrayList<String> parameterTypes;

		/**
		 * Initializes the UpdatedMethodSet.UpdatedMethod class
		 * TODO Annotate constructor
		 */
		//		public UpdatedMethod(MethodDeclarationContext ctx, ArrayList<String[]> newFuncParameter, String newFunctionName) {
		//			ArrayList<String> parameterNames = getParameterNamesAsList(ctx);
		//			ArrayList<String> replacementParts = new ArrayList<>();
		//			ArrayList<Integer> parameterPositions = new ArrayList<>();
		//			PrettyPrinter p = new PrettyPrinter(false);
		//			String currentPart = newFunctionName + "(";
		//			for (String[] parameter : newFuncParameter) {
		//				if (parameterNames.contains(parameter[0])) {
		//					// save curernt part, Increment pos, mark pos of paramter
		//					replacementParts.add(removeLastComma(currentPart));
		//					parameterPositions.add(parameterNames.indexOf(parameter[0]));
		//				} else {
		//					// append default value for parameter
		//					currentPart += p.getDefaultValue(parameter[1]) + ",";
		//				}
		//			}
		//
		//			this.replacementParts = replacementParts;
		//			parameterPos = parameterPositions;
		//		}



		public UpdatedMethod(MethodDeclarationContext ctx, ArrayList<String[]> parameterTypes, String newName, int fID){
			ArrayList<String> parameterNames = getParameterNamesAsList(ctx);
			ArrayList<String> replacementParts = new ArrayList<>();
			// position each parameter goes
			ArrayList<Integer> parameterPositions = new ArrayList<>();
			PrettyPrinter p = new PrettyPrinter(false);
			String currentPart = newName+"(";

			// for each parameter in new function
			for(String[] param: parameterTypes){
				// if function has parameter
				if(parameterNames.contains(param[0])){
					// save curernt part, Increment pos, mark pos of paramter
					replacementParts.add(removeLastComma(currentPart));
					currentPart = "";
					parameterPositions.add(parameterNames.indexOf(param[0]));
				}else{
					// append default value for parameter
					currentPart += p.getDefaultValue(param[1]) + ",";
				}
			}
			currentPart+=fID+")";
			replacementParts.add(removeLastComma(currentPart));
			this.replacementParts = replacementParts;
			parameterPos = parameterPositions;
		}



		private ArrayList<String> getParameterNamesAsList(MethodDeclarationContext ctx) {
			PrettyPrinter printer = new PrettyPrinter(false);
			ArrayList<String> parameterNames = new ArrayList<>();
			parameterTypes = new ArrayList<>();
			if (ctx.formalParameters().formalParameterList() != null) {
				for (FormalParameterContext p : ctx.formalParameters().formalParameterList().formalParameter()) {
					parameterNames.add(printer.visit(p.variableDeclaratorId()));
					parameterTypes.add(printer.visit(p.typeType()));
				}
				if (ctx.formalParameters().formalParameterList().lastFormalParameter() != null) {
					parameterNames.add(printer.visit(
							ctx.formalParameters().formalParameterList().lastFormalParameter().variableDeclaratorId()));
					parameterTypes.add(printer
							.visit(ctx.formalParameters().formalParameterList().lastFormalParameter().typeType()));
				}
			}
			return parameterNames;
		}

		public String getReplacement(ArrayList<String> parameterValues) {
			String replacement = replacementParts.get(0);
			if (!parameterValues.isEmpty() && !replacement.endsWith("(")) {
				replacement += ",";
			}
			for (int i = 0; i < replacementParts.size() -1 ; i++) {
				replacement += parameterValues.get(parameterPos.get(i)) + ",";
				replacement += replacementParts.get(i + 1) + ",";
			}
			if (!parameterValues.isEmpty()) {
				replacement = replacement.substring(0, replacement.length() - 1);
			}
			return replacement;
		}

	}


}
