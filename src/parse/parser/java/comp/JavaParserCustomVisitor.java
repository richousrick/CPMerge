package parse.parser.java.comp;

import java.util.ArrayList;

import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import dif.ClassNode;
import parse.parser.java.comp.JavaParser.ArrayInitializerContext;
import parse.parser.java.comp.JavaParser.BlockContext;
import parse.parser.java.comp.JavaParser.BlockStatementContext;
import parse.parser.java.comp.JavaParser.CatchClauseContext;
import parse.parser.java.comp.JavaParser.ClassBodyDeclarationContext;
import parse.parser.java.comp.JavaParser.ClassDeclarationContext;
import parse.parser.java.comp.JavaParser.ClassOrInterfaceTypeContext;
import parse.parser.java.comp.JavaParser.ClassTypeContext;
import parse.parser.java.comp.JavaParser.CompilationUnitContext;
import parse.parser.java.comp.JavaParser.ExplicitGenericInvocationContext;
import parse.parser.java.comp.JavaParser.ExpressionContext;
import parse.parser.java.comp.JavaParser.ExpressionListContext;
import parse.parser.java.comp.JavaParser.FloatLiteralContext;
import parse.parser.java.comp.JavaParser.InnerCreatorContext;
import parse.parser.java.comp.JavaParser.IntegerLiteralContext;
import parse.parser.java.comp.JavaParser.LambdaExpressionContext;
import parse.parser.java.comp.JavaParser.LiteralContext;
import parse.parser.java.comp.JavaParser.LocalVariableDeclarationContext;
import parse.parser.java.comp.JavaParser.MemberDeclarationContext;
import parse.parser.java.comp.JavaParser.MethodDeclarationContext;
import parse.parser.java.comp.JavaParser.NonWildcardTypeArgumentsContext;
import parse.parser.java.comp.JavaParser.PrimaryContext;
import parse.parser.java.comp.JavaParser.PrimitiveTypeContext;
import parse.parser.java.comp.JavaParser.StatementContext;
import parse.parser.java.comp.JavaParser.SuperSuffixContext;
import parse.parser.java.comp.JavaParser.SwitchBlockStatementGroupContext;
import parse.parser.java.comp.JavaParser.SwitchLabelContext;
import parse.parser.java.comp.JavaParser.TypeArgumentsContext;
import parse.parser.java.comp.JavaParser.TypeDeclarationContext;
import parse.parser.java.comp.JavaParser.TypeTypeContext;
import parse.parser.java.comp.JavaParser.TypeTypeOrVoidContext;
import parse.parser.java.comp.JavaParser.VariableDeclaratorContext;
import parse.parser.java.comp.JavaParser.VariableInitializerContext;

/**
 * TODO Annotate class
 * 
 * @author 146813
 * @deprecated
 */
@Deprecated
public class JavaParserCustomVisitor extends JavaParserBaseVisitor<ClassNode> {

