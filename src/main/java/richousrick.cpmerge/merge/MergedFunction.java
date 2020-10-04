package richousrick.cpmerge.merge;

import richousrick.cpmerge.dif.ASTNode;

import java.util.ArrayList;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public class MergedFunction<D> {

	private final ArrayList<? extends ASTNode<D>> originalFunctionRoots;
	private final ClassNodeSkeleton<D> rootNode;
	private String stringRepresentation;
	private final int ID;

	/**
	 * Initializes the RootNode class
	 * TODO Annotate constructor
	 * 
	 * @param iD
	 * @param node
	 * @param mapping
	 * @param mergeGroup
	 * @param firstChild
	 */
	public MergedFunction(ArrayList<ASTNode<D>> originalFunctionRoots, ClassNodeSkeleton<D> rootNode, int ID) {
		this.originalFunctionRoots = originalFunctionRoots;
		this.rootNode = rootNode;
		stringRepresentation = null;
		this.ID = ID;
	}

	public ArrayList<? extends ASTNode<D>> getOriginalFunctionRoots() {
		return originalFunctionRoots;
	}

	public ClassNodeSkeleton<D> getRootNode() {
		return rootNode;
	}

	/**
	 * @return the stringRepresentation
	 */
	public String getStringRepresentation() {
		return stringRepresentation;
	}

	/**
	 * @param stringRepresentation
	 *            the stringRepresentation to set
	 */
	public void setStringRepresentation(String stringRepresentation) {
		this.stringRepresentation = stringRepresentation;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String retStr = stringRepresentation;
		for (int i = 0; i < originalFunctionRoots.size(); i++) {
			retStr += "\n\t" + originalFunctionRoots.get(i).getIdentifier() + ": fID " + i;
		}
		return retStr;
	}

	public int getID() {
		return ID;
	}


}
