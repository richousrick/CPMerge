package dif;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import node.Node;

/**
 * This class is used to represent the parsed code in a way that can be easier
 * processed
 *
 * @author Rikkey Paal
 */
public class ClassNode extends Node<ParserRuleContext> {

	private class MinimiseReturns {
		int pos;
		HashSet<Integer> searchSet;
		boolean wasMatch = false;
		ArrayList<ClassNode> uniqueNodes;

		/**
		 * Initializes the MinimiseReturns class TODO Annotate constructor
		 *
		 * @param pos
		 * @param set1
		 */
		public MinimiseReturns(int pos, HashSet<Integer> set, ArrayList<ClassNode> nodes) {
			this.pos = pos;
			searchSet = set;
			uniqueNodes = nodes;
		}

	}


	private ClassNode[] PORepresentation;

	private String identifier;

	private String mustMatch;

	/**
	 * Type of data held in node<br>
	 * 0 : class head<br>
	 * 1 : method head<br>
	 * 2 : statement<br>
	 * 3 : temporary<br>
	 */
	private final byte type;

	/**
	 * Post order position of this node, used for debugging.
	 */
	private int pos;

	/**
	 * Number of descendants the node has + 1
	 */
	private int size = -1;

	private ClassNode parent = null;


	/**
	 * Initializes the ClassNode class by deep copying another node.
	 *
	 * @param toCopy
	 *            node to be deep copied.
	 */
	public ClassNode(ClassNode toCopy) {
		super(toCopy.getNodeData());
		type = toCopy.type;
		identifier = new String(toCopy.identifier);
		for (final ClassNode c : toCopy.getChildrenAsCN()) {
			addChild(new ClassNode(c));
		}
	}

	/**
	 * Initialises the ClassNode with a {@link #type} of 2
	 *
	 * @param nodeData
	 *            node in the CST this node is referencing
	 * @param identifier
	 *            detailing what the value in the CST is
	 */
	public ClassNode(ParserRuleContext nodeData, String identifier) {
		this(nodeData, identifier, (byte) 2);
	}

	/**
	 * Initialises the ClassNode class
	 *
	 * @param nodeData
	 *            node in the CST this node is referencing
	 * @param identifier
	 *            detailing what the value in the CST is
	 * @param type
	 *            the {@link #type}
	 *
	 */
	public ClassNode(ParserRuleContext nodeData, String identifier, byte type) {
		super(nodeData);
		this.identifier = identifier;
		this.type = type;
	}

	/**
	 * Initialises the ClassNode class as a method head
	 *
	 * @param nodeData
	 *            node in the CST this node is referencing
	 * @param identifier
	 *            detailing what the value in the CST is
	 *
	 * @param mustMatch
	 *            a sting containing data that must be identical to another node
	 *            when checking. Useful for adding extra information that must
	 *            be checked. e.g. checking return type matches.
	 *
	 */
	public ClassNode(ParserRuleContext nodeData, String identifier, String mustMatch) {
		super(nodeData);
		this.identifier = identifier;
		type = 1;
		this.mustMatch = mustMatch;
	}


	/*
	 * (non-Javadoc)
	 * @see node.Node#addChild(node.Node)
	 */
	@Override
	public void addChild(Node c) {
		if (c != null) {
			((ClassNode) c).setParent(this);
			super.addChild(c);
		} else {
			System.err.println("Trying to add a null node to the class");
		}
	}

	/**
	 * Creates a new ClassNode from the text given, this is then
	 * {@link #addChild(Node) added as a child}.
	 *
	 * @param identifier
	 *            of the new ClassNode
	 */
	public void addChild(String identifier) {
		addChild(new ClassNode(null, identifier));
	}

	/**
	 * Runs {@link #addChild(String)} using the text from the terminal node.
	 *
	 * @param node
	 *            to be used as the name of the new ClassNode
	 */
	public void addChild(TerminalNode node) {
		addChild(new ClassNode(null, node.getText()));
	}

	/**
	 * Removes the specified child
	 *
	 * @param node
	 *            to be removed
	 */
	public void removeChild(ClassNode node) {
		getChildren().remove(node);
	}

	/**
	 * Removes the specified children
	 *
	 * @param children
	 *            to remove
	 */
	public void removeChildren(Collection<ClassNode> children) {
		getChildren().removeAll(children);
	}