	ArrayList<ClassNode> classes;

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitCompilationUnit(parse.
	 * parser.java.comp.JavaParser.CompilationitContext)
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
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitClassBodyDeclaration(
	 * parse.parser.java.comp.JavaParser.ClassBodyDeclarationContext)
	 */
	@Override
	public ClassNode visitClassBodyDeclaration(ClassBodyDeclarationContext ctx) {
		// TODO check what i need
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
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitBlock(parse.parser.java
	 * .comp.JavaParser.BlockContext)
	 */
	@Override
	public ClassNode visitBlock(BlockContext ctx) {
		ClassNode node = new ClassNode(ctx, "block", (byte) 3);
		for (BlockStatementContext statement : ctx.blockStatement()) {
			ClassNode statementNode = visitBlockStatement(statement);
			if (statementNode != null) {
				node.addChild(statementNode);
			}
		}
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitBlockStatement(parse.
	 * parser.java.comp.JavaParser.BlockStatementContext)
	 */
	@Override
	public ClassNode visitBlockStatement(BlockStatementContext ctx) {
		if (ctx.localVariableDeclaration() != null) {
			return visitLocalVariableDeclaration(ctx.localVariableDeclaration());
		} else if (ctx.statement() != null) {
			return visitStatement(ctx.statement());
		} else {
			if (ctx.localTypeDeclaration().classDeclaration() != null) {
				return visitClassDeclaration(ctx.localTypeDeclaration().classDeclaration());
			} else {
				return null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitStatement(parse.parser.
	 * java.comp.JavaParser.StatementContext)
	 */
	@Override
	public ClassNode visitStatement(StatementContext ctx) {
		ClassNode node = new ClassNode(ctx, "Undefined");
		if (ctx.ASSERT() != null) {
			node.setIdentifier("Assert");
			for (ExpressionContext exp : ctx.expression()) {
				node.addChild(visitExpression(exp));
			}
		} else if (ctx.IF() != null) {
			node.setIdentifier("If");
			node.addChild(visitParExpression(ctx.parExpression()));
			for (StatementContext statement : ctx.statement()) {
				node.addChild(visitStatement(statement));
			}
		} else if (ctx.FOR() != null) {
			node.setIdentifier("For");
			node.addChild(visitForControl(ctx.forControl()));
			node.addChild(visitStatement(ctx.statement(0)));
		} else if (ctx.DO() != null) {
			node.setIdentifier("Do-While");
			node.addChild(visitParExpression(ctx.parExpression()));
			node.addChild(visitStatement(ctx.statement(0)));
		} else if (ctx.WHILE() != null) {
			node.setIdentifier("While");
			node.addChild(visitParExpression(ctx.parExpression()));
			node.addChild(visitStatement(ctx.statement(0)));
		} else if (ctx.TRY() != null) {
			node.setIdentifier("try");
			if (ctx.resourceSpecification() != null) {
				node.addChild(visitResourceSpecification(ctx.resourceSpecification()));
			}
			node.addChild(visitBlock(ctx.block()));
			for (CatchClauseContext c : ctx.catchClause()) {
				node.addChild(visitCatchClause(c));
			}
			if (ctx.finallyBlock() != null) {
				node.addChild(visitFinallyBlock(ctx.finallyBlock()));
			}
		} else if (ctx.SWITCH() != null) {
			node.setIdentifier("Switch");
			node.addChild(visitParExpression(ctx.parExpression()));
			// TODO: check works
			for (int i = 2; i < ctx.getChildCount(); i++) {
				if (ctx.getChild(i) instanceof SwitchBlockStatementGroupContext) {
					node.addChild(visitSwitchBlockStatementGroup((SwitchBlockStatementGroupContext) ctx.getChild(i)));
				} else if (ctx.getChild(i) instanceof SwitchLabelContext) {
					node.addChild(visitSwitchLabel((SwitchLabelContext) ctx.getChild(i)));
				}
			}
		} else if (ctx.SYNCHRONIZED() != null) {
			node.setIdentifier(ctx.SYNCHRONIZED().getText());
			node.addChild(visitParExpression(ctx.parExpression()));
			node.addChild(visitBlock(ctx.block()));
		} else if (ctx.RETURN() != null) {
			node.setIdentifier("Return");
			if (ctx.expression(0) != null) {
				node.addChild(visitExpression(ctx.expression(0)));
			}
		} else if (ctx.THROW() != null) {
			node.setIdentifier("Throw");
			if (ctx.expression(0) != null) {
				node.addChild(visitExpression(ctx.expression(0)));
			}
		} else if (ctx.statementExpression != null) {
			return visitExpression(ctx.expression(0));
		} else {
			visitChildren(ctx);
		}
		// TODO complete

		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitLocalVariableDeclaration(parse.parser.java.comp.JavaParser.
	 * LocalVariableDeclarationContext)
	 */
	@Override
	public ClassNode visitLocalVariableDeclaration(LocalVariableDeclarationContext ctx) {
		ClassNode decNode = new ClassNode(ctx, "declarations");
		decNode.addChild(visitTypeType(ctx.typeType()));
		for (VariableDeclaratorContext dec : ctx.variableDeclarators().variableDeclarator()) {
			decNode.addChild(visitVariableDeclarator(dec));
		}

		return decNode;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitVariableDeclarator(
	 * parse.parser.java.comp.JavaParser.VariableDeclaratorContext)
	 */
	@Override
	public ClassNode visitVariableDeclarator(VariableDeclaratorContext ctx) {
		ClassNode node = new ClassNode(ctx, "declaration");
		node.addChild(new ClassNode(ctx.variableDeclaratorId(), ctx.variableDeclaratorId().getText()));
		node.addChild(visitVariableInitializer(ctx.variableInitializer()));
		return node;
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
		// TODO Auto-generated method stub
		return super.visitArrayInitializer(ctx);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitExpression(parse.parser
	 * .java.comp.JavaParser.ExpressionContext)
	 */
	@Override
	public ClassNode visitExpression(ExpressionContext ctx) {
		// TODO expand implmentation
		if (ctx.primary() != null) {
			return visitPrimary(ctx.primary());
		} else if (ctx.prefix != null) {
			ClassNode node = new ClassNode(ctx, "pre:" + ctx.prefix.getText());
			for (ExpressionContext exp : ctx.expression()) {
				node.addChild(visitExpression(exp));
			}
			return node;
		} else if (ctx.postfix != null) {
			ClassNode node = new ClassNode(ctx, "post:" + ctx.postfix.getText());
			for (ExpressionContext exp : ctx.expression()) {
				node.addChild(visitExpression(exp));
			}
			return node;
		}
		ClassNode node = new ClassNode(ctx, "tmp");
		if (ctx.getChild(0) instanceof ExpressionContext) {
			node.addChild(visitExpression((ExpressionContext) ctx.getChild(0)));
			if (ctx.getChild(1) instanceof TerminalNodeImpl) {
				node.setIdentifier(ctx.getChild(1).getText());
			} else {
				return super.visitChildren(ctx);
			}

			if (ctx.getChild(2) instanceof ExpressionContext) {
				node.addChild(visitExpression((ExpressionContext) ctx.getChild(2)));
			} else if (ctx.getChild(2) instanceof TypeTypeContext) {
				node.addChild(visitTypeType((TypeTypeContext) ctx.getChild(2)));
			} else if (ctx.getChild(2) instanceof ExpressionListContext) {
				for (ClassNode c : visitExpressionList((ExpressionListContext) ctx.getChild(2)).getChildrenAsCN()) {
					node.addChild(c);
				}
			} else if (ctx.getChild(2) instanceof TerminalNodeImpl) {
				node.setIdentifier(node.getIdentifier() + " " + ctx.getChild(2).getText());
			} else if (ctx.getChild(2) instanceof ExplicitGenericInvocationContext) {
				return super.visitChildren(ctx);
			} else {
				return super.visitChildren(ctx);
			}

			if (ctx.getChild(3) == null) {
				return node;
			} else if (ctx.getChild(3) instanceof TerminalNodeImpl) {
				node.setIdentifier(node.getIdentifier() + " " + ctx.getChild(3).getText());
			} else if (ctx.getChild(3) instanceof NonWildcardTypeArgumentsContext) {
				return super.visitChildren(ctx);
			} else if (ctx.getChild(3) instanceof InnerCreatorContext) {
				return super.visitChildren(ctx);
			} else if (ctx.getChild(3) instanceof SuperSuffixContext) {
				return super.visitChildren(ctx);
			} else {
				return super.visitChildren(ctx);
			}

			if (ctx.getChild(4) == null) {
				return node;
			} else if (ctx.getChild(4) instanceof InnerCreatorContext) {
				return super.visitChildren(ctx);
			} else if (ctx.getChild(4) instanceof ExpressionContext) {
				node.addChild(visitExpression((ExpressionContext) ctx.getChild(4)));
			} else if (ctx.getChild(4) instanceof TerminalNodeImpl) {
				node.setIdentifier(node.getIdentifier() + " " + ctx.getChild(4).getText());
			} else {
				return super.visitChildren(ctx);
			}
			return node;
		} else if (ctx.getChild(0) instanceof TypeTypeContext) {
			ClassNode cn = new ClassNode(ctx, "expression");
		} else if (ctx.getChild(0) instanceof TerminalNodeImpl) {

		} else if (ctx.getChild(0) instanceof ClassTypeContext) {

		} else if (ctx.getChild(0) instanceof LambdaExpressionContext) {

		}

		return visitChildren(ctx);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.antlr.v4.runtime.tree.AbstractParseTreeVisitor#visitChildren(org.
	 * antlr.v4.runtime.tree.RuleNode)
	 */
	@Override
	public ClassNode visitChildren(RuleNode arg0) {
		System.out.println("");
		System.err.println("unimplmented stucture " + arg0.toStringTree());
		return super.visitChildren(arg0);
	}

	// private String getChildrenType(RuleNode arg0) {
	// String retString = "";
	// for (int i = 0; i < arg0.getChildCount(); i++) {
	// retString += " ";
	// ParseTree child = arg0.getChild(i);
	// if(child.getText().matches("\\d+")){
	//
	// }
	// }
	// }

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitExpressionList(parse.
	 * parser.java.comp.JavaParser.ExpressionListContext)
	 */
	@Override
	public ClassNode visitExpressionList(ExpressionListContext ctx) {
		ClassNode n = new ClassNode(ctx, "ExpressionList");
		for (ExpressionContext c : ctx.expression()) {
			n.addChild(visitExpression(c));
		}
		return n;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitTypeType(parse.parser.
	 * java.comp.JavaParser.TypeTypeContext)
	 */
	@Override
	public ClassNode visitTypeType(TypeTypeContext ctx) {
		if (ctx.primitiveType() != null) {
			return visitPrimitiveType(ctx.primitiveType());
		} else {
			return visitClassOrInterfaceType(ctx.classOrInterfaceType());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitClassOrInterfaceType(
	 * parse.parser.java.comp.JavaParser.ClassOrInterfaceTypeContext)
	 */
	@Override
	public ClassNode visitClassOrInterfaceType(ClassOrInterfaceTypeContext ctx) {
		ClassNode node = new ClassNode(ctx, "ClassOrInterfaceType");

		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i) instanceof TerminalNodeImpl) {
				node.addChild(new ClassNode(null, ctx.getChild(i).getText()));
			} else if (ctx.getChild(i) instanceof TypeArgumentsContext) {
				node.addChild(visitTypeArguments((TypeArgumentsContext) ctx.getChild(i)));
			} else {
				System.err.println("invalid child");
			}
		}

		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitPrimary(parse.parser.
	 * java.comp.JavaParser.PrimaryContext)
	 */
	@Override
	public ClassNode visitPrimary(PrimaryContext ctx) {
		if (ctx.nonWildcardTypeArguments() != null) {
			return visitChildren(ctx);
		} else if (ctx.expression() != null) {
			return visitExpression(ctx.expression());
		} else if (ctx.THIS() != null) {
			return new ClassNode(ctx, ctx.THIS().getText());
		} else if (ctx.SUPER() != null) {
			return new ClassNode(ctx, ctx.SUPER().getText());
		} else if (ctx.literal() != null) {
			return visitLiteral(ctx.literal());
		} else if (ctx.IDENTIFIER() != null) {
			return new ClassNode(ctx, ctx.IDENTIFIER().getText());
		} else if (ctx.CLASS() != null) {
			ClassNode cn = visitTypeTypeOrVoid(ctx.typeTypeOrVoid());
			cn.addChild(new ClassNode(null, ".CLASS"));
			return cn;
		} else {
			System.err.println("Undefined PrimaryContext structure");
			return super.visitPrimary(ctx);
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
		}

		return super.visitLiteral(ctx);
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
		}

		return super.visitIntegerLiteral(ctx);
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
		}

		return super.visitFloatLiteral(ctx);
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
			return visitChildren(ctx);
		}
	}

}
