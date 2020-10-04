package richousrick.cpmerge.parse.parser.java.misc.reference;

import org.antlr.v4.runtime.ParserRuleContext;
import richousrick.cpmerge.merge.MergedFunction;
import richousrick.cpmerge.parse.parser.java.comp.JavaParser.MethodDeclarationContext;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class MergedFunctionPass {
	MethodDeclarationContext ctx;
	MergedFunction<ParserRuleContext> newFunc;
	/**
	 * Initializes the MergedFunctionPassOver class
	 * TODO Annotate constructor
	 * @param ctx
	 * @param newFunc
	 */
	public MergedFunctionPass(MethodDeclarationContext ctx, MergedFunction<ParserRuleContext> newFunc) {
		this.ctx = ctx;
		this.newFunc = newFunc;
	}

	/**
	 * @return the ctx
	 */
	public MethodDeclarationContext getCtx() {
		return ctx;
	}

	/**
	 * @return the newFunc
	 */
	public MergedFunction<ParserRuleContext> getNewFunc() {
		return newFunc;
	}



}
