package merge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import dif.ClassNode;
import merge.MergeGroup.MultiMapping.Mapping;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public class MergeGroup {
	private ClassNode sharedTree;
	// private ArrayList<ArrayList<ClassNode>> mergeCandidates;
	private final ArrayList<List<int[]>> functionMapping;
	private final ArrayList<ClassNode> functions;

	/**
	 * Initializes the MergeGroup class
	 * TODO Annotate constructor
	 * @param sharedTree
	 * @param mergeCandidates
	 */
	public MergeGroup(ArrayList<ClassNode> functions, ArrayList<List<int[]>> functionMapping) {
		this.functionMapping  =functionMapping;
		this.functions = functions;
	}

	public String getMethodHeaders() {
		String s = "{";
		for (ClassNode c : functions) {
			s += c.getIdentifier() + ",";
		}
		s = s.substring(0, s.length() - 1);
		s += "}\n";
		// if (functions.size() == 2) {
		// PrettyPrinter p = new PrettyPrinter(true);
		// ArrayList<ClassNode>[] tmp = getMergeCandidates(functions.get(0),
		// functions.get(1));
		// for (ArrayList<ClassNode> ttmp : tmp) {
		// for (ClassNode ctmp : ttmp) {
		// s += ctmp.toString() + "\n";
		// }
		// }
		// }
		// for (List<int[]> i : functionMapping) {
		// s += Arrays.deepToString(i.toArray());
		// }
		return s.replaceAll("\\]\\]\\[\\[", "]]\n\t[[");
	}

	public ArrayList<ClassNode> getFunctions() {
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


	private void getDifferences(ArrayList<Integer> functionPositons) {
		ArrayList<ClassNode> differences = new ArrayList<>();

	}

	private List<int[]> getRelationship(int f1Pos, int f2Pos){
		if(f1Pos==f2Pos)
			throw new IllegalArgumentException("Cannot reference mapping to self");
		int pos = 0;
		int len = functions.size();
		if(f1Pos>f2Pos) {
			for(int i = 0 ; i<f2Pos; i++) {
				pos+=len;
				len--;
			}
		}else {
			for(int i = 0 ; i<f1Pos; i++) {
				pos+=len;
				len--;
			}
		}
		return functionMapping.get(pos);
	}

	public String printShardeCode() {
		String s = "";
		HashMap<int[], ClassNode[]> code = getSharedCode();
		for (Entry<int[], ClassNode[]> entry : code.entrySet()) {
			s += "\tU" + entry.getKey().toString();
			for (ClassNode c : entry.getValue()) {
				s += c.print("\t\t", true, false);
			}
		}
		return s;
	}

	private HashMap<int[], ClassNode[]> getSharedCode() {

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
		MultiMapping m = new MultiMapping(functions.size());

		for (List<int[]> mapping : functionMapping) {
			int a = 0;
			int b = 1;
			for (int[] map : mapping) {
				if (map[0] != 0 && map[1] != 0) {
					m.addMapping(a, b, map[0], map[1]);
				}
				b++;
				if (b == functionMapping.size()) {
					a++;
					b = a + 1;
				}
			}
		}
		HashMap<int[], ClassNode[]> retMap = new HashMap<>();
		for (Entry<String, ArrayList<Mapping>> e : m.getUniqueSets().entrySet()) {
			String[] setKeyStrings = e.getKey().split(",");
			int[] setKeys = new int[setKeyStrings.length];
			for (int i = 0; i < setKeys.length; i++) {
				setKeys[i] = Integer.parseInt(setKeyStrings[i]);
			}
			ArrayList<ClassNode> classes = new ArrayList<>(e.getValue().size());
			Iterator<Entry<Integer, Integer>> entrySets = e.getValue().get(0).getMappings().entrySet().iterator();
			for (int i = 0; i < e.getValue().size(); i++) {
				Entry<Integer, Integer> tmpEntry = entrySets.next();
				classes.add(functions.get(tmpEntry.getKey()).getPostOrderList()[tmpEntry.getValue()]);
			}
			ArrayList<ClassNode> minimalClasses = new ArrayList<>();
			for (ClassNode c : classes) {
				if (!classes.contains(c.getParent())) {
					minimalClasses.add(c);
				}
			}

			retMap.put(setKeys, (ClassNode[]) minimalClasses.toArray());
		}
		return retMap;
	}

	/**
	 *TODO annotate class
	 * @author Rikkey Paal
	 */
	class MultiMapping {
		ClassNode c;
		ArrayList<Mapping> mappingList = new ArrayList<>();
		ArrayList<ArrayList<Mapping>> mappings = new ArrayList<>();

		/**
		 * Initializes the MergeGroup.MultiMaping class
		 * @param size number of functions that will be in the
		 */
		public MultiMapping(int size) {
			for (int i = 0; i < size; i++) {
				mappings.add(new ArrayList<>(Collections.nCopies(size, null)));
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
			Mapping ref = null;
			try {
				ref = mappings.get(fid1).get(npos1);
			} catch (IndexOutOfBoundsException e) {

			}
			if (ref != null) {
				ref.addMapping(fid2, npos2);

			} else {
				ref = new Mapping(fid1, fid2, npos1, npos2);
				mappings.get(fid1).add(npos1, ref);
			}
			mappings.get(fid2).add(npos2, ref);
			mappingList.add(ref);
		}

		/**
		 * TODO remove child elements
		 *
		 * @return a collection of unique nodes
		 */
		public HashMap<String, ArrayList<Mapping>> getUniqueSets(){
			HashMap<String, ArrayList<Mapping>> sets = new HashMap<>();
			ArrayList<Mapping> validMappings = new ArrayList<>();
			for(Mapping m: mappingList) {
				// get sets funcions
				String funcIds = m.getSet();
				// insert to sets
				if(!sets.containsKey(funcIds)) {
					sets.put(funcIds, new ArrayList<>());
				}
				sets.get(funcIds).add(m);
			}

			return sets;
		}


		/**
		 * A class Used to store all references to an identical node in the
		 * tree.
		 *
		 * @author Rikkey Paal
		 */
		class Mapping {

			private final HashMap<Integer, Integer> mappings = new HashMap();

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

			@Override
			public String toString() {
				String retString = "";
				for (Entry<Integer, Integer> e : mappings.entrySet()) {
					retString += "(" + e.getKey() + "," + e.getValue() + "),";
				}
				return retString.substring(0, retString.length() - 1);
			}

			public HashMap<Integer, Integer> getMappings() {
				return mappings;
			}


			public String getSet() {
				String sets = "";
				for(Integer key:mappings.keySet()) {
					sets+=key+",";
				}
				return sets.substring(0,sets.length()-1);
			}

		}

		class UniqueSets {
			ArrayList<Mapping> mappings = new ArrayList<>();

		}
	}


	private ArrayList<ClassNode>[] getMergeCandidates(ClassNode function1, ClassNode function2) {
		HashSet<Integer> set1 = new HashSet<>();
		HashSet<Integer> set2 = new HashSet<>();
		ArrayList<ClassNode>[] mergeCandidates = new ArrayList[3];
		for (int[] map : functionMapping.get(0)) {
			if (map[0] == 0) {
				set2.add(map[1]);
			} else if (map[1] == 0) {
				set1.add(map[0]);
			}
		}
		ClassNode f1c = new ClassNode(function1);
		mergeCandidates[0] = new ArrayList<>();
		mergeCandidates[0].add(f1c);
		mergeCandidates[1] = f1c.getMinimalNodesFromPostOrder(set1, true);
		mergeCandidates[2] = function2.getMinimalNodesFromPostOrder(set2, false);

		return mergeCandidates;
	}

}
