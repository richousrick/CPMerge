package datatypes.symboltable;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This class represents a symbol table, this does not factor in other symbol tables in different scopes
 * @author Rikkey Paal
 */
public class LocalSymbolTable {
	private ArrayList<SymbolTableEntry> indexedSymbolTable;
	private HashMap<String, SymbolTableEntry> mappedSymbolTable;
	private int startIndex;
	
	/**
	 * Initializes the LocalSymbolTable class
	 * TODO Annotate constructor
	 */
	public LocalSymbolTable(int startIndex) {
		indexedSymbolTable = new ArrayList<>();
		mappedSymbolTable = new HashMap<>();
		this.startIndex = startIndex;
	}
	
	/**
	 * 
	 * @param ID of symbol
	 * @return true if symbol exists
	 */
	public boolean hasEntry(int ID){
		ID -= startIndex;
		return ID > -1 && ID < indexedSymbolTable.size();
	}
	
	/**
	 * @param name of symbol
	 * @return true if symbol exists
	 */
	public boolean hasEntry(String name){
		return mappedSymbolTable.containsKey(name);
	}
	
	/**
	 * @param name of symbol
	 * @param type, datatype of symbol
	 * @return entry representing the symbol, or null if a symbol with that name exists
	 */
	public SymbolTableEntry addSymbolTableEntry(String name, String type){
		if(!hasEntry(name)){
			SymbolTableEntry entry = new SymbolTableEntry(indexedSymbolTable.size()+startIndex, name, type);
			indexedSymbolTable.add(entry);
			mappedSymbolTable.put(name, entry);
			return entry;
		}else{
			return null;
		}
	}
	
	public SymbolTableEntry getSymbol(String name){
		return mappedSymbolTable.get(name);
	}
	
	public SymbolTableEntry getSymbol(int ID) {
		return indexedSymbolTable.get(ID - startIndex);
	}
	
}
