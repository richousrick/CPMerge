package merge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import dif.ASTNode;
import parse.PluginInterface;

/**
 * Handles the merging of multiple nodes in a group of related functions into a single AST representing the merged function
 * @author Rikkey Paal
 */
@SuppressWarnings("rawtypes")
public class MergeGroup {
	private final ArrayList<List<int[]>> functionMapping;
	final ArrayList<ASTNode<?>> functions;
	private final PluginInterface<?> plugin;
	private final int ID;

	/**s
	 * Initializes the MergeGroup class
	 * TODO Annotate constructor
	 * @param sharedTree
	 * @param mergeCandidates
	 */
	public MergeGroup(ArrayList<ASTNode<?>> functions, ArrayList<List<int[]>> functionMapping,
			PluginInterface<?> plugin, int ID) {
		this.functionMapping  =functionMapping;
		this.functions = functions;
		this.plugin = plugin;
		this.ID = ID;
	}

	public ArrayList<ASTNode<?>> getFunctions() {
		return functions;
	}

	/**
	 * Retrieves the root nodes of subtrees that are unique to each function.
	 * function 1 is also converted into a tree containing the shared nodes.
	 *
	 * @param function1
	 *            the smaller function to get the merge candidates out of. Also
	 *            all unique nodes will be removed.
	 * @param function2
	 *            the larger function to get the merge candidates from.
	 * @param Mapping
	 *            between the two
	 * @return an array containing the roots of the unique subtrees.
	 */
	// private ArrayList<ClassNode>[] getMergeCandidates(ArrayList<ClassNode>
	// functions) {
	//
	//
	//
	// }

	private List<int[]> getRelationship(int f1Pos, int f2Pos) {
		if (f1Pos == f2Pos)
			throw new IllegalArgumentException("Cannot reference mapping to self");
		int pos = 0;
		int len = functions.size();
		if (f1Pos > f2Pos) {
			for (int i = 0; i < f2Pos; i++) {
				pos += len;
				len--;
			}
		} else {
			for (int i = 0; i < f1Pos; i++) {
				pos += len;
				len--;
			}
		}
		return functionMapping.get(pos);
	}

	public String printSharedCode() {
		return "";// s;
	}

	public MergedFunction buildMergeFunction() {
		HashMap<List<Integer>, UniqueSet> uniqueSets = getMinimalUniqueSets();
		ClassNodeSkeleton root = null;
		ArrayList<Integer> sharedId = new ArrayList<>();
		for (int i = 0; i < functions.size(); i++) {
			sharedId.add(i);
		}
		// order root nodes children
		for (ClassNodeSkeleton n : uniqueSets.get(sharedId).setMembers) {
			if (n.getParent() == null) {
				if(root!=null){
					System.err.println("root twice");
					System.exit(-1);
				}
				root = n;
			}
		}
		root.orderChildren();

		// go up on each set to blockstatemetn ancestor
		plugin.preMerge(root);
		//
		// // Debug info
		// for (List<Integer> li : uniqueSets.keySet()) {
		// System.out.println("\tU"+Arrays.toString(li.toArray()));
		// for (ClassNodeSkeleton n : uniqueSets.get(li).setMembers) {
		// System.out.println("\t\t" + n.toString().replaceAll("\n", "\n\t\t"));
		// }
		// }

		// remove the shared nodes
		UniqueSet complete = uniqueSets.remove(sharedId);


		HashMap<IntermdiateAST, ArrayList<ClassNodeSkeleton>> sisterNodes = groupByParent(uniqueSets);

		// group nodes sharing the same position
		// [parentID][groupID][groupMemberID]
		ArrayList<ArrayList<ArrayList<ClassNodeSkeleton>>> shareSisterList = new ArrayList<>();
		for (ArrayList<ClassNodeSkeleton> sisterList : sisterNodes.values()) {
			Collections.sort(sisterList);
			shareSisterList.add(groupByPosition(sisterList));
		}

		// [groupID][memberSetID][memberPos]
		ArrayList<ArrayList<ArrayList<ClassNodeSkeleton>>> shareSisterGroups = new ArrayList<>();
		// optimise result by reducing number of merge groups
		// i.e. example group b2 and n
		// TODO add at end of above section
		for (ArrayList<ArrayList<ClassNodeSkeleton>> sisterGroup : shareSisterList) {
			shareSisterGroups.addAll(joinAdjacentGroups(sisterGroup));
		}

		// iterate over all groups converting them to mergeGroups


		for(ArrayList<ArrayList<ClassNodeSkeleton>> currMergeGroup: shareSisterGroups){
			HashMap<UniqueSet, ArrayList<ClassNodeSkeleton>> mergeOptions = new HashMap<>();
			for(ArrayList<ClassNodeSkeleton> groupOption : currMergeGroup){
				mergeOptions.put(groupOption.get(0).set, groupOption);
			}

			new MergePoint(mergeOptions, MergeGroup.this);
		}

		root.setUniqueSetR(complete);
		return new MergedFunction(functions, root, ID);
	}

