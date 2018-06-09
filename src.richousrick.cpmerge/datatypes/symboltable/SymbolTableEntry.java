package datatypes.symboltable;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class SymbolTableEntry{
	private int ID;
	private String name;
	private String type;
	
	/**
	 * Initializes the SymbolTableEntry class
	 * TODO Annotate constructor
	 * @param ID
	 * @param name
	 * @param type
	 */
	public SymbolTableEntry(int ID, String name, String type) {
		this.ID = ID;
		this.name = name;
		this.type = type;
	}

	public int getID() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
}
