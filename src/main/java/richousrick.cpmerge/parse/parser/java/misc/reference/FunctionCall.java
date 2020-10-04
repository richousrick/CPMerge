package richousrick.cpmerge.parse.parser.java.misc.reference;

import richousrick.cpmerge.parse.parser.java.comp.JavaParser.ExpressionContext;
import richousrick.cpmerge.parse.parser.java.misc.reference.scopes.ClassScope;
import richousrick.cpmerge.parse.parser.java.misc.reference.scopes.FunctionScope;
import richousrick.cpmerge.parse.parser.java.misc.reference.scopes.RenamedFunctionScope;
import richousrick.cpmerge.parse.parser.java.misc.reference.scopes.Scope;
import richousrick.cpmerge.parse.parser.java.visitors.PrettyPrinter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public class FunctionCall {

	private final String name;
	private final Scope enclosingScope;
	private final ExpressionContext ctx;

	private final ArrayList<String> parameterValues;
	private final String objectRef;

	private final ArrayList<String> parameterTypes;

	/**
	 * Path to the scope to start looking for the function
	 * This is the local scope if the function was locally called
	 * or the path to the class of the object it was called on
	 */
	private String startingScope;
	private FunctionScope declaration;


	/**
	 * Initializes the FunctionCall class
	 * TODO Annotate constructor
	 *
	 * @param id
	 * @param enclosingScope2
	 * @param objectRef
	 * @param ctx2
	 */
	public FunctionCall(String name, Scope enclosingScope, String objectRef, ExpressionContext ctx) {
		this.name = name;
		parameterValues = new ArrayList<>();

		if (ctx.expressionList() != null) {
			String params = new PrettyPrinter(false).visitExpressionList(ctx.expressionList());
			if (params.contains("[")) {
				for (String s : getArrayParamAsList(params)) {
					parameterValues.add(s.trim());
				}
			} else {
				parameterValues.addAll(Scope.getParametersAsList(params));
			}
		}
		parameterTypes = new ArrayList<>(Collections.nCopies(parameterValues.size(), null));

		this.enclosingScope = enclosingScope;

		this.ctx = ctx;
		this.objectRef = objectRef;
		Scope.setNeedResolving();
	}

	private ArrayList<String> getArrayParamAsList(String param) {
		ArrayList<String> retList = new ArrayList<>();
		int paramDepth = 0;
		String currString = "";
		int arrayDepth = 0;
		boolean inString = false;
		for (char c : param.toCharArray()) {
			currString += c;
			if (c == ',' && paramDepth == 0 && arrayDepth == 0) {
				retList.add(currString.substring(0, currString.length() - 1));
				currString = "";
			} else if (c == '"') {
				inString = !inString;
			} else if (!inString) {
				if (c == '{') {
					arrayDepth++;
				} else if (c == '}') {
					arrayDepth--;
				} else if (c == '(') {
					paramDepth++;
				} else if (c == ')') {
					paramDepth--;
				}
			}
		}
		if (currString.length() > 0) {
			retList.add(currString);
		}
		return retList;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the enclosingScope
	 */
	public Scope getEnclosingScope() {
		return enclosingScope;
	}


	/**
	 * @return the ctx
	 */
	public ExpressionContext getCtx() {
		return ctx;
	}


	@Override
	public String toString() {
		String retString;
		if (declaration != null) {
			retString = declaration.getPath()+"(";
		} else {
			retString = (objectRef.length() > 0 ? objectRef + "." : "") + name + "(";
		}
		for (String parameter : parameterValues) {
			retString += parameter + ", ";
		}
		if (parameterValues.size() != 0) {
			retString = retString.substring(0, retString.length() - 2);
		}
		return retString + ")";
	}

	public boolean resolve() {
		// if function already found return
		if (declaration != null)
			return true;

		Scope scope = null;
		// resolve object Type
		// if called on something resolve the type
		if (startingScope == null) {
			if (objectRef.length() > 0) {
				startingScope = enclosingScope.getObjectType(objectRef);
				if (startingScope == null) {
					System.err.println("Cannot find object function called on");
					return false;
				}

				scope = Scope.getClassScope(startingScope);
				if (scope == null) {
					System.err.println("function scope unknown");
					return false;
				}
				// if local call then get local scope
			} else {
				scope = enclosingScope;
				startingScope = scope.getPath();
			}
		}

		String parameterList = "";
		// resolve paramterTypes
		for (int i = 0; i < parameterValues.size(); i++) {
			if (parameterTypes.get(i) == null) {
				String value = parameterValues.get(i);
				String type = enclosingScope.getObjectType(parameterValues.get(i));
				if (type == null) {
					System.err.println("could not determine the type of parameter \"" + parameterValues.get(i) + "\"");
					return false;
				} else {
					parameterTypes.set(i, type);
				}

			}
			parameterList += parameterTypes.get(i) + ", ";
		}
		if (parameterList.length() > 0) {
			parameterList = parameterList.substring(0, parameterList.length() - 2);
		}

		// Concatenate (static reference | object type) name and parameters
		String functionName = name + "(" + parameterList + ")";

		declaration = scope.getFunctionDeclaration(functionName);
		if (declaration instanceof RenamedFunctionScope) {
			Scope.addFunctionCallToUpdate(this);
		}

		return declaration != null;
	}

	private ClassScope getEnclosingClassScope() {
		Scope s = enclosingScope;
		while (!(s instanceof ClassScope)) {
			s = s.getParent();
		}
		return (ClassScope) s;
	}

	public ArrayList<String> getParameterValues() {
		return parameterValues;
	}

	public FunctionScope getDeclaredFunction() {
		return declaration;
	}

	public String getObjectRef() {
		return objectRef;
	}

	public boolean hasObjectRef() {
		return objectRef != null && objectRef.length() > 0;
	}
}
