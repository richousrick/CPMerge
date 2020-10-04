package richousrick.cpmerge.dif;

import node.Node;

import java.util.ArrayList;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public abstract class ASTNode<D> extends Node<D> {

	/**
	 * Initializes the ASTNode class
	 * TODO Annotate constructor
	 *
	 * @param nodeData
	 */
	public ASTNode(D nodeData) {
		super(nodeData);
	}

	/**
	 * get the type of data held in node<br>
	 * 0 : class head<br>
	 * 1 : method head<br>
	 * 2 : statement<br>
	 * 3 : temporary<br>
	 * 4 : merge point
	 */
	public abstract byte getType();

	public abstract String getIdentifier();

	public abstract ASTNode<D> getPostOrderDecendant(int postOrderPos);

	public abstract ArrayList<? extends ASTNode<D>> getChildrenAsASTNode();

	public abstract boolean compareCharactersitics(ASTNode<?> node);

	public abstract ASTNode<D> getParent();

	public abstract int getPostOrderPos();

	public abstract int getSize();
}
