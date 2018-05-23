package parse.parser.java.misc.reference.scopes;

import java.util.ArrayList;

import org.antlr.v4.runtime.ParserRuleContext;

import parse.parser.java.comp.JavaParser.ExpressionContext;
import parse.parser.java.comp.JavaParser.InterfaceDeclarationContext;
import parse.parser.java.misc.reference.FunctionCall;
import parse.parser.java.misc.reference.Symbol;

public class InterfaceDeclaration extends ClassScope {

	/**
	 * Initializes the InterfaceDeclaration class
	 * TODO Annotate constructor
	 *
	 * @param id
	 * @param parent
	 * @param className
	 * @param extendType
	 * @param interfaces
	 */
	protected InterfaceDeclaration(InterfaceDeclarationContext id, Scope parent, String className,
			ArrayList<String> interfaces) {
		super(id, parent, className, null, interfaces);
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
	public ClassScope getClassDeclaration(String name) {
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
	 * @see parse.parser.java.misc.Scope#addSymbol(java.lang.String,
	 * org.antlr.v4.runtime.ParserRuleContext, java.lang.String,
	 * java.util.ArrayList)
	 */
	@Override
	public Symbol addSymbol(String id, ParserRuleContext ctx, String type, ArrayList<String> modifiers) {
		return null;
	}

}