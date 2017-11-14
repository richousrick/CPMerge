/**
 * 
 */
package dif;

import org.antlr.v4.runtime.ParserRuleContext;

import costmodel.CostModel;
import node.Node;
import ref.Helper;

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

		return ((ClassNode) arg0).getIdentifier().equals(((ClassNode) arg1).getIdentifier()) ? 0.0f : 1.0f;
	}

}
