package parse.parser.java.misc.reference;

import java.util.ArrayList;
import java.util.Collections;

import org.antlr.v4.runtime.ParserRuleContext;

import parse.parser.java.misc.reference.scopes.Scope;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class Symbol {

	private final String name;
	protected String type;
	private String fullType;
	private final Scope context;
	private final ParserRuleContext ctx;
	private final ArrayList<String> modifiers;
	private final ArrayList<FunctionCall> references;

	/**
	 * Initializes the SymbolReferecne class
	 * TODO Annotate constructor
	 * @param name
	 * @param type
	 * @param context
	 * @param staticType
	 */
	public Symbol(String name, String type, Scope context,
			ParserRuleContext ctx, ArrayList<String> modifiers) {
		this.name = name;
		this.type = type;
		this.context = context;
		this.ctx = ctx;
		if (modifiers != null) {
			modifiers.removeAll(Collections.singleton(null));
		}
		this.modifiers = modifiers;
		references = new ArrayList<>();
	}
	public String getName() {
		return name;
	}
	public String getType() {
		return fullType;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Scope getContext() {
		return context;
	}
	public ParserRuleContext getCtx() {
		return ctx;
	}
	public ArrayList<String> getModifiers() {
		return modifiers;
	}

	public boolean hasModifier(String modifier){
		return modifiers.contains(modifier);
	}

	public void addReference(FunctionCall reference) {
		references.add(reference);
	}

	public ArrayList<FunctionCall> getReferences() {
		return references;
	}

	@Override
	public String toString() {
		String s = "";
		for (String modifier : modifiers) {
			s += modifier + " ";
		}
		s += type + " " + name;
		return s;
	}

	/**
	 * Resolves the type of the symbol
	 *
	 * @return true if type could be resolved
	 */
	public boolean resolve() {
		// resolve type
		if (fullType == null) {
			String typeName = context.getTypePath(type);
			if (typeName != null) {
				fullType = typeName;
			} else
				return false;
		}
		return true;
	}

}
