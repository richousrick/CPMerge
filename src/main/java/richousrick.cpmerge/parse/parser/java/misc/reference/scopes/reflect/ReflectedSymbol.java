package richousrick.cpmerge.parse.parser.java.misc.reference.scopes.reflect;

import richousrick.cpmerge.parse.parser.java.misc.reference.FunctionCall;
import richousrick.cpmerge.parse.parser.java.misc.reference.Symbol;
import richousrick.cpmerge.parse.parser.java.misc.reference.scopes.Scope;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public class ReflectedSymbol extends Symbol {

	Field field;

	/**
	 * Initializes the ReflectedSymbol class
	 * TODO Annotate constructor
	 *
	 * @param name
	 * @param type
	 * @param context
	 * @param ctx
	 * @param modifiers
	 */
	public ReflectedSymbol(Field field, Scope context) {
		super(field.getName(), field.getType().getName(), context, null, null);
		this.field = field;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.misc.reference.Symbol#setType(java.lang.String)
	 */
	@Override
	public void setType(String type) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * richousrick.cpmerge.parse.parser.java.misc.reference.Symbol#addReference(richousrick.cpmerge.parse.parser.java.
	 * misc.reference.FunctionCall)
	 */
	@Override
	public void addReference(FunctionCall reference) {
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.misc.reference.Symbol#getReferences()
	 */
	@Override
	public ArrayList<FunctionCall> getReferences() {
		return new ArrayList<>();
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.misc.reference.Symbol#resolve()
	 */
	@Override
	public boolean resolve() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.parser.java.misc.reference.Symbol#getType()
	 */
	@Override
	public String getType() {
		return type;
	}

}
