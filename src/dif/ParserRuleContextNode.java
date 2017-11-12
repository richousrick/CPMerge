/**
 * 
 */
package dif;

import java.util.Vector;

import org.antlr.v4.runtime.ParserRuleContext;

import node.Node;

/**
 * @author Rikkey Paal
 *
 */
public class ParserRuleContextNode extends Node<ParserRuleContext>{

	ParserRuleContext root;
	
	/**
	 * @param nodeData
	 */
	public ParserRuleContextNode(ParserRuleContext nodeData) {
		super(null);
		root=nodeData;
	}

	@Override
	public void addChild(Node c) {
		root.addChild((ParserRuleContext)c.getNodeData());
	}

	@Override
	public Vector<Node<ParserRuleContext>> getChildren() {
		System.err.println("Method, ParserRuleContextNode.getChildren() should not be called");
		return super.getChildren();
	}

	@Override
	public int getNodeCount() {
		return root.children.size();
	}

	@Override
	public ParserRuleContext getNodeData() {
		return root;
	}

	@Override
	public void setNodeData(ParserRuleContext nodeData) {
		// TODO Auto-generated method stub
		super.setNodeData(nodeData);
	}

	@Override
	public String toString() {
		return root.toString();
	}
	
	

}
