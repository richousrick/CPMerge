package datatypes.ast;

import java.util.ArrayList;

import node.Node;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public abstract class ClassASTNode<D> extends ASTNode<D>{

	/**
	 * Initializes the ClassASTNode class
	 * @param nodeData
	 * @param parent
	 */
	protected ClassASTNode(D nodeData, ASTNode<D> parent) {
		super(nodeData, parent);
	}

	/* (non-Javadoc)
	 * @see datatypes.ast.ASTNode#getType()
	 */
	@Override
	public final byte getType() {
		return 1;
	}
	
	/**
	 * Add a class to be a child of this class
	 * @param node
	 */
	public void addChild(ClassASTNode<D> node) {
		super.addChild(node);
	}
	
	/**
	 * Add a function to be a child of this class
	 * @param node
	 */
	public void addChild(FunctionASTNode<D> node) {
		super.addChild(node);
	}

	/**
	 * @return all children that are {@link FunctionASTNode}'s
	 */
	public ArrayList<FunctionASTNode<D>> getFunctions(){
		ArrayList<FunctionASTNode<D>> functions = new ArrayList<>();
		for(Node<D> child:getChildren()){
			if(child instanceof FunctionASTNode<?>){
				functions.add((FunctionASTNode<D>) child);
			}
		}
		return functions;
	}
	
	/**
	 * @return all children that are {@link ClassASTNode}'s
	 */
	public ArrayList<ClassASTNode<D>> getClasses(){
		ArrayList<ClassASTNode<D>> classes= new ArrayList<>();
		for(Node<D> child:getChildren()){
			if(child instanceof ClassASTNode<?>){
				classes.add((ClassASTNode<D>) child);
			}
		}
		return classes;
	}

	
	
}
