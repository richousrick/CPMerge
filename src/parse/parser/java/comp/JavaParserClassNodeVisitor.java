package parse.parser.java.comp;

import java.util.ArrayList;

import org.antlr.v4.runtime.tree.TerminalNode;

import dif.ClassNode;
import parse.parser.java.comp.JavaParser.ArgumentsContext;
import parse.parser.java.comp.JavaParser.ArrayCreatorRestContext;
import parse.parser.java.comp.JavaParser.ArrayInitializerContext;
import parse.parser.java.comp.JavaParser.BlockContext;
import parse.parser.java.comp.JavaParser.BlockStatementContext;
import parse.parser.java.comp.JavaParser.CatchClauseContext;
import parse.parser.java.comp.JavaParser.CatchTypeContext;
import parse.parser.java.comp.JavaParser.ClassBodyDeclarationContext;
import parse.parser.java.comp.JavaParser.ClassCreatorRestContext;
import parse.parser.java.comp.JavaParser.ClassDeclarationContext;
import parse.parser.java.comp.JavaParser.ClassOrInterfaceModifierContext;
import parse.parser.java.comp.JavaParser.ClassOrInterfaceTypeContext;
import parse.parser.java.comp.JavaParser.ClassTypeContext;
import parse.parser.java.comp.JavaParser.CompilationUnitContext;
import parse.parser.java.comp.JavaParser.CreatedNameContext;
import parse.parser.java.comp.JavaParser.CreatorContext;
import parse.parser.java.comp.JavaParser.EnhancedForControlContext;
import parse.parser.java.comp.JavaParser.ExplicitGenericInvocationContext;
import parse.parser.java.comp.JavaParser.ExplicitGenericInvocationSuffixContext;
import parse.parser.java.comp.JavaParser.ExpressionContext;
import parse.parser.java.comp.JavaParser.ExpressionListContext;
import parse.parser.java.comp.JavaParser.FinallyBlockContext;
import parse.parser.java.comp.JavaParser.FloatLiteralContext;
import parse.parser.java.comp.JavaParser.ForControlContext;
import parse.parser.java.comp.JavaParser.ForInitContext;
import parse.parser.java.comp.JavaParser.FormalParameterContext;
import parse.parser.java.comp.JavaParser.FormalParameterListContext;
import parse.parser.java.comp.JavaParser.InnerCreatorContext;
import parse.parser.java.comp.JavaParser.IntegerLiteralContext;
import parse.parser.java.comp.JavaParser.LambdaBodyContext;
import parse.parser.java.comp.JavaParser.LambdaExpressionContext;
import parse.parser.java.comp.JavaParser.LambdaParametersContext;
import parse.parser.java.comp.JavaParser.LastFormalParameterContext;
import parse.parser.java.comp.JavaParser.LiteralContext;
import parse.parser.java.comp.JavaParser.LocalTypeDeclarationContext;
import parse.parser.java.comp.JavaParser.LocalVariableDeclarationContext;
import parse.parser.java.comp.JavaParser.MemberDeclarationContext;
import parse.parser.java.comp.JavaParser.MethodDeclarationContext;
import parse.parser.java.comp.JavaParser.NonWildcardTypeArgumentsContext;
import parse.parser.java.comp.JavaParser.NonWildcardTypeArgumentsOrDiamondContext;
import parse.parser.java.comp.JavaParser.ParExpressionContext;
import parse.parser.java.comp.JavaParser.PrimaryContext;
import parse.parser.java.comp.JavaParser.PrimitiveTypeContext;
import parse.parser.java.comp.JavaParser.QualifiedNameContext;
import parse.parser.java.comp.JavaParser.ResourceContext;
import parse.parser.java.comp.JavaParser.ResourceSpecificationContext;
import parse.parser.java.comp.JavaParser.ResourcesContext;
import parse.parser.java.comp.JavaParser.StatementContext;
import parse.parser.java.comp.JavaParser.SuperSuffixContext;
import parse.parser.java.comp.JavaParser.SwitchBlockStatementGroupContext;
import parse.parser.java.comp.JavaParser.SwitchLabelContext;
import parse.parser.java.comp.JavaParser.TypeArgumentContext;
import parse.parser.java.comp.JavaParser.TypeArgumentsContext;
import parse.parser.java.comp.JavaParser.TypeArgumentsOrDiamondContext;
import parse.parser.java.comp.JavaParser.TypeDeclarationContext;
import parse.parser.java.comp.JavaParser.TypeListContext;
import parse.parser.java.comp.JavaParser.TypeTypeContext;
import parse.parser.java.comp.JavaParser.TypeTypeOrVoidContext;
import parse.parser.java.comp.JavaParser.VariableDeclaratorContext;
import parse.parser.java.comp.JavaParser.VariableDeclaratorIdContext;
import parse.parser.java.comp.JavaParser.VariableDeclaratorsContext;
import parse.parser.java.comp.JavaParser.VariableInitializerContext;
import parse.parser.java.comp.JavaParser.VariableModifierContext;

/**
 * TODO Annotate class
 * 
 * @author 146813
 */
public class JavaParserClassNodeVisitor extends JavaParserBaseVisitor<ClassNode> {

