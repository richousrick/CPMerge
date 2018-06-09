package datatypes.ast;

import java.util.ArrayList;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public abstract class FileNode<D> extends ASTNode<D>{

	/**
	 * Initializes the FileNode class
	 * TODO Annotate constructor
	 * @param nodeData
	 * @param parent
	 */
	public FileNode(D nodeData) {
		super(nodeData, null);
	}

	/* (non-Javadoc)
	 * @see datatypes.ast.ASTNode#getType()
	 */
	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * Add the class to be a child of the file
	 * @param node
	 */
	public void addChild(ClassASTNode<D> node){
		super.addChild(node);
	}
	
	/**
	 * @return all classes that are children of this file
	 */
	public ArrayList<ClassASTNode<D>> getRootClasses(){
		ArrayList<ClassASTNode<D>> classes = new ArrayList<>();
		for(ASTNode<D> child : getAllChildren()){
			classes.add((ClassASTNode<D>) child);
		}
		return classes;
	}

	/**
	 * Get all classes in the AST
	 * @return all classes in the AST
	 */
	public ArrayList<ClassASTNode<D>> getAllClasses(){
		ArrayList<ClassASTNode<D>> processedClasses = new ArrayList<>();
		ArrayList<ClassASTNode<D>> unprocessedClasses = getRootClasses();
		while(!unprocessedClasses.isEmpty()){
			ClassASTNode<D> currentClass = unprocessedClasses.remove(0);
			unprocessedClasses.addAll(currentClass.getClasses());
			processedClasses.add(currentClass);
		}
		return processedClasses;
	}
}
