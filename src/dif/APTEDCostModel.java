/**
 * 
 */
package dif;

import org.antlr.v4.runtime.ParserRuleContext;

import costmodel.CostModel;
import node.Node;

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

		if (cn0.getType() == cn1.getType() && cn0.getType() != 2)
			return 0.0f;
		else
			return cn0.getIdentifier().equals(cn1.getIdentifier()) ? 0.0f : 3.0f;
	}

}
