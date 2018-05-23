package parse.parser.java.misc.reference.scopes;

import java.util.ArrayList;

import org.antlr.v4.runtime.ParserRuleContext;

import parse.parser.java.misc.reference.FunctionCall;
import parse.parser.java.misc.reference.Symbol;

/**
 * This scope referes to scopes that are part of a function body.
 * For instance the body of an if statement.
 *
 * @author Rikkey Paal
 */
public class InnerScope extends Scope {

	public InnerScope(ParserRuleContext id, Scope parent) {
		super(id, parent, null);
	}

	/**
	 * Creates a {@link InnerScope} to be a sub-scope
	 *
	 * @param ctx
	 *            of the subscope
	 * @return the scope created
	 */
	public InnerScope createScope(ParserRuleContext ctx) {
		InnerScope s = new InnerScope(ctx, this);
		scopes.put(new ScopeKey(ctx), s);
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.Scope#getPath()
	 */
	@Override
	public String calculatePath() {
		return parent.getPath();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.Scope#getFunctionDeclaration(java.lang.String)
	 */
	@Override
	public FunctionScope getFunctionDeclaration(String name) {
		return parent.getFunctionDeclaration(name);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * parse.parser.java.misc.Scope#getClassDeclaration(java.lang.String)
	 */
	@Override
	protected ClassScope getClassDeclaration(String name) {
		return parent.getClassDeclaration(name);
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
		String str = "";
		for (Symbol s : symbolTable.values()) {
			str += "\n\t" + s.toString().replaceAll("\n", "\n\t");
		}

		for (ArrayList<FunctionCall> fList : functionCalls.values()) {
			for (FunctionCall f : fList) {
				str += "\n\t" + f.toString().replaceAll("\n", "\n\t");
			}
		}

		for (Scope s : scopes.values()) {
			str += "\n\t" + s.toString().replaceAll("\n", "\n\t");
		}
		return str.trim();
	}

}