	/**
	 * Joins neighbouring groups that are subsets of each other.<br>
	 * Two groups x, y are combined if their unique sets follow x &sube; y or x
	 * &sup; y.<br>
	 * This is done to stop cases of conditionals checking for the same
	 * functions following each other.
	 * e.g.
	 *
	 * <pre>
	 * ...
	 * if(fID == 4){
	 * 	statement 1
	 * }
	 * if (fID == 4{
	 * 	statement 2
	 * }
	 * ...
	 * </pre>
	 *
	 * changes to
	 *
	 * <pre>
	 * ...
	 * if(fID == 4){
	 * 	statement 1
	 * 	statement 2
	 * }
	 * ...
	 * </pre>
	 *
	 * creates a 3D list [groupPos][uniqueSetID][statementPos].<br>
	 * groupPos : relative position of the group.<br>
	 * uniqueSetID : position of unique set in that group.<br>
	 * statementPos : position of the statement in the unique set.
	 *
	 * @param sisterGroup
	 *            group of sister nodes {@link #groupByPosition(ArrayList)
	 *            grouped by position}
	 * @return 3D list of {@link ClassNodeSkeleton}'s referenced
	 *         [groupPos][uniqueSetID][statementPos]
	 */
	private ArrayList<ArrayList<ArrayList<ClassNodeSkeleton>>> joinAdjacentGroups(
			ArrayList<ArrayList<ClassNodeSkeleton>> sisterGroup) {
		// completed list of adjacent groups
		ArrayList<ArrayList<ArrayList<ClassNodeSkeleton>>> shareSisterGroups = new ArrayList<>();
		// current list of adjacent groups
		ArrayList<ArrayList<ClassNodeSkeleton>> adjacentGroup = new ArrayList<>();
		for (ClassNodeSkeleton cns : sisterGroup.get(0)) {
			adjacentGroup.add(new ArrayList<>(Arrays.asList(cns)));
		}
		// unique sets of each group in adjacent set
		ArrayList<UniqueSet> adjacentGroupSet = getSets(sisterGroup.get(0));

		for (int sisterGroupIterator = 1; sisterGroupIterator < sisterGroup.size(); sisterGroupIterator++) {
			//
			ArrayList<ClassNodeSkeleton> currentGroup = sisterGroup.get(sisterGroupIterator);

			// Unique sets of the current group to check
			ArrayList<UniqueSet> currSisterGroupSet = getSets(currentGroup);
			// Compare sets
			switch (compareGroups(adjacentGroupSet, currSisterGroupSet)) {
				case -1:
					// Adjacent subset of current
					// copy current int adjacent, inserting new unique sets

					// go over every element in adjacentgroup
					// for (int adjacentMemberIterator = 0;
					// adjacentMemberIterator < adjacentGroup
					// .size(); adjacentMemberIterator++) {
					// int currMemberPos = currSisterGroupSet
					// .indexOf(adjacentGroupSet.get(adjacentMemberIterator));
					// if (currMemberPos > -1) {
					// adjacentGroup.get(adjacentMemberIterator)
					// .add(sisterGroup.get(sisterGroupIterator).get(currMemberPos));
					// }else {
					// ArrayList<ClassNodeSkeleton> newList = new ArrayList<>();
					// newList.add(sisterGroup.get(sisterGroupIterator).get(currMemberPos));
					// adjacentGroup.add(newList);
					// adjacentGroupSet.add(currSisterGroupSet.get(index))
					// }
					// }

					// iterate through sets in current group
					for (int currentMemberIterator = 0; currentMemberIterator < currentGroup
							.size(); currentMemberIterator++) {
						// index of current set in adjacent group
						int adjacentIndex = adjacentGroupSet.indexOf(currSisterGroupSet.get(currentMemberIterator));

						if (adjacentIndex != -1) {
							// append current element to correct set in adjacent
							// set
							adjacentGroup.get(adjacentIndex).add(currentGroup.get(currentMemberIterator));
						} else {
							// create a new entry in adjacent group and set to
							// correspond with the new element
							ArrayList<ClassNodeSkeleton> newList = new ArrayList<>();
							newList.add(sisterGroup.get(sisterGroupIterator).get(currentMemberIterator));
							adjacentGroup.add(newList);
							adjacentGroupSet.add(currSisterGroupSet.get(currentMemberIterator));
						}
					}


					break;
				case 0:
					// Adjacent equals current
					// copy current int adjacent

					// for (int adjacentMemberIterator = 0;
					// adjacentMemberIterator < adjacentGroup
					// .size(); adjacentMemberIterator++) {
					// int currMemberPos = currSisterGroupSet
					// .indexOf(adjacentGroupSet.get(adjacentMemberIterator));
					// adjacentGroup.get(adjacentMemberIterator)
					// .add(sisterGroup.get(sisterGroupIterator).get(currMemberPos));
					// }

				case 1:
					// Current subset of adjacent
					// copy current int adjacent

					/*
					 * for (int adjacentMemberIterator = 0;
					 * adjacentMemberIterator < adjacentGroup
					 * .size(); adjacentMemberIterator++) {
					 * int currMemberPos = currSisterGroupSet
					 * .indexOf(adjacentGroupSet.get(adjacentMemberIterator));
					 * if (currMemberPos > -1) {
					 * adjacentGroup.get(adjacentMemberIterator)
					 * .add(sisterGroup.get(sisterGroupIterator).get(
					 * currMemberPos));
					 * }else {
					 * throw new
					 * UnexpectedException("node should be contained in adjacent group"
					 * );
					 * }
					 * }
					 */

					// iterate through sets in current group
					for (int currentMemberIterator = 0; currentMemberIterator < currentGroup
							.size(); currentMemberIterator++) {
						// index of current set in adjacent group
						int adjacentIndex = adjacentGroupSet.indexOf(currSisterGroupSet.get(currentMemberIterator));

						// append current element to correct set in adjacent set

						adjacentGroup.get(adjacentIndex).add(currentGroup.get(currentMemberIterator));
					}

					break;
				case 2:
					// save last group
					// set lastGroup to currGroup
					shareSisterGroups.add(adjacentGroup);
					adjacentGroup = new ArrayList<>();
					for (ClassNodeSkeleton cns : sisterGroup.get(sisterGroupIterator)) {
						adjacentGroup.add(new ArrayList<>(Arrays.asList(cns)));
					}
					break;
			}

			// if same, merge groups

		}
		shareSisterGroups.add(adjacentGroup);
		return shareSisterGroups;
	}

