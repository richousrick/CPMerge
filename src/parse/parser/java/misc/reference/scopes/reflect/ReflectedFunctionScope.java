package parse.parser.java.misc.reference.scopes.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.antlr.v4.runtime.ParserRuleContext;

import parse.parser.java.comp.JavaParser.ExpressionContext;
import parse.parser.java.misc.reference.FunctionCall;
import parse.parser.java.misc.reference.Symbol;
import parse.parser.java.misc.reference.scopes.FunctionScope;
import parse.parser.java.misc.reference.scopes.InnerScope;
/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class ReflectedFunctionScope extends FunctionScope {

	private final Method meth;

	/**
	 * Initializes the ReflectedFunctionScope class
	 * TODO Annotate constructor
	 * @param ctx
	 * @param parent
	 * @param name
	 * @param visibility
	 * @param parameters
	 */
	public ReflectedFunctionScope(Method meth) {
		super(null, null, getMethodName(meth), null, 0, null);
		this.meth = meth;
	}

	private static String getMethodName(Method meth) {
		String retName = meth.getName() + "(";
		String parameterName = "";
		for (Class<?> c : meth.getParameterTypes()) {
			parameterName += c.getSimpleName() + ", ";
		}
		if (parameterName.length() > 0) {
			parameterName = parameterName.substring(0, parameterName.length() - 2);
		}
		return retName + parameterName + ")";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.reference.scopes.ClassScope#resolveReferences()
	 */
	@Override
	protected boolean resolveReferences() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.reference.scopes.Scope#addFunctionCall(java.lang.
	 * String, parse.parser.java.comp.JavaParser.ExpressionContext,
	 * java.lang.String)
	 */
	@Override
	public FunctionCall addFunctionCall(String id, ExpressionContext ctx, String objectRef) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.reference.scopes.Scope#addSymbol(java.lang.String,
	 * org.antlr.v4.runtime.ParserRuleContext, java.lang.String,
	 * java.util.ArrayList)
	 */
	@Override
	public Symbol addSymbol(String id, ParserRuleContext ctx, String type, ArrayList<String> modifiers) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.reference.scopes.FunctionScope#createScope(org.
	 * antlr.v4.runtime.ParserRuleContext)
	 */
	@Override
	public InnerScope createScope(ParserRuleContext ctx) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.reference.scopes.FunctionScope#getReturnType()
	 */
	@Override
	public String getReturnType() {
		return meth.getReturnType().getName();
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.reference.scopes.FunctionScope#getPublicity()
	 */
	@Override
	public int getVisibility() {
		if (Modifier.isPublic(meth.getModifiers()))
			return 3;
		if (Modifier.isProtected(meth.getModifiers()))
			return 2;
		if (Modifier.isPrivate(meth.getModifiers()))
			return 0;
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.reference.scopes.FunctionScope#getPath()
	 */
	@Override
	public String getPath() {
		String path = meth.toString();
		return path.substring(path.indexOf(meth.getDeclaringClass().getName()));
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.reference.scopes.FunctionScope#toString()
	 */
	@Override
	public String toString() {
		return meth.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.reference.scopes.FunctionScope#calculatePath()
	 */
	@Override
	public String calculatePath() {
		return "";
	}
}
