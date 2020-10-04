package richousrick.cpmerge.merge;

import java.util.Arrays;
import java.util.List;

public class UniqueSet {
	final List<Integer> setFuncIds;
	final ClassNodeSkeleton[] setMembers;

	/**
	 * Initializes the UniqueSet class
	 * TODO Annotate constructor
	 *
	 * @param setFuncIds
	 * @param setMembers
	 */
	public UniqueSet(List<Integer> setFuncIds, ClassNodeSkeleton[] setMembers) {
		this.setFuncIds = setFuncIds;
		this.setMembers = setMembers;
		for (ClassNodeSkeleton m : setMembers) {
			m.setUniqueSet(this);
		}
	}

	/**
	 * @param set
	 *            to compare to
	 * @return 0: if they are equal, 1: if they share some functions, -1: if
	 *         they share no functions
	 */
	public int compareTo(UniqueSet set) {
		if (set.equals(this))
			return 0;
		else {
			for (Integer funcId : set.getSetFuncIds()) {
				if (setFuncIds.contains(funcId))
					return 1;
			}
			return -1;
		}
	}

	/**
	 * @return the setFuncIds
	 */
	public List<Integer> getSetFuncIds() {
		return setFuncIds;
	}

	/**
	 * @return the setMembers
	 */
	public ClassNodeSkeleton[] getSetMembers() {
		return setMembers;
	}

	@Override
	public String toString() {
		return Arrays.toString(setFuncIds.toArray());
	}

}