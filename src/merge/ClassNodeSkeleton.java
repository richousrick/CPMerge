package merge;

import dif.ASTNode;
import merge.MergeGroup.Mapping;

/**
 * Represents a Node in the AST
 *
 * @author Rikkey Paal
 */
public class ClassNodeSkeleton<D> extends IntermdiateAST<D> {
	private ASTNode<D> node;

	/**
	 * @param node
	 *            the node to set
	 */
	public void setNode(ASTNode<D> node) {
		this.node = node;
	}

	final Mapping mapping;

	/**
	 * Node containing the data of the first child the node represented in this
	 * class contained.
	 * This may be useful in identifying what part of the plug-in representation
	 * of the data the node resides
	 */
	final ASTNode<D> firstChild;

	/**
	 * @return the firstChild
	 */
	public ASTNode<?> getFirstChild() {
		return firstChild;
	}

	/**
	 * @return the mapping
	 */
	public Mapping getMapping() {
		return mapping;
	}

	/**
	 * Initializes the MergeGroup.ClassNodeSkeleton class
	 * TODO Annotate constructor
	 */
	public ClassNodeSkeleton(ASTNode<D> node, Mapping mapping, MergeGroup mergeGroup, ASTNode<D> firstChild) {
		super(mergeGroup);
		this.node = node;
		this.mapping = mapping;
		this.firstChild = firstChild;
	}



	/**
	 * Uses the relative positioning across all occurrences of the nodes to
	 * reorder the children.
	 * TODO check if works
	 * TODO rewrite so Nodes follow following rules
	 * 		Node N(c1,c2,...cn)
	 * 			each child cn must appear after all nodes that appear before it in its function
	 */
	public void orderChildren() {
		sortChildren();
		for (IntermdiateAST<D> child : children) {
			if (child instanceof ClassNodeSkeleton) {
				((ClassNodeSkeleton<?>) child).orderChildren();
			} else {
				System.err.println("Ordering AST with merge points added");
			}
		}
	}

	private void sortChildren() {
		NodeCostMappings<D> costs = new NodeCostMappings();
		for(IntermdiateAST<D> child : children) {
			costs.addNode(child);
		}

		for(int x = 0; x < children.size(); x++) {
			IntermdiateAST<D> childx = children.get(x);
			for(int y = x+1; y < children.size(); y++) {
				IntermdiateAST<D> childy = children.get(y);
				int costxy = childx.compareTo(childy);
				costs.addRelation(childx, childy, costxy, -costxy);
			}
		}
		children = costs.orderCosts();
	}

	@Override
	public String toString(){
		String s = node.getIdentifier() + " "+mapping.toString();
		for (IntermdiateAST<D> child : children) {
			s += "\n\t" + child.toString().replaceAll("\n", "\n\t");
		}
		return s;
	}

	public ASTNode<D> getNode() {
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see merge.MergeGroup.IntermdiateAST#simpleCodeRepresentation()
	 */
	@Override
	public String simpleCodeRepresentation() {
		String representation = node.getIdentifier();
		for (IntermdiateAST<D> child : children) {
			representation += "\n\t" + child.simpleCodeRepresentation().replaceAll("\n", "\n\t");
		}
		return representation;
	}

	/**
	 * TODO Annotate method
	 * @param i
	 * @return
	 */
	public IntermdiateAST<D> getChild(int i) {
		return children.get(i);
	}

}