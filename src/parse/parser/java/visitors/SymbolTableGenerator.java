package parse.parser.java.visitors;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import parse.parser.java.comp.JavaParser.BlockContext;
import parse.parser.java.comp.JavaParser.ClassBodyDeclarationContext;
import parse.parser.java.comp.JavaParser.ClassDeclarationContext;
import parse.parser.java.comp.JavaParser.CompilationUnitContext;
import parse.parser.java.comp.JavaParser.ConstructorDeclarationContext;
import parse.parser.java.comp.JavaParser.CreatorContext;
import parse.parser.java.comp.JavaParser.EnhancedForControlContext;
import parse.parser.java.comp.JavaParser.ExpressionContext;
import parse.parser.java.comp.JavaParser.FieldDeclarationContext;
import parse.parser.java.comp.JavaParser.FormalParameterContext;
import parse.parser.java.comp.JavaParser.ImportDeclarationContext;
import parse.parser.java.comp.JavaParser.InnerCreatorContext;
import parse.parser.java.comp.JavaParser.LastFormalParameterContext;
import parse.parser.java.comp.JavaParser.LocalTypeDeclarationContext;
import parse.parser.java.comp.JavaParser.LocalVariableDeclarationContext;
import parse.parser.java.comp.JavaParser.MethodDeclarationContext;
import parse.parser.java.comp.JavaParser.ModifierContext;
import parse.parser.java.comp.JavaParser.StatementContext;
import parse.parser.java.comp.JavaParser.TypeDeclarationContext;
import parse.parser.java.comp.JavaParser.TypeTypeContext;
import parse.parser.java.comp.JavaParser.VariableDeclaratorContext;
import parse.parser.java.comp.JavaParser.VariableModifierContext;
import parse.parser.java.comp.JavaParserBaseVisitor;
import parse.parser.java.misc.reference.scopes.ClassScope;
import parse.parser.java.misc.reference.scopes.FileScope;
import parse.parser.java.misc.reference.scopes.FunctionScope;
import parse.parser.java.misc.reference.scopes.InnerScope;
import parse.parser.java.misc.reference.scopes.Scope;

/**
 * Only visitCompilationUnit should be called
 * @author Rikkey Paal
 */
public class SymbolTableGenerator extends JavaParserBaseVisitor<Scope> {

	FileScope rootScope;
	Scope currentScope;
	PrettyPrinter printer;
	ArrayList<String> imports = new ArrayList<>();
	String packageStr;
	String fileName;

	public FunctionScope addSymbolTable(FunctionScope s, MethodDeclarationContext ctx) {
		if (printer == null) {
			printer = new PrettyPrinter(false);
		}
		Scope tmpScope = currentScope;
		currentScope = s;
		visitChildren(ctx);
		FunctionScope retScope = (FunctionScope) currentScope;
		currentScope = tmpScope;
		return retScope;
	}

	public void processClass(CompilationUnitContext ctx, String fileName) {
		this.fileName = fileName;


		visitCompilationUnit(ctx);

		rootScope.removeAllEmptyScopes();

		Scope.addFileScope(rootScope);

	}

	@Override
	public Scope visitImportDeclaration(ImportDeclarationContext ctx) {
		imports.add(new PrettyPrinter(false).visitQualifiedName(ctx.qualifiedName()));
		return null;
	}

