package parse.parser.java.misc.reference.scopes;

import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.ParserRuleContext;

import parse.parser.java.comp.JavaParser.ClassDeclarationContext;
import parse.parser.java.comp.JavaParser.CompilationUnitContext;
import parse.parser.java.comp.JavaParser.ExpressionContext;
import parse.parser.java.misc.reference.FunctionCall;
import parse.parser.java.misc.reference.Symbol;

public class FileScope extends Scope {
	/**
	 * Contains the package path
	 */
	private final String packagePath;

	private final HashMap<String, String> imports;

	// imports

	/**
	 * Initializes the FileScope class
	 * TODO Annotate constructor
	 *
	 * @param id
	 * @param parent
	 * @param classScope
	 */
	public FileScope(CompilationUnitContext id, String packagePath, ArrayList<String> imports, String name) {
		super(id, null, name);
		this.packagePath = packagePath;
		this.imports = new HashMap<>();
		for (String importStr : imports) {
			String key = importStr.substring(importStr.lastIndexOf(".") + 1);
			this.imports.put(key, importStr);
		}
		path = packagePath;
	}

	public Scope createScope(ClassDeclarationContext ctx, String name) {
		return createScope(ctx, name, null, null);
	}

	public Scope createScope(ClassDeclarationContext ctx, String name, String extendsName,
			ArrayList<String> implementsPath) {
		ClassScope s = new ClassScope(ctx, this, name, extendsName, implementsPath);
		scopes.put(new ScopeKey(name), s);
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.Scope#getPath()
	 */
	@Override
	public String calculatePath() {
		return packagePath;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.Scope#addSymbol(java.lang.String,
	 * org.antlr.v4.runtime.ParserRuleContext, java.lang.String,
	 * java.util.ArrayList)
	 */
	@Override
	public Symbol addSymbol(String id, ParserRuleContext ctx, String type, ArrayList<String> modifiers) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.Scope#addFunctionCall(java.lang.String,
	 * parse.parser.java.comp.JavaParser.ExpressionContext,
	 * java.lang.String)
	 */
	@Override
	public FunctionCall addFunctionCall(String id, ExpressionContext ctx, String objectRef) {
		return null;
	}


	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.Scope#typeDeclared(java.lang.String)
	 */
	@Override
	public String getTypePath(String typeName) {
		String name;
		for (Scope s : scopes.values()) {
			name = s.getTypePath(typeName);
			if (name != null)
				return name;
		}
		return imports.get(typeName);
	}

	/**
	 * TODO Annotate method
	 *
	 * @param className
	 * @return
	 */
	public boolean hasImport(String className) {
		return imports.containsKey(className);
	}

	/**
	 * TODO Annotate method
	 *
	 * @param className
	 * @return
	 */
	public String getImport(String className) {
		return imports.get(className);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.Scope#getFunctionDeclaration(java.lang.String)
	 */
	@Override
	public FunctionScope getFunctionDeclaration(String name) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.Scope#getClassDeclaration(java.lang.String)
	 */
	@Override
	protected ClassScope getClassDeclaration(String name) {
		if (scopes.containsKey(new ScopeKey(name)))
			return (ClassScope) scopes.get(new ScopeKey(name));
		// check imports
		if (hasImport(name))
			return Scope.getClassScope(getImport(name));
		else {
			ClassScope s = Scope.getClassScope(packagePath + "." + name);
			if (s != null)
				return s;
		}
		return Scope.getClassScope(name);
	}

	/**
	 * @return the packagePath
	 */
	public String getPackagePath() {
		return packagePath;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.Scope#resolveReferences()
	 */
	@Override
	protected boolean resolveReferences() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = name + ".java";
		for (Scope s : scopes.values()) {
			str += "\n\t" + s.toString().replaceAll("\n", "\n\t");
		}
		return str;
	}


}