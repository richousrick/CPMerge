package parse.parser.java.misc.reference.scopes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.antlr.v4.runtime.ParserRuleContext;

import merge.MergedFunction;
import parse.parser.java.comp.JavaParser.ClassDeclarationContext;
import parse.parser.java.comp.JavaParser.ConstructorDeclarationContext;
import parse.parser.java.comp.JavaParser.MethodDeclarationContext;
import parse.parser.java.misc.reference.FunctionCall;
import parse.parser.java.misc.reference.MergedFunctionPass;
import parse.parser.java.misc.reference.Symbol;
import parse.parser.java.visitors.SymbolTableGenerator;

public class ClassScope extends Scope {

	private final String extendsPath;
	private ArrayList<ClassScope> interfaces;
	private ClassScope superClass;
	private final ArrayList<String> implementsPaths;
	private HashMap<String, MergedFunction<ParserRuleContext>> updatedFucntions;
	protected int arrayDims;

	/**
	 * Initializes the ClassScope class
	 * TODO Annotate constructor
	 *
	 * @param id
	 * @param parent
	 * @param name
	 */
	public ClassScope(ClassDeclarationContext id, FileScope parent, String className, String extendType,
			ArrayList<String> interfaces) {
		this(id, (Scope) parent, className, extendType, interfaces);
	}

	public ClassScope(ClassDeclarationContext id, ClassScope parent, String className, String extendType,
			ArrayList<String> interfaces) {
		this(id, (Scope) parent, className, extendType, interfaces);
	}

	protected ClassScope(ParserRuleContext id, Scope parent, String className, String extendType,
			ArrayList<String> interfaces) {
		super(id, parent, className);
		if(extendType != null) {
			extendsPath = extendType;
		} else {
			extendsPath = "Object";
		}
		implementsPaths = interfaces;
		if (implementsPaths != null) {
			interfaces = new ArrayList<>(implementsPaths.size());
		}
		arrayDims = 0;
	}

	public ClassScope createScope(ClassDeclarationContext id, String name, String extendType,
			ArrayList<String> interfaces) {
		ClassScope s = new ClassScope(id, this, name, extendType, interfaces);
		scopes.put(new ScopeKey(name), s);
		return s;
	}

	public FunctionScope createScope(MethodDeclarationContext ctx, String name, String returnType, int visibility,
			String[] parameters) {
		FunctionScope s = new FunctionScope(ctx, this, name, returnType, visibility, parameters);
		scopes.put(new ScopeKey(name), s);
		return s;
	}

