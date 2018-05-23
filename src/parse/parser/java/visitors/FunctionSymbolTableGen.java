package parse.parser.java.visitors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.RuleNode;

import parse.parser.java.comp.JavaParser.BlockContext;
import parse.parser.java.comp.JavaParser.CatchClauseContext;
import parse.parser.java.comp.JavaParser.ClassBodyContext;
import parse.parser.java.comp.JavaParser.CreatorContext;
import parse.parser.java.comp.JavaParser.EnhancedForControlContext;
import parse.parser.java.comp.JavaParser.EnumBodyDeclarationsContext;
import parse.parser.java.comp.JavaParser.ExpressionContext;
import parse.parser.java.comp.JavaParser.ForInitContext;
import parse.parser.java.comp.JavaParser.FormalParameterContext;
import parse.parser.java.comp.JavaParser.InnerCreatorContext;
import parse.parser.java.comp.JavaParser.InterfaceBodyContext;
import parse.parser.java.comp.JavaParser.LambdaExpressionContext;
import parse.parser.java.comp.JavaParser.LastFormalParameterContext;
import parse.parser.java.comp.JavaParser.LocalTypeDeclarationContext;
import parse.parser.java.comp.JavaParser.LocalVariableDeclarationContext;
import parse.parser.java.comp.JavaParser.MethodDeclarationContext;
import parse.parser.java.comp.JavaParser.ResourceContext;
import parse.parser.java.comp.JavaParser.StatementContext;
import parse.parser.java.comp.JavaParser.VariableDeclaratorContext;
import parse.parser.java.comp.JavaParser.VariableDeclaratorIdContext;
import parse.parser.java.comp.JavaParserBaseVisitor;
import parse.parser.java.misc.validate.FunctionSymbols;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class FunctionSymbolTableGen extends JavaParserBaseVisitor<Boolean> {

	FunctionSymbols symbols;
	PrettyPrinter p;
	boolean inForInit;

	/**
	 * Initializes the FunctionSymbolTableGen class
	 * TODO Annotate constructor
	 */
	public FunctionSymbolTableGen() {
		symbols = new FunctionSymbols();
		p = new PrettyPrinter(false);
		inForInit = false;
	}

	public FunctionSymbols getSymbolTable(MethodDeclarationContext ctx) {
		visitMethodDeclaration(ctx);
		return symbols;
	}

	/* (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#visitMethodDeclaration(parse.parser.java.comp.JavaParser.MethodDeclarationContext)
	 */
	@Override
	public Boolean visitMethodDeclaration(MethodDeclarationContext ctx) {
		return enterScope(ctx);
	}

	/* (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#visitFormalParameter(parse.parser.java.comp.JavaParser.FormalParameterContext)
	 */
	@Override
	public Boolean visitFormalParameter(FormalParameterContext ctx) {
		String type = p.visit(ctx.typeType());
		String[] varaiableDeclarator = getvariableDeclaration(ctx.variableDeclaratorId());
		type += varaiableDeclarator[0];
		return symbols.addSymbol(varaiableDeclarator[1], 0, ctx, type);
	}


	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitBlock(parse.parser.java
	 * .comp.JavaParser.BlockContext)
	 */
	@Override
	public Boolean visitBlock(BlockContext ctx) {
		return enterScope(ctx);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitClassBody(parse.parser.
	 * java.comp.JavaParser.ClassBodyContext)
	 */
	@Override
	public Boolean visitClassBody(ClassBodyContext ctx) {
		return enterScope(ctx);
	}



	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitLocalVariableDeclaration(parse.parser.java.comp.JavaParser.
	 * LocalVariableDeclarationContext)
	 */
	@Override
	public Boolean visitLocalVariableDeclaration(LocalVariableDeclarationContext ctx) {
		if (ctx.typeType().classOrInterfaceType() != null)
			return invalidate();
		String type = p.visit(ctx.typeType());
		for (VariableDeclaratorContext vdctx : ctx.variableDeclarators().variableDeclarator()) {
			String[] varaiableDeclarator = getvariableDeclaration(vdctx.variableDeclaratorId());
			if (!symbols.addSymbol(varaiableDeclarator[1], inForInit ? 2 : 1, ctx, type + varaiableDeclarator[0]))
				return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitEnhancedForControl(
	 * parse.parser.java.comp.JavaParser.EnhancedForControlContext)
	 */
	@Override
	public Boolean visitEnhancedForControl(EnhancedForControlContext ctx) {
		String type = p.visit(ctx.typeType());
		String[] varaiableDeclarator = getvariableDeclaration(ctx.variableDeclaratorId());
		type += varaiableDeclarator[0];
		return symbols.addSymbol(varaiableDeclarator[1], 2, ctx, type);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitInterfaceBody(parse.
	 * parser.java.comp.JavaParser.InterfaceBodyContext)
	 */
	@Override
	public Boolean visitInterfaceBody(InterfaceBodyContext ctx) {
		return invalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitLastFormalParameter(
	 * parse.parser.java.comp.JavaParser.LastFormalParameterContext)
	 */
	@Override
	public Boolean visitLastFormalParameter(LastFormalParameterContext ctx) {
		return invalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#visitCatchClause(parse.
	 * parser.java.comp.JavaParser.CatchClauseContext)
	 */
	@Override
	public Boolean visitCatchClause(CatchClauseContext ctx) {
		return symbols.addSymbol(ctx.IDENTIFIER().getText(), 1, ctx, p.visit(ctx.catchType()));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitResource(parse.parser.
	 * java.comp.JavaParser.ResourceContext)
	 */
	@Override
	public Boolean visitResource(ResourceContext ctx) {
		String type = p.visit(ctx.classOrInterfaceType());
		String[] varaiableDeclarator = getvariableDeclaration(ctx.variableDeclaratorId());
		type += varaiableDeclarator[0];
		return symbols.addSymbol(varaiableDeclarator[1], 1, ctx, type);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitForInit(parse.parser.
	 * java.comp.JavaParser.ForInitContext)
	 */
	@Override
	public Boolean visitForInit(ForInitContext ctx) {
		inForInit = true;
		return visitChildren(ctx);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitCreator(parse.parser.
	 * java.comp.JavaParser.CreatorContext)
	 */
	@Override
	public Boolean visitCreator(CreatorContext ctx) {
		return invalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitLambdaExpression(parse.
	 * parser.java.comp.JavaParser.LambdaExpressionContext)
	 */
	@Override
	public Boolean visitLambdaExpression(LambdaExpressionContext ctx) {
		return invalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitEnumBodyDeclarations(
	 * parse.parser.java.comp.JavaParser.EnumBodyDeclarationsContext)
	 */
	@Override
	public Boolean visitEnumBodyDeclarations(EnumBodyDeclarationsContext ctx) {
		return invalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitInnerCreator(parse.
	 * parser.java.comp.JavaParser.InnerCreatorContext)
	 */
	@Override
	public Boolean visitInnerCreator(InnerCreatorContext ctx) {
		return invalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitLocalTypeDeclaration(
	 * parse.parser.java.comp.JavaParser.LocalTypeDeclarationContext)
	 */
	@Override
	public Boolean visitLocalTypeDeclaration(LocalTypeDeclarationContext ctx) {
		return invalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitExpression(parse.parser
	 * .java.comp.JavaParser.ExpressionContext)
	 */
	@Override
	public Boolean visitExpression(ExpressionContext ctx) {
		if (ctx.primary() != null)
			return checkExpressionIsValid(ctx);
		else {
			visitChildren(ctx);
			return symbols.isValid();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitStatement(parse.parser.
	 * java.comp.JavaParser.StatementContext)
	 */
	@Override
	public Boolean visitStatement(StatementContext ctx) {
		if (ctx.SWITCH() != null)
			return invalidate();
		else
			return visitChildren(ctx);
	}

	// NON VISITOR FUNCTIONS

	private boolean enterScope(ParserRuleContext ctx) {
		symbols.createScope();
		boolean val = visitChildren(ctx);
		symbols.leaveScope();
		return val;
	}

	private String[] getvariableDeclaration(VariableDeclaratorIdContext ctx) {
		String arrayParams = "";
		for(int i = 1; i < ctx.children.size(); i++) {
			arrayParams += ctx.getChild(i).getText();
		}
		return new String[] { arrayParams, ctx.IDENTIFIER().getText() };
	}

	private boolean invalidate() {
		symbols.setInvalid();
		return false;
	}

	private boolean checkExpressionIsValid(ExpressionContext e) {
		String name = null;
		try {
			name = e.primary().IDENTIFIER().getText();
		} catch (NullPointerException ex) {
			return true;
		}
		if (!symbols.exists(name))
			return invalidate();
		else
			return visitChildren(e);
	}

	// STOP PROCESSING IF INVALID

	/*
	 * (non-Javadoc)
	 * @see
	 * org.antlr.v4.runtime.tree.AbstractParseTreeVisitor#aggregateResult(java.
	 * lang.Object, java.lang.Object)
	 */
	@Override
	protected Boolean aggregateResult(Boolean aggregate, Boolean nextResult) {
		return symbols.isValid();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.antlr.v4.runtime.tree.AbstractParseTreeVisitor#shouldVisitNextChild(
	 * org.antlr.v4.runtime.tree.RuleNode, java.lang.Object)
	 */
	@Override
	protected boolean shouldVisitNextChild(RuleNode node, Boolean currentResult) {
		return symbols.isValid();
	}

}