	/**
	 * Groups nodes by their relative position.
	 * The nodes are grouped [position][candidates].
	 * They are ordered, such that the nodes (candidates) in position n should
	 * appear before nodes in position n+1, and after those in position n-1.
	 * Note, not all nodes in group n must appear before the nodes in the
	 * adjacent groups. However at least one node x in group n but appear before
	 * a node in group n+1, And node y (which may be the same node as x) must
	 * appear after a node in group n-1.
	 *
	 * @param sisterList
	 *            list of nodes that share the same parent.
	 * @return A 2d list of {@link ClassNodeSkeleton ClassNodeSkeleton's}
	 *         representing the positioning of the nodes.
	 */
	private ArrayList<ArrayList<ClassNodeSkeleton>> groupByPosition(ArrayList<ClassNodeSkeleton> sisterList) {
		ArrayList<ArrayList<ClassNodeSkeleton>> sisterGroups = new ArrayList<>();
		ArrayList<ClassNodeSkeleton> shareSisterNodes = new ArrayList<>();
		shareSisterNodes.add(sisterList.get(0));
		for (int i = 1; i < sisterList.size(); i++) {
			boolean equal = true;
			int comp = 0;
			ClassNodeSkeleton n = sisterList.get(i);
			// Compare node n with all nodes in the group
			while (equal && comp < shareSisterNodes.size()) {
				if (n.compareTo(shareSisterNodes.get(comp)) != 0) {
					equal = false;
				}
				comp++;
			}

			// if the node shares the same position as all nodes in the group
			// append the node to the group
			// otherwise, save the current group, set the current group to
			// contain only the node
			if (equal) {
				shareSisterNodes.add(n);
			} else {
				sisterGroups.add(shareSisterNodes);
				shareSisterNodes = new ArrayList<>();
				shareSisterNodes.add(n);
			}
		}
		sisterGroups.add(shareSisterNodes);
		return sisterGroups;
	}

