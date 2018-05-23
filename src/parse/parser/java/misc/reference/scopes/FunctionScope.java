package parse.parser.java.misc.reference.scopes;

import org.antlr.v4.runtime.ParserRuleContext;

import parse.parser.java.comp.JavaParser.ConstructorDeclarationContext;
import parse.parser.java.comp.JavaParser.MethodDeclarationContext;
import parse.parser.java.misc.reference.Symbol;

public class FunctionScope extends Scope {
	private final String returnName;
	/**
	 * Visibility of the function <br>
	 * 0: Private <br>
	 * 1: Default<br>
	 * 2: Protected<br>
	 * 3: Public<br>
	 */
	private final int visibility;
	private final String[] parameterTypes;
	private String returnType;
	private String[] fullParameters;
	/**
	 *
	 * Initializes the FunctionScope class
	 * TODO Annotate constructor
	 *
	 * @param id
	 * @param parent
	 * @param name
	 * @param returnType
	 * @param visibility
	 *            0: private, 3: public
	 * @param parameterTypes
	 */
	public FunctionScope(MethodDeclarationContext id, Scope parent, String name, String returnType, int visibility,
			String[] parameterTypes) {
		super(id, parent, name);
		returnName = returnType;
		this.visibility = visibility;
		this.returnType = null;
		this.parameterTypes = parameterTypes;
		if (parameterTypes != null) {
			fullParameters = new String[parameterTypes.length];
		}
	}

	/**
	 * Initializes the FunctionScope class
	 * Constructor
	 *
	 * @param ctx
	 * @param classScope
	 * @param name
	 * @param visibility
	 * @param parameters
	 */
	public FunctionScope(ConstructorDeclarationContext ctx, ClassScope parent, String name, int visibility,
			String[] parameters) {
		super(ctx, parent, name);
		returnName = name;
		this.visibility = visibility;
		returnType = null;
		parameterTypes = parameters;
		if (parameterTypes != null) {
			fullParameters = new String[parameterTypes.length];
		}
	}

	/**
	 * Creates a {@link InnerScope} to be a sub-scope
	 *
	 * @param ctx
	 *            of the subscope
	 * @return the scope created
	 */
	public InnerScope createScope(ParserRuleContext ctx) {
		InnerScope s = new InnerScope(ctx, this);
		scopes.put(new ScopeKey(ctx), s);
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.Scope#getFunctionDeclaration(java.lang.String)
	 */
	@Override
	public FunctionScope getFunctionDeclaration(String name) {
		return parent.getFunctionDeclaration(name);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.Scope#getClassDeclaration(java.lang.String)
	 */
	@Override
	protected ClassScope getClassDeclaration(String name) {
		return parent.getClassDeclaration(name);
	}

	/**
	 * @return the returnType
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * @return the visibility of the function <br>
	 *         0: Private <br>
	 *         1: Default<br>
	 *         2: Protected<br>
	 *         3: Public<br>
	 */
	public int getVisibility() {
		return visibility;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.Scope#resolveReferences()
	 */
	@Override
	protected boolean resolveReferences() {
		if (returnType == null) {
			returnType = getTypePath(returnName);
		}
		if (returnType == null)
			return false;

		if (!name.contains("(")) {
			String newName = name + "(";
			if (parameterTypes != null && parameterTypes.length > 0) {
				for (int i = 0; i < parameterTypes.length; i++) {
					if (fullParameters[i] == null) {
						fullParameters[i] = getObjectType(parameterTypes[i]);
						if (fullParameters[i] == null)
							return false;
						newName += fullParameters[i] + ", ";
					}
				}
				newName = newName.substring(0, newName.length() - 2);
			}
			newName += ")";

			if (!((ClassScope) parent).updateFunctionScopeReference(this, newName)) {
				if (isOnlySelfRefferential()) {
					// move innerscope to renamedFunction
					((RenamedFunctionScope) ((ClassScope) parent).getScope(newName)).setFunction(this);
					name = "";
				}
			} else {
				name = newName;
			}
		}

		return true;
	}

	private boolean isOnlySelfRefferential() {
		if (functionCalls.size() != 0 || scopes.size() != 1)
			return false;
		Scope s = scopes.values().iterator().next();
		if (!(s instanceof InnerScope))
			return false;
		InnerScope is = (InnerScope) s;

		return is.symbolTable.size() == 0 && is.scopes.size() == 0 && is.functionCalls.size() == 1;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.Scope#getPath()
	 */
	@Override
	public String calculatePath() {
		return parent.getPath() + "." + name;
	}

	String[] getParameters() {
		return fullParameters;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = "";
		switch (getVisibility()) {
			case 0:
				str += "private ";
				break;
			case 2:
				str += "protected ";
				break;
			case 3:
				str += "public ";
				break;
		}
		str += returnName + " " + name;
		for (Symbol symbol : symbolTable.values()) {
			str += "\n\t" + symbol.toString();
		}

		for (Scope s : scopes.values()) {
			str += "\n\t" + s.toString().replaceAll("\n", "\n\t");
		}

		return str;
	}

	public String getFullName() {
		String name = parent.getPath() + "." + getName().substring(0, getName().indexOf('('));
		String params = "";
		if (parameterTypes != null) {
			for (String param : parameterTypes) {
				params += param + ", ";
			}
			if (params.length() > 0) {
				params = params.substring(0, params.length() - 2);
			}
		}
		return name + "(" + params + ")";
	}

}