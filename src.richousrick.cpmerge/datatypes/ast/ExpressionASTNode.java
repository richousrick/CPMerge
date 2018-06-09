package datatypes.ast;

import java.util.ArrayList;

import datatypes.symboltable.SymbolTableEntry;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public abstract class ExpressionASTNode<D> extends ASTNode<D>{

	protected ArrayList<Integer> variables;
	
	/**
	 * Initializes the ASTExpressionNode class
	 * TODO Annotate constructor
	 * @param nodeData
	 */
	protected ExpressionASTNode(D nodeData, ArrayList<Integer> variables, ASTNode<D> parent) {
		super(nodeData, parent);
		this.variables = variables;
	}

	
	/* (non-Javadoc)
	 * @see dif.ASTNode#getType()
	 */
	@Override
	public byte getType() {
		return 3;
	}
	
	public ArrayList<Integer> getVariableIds(){
		return variables;
	}
	
	/**
	 * Check the variables in this expression match those in a different expression
	 * @param variables, the {@link SymbolTableEntry}'s associated with the 
	 * @return
	 */
	protected abstract boolean compareVariables(ArrayList<SymbolTableEntry> variables);
}