	/**
	 * Group nodes sharing the same parent
	 *
	 * @param uniqueSets
	 *            to group by parent
	 * @return A HashMap storing the nodes in the Unique set indexed by their
	 *         parent
	 */
	private HashMap<IntermdiateAST, ArrayList<ClassNodeSkeleton>> groupByParent(
			HashMap<List<Integer>, UniqueSet> uniqueSets) {
		HashMap<IntermdiateAST, ArrayList<ClassNodeSkeleton>> sisterNodes = new HashMap<>();
		for (List<Integer> li : uniqueSets.keySet()) {
			for (ClassNodeSkeleton n : uniqueSets.get(li).setMembers) {
				if (!sisterNodes.containsKey(n.getParent())) {
					sisterNodes.put(n.getParent(), new ArrayList<>());
				}
				sisterNodes.get(n.getParent()).add(n);
			}
		}
		return sisterNodes;
	}

	private ArrayList<UniqueSet> getSets(ArrayList<ClassNodeSkeleton> cnsList) {
		ArrayList<UniqueSet> retList = new ArrayList<>();
		for (ClassNodeSkeleton cns : cnsList) {
			retList.add(cns.set);
		}
		return retList;
	}

	/**
	 * -1: g2 contains g1<br>
	 *  0: g1 and g2 share same sets <br>
	 *  1: g1 contains g2 <br>
	 *  2: otherwise <br>
	 * TODO Annotate method
	 * @param g1
	 * @param g2
	 * @return
	 */
	private int compareGroups(ArrayList<UniqueSet> g1, ArrayList<UniqueSet> g2) {

		if(g1.size()==g2.size()) {
			for (UniqueSet u : g2) {
				if (!g1.contains(u))
					return 2;
			}
			return 0;
		}else if(g1.size()>g2.size()) {
			for (UniqueSet u : g2) {
				if (!g1.contains(u))
					return 2;
			}
			return 1;
		}else {
			for (UniqueSet u : g1) {
				if (!g2.contains(u))
					return 2;
			}
			return -1;
		}
	}

