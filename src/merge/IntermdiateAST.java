package merge;

import java.util.ArrayList;

import dif.ClassNode;

/**
 * Used to build the structure of the AST tree.
 * This is needed as the nodes in the AST cannot be inserted at a position,
 * they can only be added to the end.
 *
 * @author Rikkey Paal
 */
public abstract class IntermdiateAST implements Comparable<IntermdiateAST> {
	/**
	 *
	 */
	private final MergeGroup mergeGroup;


	/**
	 * Initializes the IntermdiateAST class
	 * TODO Annotate constructor
	 * @param mergeGroup
	 */
	IntermdiateAST(MergeGroup mergeGroup) {
		this.mergeGroup = mergeGroup;
	}

	ArrayList<IntermdiateAST> children = new ArrayList<>();
	IntermdiateAST parent;


	public void setParent(IntermdiateAST parent) {
		if (this.parent != null) {
			getParent().removeChild(this);
		}
		this.parent = parent;
		parent.children.add(this);
	}

	public void setParent(int childPos, IntermdiateAST parent) {
		if (this.parent != null) {
			getParent().removeChild(this);
		}
		this.parent = parent;
		parent.children.add(childPos, this);
	}

	public void removeChild(IntermdiateAST c) {
		children.remove(c);
	}

	/**
	 * TODO Annotate method
	 *
	 * @return
	 */
	public IntermdiateAST getParent() {
		return parent;
	}

	/**
	 * @return the children
	 */
	public ArrayList<IntermdiateAST> getChildren() {
		return children;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IntermdiateAST csComp) {
		if (parent != csComp.parent)
			return -1;
		ClassNodeSkeleton cs1 = null, cs = null;
		if (this instanceof ClassNodeSkeleton) {
			cs1 = (ClassNodeSkeleton) this;
		} else {
			System.err.println("Ordering AST with merge points added");
		}

		if (csComp instanceof ClassNodeSkeleton) {
			cs = (ClassNodeSkeleton) csComp;
		} else {
			System.err.println("Ordering AST with merge points added");
		}
		int pos = 0;
		for (int i : cs1.mapping.getMappings().keySet()) {
			if (cs.mapping.getMappings().containsKey(i)) {
				// get the Node positions relative to the parent

				ClassNode c1Node = mergeGroup.functions.get(i).getPostOrderDecendant(cs1.mapping.getMappings().get(i));
				int c1Pos = c1Node.getParent().getChildren().indexOf(c1Node);

				ClassNode c2Node = mergeGroup.functions.get(i).getPostOrderDecendant(cs.mapping.getMappings().get(i));
				int c2Pos = c2Node.getParent().getChildren().indexOf(c2Node);

				pos += Math.signum(
						Integer.compare(c1Pos, c2Pos));
			}
		}
		return pos;
	}

	abstract public String simpleCodeRepresentation();
}