package dif;

import java.util.ArrayList;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import node.Node;

/**
 * This class is used to represent the parsed code in a way that can be easier processed
 * 
 * @author Rikkey Paal
 */
public class ClassNode extends Node<ParserRuleContext> {

	private String identifier;

	/**
	 * Type of data held in node<br>
	 * 0 : class head<br>
	 * 1 : method head<br>
	 * 2 : statement<br>
	 * 3 : temporary<br>
	 */
	private byte type;

	/**
	 * Number of descendants the node has + 1
	 */
	private int size = -1;

	/**
	 * Initialises the ClassNode class
	 * 
	 * @param nodeData node in the CST this node is referencing
	 * @param identifier detailing what the value in the CST is
	 * @param type the {@link #type}
	 * 
	 */
	public ClassNode(ParserRuleContext nodeData, String identifier, byte type) {
		super(nodeData);
		this.identifier = identifier;
		this.type = type;
	}

	/**
	 * Initialises the ClassNode with a {@link #type} of 2
	 * 
	 * @param nodeData node in the CST this node is referencing
	 * @param identifier detailing what the value in the CST is
	 */
	public ClassNode(ParserRuleContext nodeData, String identifier) {
		this(nodeData, identifier, (byte)2);
	}

	public ArrayList<ClassNode> getChildrenAsCN() throws ClassCastException {
		ArrayList<ClassNode> nodes = new ArrayList<>();
		for (Node<ParserRuleContext> child : getChildren()) {
			nodes.add((ClassNode) child);
		}
		return nodes;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @return the type
	 */
	public byte getType() {
		return type;
	}

	/**
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Recalculates the {@link #size} of the node.<br>
	 * This is done by recalculating the size of all child nodes.
	 * The size is equal to the sum of the sizes of all children plus 1.
	 * @return the size of the node
	 */
	public int recalculateSize() {
		if (getChildren().size() == 0) {
			return 1;
		} else {
			size = 1;
			for (ClassNode c : getChildrenAsCN()) {
				if (c == null)
					System.out.println("?");
				size += c.recalculateSize();
			}
		}
		return size;
	}

	/*
	 * (non-Javadoc)
	 * @see node.Node#addChild(node.Node)
	 */
	@Override
	public void addChild(Node c) {
		if (c != null)
			super.addChild(c);
		else
			System.err.println("Trying to add a null node to the class");
	}

	/**
	 * Creates a new ClassNode from the text given, this is then {@link #addChild(Node) added as a child}.
	 * @param identifier of the new ClassNode
	 */
	public void addChild(String identifier) {
		super.addChild(new ClassNode(null, identifier));
	}

	/**
	 * Runs {@link #addChild(String)} using the text from the terminal node.
	 * @param node to be used as the name of the new ClassNode
	 */
	public void addChild(TerminalNode node) {
		super.addChild(new ClassNode(null, node.getText()));
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		if (size == -1)
			return recalculateSize();
		else
			return size;
	}

	/**
	 * Try's to replace long chains of nodes in the subtree with a single node.<br>
	 * This is done by finding all chains of nodes, where each node in the chain has at most 1 child.
	 * Then for each chain the identifiers of its nodes are concatenated, and used to generate a new ClassNode which will replace the chain.
	 */
	private void compressMethod() {
		ArrayList<ClassNode> children = getChildrenAsCN();
		for (ClassNode c : children) {
			c.compressMethod();
		}
		if (children.size() == 1 && children.get(0).getType() == 2) {
			this.identifier += "." + children.get(0).getIdentifier();
			getChildren().remove(0);
			for (ClassNode c : children.get(0).getChildrenAsCN()) {
				addChild(c);
			}
		}
	}

	/**
	 * {@link #compressMethod() Compresses all methods} that are inside the class.
	 * @param recalulateSize if true the class will have its size recalculated after compressing.
	 */
	private void compressClass(boolean recalulateSize) {
		for (ClassNode c : getChildrenAsCN()) {
			if (c.getType() == 2) {
				c.compressMethod();
				if (recalulateSize) {
					c.recalculateSize();
				}
			} else if (c.getType() == 1 || c.getType() == 0) {
				c.compressClass(false);
			}
		}
	}

	/**
	 * {@link #compressMethod() Compresses all methods} that are inside the class.
	 * Then {@link #recalculateSize() recalculates the size} of the class.
	 */
	public void compressClass() {
		compressClass(true);
	}

	/**
	 * Returns a string representation of the node.<br>
	 * Used for debugging and testing.<br>
	 * @param prefix to be added to each line. e.g. addong a tab for indentation.
	 * @param isTail if true \ is printed, | is printed otherwise
	 * @return A string representation of the tree
	 */
	public String print(String prefix, boolean isTail) {
		ArrayList<ClassNode> children = getChildrenAsCN();
		String str = prefix + (isTail ? "\\-- " : "|-- ") + identifier;
		for (int i = 0; i < children.size() - 1; i++) {
			str += "\n" + children.get(i).print(prefix + (isTail ? "    " : "|   "), false);
		}
		if (children.size() > 0) {
			str += "\n" + children.get(children.size() - 1).print(prefix + (isTail ? "    " : "|   "), true);
		}
		return str;
	}

	/**
	 * (non-Javadoc)
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return print("", true);
	}

}