	/**
	 * Gets the minimal unique sets.<br>
	 * Each entry maps the Function id's of the unique set and the nodes that
	 * are part of it.
	 * The nodes mapped are the roots of all subtrees in the unique set.
	 *
	 * @return the minimal unique sets
	 */
	private HashMap<List<Integer>, UniqueSet> getMinimalUniqueSets() {

		// for each node in each mapping
		// generate list of nodes that are shared
		/*
		 * i.e.
		 * [[3, 3], [0, 2], [2, 0], [4, 4], [1, 1], [5, 5], [6, 6]]
		 * [[2, 2], [0, 3], [3, 0], [4, 4], [1, 1], [5, 5], [6, 6]]
		 * [[0, 2], [0, 3], [2, 0], [3, 0], [4, 4], [1, 1], [5, 5], [6, 6]]
		 * A : 1[3] = 2[3] = 3[3]
		 * B: 2[2]
		 * c: 1[2] = 3[2]
		 * D: 1[4] = 2[4] = 3[4]
		 * E: 1[1] = 2[1] = 3[1]
		 * F: 1[5] = 2[5] = 3[5]
		 * G: 1[6] = 2[6] = 3[6]
		 * H: 3[3]
		 * U(1,2,3) = A,D,E,F,G
		 * U(1) =
		 * U(2) = B
		 * U(3) = H
		 * U(1,2) =
		 * U(1,3) = C
		 * U(2,3) =
		 * where A, B,C, etc. are pointers to a single node
		 * then
		 * remove empty lists
		 * possibly: remove child elements from lists
		 */

		// Convert mappings list to adjacency list
		MultiMapping m = new MultiMapping(functions.size());
		int a = 0;
		int b = 1;
		for (List<int[]> mapping : functionMapping) {
			for (int[] map : mapping) {
				if (map[0] == 0) {
					m.addMapping(b, map[1]);
				} else if (map[1] == 0) {
					m.addMapping(a, map[0]);
				} else {
					m.addMapping(a, b, map[0], map[1]);
				}
			}
			b++;
			if (b == functions.size()) {
				a++;
				b = a + 1;
			}
		}

		//
		HashMap<List<Integer>, UniqueSet> retMap = new HashMap<>();
		// Gen tree
		m.buildMappingSkeliton();
		// Get only root nodes of unique sets
		for (Entry<List<Integer>, ArrayList<Mapping>> e : m.getUniqueSets().entrySet()) {

			// generate list of functions in group
			ArrayList<ClassNodeSkeleton> nodes = new ArrayList<>();
			for (Mapping mtmp : e.getValue()) {
				nodes.add(mtmp.getNode());
			}

			// select only root node of subtrees
			ArrayList<ClassNodeSkeleton> minimalMapping = new ArrayList<>();
			for (ClassNodeSkeleton c : nodes) {
				if (!nodes.contains(c.getParent())) {
					minimalMapping.add(c.getMapping().getNode());
				}
			}

			retMap.put(e.getKey(),
					new UniqueSet(e.getKey(), minimalMapping.toArray(new ClassNodeSkeleton[minimalMapping.size()])));
		}
		return retMap;
	}

	/**
	 *TODO annotate class
	 * @author Rikkey Paal
	 */
	class MultiMapping {
		ASTNode<?> c;
		ArrayList<Mapping> mappingList = new ArrayList<>();
		/**
		 * mappings.get(x).get(y) returns mapping for node of function x post
		 * order position y
		 */
		ArrayList<HashMap<Integer, Mapping>> mappings = new ArrayList<>();
		Mapping root;
		/**
		 * Initializes the MergeGroup.MultiMaping class
		 * @param size number of functions that will be in the
		 */
		public MultiMapping(int size) {
			for (int i = 0; i < size; i++) {
				mappings.add(new HashMap<>());
			}
		}