	/**
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(ClassNode parent) {
		this.parent = parent;
	}


	/**
	 * Returns the children of the class as a list of {@link ClassNode}
	 *
	 * @return the children of the class as a list of {@link ClassNode}
	 * @throws ClassCastException
	 *             if there is a child that is not a ClassNode
	 */
	public ArrayList<ClassNode> getChildrenAsCN() throws ClassCastException {
		final ArrayList<ClassNode> nodes = new ArrayList<>();
		for (final Node<ParserRuleContext> child : getChildren()) {
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
	 * @return the parent
	 */
	public ClassNode getParent() {
		return parent;
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
	 * @return the type
	 */
	public byte getType() {
		return type;
	}


	/**
	 * {@link #compressMethod() Compresses all methods} that are inside the
	 * class. Then {@link #recalculateSize() recalculates the size} of the
	 * class.
	 */
	public void compressClass() {
		compressClass(true);
	}

	/**
	 * {@link #compressMethod() Compresses all methods} that are inside the
	 * class.
	 *
	 * @param recalulateSize
	 *            if true the class will have its size recalculated after
	 *            compressing.
	 */
	private void compressClass(boolean recalulateSize) {
		for (final ClassNode c : getChildrenAsCN()) {
			if (c.getType() == 2) {
				c.compressMethod();
				if (recalulateSize) {
					c.recalculateSize();
				}
			} else if (c.getType() == 1 || c.getType() == 0) {
				c.compressClass(false);
			}
		}
		setPostOrderList();
	}

	/**
	 * Try's to replace long chains of nodes in the subtree with a single
	 * node.<br>
	 * This is done by finding all chains of nodes, where each node in the chain
	 * has at most 1 child. Then for each chain the identifiers of its nodes are
	 * concatenated, and used to generate a new ClassNode which will replace the
	 * chain.
	 */
	private void compressMethod() {
		final ArrayList<ClassNode> children = getChildrenAsCN();
		for (final ClassNode c : children) {
			c.compressMethod();
		}
		if (children.size() == 1 && children.get(0).getType() == 2) {
			identifier += "." + children.get(0).getIdentifier();
			getChildren().remove(0);
			for (final ClassNode c : children.get(0).getChildrenAsCN()) {
				addChild(c);
			}
		}
	}

	private void setPostOrderList() {
		ArrayList<ClassNode> postOrderList = new ArrayList<>();
		for (ClassNode child : getChildrenAsCN()) {
			child.setPostOrderList();
			postOrderList.addAll(new ArrayList<ClassNode>(Arrays.asList(child.getPostOrderList())));
		}
		postOrderList.add(this);
		PORepresentation = new ClassNode[postOrderList.size()];
		postOrderList.toArray(PORepresentation);
	}

	public ClassNode[] getPostOrderList() {
		return PORepresentation;
	}

	/**
	 * Gets the set of nodes from the specified postorder positions. Returns
	 * only those that have no ancestors.
	 *
	 * @param positions
	 *            of the nodes to retrieve
	 * @return
	 */
	public ArrayList<ClassNode> getMinimalNodesFromPostOrder(HashSet<Integer> positions, boolean removeMatches) {
		return minimiseAndGet(positions, 0, removeMatches).uniqueNodes;
	}

	private MinimiseReturns minimiseAndGet(HashSet<Integer> positions, int posl, boolean removeMatch) {
		int total = posl;
		final ArrayList<ClassNode> classMatches = new ArrayList<>();
		for (final ClassNode c : getChildrenAsCN()) {
			final MinimiseReturns ret = c.minimiseAndGet(positions, total, removeMatch);
			total = ret.pos;
			classMatches.addAll(ret.uniqueNodes);
			positions = ret.searchSet;
		}

		total++;
		MinimiseReturns retSetAndInt;
		if (positions.contains(total)) {
			positions.remove(total);
			final ArrayList<ClassNode> c = new ArrayList<>();
			c.add(this);
			retSetAndInt = new MinimiseReturns(total, positions, c);
			retSetAndInt.wasMatch = true;
		} else {
			if (removeMatch) {
				removeChildren(classMatches);
			}
			retSetAndInt = new MinimiseReturns(total, positions, classMatches);
		}

		return retSetAndInt;
	}

	/**
	 * Returns a string representation of the node.<br>
	 * Used for debugging and testing.<br>
	 *
	 * @param prefix
	 *            to be added to each line. e.g. addong a tab for indentation.
	 * @param isTail
	 *            if true \ is printed, | is printed otherwise
	 * @return A string representation of the tree
	 */
	public String print(String prefix, boolean isTail) {
		return printMaker(prefix, isTail, false);
	}

	/**
	 * Returns a string representation of the node.<br>
	 * Used for debugging and testing.<br>
	 *
	 * @param prefix
	 *            to be added to each line. e.g. addong a tab for indentation.
	 * @param isTail
	 *            if true \ is printed, | is printed otherwise
	 * @param printPos
	 *            if true will add the post order position of the nodes to each
	 *            line
	 * @return A string representation of the tree
	 */
	public String print(String prefix, boolean isTail, boolean printPos) {
		return printMaker(prefix, isTail, printPos);
	}

	private String printMaker(String prefix, boolean isTail, boolean printPos) {
		final ArrayList<ClassNode> children = getChildrenAsCN();
		String str = prefix + (printPos ? pos + ":" : "") + (isTail ? "\\-- " : "|-- ") + identifier;
		for (int i = 0; i < children.size() - 1; i++) {
			str += "\n" + children.get(i).printMaker(prefix + (isTail ? "    " : "|   "), false, printPos);
		}
		if (children.size() > 0) {
			str += "\n"
					+ children.get(children.size() - 1).printMaker(prefix + (isTail ? "    " : "|   "), true, printPos);
		}
		return str;
	}

	/**
	 * Recalculates the {@link #size} of the node.<br>
	 * This is done by recalculating the size of all child nodes. The size is
	 * equal to the sum of the sizes of all children plus 1.
	 *
	 * @return the size of the node
	 */
	public int recalculateSize() {
		if (getChildren().size() == 0)
			return 1;
		else {
			size = 1;
			for (final ClassNode c : getChildrenAsCN()) {
				if (c == null) {
					System.err.println("Cannot calculate the size of null child");
				}
				size += c.recalculateSize();
			}
		}
		return size;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return print("", true, false);
	}

	public boolean compareMustMatch(ClassNode c) {
		if (mustMatch == null && c.mustMatch == null)
			return true;
		else if (mustMatch != null && c.mustMatch != null)
			return c.mustMatch.equals(mustMatch);
		else
			return false;
	}

}