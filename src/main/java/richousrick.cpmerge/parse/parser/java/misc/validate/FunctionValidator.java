package richousrick.cpmerge.parse.parser.java.misc.validate;

import org.antlr.v4.runtime.ParserRuleContext;
import richousrick.cpmerge.dif.ASTNode;
import richousrick.cpmerge.merge.FunctionMappings;
import richousrick.cpmerge.parse.parser.java.comp.JavaParser.MethodDeclarationContext;
import richousrick.cpmerge.parse.parser.java.visitors.FunctionSymbolTableGen;
import richousrick.cpmerge.ref.Helper;

import java.util.ArrayList;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class FunctionValidator {
	ArrayList<FunctionSymbols> functionSymbols;
	ArrayList<? extends ASTNode<ParserRuleContext>> functions;
	ArrayList<FunctionSymbols> groupSymbols;

	/**
	 * Initializes the FunctionValidator class
	 * TODO Annotate constructor
	 */
	public FunctionValidator(ArrayList<? extends ASTNode<ParserRuleContext>> functions) {
		this.functions = functions;
		functionSymbols = new ArrayList<>();
		groupSymbols = new ArrayList<>();
		for (ASTNode<ParserRuleContext> func : functions) {
			FunctionSymbolTableGen stGen = new FunctionSymbolTableGen();
			if (func.getNodeData() instanceof MethodDeclarationContext) {
				functionSymbols.add(stGen.getSymbolTable((MethodDeclarationContext) func.getNodeData()));
			} else {
				functionSymbols.add(new FunctionSymbols());
			}
		}
	}

	public ArrayList<ArrayList<Integer>> validateMergeGroup(ArrayList<ArrayList<Integer>> groups,
			FunctionMappings<ParserRuleContext> mappings) {
		boolean changed = false;
		groupSymbols = new ArrayList<>();
		// functions that are invalid for merging
		ArrayList<int[]> funcsToRemove = new ArrayList<>();
		// functions that are not part of the current mergegroup
		ArrayList<ArrayList<Integer>> funcsToMove = new ArrayList<>();

		for (int groupNo = 0; groupNo < groups.size(); groupNo++) {
			ArrayList<Integer> funcsToMoveInGroup = new ArrayList<>();
			FunctionSymbols concurrentSymbols = new FunctionSymbols();
			for (int fID : groups.get(groupNo)) {
				if (functionSymbols.get(fID).isValid()) {
					FunctionSymbols iterativeFuncSym = new FunctionSymbols(concurrentSymbols);
					if (iterativeFuncSym.combine(functionSymbols.get(fID))) {
						// function belongs in merge group
						concurrentSymbols = iterativeFuncSym;
					} else {
						// function does not belong in merge group
						funcsToMoveInGroup.add(fID);
						changed = true;
					}
				} else {
					// function is not valid for merging
					funcsToRemove.add(new int[] { groupNo, fID });
					changed = true;
				}

			}

			funcsToMove.add(funcsToMoveInGroup);
			groupSymbols.add(concurrentSymbols);
		}

		// If changed update
		if (changed) {
			// move funcs to new group
			for (int i = 0; i < funcsToMove.size(); i++) {
				if (!funcsToMove.get(i).isEmpty()) {
					groups.add(funcsToMove.get(i));
					for (int fID : funcsToMove.get(i)) {
						mappings.removeMappings(fID, funcsToMove.get(i));
						groups.get(i).remove(new Integer(fID));
					}
				}
			}

			// remove invalid funcs
			for (int[] funcToRemove : funcsToRemove) {
				mappings.removeMappings(funcToRemove[1], new ArrayList<>());
				groups.get(funcToRemove[0]).remove(new Integer(funcToRemove[1]));
			}

			// remove single sized groups
			for (int i = groups.size() - 1; i >= 0; i--) {
				if (groups.get(i).size() == 1) {
					mappings.removeMappings(groups.get(i).get(0), new ArrayList<>());
				}
				if (groups.get(i).size() <= 1) {
					groups.remove(i);
				}
			}

			return validateMergeGroup(groups, mappings);
		} else
			return groups;
	}

	/**
	 * TODO Annotate method
	 *
	 * @param id
	 * @return
	 */
	public FunctionSymbols getFunctionSymbols(int id) {
		if (id >= groupSymbols.size()) {
			Helper.exitProgram("");
		}
		return groupSymbols.get(id);
	}
}