	@Override
	public Scope visitCompilationUnit(CompilationUnitContext ctx){
		printer = new PrettyPrinter(false);
		for(ImportDeclarationContext idctx: ctx.importDeclaration()) {
			visitImportDeclaration(idctx);
		}
		if (ctx.packageDeclaration() != null) {
			packageStr = printer.visit(ctx.packageDeclaration().qualifiedName());
		} else {
			packageStr = "";
		}
		rootScope = new FileScope(ctx, packageStr, imports, fileName);

		currentScope = rootScope;
		for (TypeDeclarationContext tdctx : ctx.typeDeclaration()) {
			visit(tdctx);
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
	public Scope visitClassDeclaration(ClassDeclarationContext ctx) {
		String name = ctx.IDENTIFIER().getText();
		String extendsType = null;
		ArrayList<String> interfaces = null;

		if (ctx.EXTENDS() != null) {
			extendsType = printer.visitTypeType(ctx.typeType());
		}
		if (ctx.IMPLEMENTS() != null) {
			interfaces = new ArrayList<>();
			for (TypeTypeContext ttctx : ctx.typeList().typeType()) {
				interfaces.add(printer.visit(ttctx));
			}
		}

		Scope tmp = currentScope;
		if (currentScope instanceof FileScope) {
			currentScope = ((FileScope) currentScope).createScope(ctx, name, extendsType, interfaces);
		} else {
			currentScope = ((ClassScope) currentScope).createScope(ctx, name, extendsType, interfaces);
		}

		visitChildren(ctx);

		currentScope = tmp;
		return null;

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitLocalTypeDeclaration(
	 * parse.parser.java.comp.JavaParser.LocalTypeDeclarationContext)
	 */
	@Override
	public Scope visitLocalTypeDeclaration(LocalTypeDeclarationContext ctx) {
		return null;
	}

	@Override
	public Scope visitLocalVariableDeclaration(LocalVariableDeclarationContext ctx){

		if (ctx.typeType().classOrInterfaceType() != null
				&& ctx.typeType().classOrInterfaceType().typeArguments() != null
				&& ctx.typeType().classOrInterfaceType().typeArguments().size() > 0)
			return null;

		String type = printer.visitTypeType(ctx.typeType());

		ArrayList<String> modifiers = new ArrayList<>();
		for(VariableModifierContext vmCtx: ctx.variableModifier()){
			if (vmCtx.FINAL()!=null){
				modifiers.add(vmCtx.FINAL().getText());
				break;
			}
		}

		for(VariableDeclaratorContext vdCtx : ctx.variableDeclarators().variableDeclarator()){
			currentScope.addSymbol(printer.visitVariableDeclaratorId(vdCtx.variableDeclaratorId()), vdCtx, type, modifiers);
		}
		visitChildren(ctx);


		return null;
	}

	@Override
	public Scope visitFieldDeclaration(FieldDeclarationContext ctx){

		String type = printer.visitTypeType(ctx.typeType());

		ArrayList<String> modifiers = new ArrayList<>();
		for(ModifierContext mCtx: ((ClassBodyDeclarationContext)ctx.parent.parent).modifier()){
			modifiers.add(printer.visitModifier(mCtx));
		}

		for(VariableDeclaratorContext vdCtx : ctx.variableDeclarators().variableDeclarator()){
			currentScope.addSymbol(printer.visitVariableDeclaratorId(vdCtx.variableDeclaratorId()), vdCtx, type, modifiers);
		}

		return null;
	}

	@Override
	public Scope visitFormalParameter(FormalParameterContext ctx){

		String type = printer.visitTypeType(ctx.typeType());

		ArrayList<String> modifiers = getModifiers(ctx.variableModifier());

		currentScope.addSymbol(printer.visitVariableDeclaratorId(ctx.variableDeclaratorId()), ctx, type, modifiers);

		return null;
	}

	@Override
	public Scope visitLastFormalParameter(LastFormalParameterContext ctx){

		String type = printer.visitTypeType(ctx.typeType()) + "[]";

		ArrayList<String> modifiers = getModifiers(ctx.variableModifier());

		currentScope.addSymbol(printer.visitVariableDeclaratorId(ctx.variableDeclaratorId()), ctx, type, modifiers);

		return null;
	}

	@Override
	public Scope visitEnhancedForControl(EnhancedForControlContext ctx) {
		String type = printer.visitTypeType(ctx.typeType());

		ArrayList<String> modifiers = getModifiers(ctx.variableModifier());

		currentScope.addSymbol(printer.visitVariableDeclaratorId(ctx.variableDeclaratorId()), ctx, type, modifiers);

		return null;
	}


	public ArrayList<String> getModifiers(List<VariableModifierContext> ctx) {
		ArrayList<String> modifiers = new ArrayList<>();
		for (VariableModifierContext vmCtx : ctx) {
			if (vmCtx.FINAL()!=null){
				modifiers.add(vmCtx.FINAL().getText());
				break;
			}
		}
		return modifiers;
	}


	@Override
	public Scope visitMethodDeclaration(MethodDeclarationContext ctx) {

		String name = ctx.IDENTIFIER().getText();
		String returnType = printer.visit(ctx.typeTypeOrVoid());
		String[] parameters = printer.printParameterListTypes(ctx.formalParameters()).split(",");
		if (parameters.length == 1 && parameters[0] == "") {
			parameters = null;
		}
		int visibility = 1;
		if (ctx.parent.parent instanceof ClassBodyDeclarationContext) {
			visibility = printer.getVisibility(((ClassBodyDeclarationContext) ctx.parent.parent).modifier());
		}

		FunctionScope funcScope = ((ClassScope) currentScope).createScope(ctx, name, returnType, visibility,
				parameters);

		Scope tmp = currentScope;
		currentScope = funcScope;
		visit(ctx.formalParameters());


		visit(ctx.methodBody());
		currentScope = tmp;
		return null;
	}


	@Override
	public Scope visitConstructorDeclaration(ConstructorDeclarationContext ctx) {
		String name = ctx.IDENTIFIER().getText();

		String[] parameters = printer.printParameterListTypes(ctx.formalParameters()).split(",");
		if (parameters.length == 1 && parameters[0] == "") {
			parameters = null;
		}
		int visibility = 1;
		if (ctx.parent.parent instanceof ClassBodyDeclarationContext) {
			visibility = printer.getVisibility(((ClassBodyDeclarationContext) ctx.parent.parent).modifier());
		}

		FunctionScope funcScope = ((ClassScope) currentScope).createScope(ctx, name, visibility, parameters);

		Scope tmp = currentScope;
		currentScope = funcScope;
		visit(ctx.formalParameters());
		visit(ctx.constructorBody);
		currentScope = tmp;
		return null;
	}



	@Override
	public Scope visitExpression(ExpressionContext ctx) {
		// String id;
		// String objectRef;
		// String name = printer.visit(ctx.expression(0));
		//
		// String[] section = name.split("\\.");
		// id = section[section.length - 1];
		//
		// for (int i = 0; i < section.length - 1; i++) {
		//
		// }
		//
		// if (ctx.expressionList() != null) {
		// currentScope.addFunctionCall(id, ctx, objectRef);
		// }

		if (ctx.NEW() != null)
			return null;

		if (isFunctionCall(ctx)) {

			String name = printer.visit(ctx.expression(0));
			if (!ctx.expression(0).expression().isEmpty() && isFunctionCall(ctx.expression(0).expression(0))){
				// add functions that this function wore called on
				visitExpression(ctx.expression(0).expression(0));
			}
			String[] parts = name.split("\\.");
			String id = parts[parts.length - 1];
			String objectRef = "";
			for (int i = 0; i < parts.length - 1; i++) {
				objectRef += "." + parts[i];
			}
			if (objectRef.length() != 0) {
				objectRef = objectRef.substring(1);
			}
			currentScope.addFunctionCall(id, ctx, objectRef);
			if (ctx.expressionList() != null) {
				// visit any functions that wore declare as parameters
				visit(ctx.expressionList());
			}
		} else {
			visitChildren(ctx);
		}

		return null;
	}


	private boolean isFunctionCall(ExpressionContext ctx){
		boolean isFunctionCall = ctx.expressionList() != null;
		if (!isFunctionCall) {
			if (ctx == null || ctx.children == null) {
				System.out.println(new PrettyPrinter(false).visit(ctx.getParent().getParent().getParent()));

			}
			if (ctx.children.size() == 3 && ctx.getChild(0) instanceof ExpressionContext) {
				if (ctx.getChild(1) instanceof TerminalNodeImpl && ctx.getChild(1).getText().equals("(")) {
					if (ctx.getChild(2) instanceof TerminalNodeImpl && ctx.getChild(2).getText().equals(")")) {
						isFunctionCall = true;
					}
				}
			}
		}
		return isFunctionCall;
	}

	@Override
	public Scope visitStatement(StatementContext ctx) {
		if (ctx.FOR() != null && ctx.statement(0).blockLabel != null) {
			Scope s = visit(ctx.statement(0).blockLabel);
			Scope tmp = currentScope;
			currentScope = s;
			visit(ctx.forControl());
			currentScope = tmp;
		} else {
			visitChildren(ctx);
		}
		return null;
	}

	@Override
	public Scope visitBlock(BlockContext ctx){
		InnerScope blockScope = null;
		if (currentScope instanceof FunctionScope) {
			blockScope = ((FunctionScope) currentScope).createScope(ctx);
		} else if (currentScope instanceof InnerScope) {
			blockScope = ((InnerScope) currentScope).createScope(ctx);
		}
		if (blockScope != null) {
			Scope tmpScope = currentScope;
			currentScope = blockScope;
			visitChildren(ctx);
			currentScope = tmpScope;
			return blockScope;
		} else
			return null;

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitCreator(parse.parser.
	 * java.comp.JavaParser.CreatorContext)
	 */
	@Override
	public Scope visitCreator(CreatorContext ctx) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.comp.JavaParserBaseVisitor#visitInnerCreator(parse.
	 * parser.java.comp.JavaParser.InnerCreatorContext)
	 */
	@Override
	public Scope visitInnerCreator(InnerCreatorContext ctx) {
		return null;
	}
}
