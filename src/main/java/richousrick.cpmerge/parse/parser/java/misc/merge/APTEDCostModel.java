/**
 *
 */
package richousrick.cpmerge.parse.parser.java.misc.merge;

import costmodel.CostModel;
import node.Node;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Rikkey Paal
 *
 */
public class APTEDCostModel implements CostModel<ParserRuleContext> {

	/*
	 * (non-Javadoc)
	 * @see costmodel.CostModel#del(node.Node)
	 */
	@Override
	public float del(Node<ParserRuleContext> arg0) {
		return 1.0f;
	}

	/*
	 * (non-Javadoc)
	 * @see costmodel.CostModel#ins(node.Node)
	 */
	@Override
	public float ins(Node<ParserRuleContext> arg0) {
		return 1.0f;
	}

	/*
	 * (non-Javadoc)
	 * @see costmodel.CostModel#ren(node.Node, node.Node)
	 */
	@Override
	public float ren(Node<ParserRuleContext> arg0, Node<ParserRuleContext> arg1) {
		// TODO : check correct terms
		ClassNode cn0 = (ClassNode) arg0;
		ClassNode cn1 = (ClassNode) arg1;

		// if class or method header match
		if (cn0.getType() == cn1.getType() && cn0.getType() < 2)
			return 0.0f;
		else {
			// if nodes contain the same data and are contained in the same
			// structure return 0 otherwise return 2
			if (cn0.getIdentifier().equals(cn1.getIdentifier()))
				return sameEnclosingStructure(cn0, cn1, true) ? 0.0f : 2.0f;
			else
				return 2.0f;
		}

	}


	/**
	 * This checks that the two specified nodes are contained in the same structure.
	 * Returns true if the identifiers of the ancesetors of the nodes are identical.
	 * @param cn0 first {@link ClassNode} to check
	 * @param cn1 second {@link ClassNode} to check
	 * @return true if the two {@link ClassNode}'s are contained in the same structure
	 */
	private boolean sameEnclosingStructure(ClassNode cn0, ClassNode cn1, boolean recursive){
		if(cn0.getParent().getType() == cn0.getParent().getType() && cn0.getParent().getType() < 2)
			return true;
		else{
			if(recursive)
				return compareNodes(cn0.getParent(), cn1.getParent()) && sameEnclosingStructure(cn0.getParent(), cn1.getParent(), recursive);
			else
				return compareNodes(cn0.getParent(), cn1.getParent());
		}
	}

	private boolean compareNodes(ClassNode cn0, ClassNode cn1){
		return cn0.getIdentifier().equals(cn1.getIdentifier());
	}

}
