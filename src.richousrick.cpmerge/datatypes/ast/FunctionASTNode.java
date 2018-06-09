package datatypes.ast;

import java.util.ArrayList;
import java.util.HashMap;

import datatypes.symboltable.LocalSymbolTable;
import parse.parser.java.visitors.SymbolTableGenerator;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public abstract class FunctionASTNode<D> extends ASTNode<D>{

	
	
	protected ArrayList<Integer> parameterIDs;
	protected ArrayList<Object> functionData;
	
	/**
	 * 
	 * Initializes the FunctionASTNode class
	 * @param nodeData data stored in the ASTNode
	 * @param parent of this node
	 * @param parameterIDs, Ids in the {@link LocalSymbolTable} of the parameters
	 * @param functionData, extra data to use when comparing the functions, e.g. visibility, return type
	 */
	protected FunctionASTNode(D nodeData, ASTNode<D> parent, ArrayList<Integer> parameterIDs, ArrayList<Object> functionData) {
		super(nodeData, parent);
		this.parameterIDs = parameterIDs;
	}
	
	private FunctionASTNode(D nodeData, ASTNode<D> parent){
		this(nodeData, parent, new ArrayList<>(), new ArrayList<>());
	}
	
	/* (non-Javadoc)
	 * @see ast.ASTNode#getType()
	 */
	@Override
	public final byte getType() {
		return 2;
	}
	
	/**
	 * Get the descendant with the specified post order position from this node
	 * @param postOrderPos, position of child
	 * @return the descendant with the specified post order position from this node
	 */
	public ASTNode<D> getPostOrderDecendant(int postOrderPos){
		return getPostOrderDecendant(postOrderPos, this);
	}
	
	ASTNode<D> getPostOrderDecendant(int postOrderPos, ASTNode<D> root){
		ArrayList<? extends ASTNode<D>> children = root.getAllChildren();
		if(children.isEmpty()){
			return postOrderPos == 1 ? root : null;
		}
		
		ASTNode<D> rootCandidate = children.remove(0);
		int curCandidateStartPos = 1;
		
		while(rootCandidate!= null){
			int candSize = rootCandidate.getSize()-1;
			if(curCandidateStartPos + candSize >= postOrderPos){
				// if node is is current candidate
				return getPostOrderDecendant(postOrderPos-curCandidateStartPos+1, rootCandidate);
			} else{
				if(children.isEmpty()){
					return root;
				}else{
					rootCandidate = children.remove(0);
					curCandidateStartPos += candSize+1;
				}
			}
		}
		return null;
	}
	
	/**
	 * Used to check the functions 
	 * @param node
	 * @return
	 */
	public boolean compareCharactersitics(FunctionASTNode<D> func){
		if(parameterIDs.size()!= func.parameterIDs.size() || functionData.size() != func.functionData.size()){
			return false;
		}
		return compareFunctionData(func) && compareParameters(func);
		
	}

	
	protected boolean compareParameters(FunctionASTNode<D> func){
		return true;
	}
	
	protected boolean compareFunctionData(FunctionASTNode<D> func){
		return true;
	}
	
	public ArrayList<Integer> getParameterIDs() {
		return parameterIDs;
	}

	public ArrayList<Object> getFunctionData() {
		return functionData;
	}
	
	public void addChild(ExpressionASTNode<D> child){
		super.addChild(child);
	}
	
	
	
}
