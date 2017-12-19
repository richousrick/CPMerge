package dif;

import java.util.ArrayList;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import node.Node;

/**
 * TODO Annotate class
 * 
 * @author 146813
 */
public class ClassNode extends Node<ParserRuleContext> {

	private String identifier;

	/**
	 * Type of data held in node 0:class head 1:method head 2:statement
	 * 3:temporary
	 */
	private byte type;

	private int size = -1;

	/**
	 * Initializes the ClassNode class TODO Annotate constructor
	 * 
	 * @param nodeData
	 * @param identifier
	 * @param type
	 * 
	 */
	public ClassNode(ParserRuleContext nodeData, String identifier, byte type) {
		super(nodeData);
		this.identifier = identifier;
		this.type = type;
	}

	/**
	 * Initializes the ClassNode class TODO Annotate constructor
	 * 
	 * @param nodeData
	 * @param identifier
	 * @param type
	 * 
	 */
	public ClassNode(ParserRuleContext nodeData, String identifier) {
		super(nodeData);
		this.identifier = identifier;
		this.type = (byte) 2;
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

	public void addChild(String identifier) {
		super.addChild(new ClassNode(null, identifier));
	}

	public void addChild(TerminalNode identifier) {
		super.addChild(new ClassNode(null, identifier.getText()));
	}

	public int getSize() {
		if (size == -1)
			return recalculateSize();
		else
			return size;
	}

	private void compress() {
		ArrayList<ClassNode> children = getChildrenAsCN();
		for (ClassNode c : children) {
			c.compress();
		}
		if (children.size() == 1 && children.get(0).getType() == 2) {
			this.identifier += "." + children.get(0).getIdentifier();
			getChildren().remove(0);
			for (ClassNode c : children.get(0).getChildrenAsCN()) {
				addChild(c);
			}
		}
	}

	public void compressClass(boolean topLevel) {
		for (ClassNode c : getChildrenAsCN()) {
			if (c.getType() == 2) {
				c.compress();
				if (topLevel) {
					c.recalculateSize();
				}
			} else if (c.getType() == 1 || c.getType() == 0) {
				c.compressClass(false);
			}
		}
	}

	public void compressClass() {
		compressClass(true);
	}

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

	@Override
	public String toString() {
		return print("", true);
	}

}