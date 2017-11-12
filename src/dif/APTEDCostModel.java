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
public class APTEDCostModel implements CostModel<ParserRuleContext>{

	/* (non-Javadoc)
	 * @see costmodel.CostModel#del(node.Node)
	 */
	@Override
	public float del(Node<ParserRuleContext> arg0) {
		return 1.0f;
	}

	/* (non-Javadoc)
	 * @see costmodel.CostModel#ins(node.Node)
	 */
	@Override
	public float ins(Node<ParserRuleContext> arg0) {
		return 1.0f;
	}

	/* (non-Javadoc)
	 * @see costmodel.CostModel#ren(node.Node, node.Node)
	 */
	@Override
	public float ren(Node<ParserRuleContext> arg0,
			Node<ParserRuleContext> arg1) {
		//TODO : check correct terms
		return Helper.parserContextEqual(arg0.getNodeData(), arg1.getNodeData(), true, true)?0.0f:1.0f;
	}
	

}
