package datatypes.ast;

import java.util.ArrayList;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class ASTNodeUtils {

	public static ArrayList<String> getPathList(ASTNode<?> node){
		ArrayList<String> pathParts = new ArrayList<>();
		ASTNode<?> currentNode = node;
		while(node!=null){
			pathParts.add(0, node.getIdentifier());
			node = node.getParent();
		}
		return pathParts;
	}
	
}
