package merge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import ref.Helper;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class AdjacencyList<D, R> {

	protected final HashMap<D, Node<D, R>> nodes;

	/**
	 * Initializes the AdjacencyList class
	 * TODO Annotate constructor
	 */
	public AdjacencyList() {
		nodes = new HashMap();
	}

	public void addNode(D data) {
		nodes.put(data, new Node<D, R>(data));
	}

	public void addRelation(D element1, D element2, R cost12, R cost21) {
		Node<D, R> node1 = nodes.get(element1);
		Node<D, R> node2 = nodes.get(element2);
		node1.addRelation(node2, cost12);
		node2.addRelation(node1, cost21);
	}

	public void removeMappings(Node<D, R> node) {
		for (Entry<Node<D, R>, R> entry : node.relationships.entrySet()) {
			entry.getKey().relationships.remove(node);
		}
	}

	class Node<D, R> {
		final D data;
		final HashMap<Node<D, R>, R> relationships;

		Node(D data) {
			this.data = data;
			relationships = new HashMap<>();
		}

		private void addRelation(Node<D, R> node, R cost) {
			relationships.put(node, cost);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		HashMap<D, Integer> posiitons = new HashMap<>();
		int pos = 0;
		for(Node<D, R> node: nodes.values()) {
			posiitons.put(node.data, pos);
			pos ++;
		}

		String s = "";
		for(Node<D, R> entry : nodes.values()) {
			for (Entry<Node<D, R>, R> sibling : entry.relationships.entrySet()) {

				s += "[" + posiitons.get(entry.data) + "," + posiitons.get(sibling.getKey().data) + " = "
						+ sibling.getValue() + "],";
			}
		}
		if(s.length()>0) {
			s = s.substring(0, s.length()-1);
		}
		return s;
	}
}

class NodeCostMappings<D> extends AdjacencyList<IntermdiateAST<D>, Integer> {

	public ArrayList<IntermdiateAST<D>> orderCosts() {
		ArrayList<Node<IntermdiateAST<D>, Integer>> nodesToProcess = new ArrayList<>(nodes.values());
		ArrayList<IntermdiateAST<D>> orderedNodes = new ArrayList<>();
		while (!nodesToProcess.isEmpty()) {
			Node<IntermdiateAST<D>, Integer> matchedNode = null;
			// find a node that does not appear after any other unordered nodes
			NodeSearch: for (Node<IntermdiateAST<D>, Integer> node : nodesToProcess) {
				for (Integer mapping : node.relationships.values()) {
					if (mapping > 0) {
						continue NodeSearch;
					}
				}
				matchedNode = node;
				break;
			}
			if (matchedNode == null) {
				Helper.exitProgram("Could not order function elements");
			}

			orderedNodes.add(matchedNode.data);
			removeMappings(matchedNode);
			nodesToProcess.remove(matchedNode);

		}

		return orderedNodes;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		HashMap<IntermdiateAST<D>, Integer> posiitons = new HashMap<>();
		int pos = 0;
		for (Node<IntermdiateAST<D>, Integer> node : nodes.values()) {
			posiitons.put(node.data, pos);
			pos++;
		}

		String s = "";
		for (Node<IntermdiateAST<D>, Integer> entry : nodes.values()) {
			for (Entry<Node<IntermdiateAST<D>, Integer>, Integer> sibling : entry.relationships.entrySet()) {
				char c = ' ';
				if (sibling.getValue() < 0) {
					c = '<';
				} else if (sibling.getValue() == 0) {
					c = '=';
				} else {
					c = '>';
				}
				s += "[" + posiitons.get(entry.data) + c + posiitons.get(sibling.getKey().data) + "],";
			}
		}
		if (s.length() > 0) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}
}
