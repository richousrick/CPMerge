package parse.parser.java.misc.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class FunctionSymbols {

	final Scope rootScope;
	Scope pointer;
	boolean valid;
	private HashMap<String, FunctionSymbol> symbolTable;
	private ArrayList<FunctionSymbol> declaredSymbols;
	/**
	 * Initializes the FunctionSymbols class
	 * TODO Annotate constructor
	 */
	public FunctionSymbols() {
		rootScope = new Scope(null);
		pointer = rootScope;
		valid = true;
		symbolTable = null;
		declaredSymbols = null;
	}

	public FunctionSymbols(FunctionSymbols symbols) {
		this();
		symbolTable = new HashMap<>();
		combine(symbols);
	}

	public boolean combine(FunctionSymbols symbols) {
		if (getSymbolTable()) {
			symbolTable = combine(symbols, symbolTable);
		}
		return symbolTable != null;
	}

	public static HashMap<String, FunctionSymbol> combine(FunctionSymbols symbols,
			HashMap<String, FunctionSymbol> symbolTable) {
		symbols.getSymbolTable();
		if (symbolTable != null && symbols.symbolTable != null) {
			HashMap<String, FunctionSymbol> combinedSymbolTable = new HashMap<String, FunctionSymbol>(symbolTable);
			for (Entry<String, FunctionSymbol> entry : symbols.symbolTable.entrySet()) {
				if (combinedSymbolTable.containsKey(entry.getKey())) {
					// If already exists
					FunctionSymbol symbol = combinedSymbolTable.get(entry.getKey());
					// if types dont match dont combine
					if (!symbol.equals(entry.getValue()))
						return null;
					// if types match and one is a parameter, set it to a
					// parameter
					if (symbol.declararationtype == 1) {
						if (entry.getValue().declararationtype == 0) {
							symbol.declararationtype = 0;
						}
					}
				} else {
					// if new
					combinedSymbolTable.put(entry.getKey(), new FunctionSymbol(entry.getValue()));
				}
			}
			return combinedSymbolTable;
		}
		return null;
	}

	public boolean getSymbolTable() {
		if (symbolTable != null)
			return true;
		if (valid) {
			HashMap<String, FunctionSymbol> symbolTable = getScopesFullSymbolTable(rootScope);
			if (symbolTable == null) {
				valid = false;
			} else {
				this.symbolTable = symbolTable;
				return true;
			}
		}
		return false;
	}

	public void setInvalid() {
		valid = false;
	}

	public boolean isValid() {
		return valid;
	}

	public boolean addSymbol(String name, int declararationtype, ParserRuleContext ctx, String type) {
		if (name.equals("fID") || pointer.isDefined(name)) {
			valid = false;
			return false;
		} else {
			pointer.symbols.put(name, new FunctionSymbol(declararationtype, name, pointer, ctx, type));
			return true;
		}
	}

	public boolean exists(String name) {
		return pointer.isDefined(name);
	}

	public void createScope() {
		Scope newScope = new Scope(pointer);
		pointer.scopes.add(newScope);
		pointer = newScope;
	}

	public void leaveScope() {
		pointer = pointer.parent;
	}

	private HashMap<String, FunctionSymbol> getScopesFullSymbolTable(Scope scope){
		HashMap<String, FunctionSymbol> symbolTable = scope.symbols;
		for(Scope s : scope.scopes) {
			HashMap<String, FunctionSymbol> tmpSymbolTable = getScopesFullSymbolTable(s);
			if(tmpSymbolTable == null)
				return null;
			else {
				for(Entry<String, FunctionSymbol> entry : tmpSymbolTable.entrySet()) {
					if(symbolTable.containsKey(entry.getKey())) {
						if(!symbolTable.get(entry.getKey()).equals(entry.getValue()))
							return null;
					}else {
						symbolTable.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		return symbolTable;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String retString = (valid ? "Valid" : "Invalid") + ":\n";
		retString += String.format("%12s|%12s|%12s%n", "Name    ", "DataType  ", "Declared As");
		if (!getSymbolTable())
			return "Invalid";
		for (FunctionSymbol symbol : symbolTable.values()) {
			retString += String.format("%12s|%12s|%12s%n", symbol.name, symbol.type,
					symbol.declararationtype == 0 ? "Parameter"
							: symbol.declararationtype == 1 ? "Inner Declaration" : "Loop Variable");
		}
		return retString;
	}

	/**
	 * TODO Annotate method
	 *
	 * @return
	 */
	public ArrayList<String[]> getParameters() {
		declaredSymbols = new ArrayList<>();
		ArrayList<String[]> retList = new ArrayList<>();
		for (FunctionSymbol symbol : symbolTable.values()) {
			if (symbol.declararationtype == 0) {
				retList.add(new String[] { symbol.name, symbol.type });
			} else if (symbol.declararationtype == 1) {
				declaredSymbols.add(symbol);
			}
		}
		return retList;
	}

	public ArrayList<String[]> getVariablesToInit() {
		ArrayList<String[]> retList = new ArrayList<>();
		for (FunctionSymbol symbol : declaredSymbols) {
			retList.add(new String[] { symbol.name, symbol.type });
		}
		return retList;
	}

}

class FunctionSymbol implements Comparable<FunctionSymbol> {
	// 0 : parameter, 1: function varaible, 2: loop Variable
	int declararationtype;
	final String name;
	private final Scope enclosingScope;
	private final ParserRuleContext ctx;
	final String type;

	/**
	 * Initializes the FunctionSymbol class
	 * TODO Annotate constructor
	 *
	 * @param type
	 * @param name
	 * @param enclosingScope
	 * @param ctx
	 */
	public FunctionSymbol(int declararationtype, String name, Scope enclosingScope, ParserRuleContext ctx,
			String type) {
		this.declararationtype = declararationtype;
		this.name = name;
		this.enclosingScope = enclosingScope;
		this.ctx = ctx;
		this.type = type;
	}

	/**
	 * Initializes the FunctionSymbol class
	 * TODO Annotate constructor
	 *
	 * @param value
	 */
	public FunctionSymbol(FunctionSymbol value) {
		this(value.declararationtype, value.name, value.enclosingScope, value.ctx, value.type);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FunctionSymbol) {
			FunctionSymbol fs = (FunctionSymbol) obj;
			return name.equals(fs.name) && type.equals(fs.type);
		} else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%12s|%12s|%12s%n", name, type,
				declararationtype == 0 ? "Parameter" : declararationtype == 1 ? "Inner Declaration" : "Loop Variable");
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FunctionSymbol o) {
		return Integer.compare(declararationtype, o.declararationtype);
	}

}

class Scope {
	final ArrayList<Scope> scopes;
	final Scope parent;
	final HashMap<String, FunctionSymbol> symbols;

	Scope(Scope parent) {
		scopes = new ArrayList<>();
		symbols = new HashMap<>();
		this.parent = parent;
	}

	boolean isDefined(String name) {
		return getSymbol(name) != null;
	}

	private FunctionSymbol getSymbol(String name) {
		if (symbols.containsKey(name))
			return symbols.get(name);
		else if (parent != null)
			return parent.getSymbol(name);
		else
			return null;
	}

	/**
	 * TODO Annotate method
	 */
	private String toStringRec(boolean isFirstCall) {
		String retString = "";
		if (isFirstCall) {
			retString += String.format("%12s|%12s|%12s%n", "Name    ", "DataType  ", "Declared As");
		}
		for (FunctionSymbol symbol : symbols.values()) {
			retString += String.format("%12s|%12s|%12s%n", symbol.name, symbol.type,
					symbol.declararationtype == 0 ? "Parameter"
							: symbol.declararationtype == 1 ? "Inner Declaration" : "Loop Variable");
		}
		for (Scope s : scopes) {
			retString += "\t" + s.toStringRec(false).replaceAll("\n", "\n\t");
		}
		return retString;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.parser.java.misc.validate.FunctionSymbols#toString()
	 */
	@Override
	public String toString() {
		return toStringRec(true);
	}

}