	private ArrayList<ClassNode> classes;

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitCompilationUnit(parse.
	 * parser.java.comp.JavaParser.CompilationUnitContext)
	 */
	@Override
	public ClassNode visitCompilationUnit(CompilationUnitContext ctx) {
		ClassNode root = new ClassNode(ctx, "root", (byte) 3);
		classes = new ArrayList<>();
		for (TypeDeclarationContext type : ctx.typeDeclaration()) {
			visitTypeDeclaration(type);
		}

		for (ClassNode c : classes) {
			root.addChild(c);
		}
		return root;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitTypeDeclaration(parse.
	 * parser.java.comp.JavaParser.TypeDeclarationContext)
	 */
	@Override
	public ClassNode visitTypeDeclaration(TypeDeclarationContext ctx) {
		if (ctx.classDeclaration() != null) {
			visitClassDeclaration(ctx.classDeclaration());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitClassDeclaration(parse.
	 * parser.java.comp.JavaParser.ClassDeclarationContext)
	 */
	@Override
	public ClassNode visitClassDeclaration(ClassDeclarationContext ctx) {
		ClassNode n = new ClassNode(ctx, ctx.IDENTIFIER().getText(), (byte) 0);
		for (ClassBodyDeclarationContext cbdx : ctx.classBody().classBodyDeclaration()) {
			ClassNode dec = visitClassBodyDeclaration(cbdx);
			if (dec != null) {
				n.addChild(dec);
			}
		}
		classes.add(n);
		return new ClassNode(ctx, "ClassDeclaration " + ctx.IDENTIFIER().getText());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitClassBodyDeclaration(
	 * parse.parser.java.comp.JavaParser.ClassBodyDeclarationContext)
	 */
	@Override
	public ClassNode visitClassBodyDeclaration(ClassBodyDeclarationContext ctx) {
		if (ctx.memberDeclaration() != null) {
			return visitMemberDeclaration(ctx.memberDeclaration());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitMemberDeclaration(parse
	 * .parser.java.comp.JavaParser.MemberDeclarationContext)
	 */
	@Override
	public ClassNode visitMemberDeclaration(MemberDeclarationContext ctx) {
		if (ctx.methodDeclaration() != null) {
			return visitMethodDeclaration(ctx.methodDeclaration());
		} else if (ctx.classDeclaration() != null) {
			return visitClassDeclaration(ctx.classDeclaration());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitMethodDeclaration(parse
	 * .parser.java.comp.JavaParser.MethodDeclarationContext)
	 */
	@Override
	public ClassNode visitMethodDeclaration(MethodDeclarationContext ctx) {
		if (ctx.methodBody().block() != null) {
			ClassNode methodNode = new ClassNode(ctx, ctx.IDENTIFIER().toString(), (byte) 1);
			for (ClassNode n : visitBlock(ctx.methodBody().block()).getChildrenAsCN()) {
				methodNode.addChild(n);
			}
			return methodNode;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitTypeTypeOrVoid(parse.
	 * parser.java.comp.JavaParser.TypeTypeOrVoidContext)
	 */
	@Override
	public ClassNode visitTypeTypeOrVoid(TypeTypeOrVoidContext ctx) {
		if (ctx.VOID() != null) {
			return new ClassNode(ctx, ctx.VOID().getText());
		} else {
			return visitTypeType(ctx.typeType());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitVariableDeclarators(
	 * parse.parser.java.comp.JavaParser.VariableDeclaratorsContext)
	 */
	@Override
	public ClassNode visitVariableDeclarators(VariableDeclaratorsContext ctx) {
		ClassNode c = new ClassNode(ctx, "VARIABLEDECLARATORS");
		for (VariableDeclaratorContext v : ctx.variableDeclarator()) {
			c.addChild(visitVariableDeclarator(v));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitVariableDeclarator(
	 * parse.parser.java.comp.JavaParser.VariableDeclaratorContext)
	 */
	@Override
	public ClassNode visitVariableDeclarator(VariableDeclaratorContext ctx) {
		ClassNode c = new ClassNode(ctx, "VARIABLEDECLARATOR");
		c.addChild(visitVariableDeclaratorId(ctx.variableDeclaratorId()));
		if (ctx.variableInitializer() != null) {
			c.addChild(visitVariableInitializer(ctx.variableInitializer()));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitVariableDeclaratorId(
	 * parse.parser.java.comp.JavaParser.VariableDeclaratorIdContext)
	 */
	@Override
	public ClassNode visitVariableDeclaratorId(VariableDeclaratorIdContext ctx) {
		String name = "";
		for (int i = 0; i < ctx.getChildCount(); i++) {
			name += ctx.getChild(i).getText();
		}
		return new ClassNode(ctx, name);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitVariableInitializer(
	 * parse.parser.java.comp.JavaParser.VariableInitializerContext)
	 */
	@Override
	public ClassNode visitVariableInitializer(VariableInitializerContext ctx) {
		if (ctx.arrayInitializer() != null) {
			return visitArrayInitializer(ctx.arrayInitializer());
		} else {
			return visitExpression(ctx.expression());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitArrayInitializer(parse.
	 * parser.java.comp.JavaParser.ArrayInitializerContext)
	 */
	@Override
	public ClassNode visitArrayInitializer(ArrayInitializerContext ctx) {
		ClassNode c = new ClassNode(ctx, "{}");
		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i) instanceof VariableInitializerContext) {
				c.addChild(visitVariableInitializer((VariableInitializerContext) ctx.getChild(i)));
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitClassOrInterfaceType(
	 * parse.parser.java.comp.JavaParser.ClassOrInterfaceTypeContext)
	 */
	@Override
	public ClassNode visitClassOrInterfaceType(ClassOrInterfaceTypeContext ctx) {
		ClassNode c = new ClassNode(ctx, "classOrInterfaceType");
		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				c.addChild((TerminalNode) ctx.getChild(i));
			} else if (ctx.getChild(i) instanceof TypeArgumentContext) {
				c.addChild(visitArguments((ArgumentsContext) ctx.getChild(i)));
			} else {
				System.err.println("unexpectedContents");
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitTypeArgument(parse.
	 * parser.java.comp.JavaParser.TypeArgumentContext)
	 */
	@Override
	public ClassNode visitTypeArgument(TypeArgumentContext ctx) {
		ClassNode c = new ClassNode(ctx, "TypeArgument");
		if (ctx.getChild(0) instanceof TypeTypeContext) {
			c.addChild(visitTypeType(ctx.typeType()));
		} else {
			if (ctx.typeType() != null) {
				if (ctx.EXTENDS() != null) {
					c.addChild("? " + ctx.EXTENDS().getText());
				} else if (ctx.SUPER() != null) {
					c.addChild("? " + ctx.SUPER().getText());
				}
				c.addChild(visitTypeType(ctx.typeType()));
			} else {
				c.addChild("?");
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitFormalParameterList(
	 * parse.parser.java.comp.JavaParser.FormalParameterListContext)
	 */
	@Override
	public ClassNode visitFormalParameterList(FormalParameterListContext ctx) {
		ClassNode c = new ClassNode(ctx, "FormalParameterList");
		if (ctx.formalParameter() != null) {
			for (int i = 0; i < ctx.getChildCount(); i++) {
				if (ctx.getChild(i) instanceof FormalParameterContext) {
					c.addChild(visitFormalParameter((FormalParameterContext) ctx.getChild(i)));
				} else if (ctx.getChild(i) instanceof LastFormalParameterContext) {
					c.addChild(visitLastFormalParameter(ctx.lastFormalParameter()));
				}
			}
		} else {
			c.addChild(visitLastFormalParameter(ctx.lastFormalParameter()));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitFormalParameter(parse.
	 * parser.java.comp.JavaParser.FormalParameterContext)
	 */
	@Override
	public ClassNode visitFormalParameter(FormalParameterContext ctx) {
		ClassNode c = new ClassNode(ctx, "FormalParameter");
		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i) instanceof VariableModifierContext) {
				c.addChild(visitVariableModifier((VariableModifierContext) ctx.getChild(i)));
			} else if (ctx.getChild(i) instanceof TypeTypeContext) {
				c.addChild(visitTypeType((TypeTypeContext) ctx.getChild(i)));
			} else if (ctx.getChild(i) instanceof VariableDeclaratorIdContext) {
				c.addChild(visitVariableDeclaratorId((VariableDeclaratorIdContext) ctx.getChild(i)));
			}
		}
		return c;

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitLastFormalParameter(
	 * parse.parser.java.comp.JavaParser.LastFormalParameterContext)
	 */
	@Override
	public ClassNode visitLastFormalParameter(LastFormalParameterContext ctx) {
		ClassNode c = new ClassNode(ctx, "LastFormalParameter");
		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i) instanceof VariableModifierContext) {
				c.addChild(visitVariableModifier((VariableModifierContext) ctx.getChild(i)));
			} else if (ctx.getChild(i) instanceof TypeTypeContext) {
				c.addChild(visitTypeType((TypeTypeContext) ctx.getChild(i)));
			} else if (ctx.getChild(i) instanceof VariableDeclaratorIdContext) {
				c.addChild(visitVariableDeclaratorId((VariableDeclaratorIdContext) ctx.getChild(i)));
			} else if (ctx.getChild(i) instanceof TerminalNode) {
				c.addChild(ctx.getChild(i).getText());
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitLiteral(parse.parser.
	 * java.comp.JavaParser.LiteralContext)
	 */
	@Override
	public ClassNode visitLiteral(LiteralContext ctx) {
		if (ctx.CHAR_LITERAL() != null) {
			return new ClassNode(ctx, ctx.CHAR_LITERAL().getText());
		} else if (ctx.STRING_LITERAL() != null) {
			return new ClassNode(ctx, ctx.STRING_LITERAL().getText());
		} else if (ctx.BOOL_LITERAL() != null) {
			return new ClassNode(ctx, ctx.BOOL_LITERAL().getText());
		} else if (ctx.NULL_LITERAL() != null) {
			return new ClassNode(ctx, ctx.NULL_LITERAL().getText());
		} else if (ctx.integerLiteral() != null) {
			return visitIntegerLiteral(ctx.integerLiteral());
		} else if (ctx.floatLiteral() != null) {
			return visitFloatLiteral(ctx.floatLiteral());
		} else {
			throw new IllegalArgumentException("Unexpected literal declaration");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitIntegerLiteral(parse.
	 * parser.java.comp.JavaParser.IntegerLiteralContext)
	 */
	@Override
	public ClassNode visitIntegerLiteral(IntegerLiteralContext ctx) {
		if (ctx.DECIMAL_LITERAL() != null) {
			return new ClassNode(ctx, ctx.DECIMAL_LITERAL().getText());
		} else if (ctx.HEX_LITERAL() != null) {
			return new ClassNode(ctx, ctx.HEX_LITERAL().getText());
		} else if (ctx.OCT_LITERAL() != null) {
			return new ClassNode(ctx, ctx.OCT_LITERAL().getText());
		} else if (ctx.BINARY_LITERAL() != null) {
			return new ClassNode(ctx, ctx.BINARY_LITERAL().getText());
		} else {
			throw new IllegalArgumentException("Unexpected integer literal declaration");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitFloatLiteral(parse.
	 * parser.java.comp.JavaParser.FloatLiteralContext)
	 */
	@Override
	public ClassNode visitFloatLiteral(FloatLiteralContext ctx) {
		if (ctx.FLOAT_LITERAL() != null) {
			return new ClassNode(ctx, ctx.FLOAT_LITERAL().getText());
		} else if (ctx.HEX_FLOAT_LITERAL() != null) {
			return new ClassNode(ctx, ctx.HEX_FLOAT_LITERAL().getText());
		} else {
			throw new IllegalArgumentException("Unexpected float literal declaration");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitBlock(parse.parser.java
	 * .comp.JavaParser.BlockContext)
	 */
	@Override
	public ClassNode visitBlock(BlockContext ctx) {
		ClassNode c = new ClassNode(ctx, "Block");
		for (BlockStatementContext statement : ctx.blockStatement()) {
			c.addChild(visitBlockStatement(statement));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitBlockStatement(parse.
	 * parser.java.comp.JavaParser.BlockStatementContext)
	 */
	@Override
	public ClassNode visitBlockStatement(BlockStatementContext ctx) {
		ClassNode c = new ClassNode(ctx, "BlockStatement");
		if (ctx.localTypeDeclaration() != null) {
			c.addChild(visitLocalTypeDeclaration(ctx.localTypeDeclaration()));
		} else if (ctx.statement() != null) {
			c.addChild(visitStatement(ctx.statement()));
		}
		if (ctx.localVariableDeclaration() != null) {
			c.addChild(visitLocalVariableDeclaration(ctx.localVariableDeclaration()));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitLocalVariableDeclaration(parse.parser.java.comp.JavaParser.
	 * LocalVariableDeclarationContext)
	 */
	@Override
	public ClassNode visitLocalVariableDeclaration(LocalVariableDeclarationContext ctx) {
		ClassNode c = new ClassNode(ctx, "LocalVariableDeclaration");
		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i) instanceof VariableModifierContext) {
				c.addChild(visitVariableModifier((VariableModifierContext) ctx.getChild(i)));
			} else if (ctx.getChild(i) instanceof TypeTypeContext) {
				c.addChild(visitTypeType((TypeTypeContext) ctx.getChild(i)));
			} else if (ctx.getChild(i) instanceof VariableDeclaratorsContext) {
				c.addChild(visitVariableDeclarators((VariableDeclaratorsContext) ctx.getChild(i)));
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitLocalTypeDeclaration(
	 * parse.parser.java.comp.JavaParser.LocalTypeDeclarationContext)
	 */
	@Override
	public ClassNode visitLocalTypeDeclaration(LocalTypeDeclarationContext ctx) {
		ClassNode c = new ClassNode(ctx, "localTypeDeclaration");
		if (ctx.getChild(0) instanceof TerminalNode) {
			c.addChild(ctx.getChild(0).getText());
		} else {
			for (ClassOrInterfaceModifierContext coimc : ctx.classOrInterfaceModifier()) {
				c.addChild(visitClassOrInterfaceModifier(coimc));
			}
			if (ctx.classDeclaration() != null) {
				c.addChild(visitClassDeclaration(ctx.classDeclaration()));
			} else {
				c.addChild(new ClassNode(ctx.interfaceDeclaration(),
						"InterfaceDeclaration " + ctx.interfaceDeclaration().IDENTIFIER()));
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitStatement(parse.parser.
	 * java.comp.JavaParser.StatementContext)
	 */
	@Override
	public ClassNode visitStatement(StatementContext ctx) {
		ClassNode c = new ClassNode(ctx, "statement");
		if (ctx.ASSERT() != null) {
			c.addChild(ctx.ASSERT());
			for (ExpressionContext exp : ctx.expression()) {
				c.addChild(visitExpression(exp));
			}
		} else if (ctx.IF() != null) {
			c.addChild(ctx.IF());
			c.addChild(visitParExpression(ctx.parExpression()));
			c.addChild(visitStatement(ctx.statement(0)));
			if (ctx.ELSE() != null) {
				c.addChild(ctx.ELSE());
				c.addChild(visitStatement(ctx.statement(1)));
			}
		} else if (ctx.FOR() != null) {
			c.addChild(ctx.FOR());
			c.addChild(visitForControl(ctx.forControl()));
			c.addChild(visitStatement(ctx.statement(0)));
		} else if (ctx.DO() != null) {
			c.addChild(ctx.DO());
			c.addChild(visitStatement(ctx.statement(0)));
			c.addChild(ctx.WHILE());
			c.addChild(visitParExpression(ctx.parExpression()));
		} else if (ctx.WHILE() != null) {
			c.addChild(ctx.WHILE());
			c.addChild(visitParExpression(ctx.parExpression()));
			c.addChild(visitStatement(ctx.statement(0)));
		} else if (ctx.TRY() != null) {
			c.addChild(ctx.TRY());
			if (ctx.resourceSpecification() != null) {
				c.addChild(visitResourceSpecification(ctx.resourceSpecification()));
				c.addChild(visitBlock(ctx.block()));
				for (CatchClauseContext ccc : ctx.catchClause()) {
					c.addChild(visitCatchClause(ccc));
				}
				if (ctx.finallyBlock() != null) {
					c.addChild(visitFinallyBlock(ctx.finallyBlock()));
				}
			} else {
				c.addChild(visitBlock(ctx.block()));
				for (CatchClauseContext ccc : ctx.catchClause()) {
					c.addChild(visitCatchClause(ccc));
				}
				if (ctx.finallyBlock() != null) {
					c.addChild(visitFinallyBlock(ctx.finallyBlock()));
				}
			}
		} else if (ctx.SWITCH() != null) {
			c.addChild(ctx.SWITCH());
			c.addChild(visitParExpression(ctx.parExpression()));
			for (SwitchBlockStatementGroupContext sbsgc : ctx.switchBlockStatementGroup()) {
				c.addChild(visitSwitchBlockStatementGroup(sbsgc));
			}
			for (SwitchLabelContext sl : ctx.switchLabel()) {
				c.addChild(visitSwitchLabel(sl));
			}
		} else if (ctx.SYNCHRONIZED() != null) {
			c.addChild(ctx.SYNCHRONIZED());
			c.addChild(visitParExpression(ctx.parExpression()));
			c.addChild(visitBlock(ctx.block()));
		} else if (ctx.RETURN() != null) {
			c.addChild(ctx.RETURN());
			if (ctx.expression(0) != null) {
				c.addChild(visitExpression(ctx.expression(0)));
			}
		} else if (ctx.THROW() != null) {
			c.addChild(ctx.THROW());
			c.addChild(visitExpression(ctx.expression(0)));
		} else if (ctx.BREAK() != null) {
			c.addChild(ctx.BREAK());
			if (ctx.IDENTIFIER() != null) {
				c.addChild(ctx.IDENTIFIER());
			}
		} else if (ctx.CONTINUE() != null) {
			c.addChild(ctx.CONTINUE());
			if (ctx.IDENTIFIER() != null) {
				c.addChild(ctx.IDENTIFIER());
			}
		} else {
			if (ctx.blockLabel != null) {
				c.addChild(visitBlock(ctx.blockLabel));
			} else if (ctx.statementExpression != null) {
				c.addChild(visitExpression(ctx.statementExpression));
			} else if (ctx.IDENTIFIER() != null) {
				c.addChild(ctx.IDENTIFIER());
				c.addChild(visitStatement(ctx.statement(0)));
			} else if (ctx.SEMI() != null) {
				c.addChild(ctx.SEMI());
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#visitCatchClause(parse.
	 * parser.java.comp.JavaParser.CatchClauseContext)
	 */
	@Override
	public ClassNode visitCatchClause(CatchClauseContext ctx) {
		ClassNode c = new ClassNode(ctx, "CatchClause");
		c.addChild(ctx.CATCH());
		for (VariableModifierContext vm : ctx.variableModifier()) {
			c.addChild(visitVariableModifier(vm));
		}
		c.addChild(visitCatchType(ctx.catchType()));
		c.addChild(ctx.IDENTIFIER());
		c.addChild(visitBlock(ctx.block()));
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitCatchType(parse.parser.
	 * java.comp.JavaParser.CatchTypeContext)
	 */
	@Override
	public ClassNode visitCatchType(CatchTypeContext ctx) {
		ClassNode c = new ClassNode(ctx, "CatchType");
		for (QualifiedNameContext qn : ctx.qualifiedName()) {
			c.addChild(visitQualifiedName(qn));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitQualifiedName(parse.
	 * parser.java.comp.JavaParser.QualifiedNameContext)
	 */
	@Override
	public ClassNode visitQualifiedName(QualifiedNameContext ctx) {
		String s = "";
		for (TerminalNode t : ctx.IDENTIFIER()) {
			s += t.getText() + ".";
		}
		return new ClassNode(ctx, s.substring(0, s.length() - 1));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitFinallyBlock(parse.
	 * parser.java.comp.JavaParser.FinallyBlockContext)
	 */
	@Override
	public ClassNode visitFinallyBlock(FinallyBlockContext ctx) {
		ClassNode c = new ClassNode(ctx, "FinallyBlock");
		c.addChild(ctx.FINALLY());
		c.addChild(visitBlock(ctx.block()));
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitResourceSpecification(
	 * parse.parser.java.comp.JavaParser.ResourceSpecificationContext)
	 */
	@Override
	public ClassNode visitResourceSpecification(ResourceSpecificationContext ctx) {
		ClassNode c = new ClassNode(ctx, "ResourceSpecification");
		if (ctx.resources() != null) {
			c.addChild(visitResources(ctx.resources()));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitResources(parse.parser.
	 * java.comp.JavaParser.ResourcesContext)
	 */
	@Override
	public ClassNode visitResources(ResourcesContext ctx) {
		ClassNode c = new ClassNode(ctx, "Resources");
		for (ResourceContext rc : ctx.resource()) {
			c.addChild(visitResource(rc));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitResource(parse.parser.
	 * java.comp.JavaParser.ResourceContext)
	 */
	@Override
	public ClassNode visitResource(ResourceContext ctx) {
		ClassNode c = new ClassNode(ctx, "Resource");
		for (VariableModifierContext vmc : ctx.variableModifier()) {
			c.addChild(visitVariableModifier(vmc));
		}
		c.addChild(visitClassOrInterfaceType(ctx.classOrInterfaceType()));
		c.addChild(visitVariableDeclaratorId(ctx.variableDeclaratorId()));
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitSwitchBlockStatementGroup(parse.parser.java.comp.JavaParser.
	 * SwitchBlockStatementGroupContext)
	 */
	@Override
	public ClassNode visitSwitchBlockStatementGroup(SwitchBlockStatementGroupContext ctx) {
		ClassNode c = new ClassNode(ctx, "SwitchBlockStatementGroup");
		for (SwitchLabelContext slc : ctx.switchLabel()) {
			c.addChild(visitSwitchLabel(slc));
		}
		for (BlockStatementContext bsc : ctx.blockStatement()) {
			c.addChild(visitBlockStatement(bsc));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#visitSwitchLabel(parse.
	 * parser.java.comp.JavaParser.SwitchLabelContext)
	 */
	@Override
	public ClassNode visitSwitchLabel(SwitchLabelContext ctx) {
		ClassNode c = new ClassNode(ctx, "SwitchLabel");
		if (ctx.DEFAULT() != null) {
			c.addChild(ctx.DEFAULT().getText() + ":");
		} else {
			c.addChild(ctx.CASE());
			if (ctx.expression() != null) {
				c.addChild(visitExpression(ctx.expression()));
			} else {
				c.addChild(ctx.IDENTIFIER().getText() + ":");
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitForControl(parse.parser
	 * .java.comp.JavaParser.ForControlContext)
	 */
	@Override
	public ClassNode visitForControl(ForControlContext ctx) {
		ClassNode c = new ClassNode(ctx, "ForControl");
		if (ctx.enhancedForControl() != null) {
			c.addChild(visitEnhancedForControl(ctx.enhancedForControl()));
		} else {
			if (ctx.forInit() != null) {
				c.addChild(visitForInit(ctx.forInit()));
			}
			if (ctx.expression() != null) {
				c.addChild(visitExpression(ctx.expression()));
			}
			if (ctx.expressionList() != null) {
				c.addChild(visitExpressionList(ctx.expressionList()));
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitForInit(parse.parser.
	 * java.comp.JavaParser.ForInitContext)
	 */
	@Override
	public ClassNode visitForInit(ForInitContext ctx) {
		ClassNode c = new ClassNode(ctx, "ForInit");
		if (ctx.localVariableDeclaration() != null) {
			c.addChild(visitLocalVariableDeclaration(ctx.localVariableDeclaration()));
		} else {
			c.addChild(visitExpressionList(ctx.expressionList()));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitEnhancedForControl(
	 * parse.parser.java.comp.JavaParser.EnhancedForControlContext)
	 */
	@Override
	public ClassNode visitEnhancedForControl(EnhancedForControlContext ctx) {
		ClassNode c = new ClassNode(ctx, "EnhancedForControl");
		for (VariableModifierContext vmc : ctx.variableModifier()) {
			c.addChild(visitVariableModifier(vmc));
		}
		c.addChild(visitTypeType(ctx.typeType()));
		c.addChild(visitVariableDeclaratorId(ctx.variableDeclaratorId()));
		c.addChild(visitExpression(ctx.expression()));
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitParExpression(parse.
	 * parser.java.comp.JavaParser.ParExpressionContext)
	 */
	@Override
	public ClassNode visitParExpression(ParExpressionContext ctx) {
		ClassNode c = new ClassNode(ctx, "ParExpression");
		c.addChild(visitExpression(ctx.expression()));
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitExpressionList(parse.
	 * parser.java.comp.JavaParser.ExpressionListContext)
	 */
	@Override
	public ClassNode visitExpressionList(ExpressionListContext ctx) {
		ClassNode c = new ClassNode(ctx, "ExpressionList");
		for (ExpressionContext exp : ctx.expression()) {
			c.addChild(visitExpression(exp));
		}
		return c;

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitExpression(parse.parser
	 * .java.comp.JavaParser.ExpressionContext)
	 */
	@Override
	public ClassNode visitExpression(ExpressionContext ctx) {
		ClassNode c = new ClassNode(ctx, "Expression");
		if (ctx.primary() != null) {
			c.addChild(visitPrimary(ctx.primary()));
		} else if (ctx.bop != null && ctx.bop.getText().equals(".")) {
			c.addChild(visitExpression(ctx.expression(0)));
			if (ctx.IDENTIFIER() != null) {
				c.addChild(ctx.IDENTIFIER());
			} else if (ctx.THIS() != null) {
				c.addChild(ctx.THIS());
			} else if (ctx.NEW() != null) {
				c.addChild(ctx.NEW());
				if (ctx.nonWildcardTypeArguments() != null) {
					c.addChild(visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments()));
				}
				c.addChild(visitInnerCreator(ctx.innerCreator()));
			} else if (ctx.SUPER() != null) {
				c.addChild(ctx.SUPER());
				c.addChild(visitSuperSuffix(ctx.superSuffix()));
			} else {
				c.addChild(visitExplicitGenericInvocation(ctx.explicitGenericInvocation()));
			}
		} else {
			for (int i = 0; i < ctx.getChildCount(); i++) {
				if (ctx.getChild(i) instanceof ExpressionContext) {
					c.addChild(visitExpression((ExpressionContext) ctx.getChild(i)));
				} else if (ctx.getChild(i) instanceof TerminalNode) {
					c.addChild((TerminalNode) ctx.getChild(i));
				} else if (ctx.getChild(i) instanceof ExpressionListContext) {
					c.addChild(visitExpressionList((ExpressionListContext) ctx.getChild(i)));
				} else if (ctx.getChild(i) instanceof CreatorContext) {
					c.addChild(visitCreator((CreatorContext) ctx.getChild(i)));
				} else if (ctx.getChild(i) instanceof TypeTypeContext) {
					c.addChild(visitTypeType((TypeTypeContext) ctx.getChild(i)));
				} else if (ctx.getChild(i) instanceof LambdaExpressionContext) {
					c.addChild(visitLambdaExpression((LambdaExpressionContext) ctx.getChild(i)));
				} else if (ctx.getChild(i) instanceof TypeArgumentsContext) {
					c.addChild(visitTypeArguments((TypeArgumentsContext) ctx.getChild(i)));
				} else if (ctx.getChild(i) instanceof ClassTypeContext) {
					c.addChild(visitClassType((ClassTypeContext) ctx.getChild(i)));
				}
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitLambdaExpression(parse.
	 * parser.java.comp.JavaParser.LambdaExpressionContext)
	 */
	@Override
	public ClassNode visitLambdaExpression(LambdaExpressionContext ctx) {
		ClassNode c = new ClassNode(ctx, "LambdaExpression");
		c.addChild(visitLambdaParameters(ctx.lambdaParameters()));
		c.addChild(visitLambdaBody(ctx.lambdaBody()));
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitLambdaParameters(parse.
	 * parser.java.comp.JavaParser.LambdaParametersContext)
	 */
	@Override
	public ClassNode visitLambdaParameters(LambdaParametersContext ctx) {
		ClassNode c = new ClassNode(ctx, "LambdaParameters");
		for (TerminalNode tn : ctx.IDENTIFIER()) {
			c.addChild(tn);
		}
		if (ctx.formalParameterList() != null) {
			c.addChild(visitFormalParameterList(ctx.formalParameterList()));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitLambdaBody(parse.parser
	 * .java.comp.JavaParser.LambdaBodyContext)
	 */
	@Override
	public ClassNode visitLambdaBody(LambdaBodyContext ctx) {
		ClassNode c = new ClassNode(ctx, "LambdaBody");
		if (ctx.expression() != null) {
			c.addChild(visitExpression(ctx.expression()));
		} else {
			c.addChild(visitBlock(ctx.block()));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitPrimary(parse.parser.
	 * java.comp.JavaParser.PrimaryContext)
	 */
	@Override
	public ClassNode visitPrimary(PrimaryContext ctx) {
		ClassNode c = new ClassNode(ctx, "Primary");
		if (ctx.expression() != null) {
			c.addChild(visitExpression(ctx.expression()));
		} else if (ctx.SUPER() != null) {
			c.addChild(ctx.SUPER());
		} else if (ctx.literal() != null) {
			c.addChild(visitLiteral(ctx.literal()));
		} else if (ctx.IDENTIFIER() != null) {
			c.addChild(ctx.IDENTIFIER());
		} else if (ctx.typeTypeOrVoid() != null) {
			c.addChild(visitTypeTypeOrVoid(ctx.typeTypeOrVoid()));
			c.addChild(ctx.CLASS());
		} else if (ctx.nonWildcardTypeArguments() != null) {
			c.addChild(visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments()));
			if (ctx.explicitGenericInvocationSuffix() != null) {
				c.addChild(visitExplicitGenericInvocationSuffix(ctx.explicitGenericInvocationSuffix()));
			} else {
				c.addChild(ctx.THIS());
				c.addChild(visitArguments(ctx.arguments()));
			}
		} else {
			c.addChild(ctx.THIS());
		}
		return c;

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitClassType(parse.parser.
	 * java.comp.JavaParser.ClassTypeContext)
	 */
	@Override
	public ClassNode visitClassType(ClassTypeContext ctx) {
		ClassNode c = new ClassNode(ctx, "ClassType");
		if (ctx.classOrInterfaceType() != null) {
			c.addChild(visitClassOrInterfaceType(ctx.classOrInterfaceType()));
		}
		c.addChild(ctx.IDENTIFIER());
		if (ctx.typeArguments() != null) {
			c.addChild(visitTypeArguments(ctx.typeArguments()));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitCreator(parse.parser.
	 * java.comp.JavaParser.CreatorContext)
	 */
	@Override
	public ClassNode visitCreator(CreatorContext ctx) {
		ClassNode c = new ClassNode(ctx, "Creator");
		if (ctx.nonWildcardTypeArguments() != null) {
			c.addChild(visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments()));
			c.addChild(visitCreatedName(ctx.createdName()));
			c.addChild(visitClassCreatorRest(ctx.classCreatorRest()));
		} else {
			c.addChild(visitCreatedName(ctx.createdName()));
			if (ctx.classCreatorRest() != null) {
				c.addChild(visitClassCreatorRest(ctx.classCreatorRest()));
			} else {
				c.addChild(visitArrayCreatorRest(ctx.arrayCreatorRest()));
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#visitCreatedName(parse.
	 * parser.java.comp.JavaParser.CreatedNameContext)
	 */
	@Override
	public ClassNode visitCreatedName(CreatedNameContext ctx) {
		ClassNode c = new ClassNode(ctx, "CreatedName");
		if (ctx.primitiveType() != null) {
			c.addChild(visitPrimitiveType(ctx.primitiveType()));
		} else {
			for (int i = 0; i < ctx.getChildCount(); i++) {
				if (ctx.getChild(i) instanceof TerminalNode) {
					c.addChild((TerminalNode) ctx.getChild(i));
				} else if (ctx.getChild(i) instanceof TypeArgumentsOrDiamondContext) {
					c.addChild(visitTypeArgumentsOrDiamond((TypeArgumentsOrDiamondContext) ctx.getChild(i)));
				}
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitInnerCreator(parse.
	 * parser.java.comp.JavaParser.InnerCreatorContext)
	 */
	@Override
	public ClassNode visitInnerCreator(InnerCreatorContext ctx) {
		ClassNode c = new ClassNode(ctx, "InnerCreator");
		c.addChild(ctx.IDENTIFIER());
		if (ctx.nonWildcardTypeArgumentsOrDiamond() != null) {
			c.addChild(visitNonWildcardTypeArgumentsOrDiamond(ctx.nonWildcardTypeArgumentsOrDiamond()));
		}
		c.addChild(visitClassCreatorRest(ctx.classCreatorRest()));
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitArrayCreatorRest(parse.
	 * parser.java.comp.JavaParser.ArrayCreatorRestContext)
	 */
	@Override
	public ClassNode visitArrayCreatorRest(ArrayCreatorRestContext ctx) {
		ClassNode c = new ClassNode(ctx, "ArrayCreatorRest");
		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				c.addChild((TerminalNode) ctx.getChild(i));
			} else if (ctx.getChild(i) instanceof ArrayInitializerContext) {
				c.addChild(visitArrayInitializer((ArrayInitializerContext) ctx.getChild(i)));
			} else if (ctx.getChild(i) instanceof ExpressionContext) {
				c.addChild(visitExpression((ExpressionContext) ctx.getChild(i)));
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitClassCreatorRest(parse.
	 * parser.java.comp.JavaParser.ClassCreatorRestContext)
	 */
	@Override
	public ClassNode visitClassCreatorRest(ClassCreatorRestContext ctx) {
		ClassNode c = new ClassNode(ctx, "ClassCreatorRest");
		c.addChild(visitArguments(ctx.arguments()));
		if (ctx.classBody() != null) {
			c.addChild(visitClassBody(ctx.classBody()));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitExplicitGenericInvocation(parse.parser.java.comp.JavaParser.
	 * ExplicitGenericInvocationContext)
	 */
	@Override
	public ClassNode visitExplicitGenericInvocation(ExplicitGenericInvocationContext ctx) {
		ClassNode c = new ClassNode(ctx, "ExplicitGenericInvocation");
		c.addChild(visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments()));
		c.addChild(visitExplicitGenericInvocationSuffix(ctx.explicitGenericInvocationSuffix()));
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitTypeArgumentsOrDiamond(
	 * parse.parser.java.comp.JavaParser.TypeArgumentsOrDiamondContext)
	 */
	@Override
	public ClassNode visitTypeArgumentsOrDiamond(TypeArgumentsOrDiamondContext ctx) {
		ClassNode c = new ClassNode(ctx, "TypeArgumentsOrDiamond");
		if (ctx.typeArguments() != null) {
			c.addChild(visitTypeArguments(ctx.typeArguments()));
		} else {
			c.addChild("<>");
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitNonWildcardTypeArgumentsOrDiamond(parse.parser.java.comp.JavaParser.
	 * NonWildcardTypeArgumentsOrDiamondContext)
	 */
	@Override
	public ClassNode visitNonWildcardTypeArgumentsOrDiamond(NonWildcardTypeArgumentsOrDiamondContext ctx) {
		ClassNode c = new ClassNode(ctx, "NonWildcardTypeArgumentsOrDiamond");
		if (ctx.nonWildcardTypeArguments() != null) {
			c.addChild(visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments()));
		} else {
			c.addChild("<>");
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitNonWildcardTypeArguments(parse.parser.java.comp.JavaParser.
	 * NonWildcardTypeArgumentsContext)
	 */
	@Override
	public ClassNode visitNonWildcardTypeArguments(NonWildcardTypeArgumentsContext ctx) {
		ClassNode c = new ClassNode(ctx, "NonWildcardTypeArguments");
		c.addChild(visitTypeList(ctx.typeList()));
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitTypeList(parse.parser.
	 * java.comp.JavaParser.TypeListContext)
	 */
	@Override
	public ClassNode visitTypeList(TypeListContext ctx) {
		ClassNode c = new ClassNode(ctx, "TypeList");
		for (TypeTypeContext ttc : ctx.typeType()) {
			c.addChild(visitTypeType(ttc));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitTypeType(parse.parser.
	 * java.comp.JavaParser.TypeTypeContext)
	 */
	@Override
	public ClassNode visitTypeType(TypeTypeContext ctx) {
		ClassNode c = new ClassNode(ctx, "TypeType");
		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				c.addChild(ctx.getChild(i).getText());
			} else if (ctx.getChild(i) instanceof ClassOrInterfaceTypeContext) {
				c.addChild(visitClassOrInterfaceType(ctx.classOrInterfaceType()));
			} else if (ctx.getChild(i) instanceof PrimitiveTypeContext) {
				c.addChild(visitPrimitiveType(ctx.primitiveType()));
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitPrimitiveType(parse.
	 * parser.java.comp.JavaParser.PrimitiveTypeContext)
	 */
	@Override
	public ClassNode visitPrimitiveType(PrimitiveTypeContext ctx) {
		if (ctx.BOOLEAN() != null) {
			return new ClassNode(ctx, ctx.BOOLEAN().getText());
		} else if (ctx.CHAR() != null) {
			return new ClassNode(ctx, ctx.CHAR().getText());
		} else if (ctx.BYTE() != null) {
			return new ClassNode(ctx, ctx.BYTE().getText());
		} else if (ctx.SHORT() != null) {
			return new ClassNode(ctx, ctx.SHORT().getText());
		} else if (ctx.INT() != null) {
			return new ClassNode(ctx, ctx.INT().getText());
		} else if (ctx.LONG() != null) {
			return new ClassNode(ctx, ctx.LONG().getText());
		} else if (ctx.FLOAT() != null) {
			return new ClassNode(ctx, ctx.FLOAT().getText());
		} else if (ctx.DOUBLE() != null) {
			return new ClassNode(ctx, ctx.DOUBLE().getText());
		} else {
			throw new IllegalArgumentException("Unexpected primiative type declaration");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitTypeArguments(parse.
	 * parser.java.comp.JavaParser.TypeArgumentsContext)
	 */
	@Override
	public ClassNode visitTypeArguments(TypeArgumentsContext ctx) {
		ClassNode c = new ClassNode(ctx, "TypeArguments");
		for (TypeArgumentContext tac : ctx.typeArgument()) {
			c.addChild(visitTypeArgument(tac));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#visitSuperSuffix(parse.
	 * parser.java.comp.JavaParser.SuperSuffixContext)
	 */
	@Override
	public ClassNode visitSuperSuffix(SuperSuffixContext ctx) {
		ClassNode c = new ClassNode(ctx, "SuperSuffix");
		if (ctx.IDENTIFIER() != null) {
			c.addChild(ctx.IDENTIFIER());
		}
		if (ctx.arguments() != null) {
			c.addChild(visitArguments(ctx.arguments()));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitExplicitGenericInvocationSuffix(parse.parser.java.comp.JavaParser.
	 * ExplicitGenericInvocationSuffixContext)
	 */
	@Override
	public ClassNode visitExplicitGenericInvocationSuffix(ExplicitGenericInvocationSuffixContext ctx) {
		ClassNode c = new ClassNode(ctx, "ExplicitGenericInvocationSuffix");
		if (ctx.SUPER() != null) {
			c.addChild(ctx.SUPER());
			c.addChild(visitSuperSuffix(ctx.superSuffix()));
		} else {
			c.addChild(ctx.IDENTIFIER());
			c.addChild(visitArguments(ctx.arguments()));
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitArguments(parse.parser.
	 * java.comp.JavaParser.ArgumentsContext)
	 */
	@Override
	public ClassNode visitArguments(ArgumentsContext ctx) {
		ClassNode c = new ClassNode(ctx, "Arguments");
		if (ctx.expressionList() != null) {
			c.addChild(visitExpressionList(ctx.expressionList()));
		}
		return c;
	}

}
