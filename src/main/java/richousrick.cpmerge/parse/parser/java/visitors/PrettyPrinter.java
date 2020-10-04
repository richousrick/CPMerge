package richousrick.cpmerge.parse.parser.java.visitors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import richousrick.cpmerge.merge.MergedFunction;
import richousrick.cpmerge.parse.parser.java.comp.JavaParser.*;
import richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor;
import richousrick.cpmerge.parse.parser.java.misc.merge.ClassNode;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public class PrettyPrinter extends JavaParserBaseVisitor<String> {

	private final boolean doRecursion;
	private boolean doVariableInit = true;
	private boolean doVariableDec = true;
	private boolean doHeadParameters = true;
	private boolean doHeadIdentifier = true;
	private boolean doHeadModifiers = true;
	private boolean doTrailingBrace = true;


	/**
	 * @param doTrailingBrace
	 *            the doTrailingBrace to set
	 */
	public void setDoTrailingBrace(boolean doTrailingBrace) {
		this.doTrailingBrace = doTrailingBrace;
	}

	/**
	 * @param doHeadModifiers
	 *            the doHeadModifiers to set
	 */
	public void setDoHeadModifiers(boolean doHeadModifiers) {
		this.doHeadModifiers = doHeadModifiers;
	}

	public void setDoHeadParameters(boolean doHeadParameters) {
		this.doHeadParameters = doHeadParameters;
	}

	public void setDoHeadIdentifier(boolean doHeadStart) {
		doHeadIdentifier = doHeadStart;
	}

	/**
	 * @return the doVariableDec
	 */
	public boolean isDoVariableDec() {
		return doVariableDec;
	}

	/**
	 * @param doVariableDec
	 *            the doVariableDec to set
	 */
	public void setDoVariableDec(boolean doVariableDec) {
		this.doVariableDec = doVariableDec;
	}

	/**
	 * @param doVariableInit
	 *            the doVariableInit to set
	 */
	public void setDoVariableInit(boolean doVariableInit) {
		this.doVariableInit = doVariableInit;
	}

	/**
	 * @return the doVariableInit
	 */
	public boolean isDoVariableInit() {
		return doVariableInit;
	}

	/**
	 * Initializes the PrettyPrinter class TODO Annotate constructor
	 */
	public PrettyPrinter(boolean doRecursion) {
		this.doRecursion = doRecursion;
	}

	public String printFunction(ClassNode c) {
		if (c.getNodeData() instanceof MethodDeclarationContext)
			return visitMethodDeclaration((MethodDeclarationContext) c.getNodeData());
		else
			return null;
	}

	private String removeIfTrailing(String target, String s) {
		if (target.endsWith(s) && target.length() > s.length())
			return target.substring(0, target.length() - s.length());
		return target;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitClassDeclaration(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.ClassDeclarationContext)
	 */
	@Override
	public String visitClassDeclaration(ClassDeclarationContext ctx) {
		String s = ctx.CLASS().getText();
		s += " " + ctx.IDENTIFIER().getText();
		if (ctx.typeParameters() != null) {
			s += " " + visitTypeParameters(ctx.typeParameters());
		}
		if (ctx.typeType() != null) {
			s += " " + ctx.EXTENDS().getText() + " " + visitTypeType(ctx.typeType());
		}
		if (ctx.IMPLEMENTS() != null) {
			s += " " + ctx.IMPLEMENTS().getText() + " " + visitTypeList(ctx.typeList());
		}
		return s;
	}

	@Override
	public String visitPackageDeclaration(PackageDeclarationContext ctx) {
		String s = ctx.PACKAGE().getText() + " ";
		s += visitQualifiedName(ctx.qualifiedName());
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitTypeParameters(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.TypeParametersContext)
	 */
	@Override
	public String visitTypeParameters(TypeParametersContext ctx) {
		String s = "<";
		for (TypeParameterContext tpc : ctx.typeParameter()) {
			s += visitTypeParameter(tpc) + ",";
		}
		s = removeIfTrailing(s, ",") + ">";
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitTypeParameter(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.TypeParameterContext)
	 */
	@Override
	public String visitTypeParameter(TypeParameterContext ctx) {
		String s = ctx.IDENTIFIER().getText();
		if (ctx.EXTENDS() != null) {
			s += " " + ctx.EXTENDS().getText();
			s += " " + visitTypeBound(ctx.typeBound());
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitTypeBound(richousrick.cpmerge.parse.parser.
	 * java.comp.JavaParser.TypeBoundContext)
	 */
	@Override
	public String visitTypeBound(TypeBoundContext ctx) {
		String s = "";
		for (TypeTypeContext ttc : ctx.typeType()) {
			s += visitTypeType(ttc) + "&";
		}
		return removeIfTrailing(s, "&");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitMethodDeclaration(richousrick.cpmerge.parse
	 * .parser.java.comp.JavaParser.MethodDeclarationContext)
	 */
	@Override
	public String visitMethodDeclaration(MethodDeclarationContext ctx) {
		if (ctx.methodBody().block() != null) {
			String methodHead = "";
			if (doHeadModifiers) {
				for (ModifierContext mc : ((ClassBodyDeclarationContext) ctx.getParent().getParent()).modifier()) {
					methodHead += visitModifierAsString(mc) + " ";
				}
				methodHead += visitTypeTypeOrVoid(ctx.typeTypeOrVoid());
			}
			if (doHeadIdentifier) {
				methodHead += " " + ctx.IDENTIFIER().getText();
			}
			if(doHeadParameters){
				methodHead += visitFormalParameters(ctx.formalParameters());
			}
			if (!doRecursion) {
				if (doTrailingBrace) {
					methodHead += "{";
				}
				return methodHead;
			} else {
				String method = methodHead + visitMethodBody(ctx.methodBody());
				method = method.trim();
				return method;
			}

		} else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitFormalParameters(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.FormalParametersContext)
	 */
	@Override
	public String visitFormalParameters(FormalParametersContext ctx) {
		String s = "(";
		if (ctx.formalParameterList() != null) {
			s += visitFormalParameterList(ctx.formalParameterList());
		}
		return s + ")";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitFormalParameterList(
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParser.FormalParameterListContext)
	 */
	@Override
	public String visitFormalParameterList(FormalParameterListContext ctx) {
		String s = "";
		for (FormalParameterContext f : ctx.formalParameter()) {
			s += visitFormalParameter(f) + ",";
		}
		if (ctx.lastFormalParameter() != null) {
			s += visitLastFormalParameter(ctx.lastFormalParameter());
		}

		s = removeIfTrailing(s, ",");
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitFormalParameter(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.FormalParameterContext)
	 */
	@Override
	public String visitFormalParameter(FormalParameterContext ctx) {
		String s = "";
		for (VariableModifierContext vmc : ctx.variableModifier()) {
			s += visitVariableModifier(vmc) + " ";
		}

		s += visitTypeType(ctx.typeType());
		s += " " + visitVariableDeclaratorId(ctx.variableDeclaratorId());
		return s;
	}


	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitLastFormalParameter(
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParser.LastFormalParameterContext)
	 */
	@Override
	public String visitLastFormalParameter(LastFormalParameterContext ctx) {
		String s = "";
		for (VariableModifierContext vmc : ctx.variableModifier()) {
			s += visitVariableModifier(vmc) + " ";
		}

		s += visitTypeType(ctx.typeType());
		s += " ... " + visitVariableDeclaratorId(ctx.variableDeclaratorId());
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitTypeTypeOrVoid(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.TypeTypeOrVoidContext)
	 */
	@Override
	public String visitTypeTypeOrVoid(TypeTypeOrVoidContext ctx) {
		if (ctx.VOID() != null)
			return ctx.VOID().getText();
		else
			return visitTypeType(ctx.typeType());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitVariableDeclarators(
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParser.VariableDeclaratorsContext)
	 */
	@Override
	public String visitVariableDeclarators(VariableDeclaratorsContext ctx) {
		String s = "";
		for (VariableDeclaratorContext v : ctx.variableDeclarator()) {
			s += visitVariableDeclarator(v) + ",";
		}
		return s.substring(0, s.length() - 1);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitVariableDeclarator(
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParser.VariableDeclaratorContext)
	 */
	@Override
	public String visitVariableDeclarator(VariableDeclaratorContext ctx) {
		String s = visitVariableDeclaratorId(ctx.variableDeclaratorId());
		if (doVariableInit) {
			if (ctx.variableInitializer() != null) {
				s += " = " + visitVariableInitializer(ctx.variableInitializer());
			}
		} else {
			ParserRuleContext parent = ctx.getParent().getParent();
			if (parent instanceof LocalVariableDeclarationContext) {
				if (((LocalVariableDeclarationContext) parent).typeType().primitiveType() == null) {
					s += " = null";
				}
			}
		}

		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitVariableDeclaratorId(
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParser.VariableDeclaratorIdContext)
	 */
	@Override
	public String visitVariableDeclaratorId(VariableDeclaratorIdContext ctx) {
		String name = "";
		for (int i = 0; i < ctx.getChildCount(); i++) {
			name += ctx.getChild(i).getText();
		}
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitMethodBody(richousrick.cpmerge.parse.parser
	 * .java.comp.JavaParser.MethodBodyContext)
	 */
	@Override
	public String visitMethodBody(MethodBodyContext ctx) {
		if (ctx.block() != null)
			return visitBlock(ctx.block());
		else
			return ";";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitVariableInitializer(
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParser.VariableInitializerContext)
	 */
	@Override
	public String visitVariableInitializer(VariableInitializerContext ctx) {
		if (ctx.arrayInitializer() != null)
			return visitArrayInitializer(ctx.arrayInitializer());
		else
			return visitExpression(ctx.expression());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitArrayInitializer(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.ArrayInitializerContext)
	 */
	@Override
	public String visitArrayInitializer(ArrayInitializerContext ctx) {
		String s = "{";
		for (VariableInitializerContext vic : ctx.variableInitializer()) {
			s += visitVariableInitializer(vic) + ",";
		}
		s = removeIfTrailing(s, ",") + "}";
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitClassOrInterfaceType(
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParser.ClassOrInterfaceTypeContext)
	 */
	@Override
	public String visitClassOrInterfaceType(ClassOrInterfaceTypeContext ctx) {
		String s = "";
		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				s += ctx.getChild(i);
			} else if (ctx.getChild(i) instanceof TypeArgumentsContext) {
				s += visitTypeArguments((TypeArgumentsContext) ctx.getChild(i));
			}
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitTypeArgument(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.TypeArgumentContext)
	 */
	@Override
	public String visitTypeArgument(TypeArgumentContext ctx) {
		if (ctx.getChild(0) instanceof TypeTypeContext)
			return visitTypeType(ctx.typeType());
		else {
			String s = "?";
			if (ctx.typeType() != null) {
				if (ctx.EXTENDS() != null) {
					s += "? " + ctx.EXTENDS().getText();
				} else if (ctx.SUPER() != null) {
					s += "? " + ctx.SUPER().getText();
				}
				s += visitTypeType(ctx.typeType());
			}
			return s;
		}
	}


	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitLiteral(richousrick.cpmerge.parse.parser.
	 * java.comp.JavaParser.LiteralContext)
	 */
	@Override
	public String visitLiteral(LiteralContext ctx) {
		if (ctx.CHAR_LITERAL() != null)
			return ctx.CHAR_LITERAL().getText();
		else if (ctx.STRING_LITERAL() != null)
			return ctx.STRING_LITERAL().getText();
		else if (ctx.BOOL_LITERAL() != null)
			return ctx.BOOL_LITERAL().getText();
		else if (ctx.NULL_LITERAL() != null)
			return ctx.NULL_LITERAL().getText();
		else if (ctx.integerLiteral() != null)
			return visitIntegerLiteral(ctx.integerLiteral());
		else if (ctx.floatLiteral() != null)
			return visitFloatLiteral(ctx.floatLiteral());
		else
			throw new IllegalArgumentException("Unexpected literal declaration");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitIntegerLiteral(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.IntegerLiteralContext)
	 */
	@Override
	public String visitIntegerLiteral(IntegerLiteralContext ctx) {
		if (ctx.DECIMAL_LITERAL() != null)
			return ctx.DECIMAL_LITERAL().getText();
		else if (ctx.HEX_LITERAL() != null)
			return ctx.HEX_LITERAL().getText();
		else if (ctx.OCT_LITERAL() != null)
			return ctx.OCT_LITERAL().getText();
		else if (ctx.BINARY_LITERAL() != null)
			return ctx.BINARY_LITERAL().getText();
		else
			throw new IllegalArgumentException("Unexpected integer literal declaration");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitFloatLiteral(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.FloatLiteralContext)
	 */
	@Override
	public String visitFloatLiteral(FloatLiteralContext ctx) {
		if (ctx.FLOAT_LITERAL() != null)
			return ctx.FLOAT_LITERAL().getText();
		else if (ctx.HEX_FLOAT_LITERAL() != null)
			return ctx.HEX_FLOAT_LITERAL().getText();
		else
			throw new IllegalArgumentException("Unexpected float literal declaration");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitBlock(richousrick.cpmerge.parse.parser.java
	 * .comp.JavaParser.BlockContext)
	 */
	@Override
	public String visitBlock(BlockContext ctx) {
		String s = "";
		if (doRecursion) {
			s = "{\n";

			for (BlockStatementContext statement : ctx.blockStatement()) {
				s += "\t" + visitBlockStatement(statement) + "\n";
			}
			s += "}";
		} else {
			for (BlockStatementContext statement : ctx.blockStatement()) {
				s += visitBlockStatement(statement) + "\n";
			}
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitBlockStatement(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.BlockStatementContext)
	 */
	@Override
	public String visitBlockStatement(BlockStatementContext ctx) {
		if (ctx.localTypeDeclaration() != null)
			return visitLocalTypeDeclaration(ctx.localTypeDeclaration());
		else if (ctx.statement() != null)
			return visitStatement(ctx.statement());
		else if (ctx.localVariableDeclaration() != null)
			return visitLocalVariableDeclaration(ctx.localVariableDeclaration()) + ";";
		else
			throw new InvalidParameterException("Invalid Child Statement");
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitLocalVariableDeclaration(richousrick.cpmerge.parse.parser.java.comp.JavaParser.
	 * LocalVariableDeclarationContext)
	 */
	@Override
	public String
	visitLocalVariableDeclaration(LocalVariableDeclarationContext ctx) {
		String s = "";
		if (doVariableDec) {
			for (VariableModifierContext vmc : ctx.variableModifier()) {
				s += visitVariableModifier(vmc) + " ";
			}
			s += visitTypeType(ctx.typeType()) + " ";
		}
		s += visitVariableDeclarators(ctx.variableDeclarators());
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitLocalTypeDeclaration(
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParser.LocalTypeDeclarationContext)
	 */
	@Override
	public String visitLocalTypeDeclaration(LocalTypeDeclarationContext ctx){
		String s = "";
		if (ctx.getChild(0) instanceof TerminalNode) {
			s += ctx.getChild(0).getText();
		} else {
			for (ClassOrInterfaceModifierContext coimc :
				ctx.classOrInterfaceModifier()) {
				s += visitClassOrInterfaceModifier(coimc) + " ";
			}
			if (ctx.classDeclaration() != null) {
				s += visitClassDeclaration(ctx.classDeclaration());
			} else {
				s += visitInterfaceDeclaration(ctx.interfaceDeclaration());
			}
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitStatement(richousrick.cpmerge.parse.parser.
	 * java.comp.JavaParser.StatementContext)
	 */
	@Override
	public String visitStatement(StatementContext ctx) {
		String s = "";
		if (ctx.ASSERT() != null) {
			s = ctx.ASSERT().getText();
			for (ExpressionContext exp : ctx.expression()) {
				s+=visitExpression(exp) +":";
			}
			s = removeIfTrailing(s, ":") + ";";
			return s;
		} else if (ctx.IF() != null) {
			s = ctx.IF().getText();
			s+=visitParExpression(ctx.parExpression());
			if (doRecursion) {
				s += visitStatement(ctx.statement(0));
			}
			if (ctx.ELSE() != null) {
				s+=ctx.ELSE();
				if (doRecursion) {
					s += visitStatement(ctx.statement(1));
				}
			}
		} else if (ctx.FOR() != null) {
			s = ctx.FOR().getText() + "(";
			s += visitForControl(ctx.forControl()) + ")";
			if (doRecursion) {
				s += visitStatement(ctx.statement(0));
			}
		} else if (ctx.DO() != null) {
			// s = ctx.DO().getText();
			// if (doRecursion) {
			// s += visitStatement(ctx.statement(0));
			// }
			s = ctx.WHILE().getText();
			s += visitParExpression(ctx.parExpression()) + ";";
		} else if (ctx.WHILE() != null) {
			s = ctx.WHILE().getText();
			s += visitParExpression(ctx.parExpression());
			if (doRecursion) {
				s += visitStatement(ctx.statement(0));
			}
		} else if (ctx.TRY() != null) {
			s = ctx.TRY().getText();
			if (ctx.resourceSpecification() != null) {
				s += visitResourceSpecification(ctx.resourceSpecification());
				s += visitBlock(ctx.block());
				for (CatchClauseContext ccc : ctx.catchClause()) {
					s += visitCatchClause(ccc) + " ";
				}

				if (ctx.finallyBlock() != null) {
					s += visitFinallyBlock(ctx.finallyBlock());
				}
			} else {
				s += visitBlock(ctx.block());
				for (CatchClauseContext ccc : ctx.catchClause()) {
					s += visitCatchClause(ccc) + " ";
				}
				if (ctx.finallyBlock() != null) {
					s += visitFinallyBlock(ctx.finallyBlock());
				}
			}
		} else if (ctx.SWITCH() != null) {
			s = ctx.SWITCH().getText();
			s += visitParExpression(ctx.parExpression()) + "{\n";
			for (SwitchBlockStatementGroupContext sbsgc :
				ctx.switchBlockStatementGroup()) {
				s += visitSwitchBlockStatementGroup(sbsgc);
			}
			for (SwitchLabelContext sl : ctx.switchLabel()) {
				s += visitSwitchLabel(sl);
			}
			s += "}";
		} else if (ctx.SYNCHRONIZED() != null) {
			s = ctx.SYNCHRONIZED().getText();
			s += visitParExpression(ctx.parExpression());
			s += visitBlock(ctx.block());
		} else if (ctx.RETURN() != null) {
			s = ctx.RETURN().getText();
			if (ctx.expression(0) != null) {
				s += " " + visitExpression(ctx.expression(0));
			}
			s += ";";
		} else if (ctx.THROW() != null) {
			s = ctx.THROW().getText();
			s += visitExpression(ctx.expression(0)) + ";";
		} else if (ctx.BREAK() != null) {
			s = ctx.BREAK().getText();
			if (ctx.IDENTIFIER() != null) {
				s += ctx.IDENTIFIER().getText();
			}
			s += ";";
		} else if (ctx.CONTINUE() != null) {
			s = ctx.CONTINUE().getText();
			if (ctx.IDENTIFIER() != null) {
				s += ctx.IDENTIFIER().getText();
			}
			s += ";";
		} else {
			if (ctx.blockLabel != null)
				return "\t" + visitBlock(ctx.blockLabel).replaceAll("\n", "\n\t");
			else if (ctx.statementExpression != null)
				return visitExpression(ctx.statementExpression)+";";
			else if (ctx.IDENTIFIER() != null) {
				s = ctx.IDENTIFIER().getText() + ":";
				if (doRecursion) {
					s += visitStatement(ctx.statement(0));
				}
			} else if (ctx.SEMI() != null)
				return ctx.SEMI().getText();
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitCatchClause(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.CatchClauseContext)
	 */
	@Override
	public String visitCatchClause(CatchClauseContext ctx) {
		String s = ctx.CATCH().getText() + "(";
		for (VariableModifierContext vm : ctx.variableModifier()) {
			s += visitVariableModifier(vm) + " ";
		}
		s += visitCatchType(ctx.catchType());
		s += " " + ctx.IDENTIFIER().getText() + ")";
		s += visitBlock(ctx.block());
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitCatchType(richousrick.cpmerge.parse.parser.
	 * java.comp.JavaParser.CatchTypeContext)
	 */
	@Override
	public String visitCatchType(CatchTypeContext ctx) {
		String s = "";
		for (QualifiedNameContext qn : ctx.qualifiedName()) {
			s += visitQualifiedName(qn) + "|";
		}
		return removeIfTrailing(s, "|");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitQualifiedName(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.QualifiedNameContext)
	 */
	@Override
	public String visitQualifiedName(QualifiedNameContext ctx) {
		String s = "";
		for (TerminalNode t : ctx.IDENTIFIER()) {
			s += t.getText() + ".";
		}
		return removeIfTrailing(s, ".");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitFinallyBlock(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.FinallyBlockContext)
	 */
	@Override
	public String visitFinallyBlock(FinallyBlockContext ctx) {
		String s = "";
		s += ctx.FINALLY().getText();
		s += visitBlock(ctx.block());
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitResourceSpecification(
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParser.ResourceSpecificationContext)
	 */
	@Override
	public String visitResourceSpecification(ResourceSpecificationContext
			ctx) {
		String s = "";
		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				s += ctx.getChild(i).getText();
			} else {
				s += visitResources(ctx.resources());
			}
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitResources(richousrick.cpmerge.parse.parser.
	 * java.comp.JavaParser.ResourcesContext)
	 */
	@Override
	public String visitResources(ResourcesContext ctx) {
		String s = "";
		for (ResourceContext rc : ctx.resource()) {
			s += visitResource(rc) + ",";
		}
		return removeIfTrailing(s, ",");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitResource(richousrick.cpmerge.parse.parser.
	 * java.comp.JavaParser.ResourceContext)
	 */
	@Override
	public String visitResource(ResourceContext ctx) {
		String s = "";
		for (VariableModifierContext vmc : ctx.variableModifier()) {
			s += visitVariableModifier(vmc) + " ";
		}
		s += visitClassOrInterfaceType(ctx.classOrInterfaceType()) + " ";
		s += visitVariableDeclaratorId(ctx.variableDeclaratorId()) + " = ";
		s += visitExpression(ctx.expression());
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitSwitchBlockStatementGroup(richousrick.cpmerge.parse.parser.java.comp.JavaParser.
	 * SwitchBlockStatementGroupContext)
	 */
	@Override
	public String visitSwitchBlockStatementGroup(SwitchBlockStatementGroupContext ctx) {
		String s = "";
		for (SwitchLabelContext slc : ctx.switchLabel()) {
			s += visitSwitchLabel(slc) + "\n";
		}
		s += "\t";
		for (BlockStatementContext bsc : ctx.blockStatement()) {
			s += visitBlockStatement(bsc).replaceAll("\n", "\n\t");
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitSwitchLabel(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.SwitchLabelContext)
	 */
	@Override
	public String visitSwitchLabel(SwitchLabelContext ctx) {
		String s = "";
		if (ctx.DEFAULT() != null) {
			s += ctx.DEFAULT().getText() + ":";
		} else {
			s += ctx.CASE().getText() + " ";
			if (ctx.expression() != null) {
				s += visitExpression(ctx.expression());
			} else {
				s += ctx.IDENTIFIER().getText();
			}
			s += ":";
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitForControl(richousrick.cpmerge.parse.parser
	 * .java.comp.JavaParser.ForControlContext)
	 */
	@Override
	public String visitForControl(ForControlContext ctx) {
		if (ctx.enhancedForControl() != null)
			return visitEnhancedForControl(ctx.enhancedForControl());
		else {
			String s = "";
			if (ctx.forInit() != null) {
				s += visitForInit(ctx.forInit());
			}
			s += ";";
			if (ctx.expression() != null) {
				s += visitExpression(ctx.expression());
			}
			s += ";";
			if (ctx.expressionList() != null) {
				s += visitExpressionList(ctx.expressionList());
			}
			return s;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitForInit(richousrick.cpmerge.parse.parser.
	 * java.comp.JavaParser.ForInitContext)
	 */
	@Override
	public String visitForInit(ForInitContext ctx) {
		if (ctx.localVariableDeclaration() != null)
			return visitLocalVariableDeclaration(ctx.localVariableDeclaration());
		else
			return visitExpressionList(ctx.expressionList());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitEnhancedForControl(
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParser.EnhancedForControlContext)
	 */
	@Override
	public String visitEnhancedForControl(EnhancedForControlContext ctx) {
		String s = "";
		for (VariableModifierContext vmc : ctx.variableModifier()) {
			s += visitVariableModifier(vmc) + " ";
		}
		s += visitTypeType(ctx.typeType()) + " ";
		s += visitVariableDeclaratorId(ctx.variableDeclaratorId()) + ":";
		s += visitExpression(ctx.expression());
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitParExpression(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.ParExpressionContext)
	 */
	@Override
	public String visitParExpression(ParExpressionContext ctx) {
		return "(" + visitExpression(ctx.expression()) + ")";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitExpressionList(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.ExpressionListContext)
	 */
	@Override
	public String visitExpressionList(ExpressionListContext ctx) {
		String s = "";
		for (ExpressionContext exp : ctx.expression()) {
			s += visitExpression(exp) + ",";
		}
		return removeIfTrailing(s, ",");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitExpression(richousrick.cpmerge.parse.parser
	 * .java.comp.JavaParser.ExpressionContext)
	 */
	@Override
	public String visitExpression(ExpressionContext ctx) {
		String s = "";
		if (ctx.primary() != null)
			return visitPrimary(ctx.primary());
		else if (ctx.bop != null && ctx.bop.getText().equals(".")) {
			s += visitExpression(ctx.expression(0)) + ".";
			if (ctx.IDENTIFIER() != null) {
				s+= ctx.IDENTIFIER().getText();
			} else if (ctx.THIS() != null) {
				s+= ctx.THIS().getText();
			} else if (ctx.NEW() != null) {
				s += ctx.NEW().getText();
				if (ctx.nonWildcardTypeArguments() != null) {
					s += " " + visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments());
				}
				s += " " + visitInnerCreator(ctx.innerCreator());
			} else if (ctx.SUPER() != null) {
				s += ctx.SUPER().getText();
				s += s + " " + visitSuperSuffix(ctx.superSuffix());
			} else {
				s+= visitExplicitGenericInvocation(ctx.explicitGenericInvocation());
			}
		} else {
			for (int i = 0; i < ctx.getChildCount(); i++) {
				if (ctx.getChild(i) instanceof ExpressionContext) {
					s += visitExpression((ExpressionContext) ctx.getChild(i));
				} else if (ctx.getChild(i) instanceof TerminalNode) {
					s += ctx.getChild(i).getText();
					if (!s.endsWith("(") && !s.endsWith(")")) {
						s += " ";
					}
				} else if (ctx.getChild(i) instanceof ExpressionListContext) {
					s += visitExpressionList((ExpressionListContext) ctx.getChild(i));
				} else if (ctx.getChild(i) instanceof CreatorContext) {
					s += visitCreator((CreatorContext) ctx.getChild(i));
				} else if (ctx.getChild(i) instanceof TypeTypeContext) {
					s += visitTypeType((TypeTypeContext) ctx.getChild(i));
				} else if (ctx.getChild(i) instanceof LambdaExpressionContext) {
					s+=visitLambdaExpression((LambdaExpressionContext)
							ctx.getChild(i));
				} else if (ctx.getChild(i) instanceof TypeArgumentsContext) {
					s += visitTypeArguments((TypeArgumentsContext) ctx.getChild(i));
				} else if (ctx.getChild(i) instanceof ClassTypeContext) {
					s += visitClassType((ClassTypeContext) ctx.getChild(i));
				}
			}
		}
		return s.trim();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitLambdaExpression(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.LambdaExpressionContext)
	 */
	@Override
	public String visitLambdaExpression(LambdaExpressionContext ctx) {
		String s = visitLambdaParameters(ctx.lambdaParameters());
		s += "->" + visitLambdaBody(ctx.lambdaBody());
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitLambdaParameters(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.LambdaParametersContext)
	 */
	@Override
	public String visitLambdaParameters(LambdaParametersContext ctx) {
		String s = "";
		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				s += ctx.getChild(i).getText();
			} else {
				s += visitFormalParameterList(ctx.formalParameterList());
			}
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitLambdaBody(richousrick.cpmerge.parse.parser
	 * .java.comp.JavaParser.LambdaBodyContext)
	 */
	@Override
	public String visitLambdaBody(LambdaBodyContext ctx) {
		if (ctx.expression() != null)
			return visitExpression(ctx.expression());
		else
			return visitBlock(ctx.block());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitPrimary(richousrick.cpmerge.parse.parser.
	 * java.comp.JavaParser.PrimaryContext)
	 */
	@Override
	public String visitPrimary(PrimaryContext ctx) {
		if (ctx.expression() != null)
			return "(" + visitExpression(ctx.expression()) + ")";
		else if (ctx.SUPER() != null)
			return ctx.SUPER().getText();
		else if (ctx.literal() != null)
			return visitLiteral(ctx.literal());
		else if (ctx.IDENTIFIER() != null)
			return ctx.IDENTIFIER().getText();
		else if (ctx.typeTypeOrVoid() != null) {
			String s = visitTypeTypeOrVoid(ctx.typeTypeOrVoid());
			s += "." + ctx.CLASS();
			return s;
		} else if (ctx.nonWildcardTypeArguments() != null) {
			String s = visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments()) + " ";

			if (ctx.explicitGenericInvocationSuffix() != null) {
				s += visitExplicitGenericInvocationSuffix(ctx.explicitGenericInvocationSuffix());
			} else {
				s += ctx.THIS().getText() + " ";
				s += visitArguments(ctx.arguments());
			}
			return s;
		} else
			return ctx.THIS().getText();

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitClassType(richousrick.cpmerge.parse.parser.
	 * java.comp.JavaParser.ClassTypeContext)
	 */
	@Override
	public String visitClassType(ClassTypeContext ctx) {
		String s = "";
		if (ctx.classOrInterfaceType() != null) {
			s += visitClassOrInterfaceType(ctx.classOrInterfaceType()) + ".";
		}
		s += ctx.IDENTIFIER().getText() + " ";
		if (ctx.typeArguments() != null) {
			s += visitTypeArguments(ctx.typeArguments()) + " ";
		}
		return s.trim();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitCreator(richousrick.cpmerge.parse.parser.
	 * java.comp.JavaParser.CreatorContext)
	 */
	@Override
	public String visitCreator(CreatorContext ctx) {
		String s = "";
		if (ctx.nonWildcardTypeArguments() != null) {
			s += visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments()) + " ";
			s += visitCreatedName(ctx.createdName()) + " ";
			s += visitClassCreatorRest(ctx.classCreatorRest());
		} else {
			s += visitCreatedName(ctx.createdName()) + " ";
			if (ctx.classCreatorRest() != null) {
				s += visitClassCreatorRest(ctx.classCreatorRest());
			} else {
				s += visitArrayCreatorRest(ctx.arrayCreatorRest());
			}
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitCreatedName(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.CreatedNameContext)
	 */
	@Override
	public String visitCreatedName(CreatedNameContext ctx) {
		String s = "";
		if (ctx.primitiveType() != null) {
			s += visitPrimitiveType(ctx.primitiveType());
		} else {
			for (int i = 0; i < ctx.getChildCount(); i++) {
				if (ctx.getChild(i) instanceof TerminalNode) {
					s += ctx.getChild(i).getText();
				} else if (ctx.getChild(i) instanceof TypeArgumentsOrDiamondContext) {
					s+=visitTypeArgumentsOrDiamond((TypeArgumentsOrDiamondContext)
							ctx.getChild(i)) + ".";
				}
			}
			s = removeIfTrailing(s, ".");
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitInnerCreator(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.InnerCreatorContext)
	 */
	@Override
	public String visitInnerCreator(InnerCreatorContext ctx) {
		String s = "";
		s += ctx.IDENTIFIER().getText() + " ";
		if (ctx.nonWildcardTypeArgumentsOrDiamond() != null) {
			s += visitNonWildcardTypeArgumentsOrDiamond(ctx.nonWildcardTypeArgumentsOrDiamond()) + " ";
		}
		s += visitClassCreatorRest(ctx.classCreatorRest());
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitArrayCreatorRest(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.ArrayCreatorRestContext)
	 */
	@Override
	public String visitArrayCreatorRest(ArrayCreatorRestContext ctx) {
		String s = "";
		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				s += ctx.getChild(i).getText();
			} else if (ctx.getChild(i) instanceof ArrayInitializerContext) {
				s += visitArrayInitializer((ArrayInitializerContext) ctx.getChild(i));
			} else if (ctx.getChild(i) instanceof ExpressionContext) {
				s += visitExpression((ExpressionContext) ctx.getChild(i));
			}
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitClassCreatorRest(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.ClassCreatorRestContext)
	 */
	@Override
	public String visitClassCreatorRest(ClassCreatorRestContext ctx) {
		String s = "";
		s += visitArguments(ctx.arguments()) + " ";
		if (ctx.classBody() != null) {
			s += visitClassBody(ctx.classBody());
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitExplicitGenericInvocation(richousrick.cpmerge.parse.parser.java.comp.JavaParser.
	 * ExplicitGenericInvocationContext)
	 */
	@Override
	public String
	visitExplicitGenericInvocation(ExplicitGenericInvocationContext ctx) {
		String s = "";
		s+=visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments());
		s+=visitExplicitGenericInvocationSuffix(ctx.explicitGenericInvocationSuffix());
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 *
	 richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitTypeArgumentsOrDiamond(
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParser.TypeArgumentsOrDiamondContext)
	 */
	@Override
	public String visitTypeArgumentsOrDiamond(TypeArgumentsOrDiamondContext ctx) {
		if (ctx.typeArguments() != null)
			return visitTypeArguments(ctx.typeArguments());
		else
			return "<>";
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitNonWildcardTypeArgumentsOrDiamond(richousrick.cpmerge.parse.parser.java.comp.JavaParser.
	 * NonWildcardTypeArgumentsOrDiamondContext)
	 */
	@Override
	public String visitNonWildcardTypeArgumentsOrDiamond(NonWildcardTypeArgumentsOrDiamondContext ctx) {
		if (ctx.nonWildcardTypeArguments() != null)
			return visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments());
		else
			return "<>";
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitNonWildcardTypeArguments(richousrick.cpmerge.parse.parser.java.comp.JavaParser.
	 * NonWildcardTypeArgumentsContext)
	 */
	@Override
	public String
	visitNonWildcardTypeArguments(NonWildcardTypeArgumentsContext ctx) {
		return "<" + visitTypeList(ctx.typeList()) + ">";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitTypeList(richousrick.cpmerge.parse.parser.
	 * java.comp.JavaParser.TypeListContext)
	 */
	@Override
	public String visitTypeList(TypeListContext ctx) {
		String s = "";
		for (TypeTypeContext ttc : ctx.typeType()) {
			s += visitTypeType(ttc) + ",";
		}
		return removeIfTrailing(s, ",");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitTypeType(richousrick.cpmerge.parse.parser.
	 * java.comp.JavaParser.TypeTypeContext)
	 */
	@Override
	public String visitTypeType(TypeTypeContext ctx) {
		String s = "";

		for (int i = 0; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				s += ctx.getChild(i).getText();
			} else if (ctx.getChild(i) instanceof ClassOrInterfaceTypeContext) {
				s += visitClassOrInterfaceType(ctx.classOrInterfaceType());
			} else if (ctx.getChild(i) instanceof PrimitiveTypeContext) {
				s += visitPrimitiveType(ctx.primitiveType());
			}
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitPrimitiveType(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.PrimitiveTypeContext)
	 */
	@Override
	public String visitPrimitiveType(PrimitiveTypeContext ctx) {
		if (ctx.BOOLEAN() != null)
			return ctx.BOOLEAN().getText();
		else if (ctx.CHAR() != null)
			return ctx.CHAR().getText();
		else if (ctx.BYTE() != null)
			return ctx.BYTE().getText();
		else if (ctx.SHORT() != null)
			return ctx.SHORT().getText();
		else if (ctx.INT() != null)
			return ctx.INT().getText();
		else if (ctx.LONG() != null)
			return ctx.LONG().getText();
		else if (ctx.FLOAT() != null)
			return ctx.FLOAT().getText();
		else if (ctx.DOUBLE() != null)
			return ctx.DOUBLE().getText();
		else
			throw new IllegalArgumentException("Unexpected primiative type declaration");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitTypeArguments(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.TypeArgumentsContext)
	 */
	@Override
	public String visitTypeArguments(TypeArgumentsContext ctx) {
		String s = "<";
		for (TypeArgumentContext tac : ctx.typeArgument()) {
			s += visitTypeArgument(tac) + ",";
		}
		return removeIfTrailing(s, ",") + ">";
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitSuperSuffix(richousrick.cpmerge.parse.
	 * parser.java.comp.JavaParser.SuperSuffixContext)
	 */
	@Override
	public String visitSuperSuffix(SuperSuffixContext ctx) {
		String s = "";
		if (ctx.IDENTIFIER() != null) {
			s += ".";
			s += ctx.IDENTIFIER().getText();
		}
		if (ctx.arguments() != null) {
			s += visitArguments(ctx.arguments());
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#
	 * visitExplicitGenericInvocationSuffix(richousrick.cpmerge.parse.parser.java.comp.JavaParser.
	 * ExplicitGenericInvocationSuffixContext)
	 */
	@Override
	public String visitExplicitGenericInvocationSuffix(ExplicitGenericInvocationSuffixContext ctx) {
		String s = "";
		if (ctx.SUPER() != null) {
			s += ctx.SUPER().getText() + " ";
			s += visitSuperSuffix(ctx.superSuffix());
		} else {
			s += ctx.IDENTIFIER().getText() + " ";
			s += visitArguments(ctx.arguments());
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.comp.JavaParserBaseVisitor#visitArguments(richousrick.cpmerge.parse.parser.
	 * java.comp.JavaParser.ArgumentsContext)
	 */
	@Override
	public String visitArguments(ArgumentsContext ctx) {
		String s = "(";
		if (ctx.expressionList() != null) {
			s+=visitExpressionList(ctx.expressionList());
		}
		return s+")";
	}

	public String visitModifierAsString(ModifierContext ctx) {
		if (ctx.NATIVE() != null)
			return "native";
		else if (ctx.SYNCHRONIZED() != null)
			return "synchronized";
		else if (ctx.TRANSIENT() != null)
			return "transient";
		else if (ctx.VOLATILE() != null)
			return "volatile";
		else
			return visitClassOrInterfaceModifier(ctx.classOrInterfaceModifier());
	}

	@Override
	public String visitClassOrInterfaceModifier(ClassOrInterfaceModifierContext ctx) {
		String str = "";
		if (ctx.PUBLIC() != null) {
			str = "public";
		} else if (ctx.PROTECTED() != null) {
			str = "protected";
		} else if (ctx.PRIVATE() != null) {
			str = "private";
		} else if (ctx.STATIC() != null) {
			str = "static";
		} else if (ctx.ABSTRACT() != null) {
			str = "abstract";
		} else if (ctx.FINAL() != null) {
			str = "final";
		} else if (ctx.STRICTFP() != null) {
			str = "strictfp";
		}
		return str;
	}

	/**
	 * TODO Annotate method
	 * @param rootNode
	 * @return
	 */
	public String getDefaultReturns(MergedFunction rootNode) {
		MethodDeclarationContext ctx = (MethodDeclarationContext) rootNode.getRootNode().getNode().getNodeData();
		return getDefaultValue(visitTypeTypeOrVoid(ctx.typeTypeOrVoid()));
	}

	public String getDefaultValue(String type) {
		switch (type.toLowerCase()) {
			case "boolean":
				return "false";
			case "char":
				return "'\\u0000'";
			case "byte":
			case "short":
			case "int":
				return "0";
			case "long":
				return "0L";
			case "float":
				return "0.0f";
			case "double":
				return "0.0d";
			case "void":
				return "void";
			default:
				return "null";
		}
	}

	public String getNodePath(ParserRuleContext ctx) {
		String path = "";
		ParserRuleContext curCtx = ctx;
		while (curCtx.getParent() != null) {
			if (curCtx instanceof ClassDeclarationContext) {
				path = "$" + ((ClassDeclarationContext) curCtx).IDENTIFIER().getText() + path;
			}
			curCtx = curCtx.getParent();
		}
		if (path.length() > 0) {
			path = path.substring(1);
		}
		if (((CompilationUnitContext) curCtx).packageDeclaration() != null) {
			path = visit(((CompilationUnitContext) curCtx).packageDeclaration().qualifiedName()) + "." + path;
		}

		return path;
	}

	public String getNodeName(ParserRuleContext ctx) {
		String path = getNodePath(ctx);
		if (ctx instanceof MethodDeclarationContext) {
			MethodDeclarationContext meth = (MethodDeclarationContext) ctx;
			path += "." + meth.IDENTIFIER().getText() + "(";
			if (meth.formalParameters() != null) {
				path += printParameterListTypes(meth.formalParameters());
			}
			path += ")";
		}
		return path;
	}

	public ArrayList<String[]> getParameterList(FormalParametersContext parameters) {
		ArrayList<String[]> retList = new ArrayList<>();
		if (parameters.formalParameterList() != null) {
			FormalParameterListContext parameterList = parameters.formalParameterList();

			if (parameterList.formalParameter() != null) {
				for (FormalParameterContext fP : parameterList.formalParameter()) {
					retList.add(new String[] { visit(fP.typeType()), visit(fP.variableDeclaratorId()) });
				}
			}
			if (parameterList.lastFormalParameter() != null) {
				LastFormalParameterContext lFPC = parameterList.lastFormalParameter();
				retList.add(new String[] { "... " + visit(lFPC.typeType()), visit(lFPC.variableDeclaratorId()) });
			}
		}
		return retList;
	}

	public String printParameterListTypes(FormalParametersContext parameters) {
		String retStr = "";
		if (parameters.formalParameterList() != null) {
			for (FormalParameterContext ctx : parameters.formalParameterList().formalParameter()) {
				retStr += visitTypeType(ctx.typeType()) + ",";
			}
			if (parameters.formalParameterList().lastFormalParameter() != null) {
				retStr += visitTypeType(parameters.formalParameterList().lastFormalParameter().typeType()) + ",";
			}
			if (retStr.length() > 0) {
				retStr = retStr.substring(0, retStr.length() - 1);
			}
		}
		return retStr;
	}

	public String printParameterListNames(FormalParametersContext parameters) {
		String retStr = "";
		if (parameters.formalParameterList() != null) {
			for (FormalParameterContext ctx : parameters.formalParameterList().formalParameter()) {
				retStr += ctx.variableDeclaratorId().IDENTIFIER().getText() + ",";
			}
			if (parameters.formalParameterList().lastFormalParameter() != null) {
				retStr += parameters.formalParameterList().lastFormalParameter().variableDeclaratorId().IDENTIFIER()
						.getText() + ",";
			}
			if (retStr.length() > 0) {
				retStr = retStr.substring(0, retStr.length() - 1);
			}
		}
		return retStr;
	}

	public String getPath(ParserRuleContext ctx) {
		String path = "";

		return path;
	}

	public int getVisibility(List<ModifierContext> list) {
		int visibility = 1;
		for(ModifierContext mctx: list) {
			if (mctx.classOrInterfaceModifier() != null) {
				int i = getVisibility(mctx.classOrInterfaceModifier());
				if (i != -1) {
					visibility = i;
				}
			}
		}
		return visibility;
	}

	private int getVisibility(ClassOrInterfaceModifierContext ctx) {
		if (ctx.PUBLIC() != null)
			return 3;
		else if (ctx.PROTECTED() != null)
			return 2;
		else if (ctx.PRIVATE() != null)
			return 0;
		else
			return -1;
	}

	/**
	 * TODO Annotate method
	 *
	 * @param nodeData
	 * @return
	 */
	public String visitLocalVariableDeclarationNoInit(LocalVariableDeclarationContext nodeData) {
		return visit(nodeData.variableDeclarators()) + ";";
	}

}
