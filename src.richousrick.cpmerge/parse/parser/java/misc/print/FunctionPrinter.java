package parse.parser.java.misc.print;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.antlr.v4.runtime.ParserRuleContext;

import dif.ASTNode;
import merge.ClassNodeSkeleton;
import merge.IntermdiateAST;
import merge.MergePoint;
import merge.MergedFunction;
import merge.UniqueSet;
import parse.parser.java.comp.JavaParser.BlockStatementContext;
import parse.parser.java.comp.JavaParser.LocalVariableDeclarationContext;
import parse.parser.java.comp.JavaParser.StatementContext;
import parse.parser.java.misc.validate.FunctionSymbols;
import parse.parser.java.visitors.PrettyPrinter;
import ref.Helper;

/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class FunctionPrinter {

	PrettyPrinter p;
	String defaultReturns;
	FunctionSymbols symbols;

	/**
	 * Initializes the FunctionPrinter class
	 * TODO Annotate constructor
	 */
	public FunctionPrinter() {
		p = new PrettyPrinter(false);

	}

	public String prettyPrintFunction(MergedFunction<ParserRuleContext> rootNode, FunctionSymbols symbols) {
		ClassNodeSkeleton<ParserRuleContext> root = rootNode.getRootNode();
		this.symbols = symbols;
		String head = getNewFuncHead(rootNode);
		// visit all posible heads
		defaultReturns = p.getDefaultReturns(rootNode);
		boolean returns = !defaultReturns.equals("void");

		String body = initValues().trim().replace("\n", "\n\t") + "\n";
		for(int i = 0; i<root.getChildren().size(); i++){
			IntermdiateAST<ParserRuleContext> child = root.getChild(i);
			boolean mustReturn = returns && i == root.getChildren().size()-1;
			if (child instanceof ClassNodeSkeleton) {
				body += "\t" + prettyPrintBody((ClassNodeSkeleton<ParserRuleContext>) child).replaceAll("\n", "\n\t");
			} else {
				body += "\t" + prettyPrintMergePoint((MergePoint<ParserRuleContext>) child, mustReturn).replaceAll("\n",
						"\n\t");
			}
			body += "\n";

		}

		return head + "\n\t" + body.trim() + "\n}";
	}

	/**
	 * TODO Annotate method
	 *
	 * @return
	 */
	private String initValues() {
		String inits = "";
		for (String[] init : symbols.getVariablesToInit()) {
			inits += init[1] + " " + init[0] + " = " + p.getDefaultValue(init[1]) + ";\n";
		}
		return inits;
	}

	private String getNewFuncHead(MergedFunction<ParserRuleContext> root) {
		p.setDoTrailingBrace(false);
		String headModfier = getHeadModifiers(root);
		String headIdentifier = getHeadIdentifier(root);
		String headParameters = getHeadParameters();
		return headModfier + " " + headIdentifier + " " + headParameters;
	}

	private String getHeadModifiers(MergedFunction<ParserRuleContext> r) {
		p.setDoHeadIdentifier(false);
		p.setDoHeadModifiers(true);
		p.setDoHeadParameters(false);

		return p.visit(r.getRootNode().getNode().getNodeData());
	}

	private String getHeadIdentifier(MergedFunction<ParserRuleContext> r) {
		p.setDoHeadIdentifier(true);
		p.setDoHeadModifiers(false);
		p.setDoHeadParameters(false);

		String identifier = "";

		for (ASTNode<ParserRuleContext> c : r.getOriginalFunctionRoots()) {
			identifier += p.visit(c.getNodeData()).trim() + "_";
		}

		return identifier.substring(0, identifier.length() - 1);
	}

	private String getHeadParameters() {
		ArrayList<String[]> parameters = symbols.getParameters();
		String parameterStr = "(";
		for (String[] param : parameters) {
			parameterStr += param[1] + " " + param[0] + ", ";
		}
		parameterStr += "int fID){";
		return parameterStr;
	}

	private String prettyPrintBody(ClassNodeSkeleton<ParserRuleContext> c) {
		ParserRuleContext ctx = c.getNode().getNodeData();
		if (ctx instanceof BlockStatementContext) {
			ctx = ((BlockStatementContext) ctx).statement();
		}
		if(ctx instanceof StatementContext) {

			if (((StatementContext) ctx).IF() != null)
				return prettyPrintBodyIf(c);
			else if (((StatementContext) ctx).DO() != null)
				return prettyPrintDoWhile(c);
		}
		return prettyPrintBodyDefault(c);
	}

	/**
	 * TODO Annotate method
	 *
	 * @param c
	 * @return
	 */
	private String prettyPrintDoWhile(ClassNodeSkeleton<ParserRuleContext> c) {
		String body = "do {\n\t";
		for (IntermdiateAST<ParserRuleContext> child : c.getChildren()) {
			if (child instanceof ClassNodeSkeleton) {
				body += prettyPrintBody((ClassNodeSkeleton<ParserRuleContext>) child).replaceAll("\n", "\n\t");
			} else {
				body += prettyPrintMergePoint((MergePoint<ParserRuleContext>) child).replaceAll("\n", "\n\t");
			}
			body += "\n";
		}
		body += "}\n";
		body += p.visit(c.getNode().getNodeData());
		return body;
	}

	private String prettyPrintBodyIf(ClassNodeSkeleton<ParserRuleContext> c) {
		if (c.getChildren().get(0) instanceof MergePoint) {
			boolean elseFlag = false;

			String head = p.visit(c.getNode().getNodeData());
			if(head.endsWith("else")) {
				head = head.substring(0,head.length()-4);
				elseFlag = true;
			}
			head += "{";

			// get list of cases for if and else
			HashMap<UniqueSet, ArrayList<ClassNodeSkeleton<ParserRuleContext>>> ifCases = new HashMap<>();
			HashMap<UniqueSet, ArrayList<ClassNodeSkeleton<ParserRuleContext>>> elseCases = new HashMap<>();

			for (Entry<UniqueSet, ArrayList<ClassNodeSkeleton<ParserRuleContext>>> options : ((MergePoint<ParserRuleContext>) c
					.getChildren().get(0)).getMergeOptions().entrySet()) {
				if (elseFlag) {
					if (options.getValue().get(0).toString().startsWith("then")) {
						ArrayList<ClassNodeSkeleton<ParserRuleContext>> tmp = new ArrayList<>();
						tmp.add(options.getValue().get(0));
						ifCases.put(options.getKey(), tmp);

						tmp = new ArrayList<>();
						tmp.add(options.getValue().get(1));
						elseCases.put(options.getKey(), tmp);
					} else {
						ArrayList<ClassNodeSkeleton<ParserRuleContext>> tmp = new ArrayList<>();
						tmp.add(options.getValue().get(1));
						ifCases.put(options.getKey(), tmp);

						tmp = new ArrayList<>();
						tmp.add(options.getValue().get(0));
						elseCases.put(options.getKey(), tmp);
					}
				} else {
					ArrayList<ClassNodeSkeleton<ParserRuleContext>> tmp = new ArrayList<>();
					tmp.add(options.getValue().get(0));
					ifCases.put(options.getKey(), tmp);
				}
			}


			head += "\n\t" + prettyPrintMergePoint(new MergePoint<ParserRuleContext>(ifCases, c.getMergeGroup(), c, 0))
			.replaceAll("\n", "\n\t").trim();
			head += "\n}";
			if (elseFlag) {
				head += " else {";
				head += "\n\t"
						+ prettyPrintMergePoint(new MergePoint<ParserRuleContext>(ifCases, c.getMergeGroup(), c, 0))
						.replaceAll("\n", "\n\t").trim();
				head += "\n}";
			}
			return head;
		} else {
			boolean elseFlag = false;
			String head = p.visit(c.getNode().getNodeData());
			if (head.endsWith("else")) {
				head = head.substring(0, head.length() - 4);
				elseFlag = true;
			}
			head += "{\n\t";

			if (elseFlag) {
				if (c.getChildren().get(0).toString().startsWith("then")) {
					head += p
							.visit(((ClassNodeSkeleton<ParserRuleContext>) c.getChildren().get(0)).getNode()
									.getNodeData())
							.replaceAll("\n", "\n\t");
					head = head.trim() + "\n} else {\n\t";
					head += p
							.visit(((ClassNodeSkeleton<ParserRuleContext>) c.getChildren().get(1)).getNode()
									.getNodeData())
							.replaceAll("\n", "\n\t");
				} else {
					head += p
							.visit(((ClassNodeSkeleton<ParserRuleContext>) c.getChildren().get(1)).getNode()
									.getNodeData())
							.replaceAll("\n", "\n\t");
					head = head.trim() + "\n} else {\n";
					head += p
							.visit(((ClassNodeSkeleton<ParserRuleContext>) c.getChildren().get(0)).getNode()
									.getNodeData())
							.replaceAll("\n", "\n\t");
				}
			} else {
				head += p.visit(((ClassNodeSkeleton<ParserRuleContext>) c.getChildren().get(0)).getNode().getNodeData())
						.replaceAll("\n",
								"\n\t");
			}
			return head.trim() + "\n}";
		}
	}

	private String prettyPrintBodyDefault(ClassNodeSkeleton<ParserRuleContext> c) {
		String body = "";

		if (c.getNode().getNodeData() instanceof BlockStatementContext
				&& ((BlockStatementContext) c.getNode().getNodeData()).localVariableDeclaration() != null) {
			body = p.visitLocalVariableDeclarationNoInit(
					((BlockStatementContext) c.getNode().getNodeData()).localVariableDeclaration());
		} else {
			body = p.visit(c.getNode().getNodeData());
		}
		if (c.getChildren().size() > 0) {
			body += "{\n\t";
			for (IntermdiateAST<ParserRuleContext> child : c.getChildren()) {
				if (child instanceof ClassNodeSkeleton) {
					body += prettyPrintBody((ClassNodeSkeleton<ParserRuleContext>) child).replaceAll("\n", "\n\t");
				} else {
					body += prettyPrintMergePoint((MergePoint<ParserRuleContext>) child).replaceAll("\n", "\n\t");
				}
				body += "\n";
			}
			body += "}\n";
		}
		return body.trim();
	}

	private String prettyPrintMergePoint(MergePoint<ParserRuleContext> c) {
		return prettyPrintMergePoint(c,false);
	}

	private String prettyPrintMergePoint(MergePoint<ParserRuleContext> c, boolean mustReturn) {
		int options = c.getMergeOptions().size();
		// if only 1 option use if statement
		boolean useSwitch = options != 1;

		// if there are two options that make up all possibilities then use if
		// else
		if (useSwitch && options == 2) {
			ArrayList<Integer> sets = new ArrayList<>();
			for (UniqueSet set : c.getMergeOptions().keySet()) {
				sets.addAll(set.getSetFuncIds());
			}
			List<Integer> containerSet = null;
			try {
				containerSet = c.getParent().getUniqueSet().getSetFuncIds();
			} catch (Exception e) {
				Helper.exitProgram("??");
			}
			if (sets.size() == containerSet.size() && sets.containsAll(containerSet)) {
				useSwitch = false;
			}
		}

		return useSwitch ? prettyPrintMergePointSwitch(c, mustReturn) : prettyPrintMergePointIf(c, mustReturn);

	}

	private String prettyPrintMergePointIf(MergePoint<ParserRuleContext> c, boolean mustReturn) {
		// get entries of the if and else cases
		Entry<UniqueSet, ArrayList<ClassNodeSkeleton<ParserRuleContext>>> caseIf = null;
		Entry<UniqueSet, ArrayList<ClassNodeSkeleton<ParserRuleContext>>> caseElse = null;
		Iterator<Entry<UniqueSet, ArrayList<ClassNodeSkeleton<ParserRuleContext>>>> iterator = c.getMergeOptions()
				.entrySet().iterator();
		caseIf = iterator.next();
		if (iterator.hasNext()) {
			caseElse = iterator.next();
			if (caseElse.getKey().getSetFuncIds().size() < caseIf.getKey().getSetFuncIds().size()) {
				Entry<UniqueSet, ArrayList<ClassNodeSkeleton<ParserRuleContext>>> tmp = caseElse;
				caseElse = caseIf;
				caseIf = tmp;
			}
		}

		// init variables declared in if statement
		ArrayList<ClassNodeSkeleton<ParserRuleContext>> declarationsToInit = getDeclarationsToInit(c);
		String str = ""; // initVariableDecalarations(declarationsToInit);

		// add if head
		str += "\nif(";
		for (int id : caseIf.getKey().getSetFuncIds()) {
			str += "fID == " + id + " || ";
		}

		// add if body
		str = str.substring(0, str.length() - 3) + "){";
		for (ClassNodeSkeleton<ParserRuleContext> cnc : caseIf.getValue()) {
			if (declarationsToInit.contains(cnc)) {
				str += "\n\t" + printVariableDeclaration(cnc);
			} else {
				str += "\n\t" + prettyPrintBody(cnc);
			}
		}
		str = str.trim() + "\n}";

		// add else case if
		if (caseElse != null) {
			str += " else {";
			for (ClassNodeSkeleton<ParserRuleContext> cnc : caseElse.getValue()) {
				if (declarationsToInit.contains(cnc)) {
					str += "\n\t" + printVariableDeclaration(cnc);
				} else {
					str += "\n\t" + prettyPrintBody(cnc);
				}
			}
			str = str.trim() + "\n}";
		}else if(mustReturn){
			str += " else {";
			str+="\n\treturn " + defaultReturns  +";";
		}




		return str.trim();
	}

	private String prettyPrintMergePointSwitch(MergePoint<ParserRuleContext> c, boolean mustReturn) {
		// init variables declared in if statement
		ArrayList<ClassNodeSkeleton<ParserRuleContext>> declarationsToInit = getDeclarationsToInit(c);
		String str = ""; // initVariableDecalarations(declarationsToInit);

		// add switch head
		str += "\nswitch (fID) {";

		for (Entry<UniqueSet, ArrayList<ClassNodeSkeleton<ParserRuleContext>>> option : c.getMergeOptions()
				.entrySet()) {
			// add cases
			for (int id : option.getKey().getSetFuncIds()) {
				str += "\n\tcase " + id + ":";
			}
			String body = "";
			// add body
			for (ClassNodeSkeleton<ParserRuleContext> cnc : option.getValue()) {
				if (declarationsToInit.contains(cnc)) {
					body += "\n\t\t" + printVariableDeclaration(cnc);
				} else {
					body += "\n\t\t" + prettyPrintBody(cnc).replaceAll("\n", "\n\t\t");
				}

			}
			String pattern = "[\\s.]*return.*;\\s*$";
			if (!body.matches(pattern)) {
				body += "\n\t\tbreak;";
			}
			str += body;
		}
		if(mustReturn){
			str += "\n\tdefault:";
			str += "\n\t\treturn " + defaultReturns +";";
		}

		str += "\n}";
		return str.trim();
	}


	// private String
	// initVariableDecalarations(ArrayList<ClassNodeSkeleton<ParserRuleContext>>
	// declarationsToInit) {
	// p.setDoVariableInit(false);
	// ArrayList<String> decs = new ArrayList<>();
	// for (ClassNodeSkeleton<ParserRuleContext> node : declarationsToInit) {
	// String str = p.visit(node.getNode().getNodeData());
	// if (!decs.contains(str)) {
	// decs.add(str);
	// }
	// }
	//
	// p.setDoVariableInit(true);
	// String declarations = "";
	// for (String s : decs) {
	// declarations += s + "\n";
	// }
	//
	// return declarations.trim();
	// }

	private String printVariableDeclaration(ClassNodeSkeleton<ParserRuleContext> declaration) {
		p.setDoVariableDec(false);
		String declarations = p.visit(declaration.getNode().getNodeData());
		p.setDoVariableDec(true);
		return declarations.trim();
	}

	/**
	 * Gets a list of varaibleDecalrations that need to be initalised
	 *
	 * @return
	 */
	private ArrayList<ClassNodeSkeleton<ParserRuleContext>> getDeclarationsToInit(MergePoint<ParserRuleContext> c) {
		// get LocalVariableDeclarationContext
		ArrayList<ClassNodeSkeleton<ParserRuleContext>> childDeclarations = new ArrayList<>();
		for (IntermdiateAST<ParserRuleContext> child : c.getChildren()) {
			if (child instanceof ClassNodeSkeleton) {
				ParserRuleContext ctx = ((ClassNodeSkeleton<ParserRuleContext>) child).getNode().getNodeData();
				if (ctx instanceof BlockStatementContext) {
					ctx = ((BlockStatementContext) ctx).localVariableDeclaration();
				}
				if (ctx instanceof LocalVariableDeclarationContext) {
					childDeclarations.add((ClassNodeSkeleton<ParserRuleContext>) child);
				}
			}
		}
		// TODO trim down list better

		return childDeclarations;
	}
}
