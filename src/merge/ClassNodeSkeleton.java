package merge;

import java.util.Collections;

import dif.ClassNode;
import merge.MergeGroup.Mapping;

/**
 * Represents a Node in the AST
 *
 * @author Rikkey Paal
 */
public class ClassNodeSkeleton extends IntermdiateAST {
	private ClassNode node;

	/**
	 * @param node
	 *            the node to set
	 */
	public void setNode(ClassNode node) {
		this.node = node;
	}

	final Mapping mapping;

	UniqueSet uniqueSet;

	/**
	 * Node containing the data of the first child the node represented in this
	 * class contained.
	 * This may be useful in identifying what part of the plug-in representation
	 * of the data the node resides
	 */
	final ClassNode firstChild;

	/**
	 * @return the firstChild
	 */
	public ClassNode getFirstChild() {
		return firstChild;
	}

	/**
	 * @return the mapping
	 */
	public Mapping getMapping() {
		return mapping;
	}

	/**
	 * @param uniqueSet
	 *            the uniqueSet to set
	 */
	public void setUniqueSet(UniqueSet uniqueSet) {
		this.uniqueSet = uniqueSet;
	}

	/**
	 * Initializes the MergeGroup.ClassNodeSkeleton class
	 * TODO Annotate constructor
	 */
	public ClassNodeSkeleton(ClassNode node, Mapping mapping, MergeGroup mergeGroup, ClassNode firstChild) {
		super(mergeGroup);
		this.node = node;
		this.mapping = mapping;
		this.firstChild = firstChild;
	}

	/**
	 * @return the uniqueSet
	 */
	public UniqueSet getUniqueSet() {
		return uniqueSet;
	}

	void moveToMergePoint() {

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
		//			children.sort(new Comparator<IntermdiateAST>() {
		//				@Override
		//				public int compare(IntermdiateAST c1, IntermdiateAST c2) {
		//					ClassNodeSkeleton cs1 = null, cs2 = null;
		//					if (c1 instanceof ClassNodeSkeleton) {
		//						cs1 = (ClassNodeSkeleton) c1;
		//					} else {
		//						System.err.println("Ordering AST with merge points added");
		//					}
		//
		//					if (c2 instanceof ClassNodeSkeleton) {
		//						cs2 = (ClassNodeSkeleton) c2;
		//					} else {
		//						System.err.println("Ordering AST with merge points added");
		//					}
		//					int pos = 0;
		//					for (int i : cs1.mapping.getMappings().keySet()) {
		//						if (cs2.mapping.getMappings().containsKey(i)) {
		//							// get the Node positions relative to the parent
		//
		//							ClassNode c1Node = functions.get(i).getPostOrderDecendant(cs1.mapping.getMappings().get(i));
		//							int c1Pos = c1Node.getParent().getChildren().indexOf(c1Node);
		//
		//							ClassNode c2Node = functions.get(i).getPostOrderDecendant(cs2.mapping.getMappings().get(i));
		//							int c2Pos = c2Node.getParent().getChildren().indexOf(c2Node);
		//
		//							pos += Math.signum(
		//									Integer.compare(c1Pos, c2Pos));
		//						}
		//					}
		//					return pos;
		//				}
		//			});
		Collections.sort(children);
		for (IntermdiateAST child : children) {
			if (child instanceof ClassNodeSkeleton) {
				((ClassNodeSkeleton) child).orderChildren();
			} else {
				System.err.println("Ordering AST with merge points added");
			}
		}
	}
	@Override
	public String toString(){
		String s = node.getIdentifier() + " "+mapping.toString();
		for(IntermdiateAST child:children){
			s += "\n\t" + child.toString().replaceAll("\n", "\n\t");
		}
		return s;
	}

	public ClassNode getNode() {
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see merge.MergeGroup.IntermdiateAST#simpleCodeRepresentation()
	 */
	@Override
	public String simpleCodeRepresentation() {
		String representation = node.getIdentifier();
		for (IntermdiateAST child : children) {
			representation += "\n\t" + child.simpleCodeRepresentation().replaceAll("\n", "\n\t");
		}
		return representation;
	}

}