		/**
		 *
		 * TODO Annotate method
		 * Note must be added in order of functions.
		 * i.e.
		 * mappings (f1,a),(f2,b),(f3,c),(fn,x)
		 * 	must be added in order
		 * (f1,a),(f2,b)
		 * (f2,b),(f3,c)
		 * (f3,c),(f4,d)
		 * ...
		 * (fn-1,x-1),(fn,x)
		 * @param fid1
		 * @param fid2
		 * @param npos1
		 * @param npos2
		 */
		public void addMapping(int fid1, int fid2, int npos1, int npos2) {

			Mapping ref1 = null, ref2 = null;
			// try to get the mapping associated with the first reference
			try {
				ref1 = mappings.get(fid1).get(npos1);
			} catch (IndexOutOfBoundsException e) {

			}

			try {
				ref2 = mappings.get(fid2).get(npos2);
			} catch (IndexOutOfBoundsException e) {

			}
			if (ref1 == null && ref2 == null) {
				// create both
				ref1 = new Mapping(fid1, fid2, npos1, npos2);
				mappings.get(fid1).put(npos1, ref1);
				mappingList.add(ref1);
				mappings.get(fid2).put(npos2, ref1);
			} else if (ref1 == null) {
				// create ref1
				ref2.addMapping(fid1, npos1);
				mappings.get(fid1).put(npos1, ref2);
			} else if (ref2 == null) {
				// create ref2
				ref1.addMapping(fid2, npos2);
				mappings.get(fid2).put(npos2, ref1);
			} else {
				if (ref1 != ref2) {
					Mapping small, large;
					if (ref1.getMappings().size() > ref2.getMappings().size()) {
						large = ref1;
						small = ref2;
					} else {
						large = ref2;
						small = ref1;
					}
					// copy all mappings from smaller set into larger set
					for (Entry<Integer, Integer> e : small.getMappings().entrySet()) {
						large.addMapping(e.getKey(), e.getValue());
						mappings.get(e.getKey()).put(e.getValue(), large);
					}
					mappingList.remove(small);
				}
			}
		}

		/**
		 * Adds the specified mapping if it does not exist
		 *
		 * @param fid
		 *            id of the function
		 * @param npos
		 *            position of the node in the function
		 */
		public void addMapping(int fid, int npos){
			Mapping ref = null;
			try {
				ref = mappings.get(fid).get(npos);
			} catch (IndexOutOfBoundsException e) {
			}
			if (ref == null) {
				ref = new Mapping(fid,npos);
				mappings.get(fid).put(npos, ref);
				mappingList.add(ref);
			}
		}

		/**
		 * TODO remove child elements
		 *
		 * @return a collection of unique nodes
		 */
		public HashMap<List<Integer>, ArrayList<Mapping>> getUniqueSets() {
			HashMap<List<Integer>, ArrayList<Mapping>> sets = new HashMap<>();
			for(Mapping m: mappingList) {
				// get sets funcions
				List<Integer> funcIds = m.getSet();
				// insert to sets
				if(!sets.containsKey(funcIds)) {
					sets.put(funcIds, new ArrayList<>());
				}
				sets.get(funcIds).add(m);
			}

			return sets;
		}

		/**
		 * Fills all mappings with a {@link ClassNodeSkeleton} representing the
		 * node
		 * This is used as the start of the AST of the merged function
		 */
		public void buildMappingSkeliton() {
			// mapping of <fID1, <nodePos, Mapping>>
			HashMap<Integer, HashMap<Integer, Mapping>> mappingMap = new HashMap<>();
			ArrayList<Mapping> mappings = new ArrayList<>();
			// generate skelitonNodes storing them by thier first position in
			// the mapping
			for (Mapping m : mappingList) {
				// Create the classNodeSkeleton
				m.buildNode();
				// for each fID fill mappingMap with map of fID, <nodePos,
				// Mapping between two>
				for(Entry<Integer, Integer> mapping: m.getMappings().entrySet()){
					if (!mappingMap.containsKey(mapping.getKey())) {
						mappingMap.put(mapping.getKey(), new HashMap<>());
					}
					mappingMap.get(mapping.getKey()).put(mapping.getValue(), m);
				}
				mappings.add(m);
			}
			root = null;
			// Update nodes with their parentsw
			for (Mapping m : mappings) {
				int[] parentPos = m.getFirstParentPos();
				if(parentPos[1] == 0){
					root = m;
				}else{
					m.setSkelitonParent(mappingMap.get(parentPos[0]).get(parentPos[1]).getNode());
				}
			}

		}
	}

	/**
	 * A class Used to store all references to an identical node in the
	 * tree.
	 *
	 * @author Rikkey Paal
	 */

	class Mapping {

		/**
		 * <a,b> refers to node b in function a
		 */
		private final HashMap<Integer, Integer> mappings = new HashMap<Integer, Integer>();

		/**
		 * Used in {@link MergeGroup#getMinimalUniqueSets()}
		 */
		private ClassNodeSkeleton node;

		private ArrayList<int[]> parentPos;

