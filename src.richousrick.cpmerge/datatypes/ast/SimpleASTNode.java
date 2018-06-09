package datatypes.ast;

/**
 * ASTNode to be used when testing
 * @author Rikkey Paal
 */
class SimpleASTNode extends ASTNode<String>{

	/**
	 * Initializes the simpleASTNode class
	 * TODO Annotate constructor
	 * @param nodeData
	 * @param parent
	 */
	public SimpleASTNode(String nodeData, ASTNode<String> parent) {
		super(nodeData, parent);
	}

	public SimpleASTNode addChild(String data){
		SimpleASTNode child = new SimpleASTNode(data, this);
		addChild(child);
		return child;
	}

	/* (non-Javadoc)
	 * @see datatypes.ast.ASTNode#getType()
	 */
	@Override
	public byte getType() {
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see node.Node#toString()
	 */
	@Override
	public String toString() {
		String s = getNodeData();
		for(ASTNode<String> child : getAllChildren()){
			s+="\n| " + child.toString().replaceAll("\n", "\n| ");
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ASTNode<String> o) {
		if(o instanceof SimpleASTNode){
			return o.getNodeData().compareTo(getNodeData());
		}else{
			return -1;
		}
	}
}