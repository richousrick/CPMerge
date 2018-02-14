package merge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import dif.ClassNode;

class FunctionMappings{
	private final ArrayList<FunctionMapping> mappings;

	/**
	 * Initializes the FunctionMappings class
	 * TODO Annotate constructor
	 */
	public FunctionMappings(ArrayList<ClassNode> functions) {
		mappings = new ArrayList<>(functions.size());
		for (ClassNode function : functions) {
			mappings.add(new FunctionMapping(function));
		}
	}

	/**
	 * Add a relation between the functions with id's f1 and f2. Set the value
	 * of the relation to mapping
	 *
	 * @param f1
	 *            id of the first function in the relation
	 * @param f2
	 *            id of the second function in the relation
	 * @param mapping
	 *            between the two nodes
	 */
	public void addRelations(int f1, int f2, List<int[]> mapping) {
		getFunctionMapping(f1).addRelation(f2, mapping);
		getFunctionMapping(f2).addRelation(f1, mapping);
	}

	/**
	 * Finds the spanning tree of functions that share mappings, starting from
	 * the specified function. This tree is returned as a list.
	 *
	 * @param id
	 *            of function to start search from.
	 * @return the list of all functions that can be reached through mappings
	 */
	public ArrayList<Integer> getContaingGroup(int id) {
		ArrayList<Integer> tree = new ArrayList<>();
		tree.add(id);
		int counter = 0;
		while (counter < tree.size() && counter < mappings.size()) {
			for (Integer i : getFunctionMapping(tree.get(counter)).getAdjacent()) {
				if (!tree.contains(i)) {
					tree.add(i);
				}
			}
			counter++;
		}
		return tree;
	}

	/**
	 * Checks if the two specified functions are related
	 *
	 * @param f1
	 *            id of first function
	 * @param f2
	 *            id of second function
	 * @return true if there is a mapping between the two functions
	 */
	public boolean areRelated(int f1, int f2) {
		return getMapping(f1, f2) != null;
	}

	/**
	 * Get the mapping of the two specified functions
	 *
	 * @param f1
	 *            id of first function
	 * @param id
	 *            of second function
	 * @return mapping between the two functions, or null if there is no such
	 *         mapping
	 */
	public List<int[]> getMapping(int f1, int f2) {
		return getFunctionMapping(f1).getMapping(f2);
	}

	/**
	 * Get the {@link ClassNode} associated with the specified function
	 *
	 * @param f1
	 *            id of the specified function
	 * @return the {@link ClassNode} associated with the specified function
	 */
	public ClassNode getFunctionNode(int f1) {
		return getFunctionMapping(f1).getNode();
	}

	private FunctionMapping getFunctionMapping(int f1) {
		return mappings.get(f1);
	}

	public ArrayList<ClassNode> getClasses() {
		ArrayList<ClassNode> nodes = new ArrayList<>();
		for (FunctionMapping fm : mappings) {
			nodes.add(fm.getNode());
		}
		return nodes;
	}

	/**
	 * Generates a list of groups of related functions. each group in this list
	 * contains the id's of functions in a specific merge group
	 *
	 * @return the functions grouped into their merge groups
	 */
	public ArrayList<ArrayList<Integer>> groupFunctions() {
		ArrayList<ArrayList<Integer>> forest = new ArrayList<>();
		ArrayList<Integer> nodesToProcess = new ArrayList<>();
		for (int i = 0; i < mappings.size(); i++) {
			nodesToProcess.add(i);
		}

		while (!nodesToProcess.isEmpty()) {
			ArrayList<Integer> nextTree = getContaingGroup(nodesToProcess.get(0));
			nodesToProcess.removeAll(nextTree);
			if (nextTree.size() > 1) {
				forest.add(nextTree);
			}
		}
		return forest;
	}

	@Override
	public String toString() {
		String str = "(";
		for (int i = 0; i < mappings.size(); i++) {
			str+=i+",";
		}
		str = str.substring(0, str.length() - 1) + "), (";
		for (int i = 0; i < mappings.size(); i++) {
			for (int x : getFunctionMapping(i).getAdjacent()) {
				if (x > i) {
					str += "[" + i + "," + x + "],";
				}
			}
		}
		return str.substring(0,str.length()-1)+")";
	}

	class FunctionMapping {
		/** Adjacency list with mapping vector */
		private final HashMap<Integer, List<int[]>> relationships;
		// ensure mapping
		// order stays consistant
		private final ClassNode node;

		/**
		 * Initializes the MergeThread.FunctionMappings class TODO Annotate
		 * constructor
		 */
		public FunctionMapping(ClassNode node) {
			relationships = new HashMap<>();
			this.node = node;
		}

		/**
		 * Set this mapping to be related to the function at the set position
		 *
		 * @param functionID
		 *            ID of the mapped function
		 * @param mapping
		 *            between this and the other function
		 */
		private void addRelation(int functionID, List<int[]> mapping) {
			relationships.put(functionID, mapping);
		}

		/**
		 * @return all relations
		 */
		private HashMap<Integer, List<int[]>> getRelations() {
			return relationships;
		}

		/**
		 * @return a list of the positions of functions this is related to
		 */
		private Set<Integer> getAdjacent() {
			return relationships.keySet();
		}

		/**
		 * get the mapping of this funciton and the function with the specified
		 * ID
		 *
		 * @param f1
		 *            ID of other function
		 * @return mapping between this function and the other, or null if there
		 *         is no such mapping
		 */
		private List<int[]> getMapping(int f1) {
			return relationships.get(f1);
		}

		private ClassNode getNode() {
			return node;
		}
	}
}