	/**
	 * TODO Annotate method
	 *
	 * @param ctx
	 * @param name
	 * @param visibility
	 * @param parameters
	 * @return
	 */
	public FunctionScope createScope(ConstructorDeclarationContext ctx, String name, int visibility,
			String[] parameters) {
		FunctionScope s = new FunctionScope(ctx, this, name, visibility, parameters);
		scopes.put(new ScopeKey(name), s);
		return s;
	}


	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.Scope#getPath()
	 */
	@Override
	public String calculatePath() {
		if (parent instanceof ClassScope)
			return parent.getPath() + "$" + name;
		else
			return parent.getPath() + "." + name;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.Scope#getClassDeclaration(java.lang.String)
	 */
	@Override
	public ClassScope getClassDeclaration(String className) {
		ClassScope scope = getClassDeclaration(className, true, true, true);
		Scope s = getParent();
		if (scope == null) {

			while (s.getParent() != null) {
				if (s instanceof ClassScope) {
					if (s.getName().equals(className))
						return (ClassScope) s;
					else {
						scope = ((ClassScope) s).getClassDeclaration(className, false, true, true);
						if (scope != null)
							return scope;
						else {
							s = s.getParent();
						}
					}
				} else
					return s.getClassDeclaration(className);
			}
		}
		if (scope != null)
			return scope;
		else
			return s.getClassDeclaration(className);
	}

	/**
	 * gets local class declarations
	 *
	 * @param className
	 * @param recursive
	 *            if true will search children
	 * @param specific
	 *            if true will use getClass to specify the return type
	 * @param getClass
	 *            if true will search for {@link ClassScope}, if false will
	 *            search for {@link InterfaceDeclaration}
	 * @return
	 */
	protected ClassScope getClassDeclaration(String className, boolean recursive, boolean specific, boolean getClass) {
		if (className.equals(name))
			return this;
		for (Scope s : scopes.values()) {
			if (s instanceof ClassScope && specific == (getClass ^ s instanceof InterfaceDeclaration)) {
				if (s.getName().equals(className))
					return (ClassScope) s;
			}
		}
		if (recursive) {
			for (Scope s : scopes.values()) {
				if (s instanceof ClassScope) {
					ClassScope ret = ((ClassScope) s).getClassDeclaration(className, recursive, specific, getClass);
					if (ret != null)
						return ret;
				}
			}
		}
		return null;
	}


	private FileScope getFileScope() {
		if (parent instanceof FileScope)
			return (FileScope) parent;
		else
			return ((ClassScope) parent).getFileScope();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.Scope#getFunctionDeclaration(java.lang.String)
	 */
	@Override
	public FunctionScope getFunctionDeclaration(String name) {
		FunctionScope functionDeclaration = getLocalFunctionDeclaration(name);

		if (functionDeclaration == null && parent instanceof ClassScope) {
			functionDeclaration = parent.getFunctionDeclaration(name);
		}
		return functionDeclaration;
	}

	public FunctionScope getLocalFunctionDeclaration(String name) {
		for (Scope s : scopes.values()) {
			if (s instanceof FunctionScope) {
				if (s.getName().equals(name))
					return (FunctionScope) s;
			}
		}
		return getLocalFunctionDeclarationUsingSubTypes(name);
	}

	/**
	 * Looks through the functions to find one that could be called with the
	 * name specified.
	 * NOTE: this does not look for the best fitting method.
	 * As such it may return the wrong function in cases with polymorphic
	 * methods with generalised and specialised parameters.<br>
	 *
	 * e.g.<br>
	 * the function chosen of those below is down to position in {@link HashMap}
	 *
	 * <pre>
	 * public boolean function(MyClass c) {
	 * 	return true;
	 * }
	 *
	 * public boolean function(MySubClass c) {
	 * 	return false;
	 * }
	 *
	 * class MyClass {
	 *
	 * }
	 *
	 * class MySubClass extends MyClass {
	 *
	 * }
	 * </pre>
	 *
	 * @param name
	 * @return
	 */
	protected FunctionScope getLocalFunctionDeclarationUsingSubTypes(String name) {
		String funcName = name.substring(0, name.indexOf("(") + 1);
		ArrayList<String> parameters = Scope.getParametersAsList(name.substring(name.indexOf("(") + 1, name.lastIndexOf(")")));

		ClassScope[] parameterTypes = new ClassScope[parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			parameterTypes[i] = Scope.getClassScope(parameters.get(i));
		}

		ArrayList<FunctionScope> scopesWithMatchingNames = new ArrayList<>();
		// get scopes that share names
		for (Scope s : scopes.values()) {
			if (s instanceof FunctionScope) {
				if (s.getName().startsWith(funcName)) {
					scopesWithMatchingNames.add((FunctionScope) s);
				}
			}
		}


		// go over each check if the types are the same or
		String[] desiredParameters;
		functionSearch: for (FunctionScope s : scopesWithMatchingNames) {
			desiredParameters = s.getParameters();
			if (parameters.size() == desiredParameters.length) {
				// Check the parameters match
				for (int i = 0; i < parameters.size(); i++) {
					// if parameters types dont match, see if subtype
					if (!parameters.get(i).equals(desiredParameters[i])) {
						ClassScope desiredParameterType = Scope.getClassScope(desiredParameters[i]);
						// if parameter supplied is not a subtype of the desired type, try next function
						if(!parameterTypes[i].isOfType(desiredParameterType)) {
							continue functionSearch;
						}
					}
				}
				// if parameters are all the same type or subtype of the desired function, return that function.

				return s;
			}
		}
		return null;
	}

	public String getExtendsPath() {
		return extendsPath;
	}

	public ArrayList<ClassScope> getInterfaces() {
		return interfaces;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.Scope#resolveReferences()
	 */
	@Override
	protected boolean resolveReferences() {
		// resolve superclass type
		if (extendsPath != null && superClass == null) {
			superClass = getClassDeclaration(extendsPath);
			if(superClass == null)
				return false;
		}

		// resolve interfaces
		if (implementsPaths != null) {
			for (int i = 0; i < implementsPaths.size(); i++) {
				if (interfaces.get(i) == null) {
					interfaces.add(i, getInterfaceDeclaration(implementsPaths.get(i)));
				}
				if (interfaces.get(i) == null)
					return false;
			}
		}

		// insert references to functions that wore removed
		updatedFucntions = new HashMap<>();
		ArrayList<String> toRemove = new ArrayList<>();
		for (Entry<String, MergedFunctionPass> e : Scope.changedFunctions.entrySet()) {
			String name = e.getKey();
			ArrayList<String> parts = Scope.splitPath(name);
			String funcName = parts.get(parts.size() - 1);
			name = name.substring(0, name.length() - (funcName.length() + 1));
			if (name.equals(getPath())) {
				ArrayList<String> parameters = Scope
						.getParametersAsList(funcName.substring(funcName.indexOf("(") + 1, funcName.length() - 1));
				if (parameters.size() == 1 && parameters.get(0).equals("")) {
					parameters = null;
				}
				String funcNameStart = funcName.substring(0, funcName.indexOf("("));

				updatedFucntions.put(funcName, e.getValue().getNewFunc());
				toRemove.add(e.getKey());
				FunctionScope s = new RenamedFunctionScope(e.getValue().getNewFunc(), this, funcNameStart, parameters);
				scopes.put(new ScopeKey(funcName), new SymbolTableGenerator().addSymbolTable(s, e.getValue().getCtx()));
			}
		}
		for (String key : toRemove) {
			Scope.changedFunctions.remove(key);
		}
		return true;
	}

	/**
	 * Updates the mapping of a functionScope.
	 * Used as the types of a functionScope is updated during resolution.
	 * e.g.
	 * before: toString
	 * after: toString(String)
	 *
	 *
	 * @param scope
	 */
	boolean updateFunctionScopeReference(FunctionScope scope, String newName) {
		scopes.remove(new ScopeKey(scope.getName()));
		if (!scopes.containsKey(new ScopeKey(newName)) || scope instanceof RenamedFunctionScope) {
			scopes.put(new ScopeKey(newName), scope);
			return true;
		}

		return false;
	}

	/**
	 * Checks if this classType is the same as or a subclass of the specified
	 * type
	 *
	 * @param type
	 *            to check against
	 * @return
	 */
	public boolean isOfType(ClassScope type) {
		if (arrayDims != type.arrayDims)
			return false;
		String targetPath = type.getPath();
		String extendsPath = getExtendsPath();
		// check current type
		if (equals(type))
			return true;
		// check superclass is of type
		if (extendsPath != null) {
			if (extendsPath.equals(targetPath))
				return true;
			else {
				// check if superclass is descendant of type
				ClassScope scope = getClassScope(extendsPath);
				if (scope != null && scope.isOfType(type))
					return true;
			}
		}
		ArrayList<ClassScope> implmentsPaths = getInterfaces();
		// check interface is of type
		if (implmentsPaths != null) {
			for (ClassScope interfac : implmentsPaths) {
				if (interfac.getPath().equals(targetPath))
					return true;
				else {
					if (interfac.isOfType(type))
						return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.Scope#getInterfaceDeclaration(java.lang.String)
	 */
	@Override
	protected ClassScope getInterfaceDeclaration(String name) {
		InterfaceDeclaration scope = (InterfaceDeclaration) getClassDeclaration(name, true, true, false);

		if (scope == null) {
			Scope s = getParent();
			while (s.getParent() != null) {
				if (s instanceof InterfaceDeclaration) {
					if (s.getName().equals(name))
						return (InterfaceDeclaration) s;
				} else
					return null;
			}
		}
		return scope;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = "class " + name;
		if (extendsPath != null) {
			ArrayList<String> parts = splitPath(extendsPath);
			str += " extends " + parts.get(parts.size() - 1);
		}
		if (implementsPaths != null) {
			str += "implements ";
			for (String imp : implementsPaths) {
				ArrayList<String> parts = splitPath(imp);
				str += parts.get(parts.size() - 1) + ", ";
			}
			str = str.substring(0, str.length() - 2);
		}

		for (Symbol s : symbolTable.values()) {
			str += "\n\t" + s.toString().replaceAll("\n", "\n\t");
		}

		for (ArrayList<FunctionCall> fList : functionCalls.values()) {
			for (FunctionCall f : fList) {
				str += "\n\t" + f.toString().replaceAll("\n", "\n\t");
			}
		}

		ArrayList<ClassScope> classes = new ArrayList<>();
		for (Scope s : scopes.values()) {
			if (s instanceof ClassScope) {
				classes.add((ClassScope) s);
			} else {
				str += "\n\t" + s.toString().replaceAll("\n", "\n\t");
			}
		}

		for (ClassScope c : classes) {
			str += "\n\t" + c.toString().replaceAll("\n", "\n\t");
		}
		return str;
	}

	public void setArrayDims(int dims) {
		if (dims != arrayDims) {
			arrayDims = dims;
			String newPath = "";
			for (int i = 0; i < arrayDims; i++) {
				newPath += '[';
			}
			path = newPath + "L" + path + ";";
		}
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.reference.scopes.Scope#getScopesToResolve()
	 */
	@Override
	protected ArrayList<Scope> getScopesToResolve() {
		ArrayList<Scope> finalScopes = new ArrayList<>();
		ArrayList<Scope> renScopes = new ArrayList<>();

		for (Scope s : scopes.values()) {
			if (s instanceof RenamedFunctionScope) {
				renScopes.add(s);
			} else {
				finalScopes.add(s);
			}
		}
		renScopes.addAll(finalScopes);
		return renScopes;
	}

}