package datatypes.ast;

import java.util.ArrayList;

import com.sun.xml.internal.ws.server.UnsupportedMediaException;

import datatypes.symboltable.LocalSymbolTable;
import node.Node;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
abstract class ASTNode<D> extends Node<D> implements Comparable<ASTNode<D>>{

	protected LocalSymbolTable symbolTable;
	
	protected final ASTNode<D> parent;
	
	/**
	 * Initializes the ASTNode class
	 * TODO Annotate constructor
	 *
	 * @param nodeData
	 */
	public ASTNode(D nodeData, ASTNode<D> parent) {
		super(nodeData);
		symbolTable = null;
		this.parent = parent;
	}

	/**
	 * get the type of data held in node<br>
	 * 0 : File
	 * 1 : class head<br>
	 * 2 : method head<br>
	 * 3 : statement<br>
	 * 4 : temporary<br>
	 * 5 : merge point
	 */
	public abstract byte getType();

	/**
	 * Used for Debugging
	 * TODO Annotate method
	 * @return
	 */
	public String getIdentifier(){
		throw new NullPointerException("Hi");
		//return "";
	}
	
	/* (non-Javadoc)
	 * @see node.Node#addChild(node.Node)
	 */
	@Deprecated
	@Override
	public void addChild(Node c) {
		throw new UnsupportedOperationException("This method should not be called, use \"addChild(ASTNode<D> node)\" instead");
	}
	
	/**
	 * Add the specified node to be a child of this node
	 * @param node to add as a child
	 */
	void addChild(ASTNode<D> node){
		super.addChild(node);
	}
	
	/**
	 * Get the total number of descendant nodes, i.e. get the number of nodes that are children, grand children,great-grand children etc.
	 * Note size includes self.
	 * So a node with no children has size 1
	 * @return the number of descendants
	 */
	public int getSize(){
		int size = 1;
		for(ASTNode<D> child : getAllChildren()){
			size += child.getSize();
		}
		return size;
	}
	
	/**
	 * @return a list of all children
	 */
	public ArrayList<? extends ASTNode<D>> getAllChildren(){
		ArrayList<ASTNode<D>> children = new ArrayList<>();
		for(Node<D> child : getChildren()){
			children.add((ASTNode<D>) child);
		}
		return children;
	}

	/**
	 * @return the nodes parent
	 */
	public ASTNode<D> getParent(){
		return parent;
	}

	/**
	 * @return the symbol table associated with the scope, or null of there is no symbol table
	 */
	public LocalSymbolTable getSymbolTable() {
		return symbolTable;
	}

	/**
	 * Set the {@link LocalSymbolTable} associated with this scope
	 * @param symbolTable to set
	 */
	public void setSymbolTable(LocalSymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}
	
}
