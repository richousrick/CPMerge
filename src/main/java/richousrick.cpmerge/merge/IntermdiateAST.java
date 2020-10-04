package richousrick.cpmerge.merge;

import richousrick.cpmerge.dif.ASTNode;

import java.util.ArrayList;
import java.util.Map.Entry;

/**
 * Used to build the structure of the AST tree.
 * This is needed as the nodes in the AST cannot be inserted at a position,
 * they can only be added to the end.
 *
 * @author Rikkey Paal
 */
public abstract class IntermdiateAST<D> implements Comparable<IntermdiateAST<D>> {

	/**
	 * What the unique set of the node is
	 * if null then belongs to all sets
	 */
	protected UniqueSet set = null;

	protected ArrayList<IntermdiateAST<D>> children = new ArrayList<>();
	protected IntermdiateAST<D> parent;
	private final MergeGroup mergeGroup;

	/**
	 * @return the mergeGroup
	 */
	public MergeGroup getMergeGroup() {
		return mergeGroup;
	}

	/**
	 * Initializes the IntermdiateAST class
	 * TODO Annotate constructor
	 *
	 * @param mergeGroup
	 */
	public IntermdiateAST(MergeGroup mergeGroup) {
		this.mergeGroup = mergeGroup;
	}

	public void setParent(IntermdiateAST<D> parent) {
		if (this.parent != null) {
			getParent().removeChild(this);
		}
		this.parent = parent;
		parent.children.add(this);
		updateUniqueSet();
	}

	public void setParent(int childPos, IntermdiateAST<D> parent) {
		if (this.parent != null) {
			getParent().removeChild(this);
		}
		this.parent = parent;
		parent.children.add(childPos, this);
		updateUniqueSet();
	}

	public void removeChild(IntermdiateAST<D> c) {
		children.remove(c);
	}

	public UniqueSet getUniqueSet() {
		return set != null ? set : parent.getUniqueSet();
	}

	private void updateUniqueSet() {
		if (parent instanceof MergePoint) {
			for (Entry<UniqueSet, ArrayList<ClassNodeSkeleton<D>>> sets : ((MergePoint<D>) parent).getMergeOptions()
					.entrySet()) {
				if (sets.getValue().contains(this)) {
					set = sets.getKey();
					return;
				}
			}
		} else {
			set = parent.set;
		}
	}

	/**
	 * TODO Annotate method
	 *
	 * @return
	 */
	public IntermdiateAST<D> getParent() {
		return parent;
	}


	/**
	 * @return the children
	 */
	public ArrayList<? extends IntermdiateAST<D>> getChildren() {
		return children;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IntermdiateAST<D> csComp) {
		if (parent != csComp.parent)
			return -1;
		ClassNodeSkeleton<D> cs1 = null, cs = null;
		if (this instanceof ClassNodeSkeleton) {
			cs1 = (ClassNodeSkeleton<D>) this;
		} else {
			System.err.println("Ordering AST with merge points added");
		}

		if (csComp instanceof ClassNodeSkeleton) {
			cs = (ClassNodeSkeleton<D>) csComp;
		} else {
			System.err.println("Ordering AST with merge points added");
		}
		int pos = 0;
		for (int i : cs1.mapping.getMappings().keySet()) {
			if (cs.mapping.getMappings().containsKey(i)) {
				// get the Node positions relative to the parent

				ASTNode<?> c1Node = mergeGroup.functions.get(i).getPostOrderDecendant(cs1.mapping.getMappings().get(i));
				int c1Pos = c1Node.getParent().getChildren().indexOf(c1Node);

				ASTNode<?> c2Node = mergeGroup.functions.get(i).getPostOrderDecendant(cs.mapping.getMappings().get(i));
				int c2Pos = c2Node.getParent().getChildren().indexOf(c2Node);

				pos += Math.signum(
						Integer.compare(c1Pos, c2Pos));
			}
		}
		return pos;
	}

	abstract public String simpleCodeRepresentation();

	public void setUniqueSetR(UniqueSet uniqueSet) {
		setUniqueSet(uniqueSet);
		for (IntermdiateAST<D> child : children) {
			if (child instanceof ClassNodeSkeleton) {
				child.setUniqueSet(set);
			} else {
				child.updateUniqueSet();
			}
		}
	}

	public void setUniqueSet(UniqueSet uniqueSet) {
		set = uniqueSet;
	}

}