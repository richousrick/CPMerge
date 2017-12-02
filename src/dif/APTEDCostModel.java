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
		// System.out.println("del: " + ((ClassNode)arg0).getIdentifier());
		return 1.0f;
	}

	/*
	 * (non-Javadoc)
	 * @see costmodel.CostModel#ins(node.Node)
	 */
	@Override
	public float ins(Node<ParserRuleContext> arg0) {
		// System.out.println("ins: " + ((ClassNode)arg0).getIdentifier());
		return 1.0f;
	}

	/*
	 * (non-Javadoc)
	 * @see costmodel.CostModel#ren(node.Node, node.Node)
	 */
	@Override
	public float ren(Node<ParserRuleContext> arg0, Node<ParserRuleContext> arg1) {
		// TODO : check correct terms
		// System.out.println("ren: "+((ClassNode)arg0).getIdentifier() +" to
		// "+((ClassNode)arg1).getIdentifier());
		ClassNode cn0 = (ClassNode) arg0;
		ClassNode cn1 = (ClassNode) arg1;

		if (cn0.getType() == 1 && cn1.getType() == 1) {
			return 0.0f;
		} else {
			return cn0.getIdentifier().equals(cn1.getIdentifier()) ? 0.0f : 1.0f;
		}
	}

}
