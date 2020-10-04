package richousrick.cpmerge.parse.parser.java.misc.reference.scopes.reflect;

import org.antlr.v4.runtime.ParserRuleContext;
import richousrick.cpmerge.parse.parser.java.comp.JavaParser.ExpressionContext;
import richousrick.cpmerge.parse.parser.java.misc.reference.FunctionCall;
import richousrick.cpmerge.parse.parser.java.misc.reference.Symbol;
import richousrick.cpmerge.parse.parser.java.misc.reference.scopes.ClassScope;
import richousrick.cpmerge.parse.parser.java.misc.reference.scopes.FunctionScope;
import richousrick.cpmerge.parse.parser.java.misc.reference.scopes.InterfaceDeclaration;
import richousrick.cpmerge.parse.parser.java.misc.reference.scopes.Scope;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public class ReflectedClassScope extends ClassScope {

	Class<?> clas;
	/**
	 * Initializes the ReflectedScope class
	 * TODO Annotate constructor
	 *
	 * @param id
	 * @param parent
	 * @param className
	 * @param extendType
	 * @param interfaces
	 * @param interfaces
	 */
	public ReflectedClassScope(Class<?> c) {
		super(null, (Scope) null, c.getSimpleName(), null, null);
		clas = c;
		path = clas.getName();
		// get dimensions
		for (char ch : path.toCharArray()) {
			if (ch == '[') {
				arrayDims++;
			} else {
				break;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.misc.reference.scopes.Scope#getParent()
	 */
	@Override
	public Scope getParent() {
		return new ReflectedClassScope(clas.getEnclosingClass());
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.misc.reference.scopes.ClassScope#getPath()
	 */
	@Override
	public String calculatePath() {
		return "";
	}

	@Override
	protected ClassScope getClassDeclaration(String className, boolean recursive, boolean specific, boolean getClass) {
		for (Class<?> c : clas.getDeclaredClasses()) {
			if (specific == (getClass ^ c.isInterface())) {
				if (c.getSimpleName().equals(className))
					return new ReflectedClassScope(c);
			}
		}
		if (recursive) {
			for (Class<?> c : clas.getDeclaredClasses()) {
				ClassScope ret = new ReflectedClassScope(c).getClassDeclaration(className, recursive, specific, getClass);
				if (ret != null)
					return ret;
			}
		}
		return null;
	}


	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.misc.reference.scopes.ClassScope#
	 * getLocalFunctionDeclaration(java.lang.String)
	 */
	@Override
	public FunctionScope getLocalFunctionDeclaration(String name) {
		for(Method meth : clas.getDeclaredMethods()) {
			ReflectedFunctionScope rfs = new ReflectedFunctionScope(meth);
			if (rfs.getName().equals(name))
				return rfs;
		}
		return getLocalFunctionDeclarationUsingSubTypes(name);
	}

	// TODO Here onward

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.misc.reference.scopes.ClassScope#
	 * getLocalFunctionDeclarationUsingSubTypes(java.lang.String)
	 */
	@Override
	protected FunctionScope getLocalFunctionDeclarationUsingSubTypes(String name) {
		String funcName = name.substring(0, name.indexOf("("));
		ArrayList<String> parameters = Scope
				.getParametersAsList(name.substring(name.indexOf("(") + 1, name.lastIndexOf(")")));

		ClassScope[] parameterTypes = new ClassScope[parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			parameterTypes[i] = Scope.getClassScope(parameters.get(i));
		}

		ArrayList<Method> scopesWithMatchingNames = new ArrayList<>();

		// get scopes that share names
		for (Method meth : clas.getDeclaredMethods()) {
			if (meth.getName().equals(funcName)) {
				scopesWithMatchingNames.add(meth);
			}
		}


		// go over each check if the types are the same or
		functionSearch: for (Method meth : scopesWithMatchingNames) {
			Class<?>[] desiredParameterClasses = meth.getParameterTypes();
			String[] desiredParameters = new String[desiredParameterClasses.length];
			for (int i = 0; i < desiredParameterClasses.length; i++) {
				desiredParameters[i] = desiredParameterClasses[i].getName();
			}

			if (parameters.size() == desiredParameters.length) {
				// Check the parameters match
				for (int i = 0; i < parameters.size(); i++) {
					// if parameters types dont match, see if subtype
					if (!parameters.get(i).equals(desiredParameters[i])) {
						ClassScope desiredParameterType = Scope.getClassScope(desiredParameters[i]);
						// if parameter supplied is not a subtype of the desired
						// type, try next function
						if (!parameterTypes[i].isOfType(desiredParameterType)) {
							continue functionSearch;
						}
					}
				}
				// if parameters are all the same type or subtype of the desired
				// function, return that function.

				return new ReflectedFunctionScope(meth);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.misc.reference.scopes.ClassScope#getExtendsPath()
	 */
	@Override
	public String getExtendsPath() {
		if (clas.getSuperclass() != null)
			return clas.getSuperclass().getName();
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.misc.reference.scopes.ClassScope#getInterfaces()
	 */
	@Override
	public ArrayList<ClassScope> getInterfaces() {
		ArrayList<ClassScope> interfaces = new ArrayList<>();
		for (Class<?> c : clas.getInterfaces()) {
			interfaces.add(Scope.getClassScope(c.getName()));
		}

		return interfaces;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.misc.reference.scopes.ClassScope#
	 * getInterfaceDeclaration(java.lang.String)
	 */
	@Override
	protected ClassScope getInterfaceDeclaration(String name) {
		InterfaceDeclaration scope = (InterfaceDeclaration) getClassDeclaration(name, true, true, false);

		if (scope == null) {
			Class<?> c = clas.getEnclosingClass();
			while (c.getEnclosingClass() != null) {
				if (c.isInterface()) {
					if (c.getSimpleName().equals(name)) {
						try {
							return ReflectedClassScope.generateReflectedScope(c.getName());
						} catch (ClassNotFoundException e) {
							return null;
						}
					}
				} else
					return null;
			}
		}
		return scope;
	}

	public static ReflectedClassScope generateReflectedScope(String pathName) throws ClassNotFoundException {
		Class<?> c = Class.forName(pathName);

		return new ReflectedClassScope(c);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.misc.reference.scopes.ClassScope#resolveReferences()
	 */
	@Override
	protected boolean resolveReferences() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.misc.reference.scopes.Scope#addFunctionCall(java.lang.
	 * String, richousrick.cpmerge.parse.parser.java.comp.JavaParser.ExpressionContext,
	 * java.lang.String)
	 */
	@Override
	public FunctionCall addFunctionCall(String id, ExpressionContext ctx, String objectRef) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.misc.reference.scopes.Scope#addSymbol(java.lang.String,
	 * org.antlr.v4.runtime.ParserRuleContext, java.lang.String,
	 * java.util.ArrayList)
	 */
	@Override
	public Symbol addSymbol(String id, ParserRuleContext ctx, String type, ArrayList<String> modifiers) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.misc.reference.scopes.ClassScope#toString()
	 */
	@Override
	public String toString() {
		return clas.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.misc.reference.scopes.Scope#getSymbol(java.lang.String)
	 */
	@Override
	public Symbol getSymbol(String id) {
		try {
			return new ReflectedSymbol(clas.getDeclaredField(id), this);
		} catch (NoSuchFieldException | SecurityException e) {
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.misc.reference.scopes.ClassScope#isOfType(richousrick.cpmerge.parse.parser.
	 * java.misc.reference.scopes.ClassScope)
	 */
	@Override
	public boolean isOfType(ClassScope type) {
		// if both are part of java, use java check instead
		if (type instanceof ReflectedClassScope)
			return ((ReflectedClassScope) type).clas.isAssignableFrom(clas);
		else
			return super.isOfType(type);
	}

}
