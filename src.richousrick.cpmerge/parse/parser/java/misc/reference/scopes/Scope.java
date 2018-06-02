package parse.parser.java.misc.reference.scopes;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;

import javafx.util.Pair;
import parse.parser.java.comp.JavaParser.ExpressionContext;
import parse.parser.java.misc.reference.FunctionCall;
import parse.parser.java.misc.reference.MergedFunctionPass;
import parse.parser.java.misc.reference.Symbol;
import parse.parser.java.misc.reference.scopes.reflect.ReflectedClassScope;
import ref.Helper;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public abstract class Scope {

	/**
	 * List of scopes contained within this scope
	 * Indexed by their {@link #id}
	 */
	protected final HashMap<ScopeKey, Scope> scopes;

	/**
	 * Unique rule that this scope belongs to.
	 */
	protected final ParserRuleContext id;

	/**
	 * The name of the scope.
	 */
	protected String name;

	/**
	 * Table of symbols within this scope.
	 */
	protected final HashMap<String, Symbol> symbolTable;

	/**
	 * List of functionCalls within this scope.
	 */
	protected final HashMap<String, ArrayList<FunctionCall>> functionCalls;

	/**
	 * Scope that encapsulates this scope.
	 */
	protected final Scope parent;

	protected static ScopePart rootScope;

	protected static HashMap<String, MergedFunctionPass> changedFunctions;

	private static ArrayList<FunctionCall> functionCallsToUpdate;

	private static boolean needResolving;

	public synchronized static void addFunctionCallToUpdate(FunctionCall func) {
		functionCallsToUpdate.add(func);
	}

	public static boolean needResolving() {
		return needResolving;
	}

	public static ArrayList<FunctionCall> getFunctionCallsToUpdate(String name2) {
		if (functionCallsToUpdate == null)
			return new ArrayList<>();
		ArrayList<FunctionCall> retFuncCalls = new ArrayList<>();
		for(FunctionCall func : functionCallsToUpdate){
			Scope s = func.getEnclosingScope();
			while(!(s instanceof FileScope)){
				s = s.getParent();
			}
			if(s.getName().equals(name2)){
				retFuncCalls.add(func);
			}
		}
		return retFuncCalls;
	}

	/**
	 * Regex that matches classpaths
	 */
	private static String classPathRegex;

	private static String symbolRegex;

	private static String functionCallRegex;

	private static String compositeRegex;

	/**
	 * Used to allow easy manipulation of regex's
	 */
	private static void initRegex() {
		// alphanum starting with lower case
		symbolRegex = "\\p{Lower}\\p{Alnum}*";

		String importPath = String.format("%1$s(\\.%1$s)*\\.", symbolRegex);
		String className = "\\p{Upper}\\p{Alnum}*";
		String primitives = "boolean|byte|char|short|int|float|long|double";

		// can be primnitve type, or class name that may have import path
		classPathRegex = String.format("(((%s)?%2$s(\\.%2$s)*(\\$%2$s)*)|%3$s)", importPath, className, primitives);


		String parameterList = String.format("(%s(, %s)*)?", classPathRegex, classPathRegex);
		//
		functionCallRegex = String.format("%s\\(%s\\)", symbolRegex, parameterList);

		String functionOrSymbol = String.format("%s(\\(%s\\))?", symbolRegex, parameterList);
		String objChain = String.format("%1$s(\\.%1$s)*", functionOrSymbol);
		String classObjChain = String.format("((%s\\.)?%s)", classPathRegex, objChain);
		compositeRegex = String.format("(%s|%s)", classPathRegex, classObjChain);
	}

	protected String path;

	class ScopeKey {
		final Object key;

		/**
		 * Initializes the Scope.ScopeKey class
		 * TODO Annotate constructor
		 */
		public ScopeKey(String key) {
			this.key = key;
		}

		public ScopeKey(ParserRuleContext ctx) {
			key = ctx;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof ScopeKey)
				return key.equals(((ScopeKey) o).key);
			else
				return false;
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}
	}

	private synchronized void initStaticFields() {
		if (rootScope == null) {
			rootScope = new ScopePart("");
		}
		if (classPathRegex == null) {
			initRegex();
			needResolving = false;
		}
	}


	/**
	 * Initializes the Scope class
	 *
	 * @param id
	 *            Unique rule that this scope belongs to.
	 * @param parent
	 *            Scope that encapsulates this scope.
	 */
	protected Scope(ParserRuleContext id, Scope parent, String name) {
		this.id = id;
		this.parent = parent;
		this.name = name;
		symbolTable = new HashMap<>();
		functionCalls = new HashMap<>();
		scopes = new HashMap<>();
		initStaticFields();
		path = calculatePath();
	}



	/**
	 * @return the {@link #id}.
	 */
	public ParserRuleContext getId() {
		return id;
	}

	/**
	 * @return the {@link #parent}.
	 */
	public Scope getParent() {
		return parent;
	}


	/**
	 * Checks if the symbol is contained within this scope.
	 * This will return true if the symbol is contained within the enclosing
	 * scope.
	 *
	 * @param id
	 *            of the symbol to check
	 * @return true if the symbol is contained within this scope.
	 */
	public boolean hasSymbol(String id){
		return symbolTable.containsKey(id);
	}

	public static ArrayList<String> getParametersAsList(String parameters) {
		ArrayList<String> parameterList = new ArrayList<>();
		String curString = "";
		int bracketCount = 0;
		for(char c : parameters.toCharArray()) {
			if(c == '(') {
				bracketCount ++;
			}
			if(c == ')') {
				bracketCount --;
			}
			if(c == ',' && bracketCount == 0) {
				parameterList.add(curString);
				curString = "";
			}else {
				curString += c;
			}
		}
		parameterList.add(curString);
		return parameterList;
	}


	/**
	 * Returns the symbol with the specified ID.
	 * Starts searching from this scope,
	 * however if it is not found it will search its ancestors scopes, starting from its parent.
	 * @param id of the symbol to retrieve.
	 * @return the symbol if found, null otherwise.
	 */
	public Symbol getSymbol(String id) {
		Symbol ref = symbolTable.get(id);
		if(ref == null && parent !=null)
			return parent.getSymbol(id);
		else
			return ref;
	}

	/**
	 * Inserts a {@link Symbol} into this scope.
	 *
	 * @param id
	 *            of the symbol.
	 * @param ctx
	 *            The rule that creates the symbol.
	 * @param type
	 *            the data type of the symbol.
	 * @param staticType
	 *            if the type is static.
	 * @param visibility
	 *            of the symbol.
	 * @return the Symbol created
	 */
	public Symbol addSymbol(String id, ParserRuleContext ctx, String type, ArrayList<String> modifiers) {
		Symbol symbol = new Symbol(id, type, this, ctx, modifiers);
		symbolTable.put(id, symbol);
		return symbol;
	}

	/**
	 * Checks if the function call is contained within this scope.
	 * This will return true if the function call is contained within the
	 * enclosing scope.
	 *
	 * @param id
	 *            of the function call to check
	 * @return true if the symbol is contained within tis scope.
	 */
	public boolean hasFunctionCall(String id) {
		return functionCalls.containsKey(id);
	}

	/**
	 * Returns the symbol with the specified ID.
	 * Starts searching from this scope,
	 * however if it is not found it will search its ancestors scopes, starting
	 * from its parent.
	 *
	 * @param id
	 *            of the symbol to retrieve.
	 * @return the symbol if found, null otherwise.
	 */
	public ArrayList<FunctionCall> getFunctionCall(String id) {
		return functionCalls.get(id);
	}

	/**
	 * Inserts a {@link Symbol} into this scope.
	 *
	 * @param id
	 *            name of the function.
	 * @param ctx
	 *            The rule that creates the symbol.
	 * @param objectRef
	 *            object the function is called on
	 * @return the Symbol created
	 */
	public FunctionCall addFunctionCall(String id, ExpressionContext ctx, String objectRef) {
		FunctionCall func = new FunctionCall(id, this, objectRef, ctx);
		if (functionCalls.get(id) == null) {
			functionCalls.put(id, new ArrayList<>());
		}
		functionCalls.get(id).add(func);
		return func;
	}

	/**
	 * Checks if the specific scope is contained within this instance.
	 * @param id of the scope to search for.
	 * @return true if the specified scope is contained within this instance. False otherwise.
	 */
	public boolean hasScope(ParserRuleContext id){
		return scopes.containsKey(new ScopeKey(id));
	}

	/**
	 * Checks if the specific scope is contained within this instance.
	 *
	 * @param id
	 *            of the scope to search for.
	 * @return true if the specified scope is contained within this instance.
	 *         False otherwise.
	 */
	public boolean hasScope(String id) {
		return scopes.containsKey(new ScopeKey(id));
	}

	public static ArrayList<String> splitPath(String path) {
		ArrayList<String> parts = new ArrayList<>();
		String part = "";
		int bracketCount = 0;
		for (char c : path.toCharArray()) {
			if (c == '(') {
				bracketCount++;
			} else if (c == ')') {
				bracketCount--;
			}
			if ((c == '.' || c == '$') && bracketCount == 0) {
				parts.add(part);
				part = "";
			} else {
				part += c;
			}
		}
		parts.add(part);
		return parts;
	}

	/**
	 * Searches for the scope.
	 * @param id of the scope to search for
	 * @param recursive if true will search descendants if the scope is not found within this instance.
	 * @return the {@link Scope} if found, null otherwise.
	 */
	public Scope getScope(ParserRuleContext id, boolean recursive){
		ScopeKey key = new ScopeKey(id);
		Scope retScope = scopes.get(key);
		if (retScope!=null || !recursive)
			return scopes.get(key);
		else{
			for(Scope s : scopes.values()){
				retScope = s.getScope(id, recursive);
				if(retScope !=null){
					break;
				}
			}
		}
		return retScope;
	}

	public Scope getScope(String id) {
		ArrayList<String> parts = Scope.splitPath(id);
		if (parts.size() > 1) {
			int pos = parts.get(0).length();
			Scope s = scopes.get(new ScopeKey(id.substring(0, pos)));
			String otherPath = id.substring(pos + 1);
			return s.getScope(otherPath);
		} else
			return scopes.get(new ScopeKey(id));
	}

	public HashMap<ScopeKey, Scope> getScopes() {
		return scopes;
	}

	public boolean addScope(Scope s, String path) {
		s.name = path;
		ScopeKey key = new ScopeKey(path);
		if (!scopes.containsKey(key)) {
			scopes.put(key, s);
			return true;
		} else
			return false;
	}

	public String getName() {
		return name;
	}

	/**
	 * Returns the string representation of the Type of the object
	 *
	 * @param name
	 *            of the object.
	 *            If the object is a function it should be supplied with the
	 *            types of parameters. e.g. myFunction(String, int)
	 * @return A string representing the type of the object (or return value if
	 *         the object was a function)
	 */
	public String getObjectType(String reference) {

		Pair<Integer, String> p = Scope.stripDimsFromClassName(reference);

		int arrDim = p.getKey();
		reference = p.getValue();
		String start = "";
		String end = "";
		for (int i = 0; i < arrDim; i++) {
			start += "[";
		}
		if (start.length() > 0) {
			start += "L";
			end = ";";
		}

		if (reference.contains("<")) {
			reference = reference.substring(0, reference.indexOf('<'));
		}

		String typeName;
		typeSwitch: switch (getReferenceType(reference)) {
			case -1:

				if (reference.charAt(0) == '(') {
					typeName = reference.substring(0, reference.indexOf(")"));
					break typeSwitch;
				} else if (reference.startsWith("new")) {
					String name = reference.replaceFirst("^new", "").trim();
					int i1 = name.indexOf('[');
					int i2 = name.indexOf('(');
					boolean isConstructor = i1 == -1 || i2 != -1 && i2 < i1;
					if (isConstructor) {
						name = name.replaceAll(" \\(", "(");
						String className = name.substring(0, name.indexOf("("));
						String rest = "";
						try {
							rest = name.substring(className.length() + 1);
							int index = 0;
							int depth = 1;
							for (char c : rest.toCharArray()) {
								index++;
								if (c == '(') {
									depth++;
								} else if (c == ')') {
									depth--;

								} else if (c == '.' && depth == 0) {
									break;
								}

							}
							rest = rest.substring(index);

						} catch (IndexOutOfBoundsException e) {
							rest = "";
						}
						ClassScope s = getClassDeclaration(className);
						if (rest.length() > 0)
							return s.getObjectType(rest);
						else
							return s.getPath();
					} else {
						name = name.replaceAll("\\{[^\\{\\}]*\\}", "");
						name = name.replaceAll("\\p{Blank}*\\[", "[");
						return getObjectType(name);
					}

				} else if (reference.contains("(")) {

					ArrayList<String> parameterParts = getfunctions(reference);
					// see if param contains binary operators
					if (parameterParts.size() > 1) {
						// if contains parts a binop b binop c ...
						// where a binop is not + return int
						for (int i = 1; i < parameterParts.size(); i += 2) {
							if (!parameterParts.get(1).equals("+"))
								return "int";
						}

						// evaluate each element being operated on to see if any
						// are chars or strings
						for (int i = 0; i < parameterParts.size(); i += 2) {
							// resolve parameter type
							String simplifiedFunc = resolveFunctionReference(parameterParts.get(i));
							// check if it counts as a fucntion
							if (getReferenceType(simplifiedFunc) == 1) {
								String paramType = getObjectType(parameterParts.get(i));
								if (paramType.equals("char") || paramType.equals("java.lang.String"))
									return "java.lang.String";
							}
						}
						return "int";
					} else {
						// resolve parameters
						String simplifiedFunc = resolveFunctionReference(reference);
						// check if it counts as a fucntion
						if (getReferenceType(simplifiedFunc) == 1) {
							FunctionScope funcScope = getFunctionDeclaration(simplifiedFunc);
							if (funcScope != null)
								return funcScope.getReturnType();
						} else
							return simplifiedFunc;
					}

				}else {

					// if primitive
					switch (reference.charAt(0)) {
						case '\'':
							typeName = "char";
							break typeSwitch;
						case '"':
							typeName = "java.lang.String";
							break typeSwitch;
						case 'f':
						case 't':
							typeName = "boolean";
							break typeSwitch;
						default:
							if (Character.isDigit(reference.charAt(0))) {
								if (reference.endsWith("L")) {
									typeName = "long";
									break typeSwitch;
								} else if (reference.endsWith("f")) {
									typeName = "float";
									break typeSwitch;
								} else if (reference.endsWith("d")) {
									typeName = "double";
									break typeSwitch;
								} else if (reference.contains(".")) {
									typeName = "float";
									break typeSwitch;
								} else {
									typeName = "int";
									break typeSwitch;
								}
							}
					}
				}
				return null;
			case 0:
				Symbol symbol = getSymbol(reference);
				if (symbol != null) {

					typeName = symbol.getType();
					break typeSwitch;
				} else
					return null;
			case 1:
				FunctionScope funcScope = getFunctionDeclaration(reference);
				if (funcScope != null) {
					typeName = funcScope.getReturnType();
					break typeSwitch;
				} else
					return null;
			case 2:
				ClassScope classScope = getClassDeclaration(reference);
				if (classScope != null) {
					typeName = classScope.getPath();
					break typeSwitch;
				} else
					return null;
			default:
				Matcher matcher = Pattern.compile("((\\p{Alpha}+)\\.)+").matcher(reference);
				if (matcher.find()) {
					String path = matcher.group(0);
					path = path.substring(0, path.length() - 1);
					ClassScope scope = getClassScope(getObjectType(path));
					String objectRef = reference.replaceFirst("((\\p{Alpha}+)\\.)+", "");
					if (scope != null) {
						typeName = scope.getObjectType(objectRef);
						break typeSwitch;
					} else
						return null;
				} else
					return null;
		}
		return start + typeName + end;
	}

	private ArrayList<String> getfunctions(String functions) {
		ArrayList<String> functionList = new ArrayList<>();
		String func = "";
		int depth = 0;
		boolean addFuncList = false;
		for (char c : functions.toCharArray()) {
			func += c;
			if (c == '(') {
				depth++;
			} else if (c == ')') {
				depth--;
				if (depth == 0) {
					addFuncList = true;
				}
			} else if (depth == 0 && !Character.isLetterOrDigit(c) && c != '.' && c != ' ' && func.length() > 1) {
				addFuncList = true;
			}

			if (addFuncList) {
				addFuncList = false;
				if (func.trim().length() > 0) {
					functionList.add(func.trim());
				}
				func = "";
			}
		}
		if (func.trim().length() > 0) {
			functionList.add(func.trim());
		}
		return functionList;
	}


	/**
	 * @return true if the scope is empty
	 */
	protected boolean isEmpty() {
		return scopes.isEmpty() && symbolTable.isEmpty() && functionCalls.isEmpty();
	}

	/**
	 * Gets the path representing this scope.
	 * e.g. src.name.project.MainClass.myFunc(String)
	 *
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Removes scopes That are empty, Used to reduce the size of the scope tree.
	 */
	public synchronized void removeAllEmptyScopes() {
		ArrayList<ScopeKey> scopesToRemove = new ArrayList<>();
		for (Entry<ScopeKey, Scope> s : scopes.entrySet()) {
			if (s.getValue().isEmpty()) {
				scopesToRemove.add(s.getKey());
			}
		}
		for (ScopeKey scopeKey : scopesToRemove) {
			scopes.remove(scopeKey);
		}

		for (Scope s : scopes.values()) {
			s.removeAllEmptyScopes();
		}
	}


	/**
	 * Get the full path of the object specified.
	 * e.g. getTypePath("MyClass")
	 * will return
	 * package.superclasses.MyClass
	 *
	 * @param typeName
	 *            name of the type
	 * @return the type path
	 */
	public String getTypePath(String typeName) {
		// strip array
		Pair<Integer, String> p = Scope.stripDimsFromClassName(typeName);
		int arrDims = p.getKey();
		typeName = p.getValue();

		//strip generic
		if(typeName.contains("<")) {
			typeName = typeName.substring(0, typeName.indexOf('<'));
		}

		ClassScope scope = getClassDeclaration(typeName);
		if (scope != null) {
			scope.setArrayDims(arrDims);
			return scope.getPath()/* + retStr */;
		} else
			return null;
	}


	/**
	 * Assumes wither a reference is:<br>
	 * -2) null <br>
	 * -1) unknown <br>
	 * 0) Referencing an object<br>
	 * 1) A function call<br>
	 * 2) A Class<br>
	 * 3-5) 0-2 but external <br>
	 * 6) Composite (Multiple of the above chained together)
	 *
	 * @param reference
	 *            to check
	 * @return the value above
	 */
	private static int getReferenceType(String reference) {

		if (reference == null) {
			System.err.println("null reference");
			return -2;
		}

		while (reference.endsWith("[]")) {
			reference = reference.substring(0, reference.length() - 2);
		}

		// check if reference to object
		if (reference.matches(symbolRegex)) {
			switch (reference) {
				case "byte":
				case "short":
				case "int":
				case "long":
				case "float":
				case "double":
				case "boolean":
				case "char":
				case "void":
					return 2;
				default:
					return 0;
			}
		}

		// check if function call (\\.\\p{Alpha}*)*
		// TODO ensure
		else if (reference.matches(functionCallRegex))
			return 1;
		// check if reference to class
		else if (reference.matches(
				classPathRegex))
			return 2;
		else {
			// if referencing external classes return the type of the object
			// being locally referenced + 3
			String str = reference.replaceFirst("((\\p{Alpha}+)\\.)+?((\\p{Alpha}+)$)*?", "");
			if (!str.equals(reference) && reference.endsWith(str)) {
				int ret = getReferenceType(str);
				if (ret != -1) {
					ret += 3;
					return ret;
				} else if (isComposite(reference))
					return 6;
				else
					return -1;

			} else
				// if type is unknown return -1
				return -1;
		}
	}

	/**
	 * Resolves function call parameter types.
	 * turns
	 * myFunc(object, "hello", new OtherClass())
	 * where object is of type MyClass
	 * into
	 * myFunc(MyClass, String, OtherClass)
	 *
	 * @param functionRefernce
	 * @return
	 */
	public String resolveFunctionReference(String functionRefernce) {
		ArrayList<String> parameters = Scope.getParametersAsList(
				functionRefernce.substring(functionRefernce.indexOf("(") + 1, functionRefernce.lastIndexOf(")")));
		if (parameters.size() == 1 && parameters.get(0).equals("")) {
			parameters = new ArrayList<>();
		}
		String retString = functionRefernce.substring(0, functionRefernce.indexOf("("));
		String params = "";
		String type = "";
		for (String param : parameters) {
			// get parameter type
			// if param is not primitive
			if (Character.isLetter(param.charAt(0))) {
				// if param is functionCall
				if (param.contains("(")) {
					String funcInterface = resolveFunctionReference(param);
					if (funcInterface.contains("(")) {
						FunctionScope funcDec = getFunctionDeclaration(funcInterface);
						if (funcDec != null) {
							type = funcDec.getReturnType();
						} else {
							type = funcInterface;
						}
					} else {
						type = funcInterface;
					}

				} else {
					type += getObjectType(param);
				}
			} else {
				type = getObjectType(param);
			}

			params += type + ", ";
			type = "";
		}

		if (params.length() > 0) {
			params = params.substring(0, params.length() - 2);
		}
		retString = retString + "(" + params + ")";

		if (retString.contains(".") && retString.indexOf('(') > retString.indexOf('.')) {
			String[] parts = null;
			// if retstring contains multiple dots
			if (retString.indexOf('.') != retString.lastIndexOf('.')) {
				parts = new String[2];
				parts[0] = retString.substring(0, retString.indexOf('.'));
				parts[1] = retString.substring(retString.indexOf('.') + 1);
			} else {
				parts = retString.split("\\.");
			}

			Scope s = getClassScope(getObjectType(parts[0]));
			retString = s.getObjectType(parts[1]);
		}
		return retString;
	}

	private static boolean isComposite(String reference) {
		return reference.matches(
				compositeRegex);
	}

	/**
	 * Get list of types of composite parts
	 *
	 * @param reference
	 * @return
	 */
	public static ArrayList<Integer> getComposite(String reference) {
		// check it matches the correct structure
		if (isComposite(reference))
			return null;
		ArrayList<Integer> types = new ArrayList<>();
		ArrayList<String> parts = Scope.splitPath(reference);
		for (int i = 0; i < parts.size(); i++) {
			types.add(0, getReferenceType(parts.get(i)));
			if (types.get(0) > 2) {
				if (types.get(0) == 3 || parts.get(i).length() > 0 && i == parts.size())
					return types;
				else {
					System.err.println("Test this");
				}
			}
		}

		return types;
	}


	/**
	 * Get path to the object referenced by Object
	 * e.g.
	 * myFunc().toString().charAt(int)
	 * returns String.charAt(int)
	 * Note: not currently as reflection not implemented
	 *
	 * @param compositeTypes
	 *            result of {@link #getComposite(String)}
	 * @param reference
	 *            to the object
	 * @return
	 */
	public String getRefPathFromComposite(ArrayList<Integer> compositeTypes, String reference) {
		Scope scope = this;
		ArrayList<String> parts = Scope.splitPath(reference);

		// convert into string List
		String last = parts.remove(parts.size() - 1);

		// for element in string
		for (String part : parts) {
			switch (compositeTypes.get(0)) {
				case 0:
					scope = getClassScope(scope.getSymbol(part).getType());
					break;
				case 1:
					scope = getClassScope(scope.getFunctionDeclaration(part).getReturnType());
					break;
			}

			compositeTypes.remove(0);
			if (scope == null)
				return null;
		}
		switch (Scope.getReferenceType(last)) {
			case 0:
			case 1:
				return scope.getPath() + "." + last;
			case 2:
				return scope.getPath() + "$" + last;
			default:
				System.err.println("unknown last type of composite");
				return scope.getPath() + "." + last;
		}

	}

	/**
	 * Get all ClassScopes declared in this scope.
	 *
	 * @param recursive
	 *            if true will return all descendant classScopes
	 * @return all ClassScopes declared in this scope.
	 */
	protected ArrayList<ClassScope> getClassScopes(boolean recursive) {
		ArrayList<ClassScope> retScopes = new ArrayList<>();
		for (Scope s : scopes.values()) {
			if (s instanceof ClassScope) {
				retScopes.add((ClassScope) s);
				if (recursive) {
					retScopes.addAll(((ClassScope) s).getClassScopes(true));
				}
			}
		}
		return retScopes;
	}

	/**
	 * Gets the {@link FunctionScope} where the function is declared
	 *
	 * @param name
	 *            of the function to find in form name(Types), e.g.
	 *            toString(String)
	 * @return
	 */
	public abstract FunctionScope getFunctionDeclaration(String name);

	/**
	 * Gets the {@link ClassScope} that matches the name provided
	 *
	 * @param name
	 *            of the class to look for
	 * @return
	 */
	protected abstract ClassScope getClassDeclaration(String name);

	/**
	 * Gets the {@link InterfaceDeclaration} that matches the name provided
	 *
	 * @param name
	 *            of the interface to look for
	 * @return
	 */
	protected ClassScope getInterfaceDeclaration(String name) {
		return getClassDeclaration(name);
	}

	/**
	 * Adds a {@link FileScope} to the rootNode, adding it to the final tree
	 * representing the file
	 *
	 * @param file
	 *            to add to the final tree
	 */
	public static void addFileScope(FileScope file) {
		ScopePart scope = rootScope;
		String[] parts = file.getPackagePath().split("\\.");
		for (String part : parts) {
			if (!scope.hasScopePart(part)) {
				scope.addScopePart(part);
			}
			scope = scope.getScopePart(part);
		}
		// add classes to scope
		for (Scope s : file.getScopes().values()) {
			scope.addClassScope((ClassScope) s);
		}
	}

	/**
	 * Finds the class using its path.
	 * e.g. src.name.project.MainClass.SubType
	 * would return the ClasssScope referencing SubTypes declaration
	 *
	 * @param classPath
	 *            path to the desired class
	 * @return the {@link ClassScope} declaring the type; if found. null
	 *         otherwise
	 */
	public static ClassScope getClassScope(String classPath) {
		if (classPath.startsWith("[") && classPath.contains("]"))
			throw new InvalidParameterException("ClassPath contains both types of array");
		// trim array notation
		Pair<Integer, String> p = stripDimsFromClassName(classPath);
		classPath = p.getValue();
		int arrDim = p.getKey();

		ArrayList<String> parts = Scope.splitPath(classPath);

		// see if scope is part of java
		ClassScope scope = getJavaClassScope(classPath, parts);


		// if scope is null get as defined type
		if (scope == null) {
			ScopePart root = rootScope;

			boolean rootSearch = true;
			for (String part : parts) {
				if (rootSearch) {
					// if part is rootscope then get that
					if (root.hasScopePart(part)) {
						root = root.getScopePart(part);
					} else {
						// if part is a class then get class
						scope = root.getClassScope(part);
						if (scope != null) {
							rootSearch = false;
						}
					}
				} else {
					// if part is a class then get class
					scope = scope.getClassDeclaration(part);
				}
			}
			if (scope == null) {
				// check java.lang types
				try {
					scope = ReflectedClassScope.generateReflectedScope("java.lang." + classPath);
				} catch (ClassNotFoundException e) {
				}
			}
		}
		if (scope != null) {
			scope.setArrayDims(arrDim);
		}
		return scope;
	}

	private static ClassScope getJavaClassScope(String classPath, ArrayList<String> parts) {
		switch (classPath) {
			case "byte":
				return new ReflectedClassScope(byte.class);
			case "short":
				return new ReflectedClassScope(short.class);
			case "int":
				return new ReflectedClassScope(int.class);
			case "long":
				return new ReflectedClassScope(long.class);
			case "float":
				return new ReflectedClassScope(float.class);
			case "double":
				return new ReflectedClassScope(double.class);
			case "boolean":
				return new ReflectedClassScope(boolean.class);
			case "char":
				return new ReflectedClassScope(char.class);
			case "void":
				return new ReflectedClassScope(void.class);
		}


		checkDefault: switch (parts.get(0)) {
			case "org":
				switch (parts.get(1)) {
					case "ietf":
					case "omg":
					case "w3c":
					case "xml":
						break;
					default:
						break checkDefault;
				}
			case "java":
			case "javax":
				try {
					return ReflectedClassScope.generateReflectedScope(classPath);
				} catch (ClassNotFoundException e) {
				}
		}

		return null;
	}

	/**
	 * Resolves the references in the scopes
	 *
	 * @return A list of scopes that wore not resolved
	 */
	protected ArrayList<Scope> resolveReferences(boolean recursive) {
		ArrayList<Scope> retScopes = new ArrayList<>();
		if (!resolveReferences()) {
			retScopes.add(this);
		}
		// needed as keys may change
		ArrayList<Scope> scopeList = getScopesToResolve();
		if (recursive) {
			for (Scope s : scopeList) {
				retScopes.addAll(s.resolveReferences(true));
			}
		}
		return retScopes;
	}

	protected ArrayList<Scope> getScopesToResolve(){
		return new ArrayList<>(scopes.values());
	}

	/**
	 * Resolves references specific to the scope
	 *
	 * @return false if some references need to be resolved
	 */
	protected abstract boolean resolveReferences();




	/**
	 * Attempts to resolve all scope specific values
	 * This calls {@link #resolveReferences(boolean)} on all scopes
	 *
	 * @param allScopes
	 * @return true if all scopes reolved
	 */
	private static boolean resolveScopeReferences(ArrayList<ClassScope> allScopes) {
		ArrayList<Scope> scopesToResolve = new ArrayList<>();
		for (ClassScope s : allScopes) {
			scopesToResolve.addAll(s.resolveReferences(true));
		}
		int size;
		do {
			size = scopesToResolve.size();
			ArrayList<Scope> newScope = new ArrayList<>();
			for (Scope s : scopesToResolve) {
				newScope.addAll(s.resolveReferences(false));
			}
			scopesToResolve = newScope;
		} while (scopesToResolve.size() > 0 && scopesToResolve.size() < size);

		return scopesToResolve.size() == 0;
	}

	/**
	 * Resolves the symbols types in the current scope
	 *
	 * @return true if sucessful
	 */
	protected boolean resolveSymbols(boolean recursive) {
		for (Symbol sym : symbolTable.values()) {
			if (!sym.resolve())
				return false;
		}
		if (recursive) {
			for (Scope sc : scopes.values()) {
				if (!sc.resolveSymbols(recursive))
					return false;
			}
		}
		return true;
	}

	/**
	 * Resolves the {@link Symbol}'s of the scopes given
	 *
	 * @param allScopes
	 *            to check
	 * @return true if all symbols resolved
	 */
	private static boolean resolveFunctions(ArrayList<ClassScope> allScopes) {
		ArrayList<FunctionCall> unresolvedFunctions = new ArrayList<>();
		// try to resolve all functions
		for (ClassScope s : allScopes) {
			unresolvedFunctions.addAll(s.resolveAllFunctions(true));
		}

		// try to resolve each function reference that failed resolution
		// keep trying to resolve functions, provided some functions are being
		// resolved each time
		int size;
		do {
			size = unresolvedFunctions.size();
			ArrayList<FunctionCall> newFunctions = new ArrayList<>();
			for (FunctionCall f : unresolvedFunctions) {
				if (!f.resolve()) {
					newFunctions.add(f);
				}
			}
			unresolvedFunctions = newFunctions;
		} while (unresolvedFunctions.size() > 0 && unresolvedFunctions.size() < size);
		return unresolvedFunctions.size() == 0;
	}

	/**
	 * Resolves the symbols types in the current scope
	 *
	 * @return true if sucessful
	 */
	protected ArrayList<FunctionCall> resolveAllFunctions(boolean recursive) {
		ArrayList<FunctionCall> unresolvedFunctions = new ArrayList<>();
		for (ArrayList<FunctionCall> funcL : functionCalls.values()) {
			for (FunctionCall func : funcL) {
				try {
					if (!func.resolve()) {
						unresolvedFunctions.add(func);
					}
				} catch (Throwable e) {
					Helper.exitProgram("Error while resolving Functions");
				}
			}
		}
		if (recursive) {
			for (Scope sc : scopes.values()) {
				try {
					unresolvedFunctions.addAll(sc.resolveAllFunctions(recursive));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return unresolvedFunctions;
	}

	/**
	 * Resolves the {@link Symbol}'s of the scopes given
	 *
	 * @param allScopes
	 *            to check
	 * @return true if all symbols resolved
	 */
	private static boolean resolveSymbols(ArrayList<ClassScope> allScopes) {
		for (ClassScope s : allScopes) {
			if (!s.resolveSymbols(true))
				return false;
		}
		return true;
	}

	/**
	 * Attempts to resolve all references
	 *
	 * @param newFunctionPaths
	 *
	 * @return True, if all references wore able to be resolved
	 */
	public static boolean resoveAllReferences(HashMap<String, MergedFunctionPass> newFunctionPaths) {
		// if no scopes exist then return true
		if (rootScope == null)
			return true;

		Scope.changedFunctions = newFunctionPaths;
		Scope.functionCallsToUpdate = new ArrayList<>();
		ArrayList<ClassScope> allScopes = rootScope.getAllClassScopes();
		if (!resolveScopeReferences(allScopes)) {
			Helper.exitProgram("Failed resolving class references");
		}
		if (!resolveSymbols(allScopes)) {
			Helper.exitProgram("Failed resolving symbols types");
		}
		if (!resolveFunctions(allScopes)) {
			Helper.exitProgram("Failed resolving function parameter types");
		}
		return true;
	}

	protected abstract String calculatePath();

	class ScopePart {
		private final String name;
		private final HashMap<String, ClassScope> classScopes;
		private final HashMap<String, ScopePart> scopeParts;

		/**
		 * Initializes the Scope.RootScope.ScopePart class
		 * TODO Annotate constructor
		 */
		public ScopePart(String name) {
			this.name = name;
			classScopes = new HashMap<>();
			scopeParts = new HashMap<>();
		}

		public String getName() {
			return name;
		}

		public boolean hasScopePart(String key) {
			return scopeParts.containsKey(key);
		}

		public boolean hasClassScope(String key) {
			return classScopes.containsKey(key);
		}

		public void addClassScope(ClassScope classScope) {
			if (hasClassScope(classScope.getName())) {
				Helper.exitProgram("Multiple classes found in location\""+classScope.getPath()+"\" ");
			}
			classScopes.put(classScope.getName(), classScope);
		}

		public void addScopePart(String name) {
			scopeParts.put(name, new ScopePart(name));
		}

		public ClassScope getClassScope(String name) {
			return classScopes.get(name);
		}

		public ScopePart getScopePart(String name) {
			return scopeParts.get(name);
		}

		public ArrayList<ClassScope> getAllClassScopes() {
			ArrayList<ClassScope> scopes = new ArrayList<>();
			for (ClassScope s : classScopes.values()) {
				scopes.add(s);
			}
			for (ScopePart p : scopeParts.values()) {
				scopes.addAll(p.getAllClassScopes());
			}
			return scopes;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			String str = name;
			for (ClassScope c : classScopes.values()) {
				str += "\n" + c.toString();
			}
			for (ScopePart s : scopeParts.values()) {
				str += "\n" + s.toString();
			}
			if (name.length() > 0) {
				str = str.replaceAll("\n", "\n\t");
			}
			return str;
		}


	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	private static Pair<Integer, String> stripDimsFromClassName(String name) {
		String className = name;
		int arrDim = 0;
		if (className.endsWith("]")) {
			while (className.endsWith("[]")) {
				className = className.substring(0, className.length() - 2);
				arrDim++;
			}
		} else if (className.startsWith("[")) {
			for (char c : className.toCharArray()) {
				if (c == '[') {
					arrDim++;
				} else {
					break;
				}
			}
			className = className.substring(arrDim + 1, className.length() - 1);
		}

		return new Pair<Integer, String>(arrDim, className);
	}

	/**
	 * TODO Annotate method
	 */
	public static void setNeedResolving() {
		needResolving = true;
	}
}