		/**
		 * Initializes the mapping class
		 *
		 * @param fId
		 *            id of the function to map
		 * @param fPos
		 *            position of node in function
		 */
		public Mapping(int fid, int fpos) {
			addMapping(fid, fpos);
		}



		/**
		 * @return
		 */
		public HashMap<Integer, Integer> getMappings() {
			return mappings;
		}


		/**
		 * @return
		 */
		public List<Integer> getSet() {
			return new ArrayList<>(mappings.keySet());
		}

		/**
		 * Initializes the mapping class
		 *
		 * @param fId1
		 *            id of the first function to map
		 * @param fId2
		 *            id of the second function to map
		 * @param fPos1
		 *            position of node in the first function
		 * @param fPos2
		 *            position of node in the second function
		 */
		public Mapping(int fid1, int fid2, int fpos1, int fpos2) {
			addMapping(fid1, fpos1);
			addMapping(fid2, fpos2);
		}

		/**
		 * Adds a new reference to the node.
		 *
		 * @param fId
		 *            id of the function to map
		 * @param fPos
		 *            position of node in function
		 */
		public void addMapping(int fid, int fpos) {
			mappings.put(fid, fpos);
		}

		/**
		 * Generates a {@link ClassNodeSkeleton} to be stored in this class
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public void buildNode() {
			int pos = Collections.min(mappings.keySet());
			ASTNode<?> n = functions.get(pos);
			ASTNode<?> mappingNode = n.getPostOrderDecendant(mappings.get(pos));
			if (mappingNode.getChildren().size() > 0) {
				node = new ClassNodeSkeleton(plugin.copyNode(mappingNode, false), this, MergeGroup.this,
						plugin.copyNode(mappingNode.getChildrenAsASTNode().get(0), true));
			} else {
				node = new ClassNodeSkeleton(plugin.copyNode(mappingNode, false), this, MergeGroup.this, null);
			}
		}

		/**
		 * Gets a list of positions of parent nodes.
		 * A list is required as there may be examples of two nodes matching which are not in the same containing structure.
		 *
		 * This may be used in an extension to allow multiple parents
		 *
		 * @return a list of positions of parent nodes
		 */
		public ArrayList<int[]> getAllParentPos() {
			if (parentPos == null) {
				parentPos = new ArrayList<>();
				// for each entry in the mapping add the position of the parent
				for (Entry<Integer, Integer> e : mappings.entrySet()) {
					int[] parentI = new int[2];
					parentI[0] = e.getKey();
					parentI[1] = functions.get(e.getKey()).getPostOrderDecendant(e.getValue()).getParent()
							.getPostOrderPos();
					parentPos.add(parentI);
				}
			}
			return parentPos;
		}

		/**
		 * Gets the position of the parent node.
		 * @return a list of positions of parent nodes
		 */
		public int[] getFirstParentPos() {
			if (parentPos == null) {
				parentPos = new ArrayList<>();
				int[] parentI = new int[2];
				int fIDPos = Collections.min(mappings.keySet());
				Integer pos2 = mappings.get(fIDPos);
				parentI[0] = fIDPos;
				parentI[1] = functions.get(fIDPos).getPostOrderDecendant(pos2).getParent()
						.getPostOrderPos();
				parentPos.add(parentI);
			}
			return parentPos.get(0);
		}

		/**
		 * Sets the parent of this node.
		 * The parent is only set if it is null.
		 * Then returns the comparison between this parent and the parent
		 *
		 * @param parent for the node
		 * @return false if there is already a parent that is not the one provided, true otherwise
		 */
		public boolean setSkelitonParent(ClassNodeSkeleton parent) {
			if (node.parent == null) {
				node.setParent(parent);
				return true;
			} else
				return node.parent.equals(parent);
		}

		public ClassNodeSkeleton getNode() {
			return node;
		}

		@Override
		public String toString() {
			String str = "(";
			for (Entry<Integer, Integer> entry : mappings.entrySet()) {
				str += "[" + entry.getKey() + "," + entry.getValue() + "],";
			}
			return str.substring(0, str.length() - 1) + ")";
		}

	}
}