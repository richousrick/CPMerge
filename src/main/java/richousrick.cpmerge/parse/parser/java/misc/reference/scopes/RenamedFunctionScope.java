package richousrick.cpmerge.parse.parser.java.misc.reference.scopes;

import org.antlr.v4.runtime.ParserRuleContext;
import richousrick.cpmerge.merge.MergedFunction;
import richousrick.cpmerge.parse.parser.java.comp.JavaParser.ClassBodyDeclarationContext;
import richousrick.cpmerge.parse.parser.java.comp.JavaParser.ExpressionContext;
import richousrick.cpmerge.parse.parser.java.comp.JavaParser.MethodDeclarationContext;
import richousrick.cpmerge.parse.parser.java.misc.reference.FunctionCall;
import richousrick.cpmerge.parse.parser.java.misc.reference.Symbol;
import richousrick.cpmerge.parse.parser.java.visitors.PrettyPrinter;

import java.util.ArrayList;
import java.util.Map.Entry;

/**
 * Used as a marker
 *
 * @author Rikkey Paal
 */
public class RenamedFunctionScope extends FunctionScope {

	MergedFunction<ParserRuleContext> node;

	/**
	 * Initializes the RenamedFunctionScope class
	 * TODO Annotate constructor
	 *
	 * @param ctx
	 * @param parent
	 * @param name
	 * @param parameters
	 * @param visibility
	 * @param parameters
	 */
	public RenamedFunctionScope(MergedFunction<ParserRuleContext> n, Scope s, String name,
			ArrayList<String> parameters) {
		super(null, s, name, getReturnType(n), getVisibility(n),
				parameters == null ? null : parameters.toArray(new String[parameters.size()]));
		node = n;
	}

	public MergedFunction<ParserRuleContext> getRootNode() {
		return node;
	}

	private static String getReturnType(MergedFunction<ParserRuleContext> n) {
		return new PrettyPrinter(false).visitTypeTypeOrVoid(
				((MethodDeclarationContext) n.getOriginalFunctionRoots().get(0).getNodeData()).typeTypeOrVoid());
	}

	private static int getVisibility(MergedFunction<ParserRuleContext> n) {
		int visibility = 1;
		if (n.getOriginalFunctionRoots().get(0).getNodeData().parent.parent instanceof ClassBodyDeclarationContext) {
			visibility = new PrettyPrinter(false).getVisibility(
					((ClassBodyDeclarationContext) n.getOriginalFunctionRoots().get(0).getNodeData().parent.parent)
							.modifier());
		}
		return visibility;
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
	 * richousrick.cpmerge.parse.parser.java.misc.reference.scopes.FunctionScope#createScope(org.
	 * antlr.v4.runtime.ParserRuleContext)
	 */
	@Override
	public InnerScope createScope(ParserRuleContext ctx) {
		return null;
	}

	/**
	 * TODO Annotate method
	 *
	 * @param functionScope
	 */
	public void setFunction(FunctionScope functionScope) {
		for (Entry<ScopeKey, Scope> e : functionScope.getScopes().entrySet()) {
			scopes.put(e.getKey(), e.getValue());
		}
		for (Entry<String, Symbol> e : functionScope.symbolTable.entrySet()) {
			symbolTable.put(e.getKey(), e.getValue());
		}
	}

}
