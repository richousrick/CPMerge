package merge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Represents a merge point
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public class MergePoint extends IntermdiateAST {

	private final HashMap<UniqueSet, ArrayList<ClassNodeSkeleton>> mergeOptions;

	/**
	 * @return the mergeOptions
	 */
	public HashMap<UniqueSet, ArrayList<ClassNodeSkeleton>> getMergeOptions() {
		return mergeOptions;
	}

	/**
	 * Initializes the MergeGroup.MergePoint class
	 *
	 * @param parent
	 *            node of the merge point
	 * @param mergeOptions
	 *            map of possible nodes at the specified position
	 *
	 */
	public MergePoint(HashMap<UniqueSet, ArrayList<ClassNodeSkeleton>> mergeOptions, MergeGroup mergeGroup) {
		super(mergeGroup);
		int insertIndex = Integer.MAX_VALUE;
		this.mergeOptions = mergeOptions;
		IntermdiateAST parent = mergeOptions.values().iterator().next().get(0).getParent();
		// make all options children of this node
		for (ArrayList<ClassNodeSkeleton> cs : mergeOptions.values()) {
			int elementIndex = cs.get(0).getParent().children.indexOf(cs.get(0));
			if (elementIndex < insertIndex) {
				insertIndex = elementIndex;
			}
			for (ClassNodeSkeleton cns : cs) {
				cns.setParent(this);
			}
		}

		setParent(insertIndex, parent);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String s = "merge";
		for (Entry<UniqueSet, ArrayList<ClassNodeSkeleton>> option : mergeOptions.entrySet()) {
			s += "\n\t" + Arrays.toString(option.getKey().setFuncIds.toArray());
			for (ClassNodeSkeleton cns : option.getValue()) {
				s += "\n\t" + cns.toString().replaceAll("\n", "\n\t\t");
			}
		}
		return s;
	}

	public void addChild(int pos, ClassNodeSkeleton child) {
		child.setParent(this);
		mergeOptions.get(child.uniqueSet).add(pos, child);
	}

	/*
	 * (non-Javadoc)
	 * @see merge.MergeGroup.IntermdiateAST#simpleCodeRepresentation()
	 */
	@Override
	public String simpleCodeRepresentation() {
		String representation = "";
		for (Entry<UniqueSet, ArrayList<ClassNodeSkeleton>> option : mergeOptions.entrySet()) {
			representation += "if( functionId in " + option.getKey().toString() + "){";
			for (ClassNodeSkeleton struct : option.getValue()) {
				representation += "\n\t" + struct.simpleCodeRepresentation().replaceAll("\n", "\n\t");
			}
			representation += "\n} else ";
		}
		return representation.substring(0, representation.length() - 6);
	}


}