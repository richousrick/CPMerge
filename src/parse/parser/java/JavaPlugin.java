package parse.parser.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import dif.ClassNode;
import merge.ClassNodeSkeleton;
import merge.IntermdiateAST;
import merge.MergePoint;
import merge.UniqueSet;
import parse.PluginInterface;
import parse.ResoultionPattern;
import parse.parser.java.comp.ASTExtractor;
import parse.parser.java.comp.JavaLexer;
import parse.parser.java.comp.JavaParser;
import parse.parser.java.comp.JavaParser.BlockStatementContext;
import parse.parser.java.comp.JavaParser.ClassBodyDeclarationContext;
import parse.parser.java.comp.JavaParser.ClassDeclarationContext;
import parse.parser.java.comp.JavaParser.CompilationUnitContext;
import parse.parser.java.comp.JavaParser.LocalVariableDeclarationContext;
import parse.parser.java.comp.JavaParser.MethodDeclarationContext;
import parse.parser.java.comp.JavaParser.ModifierContext;
import parse.parser.java.comp.JavaParser.StatementContext;
import parse.parser.java.comp.JavaParser.TypeDeclarationContext;
import parse.parser.java.comp.PrettyPrinter;
import ref.FunctionPos;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public class JavaPlugin implements PluginInterface {

	private CompilationUnitContext unit;

	/*
	 * (non-Javadoc)
	 * @see PluginInterface#getLanguageName()
	 */
	@Override
	public String getLanguageName() {
		return "Java";
	}

	/*
	 * (non-Javadoc)
	 * @see PluginInterface#getPluginVersion()
	 */
	@Override
	public String getPluginVersion() {
		return "0.0.1";
	}

	@Override
	public void parse(CharStream stream) {
		JavaLexer lexer = new JavaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JavaParser parser = new JavaParser(tokens);
		unit = parser.compilationUnit();
	}

	public String generateCode() {
		System.err.println("generateCode not implmented yet");
		return "";
	}

	/*
	 * (non-Javadoc)
	 * @see parse.PluginInterface#validfile(java.lang.String)
	 */
	@Override
	public boolean validfile(String filename) {
		return filename.substring(filename.lastIndexOf('.') + 1).equalsIgnoreCase("java");
	}

	/*
	 * (non-Javadoc)
	 * @see parse.PluginInterface#getPatterns()
	 */
	@Override
	public HashMap<Class<? extends ParserRuleContext>, ResoultionPattern> getPatterns() {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<MethodDeclarationContext> getMethodList(ClassDeclarationContext classDeclaration) {
		ArrayList<MethodDeclarationContext> methods = new ArrayList<>();
		MethodLoop: for (ClassBodyDeclarationContext body : classDeclaration.classBody().classBodyDeclaration()) {
			for (ModifierContext m : body.getRuleContexts(ModifierContext.class)) {
				if (m.classOrInterfaceModifier() != null && m.classOrInterfaceModifier().STATIC() != null) {
					continue MethodLoop;
				}
			}
			if (body.memberDeclaration().methodDeclaration() != null) {
				methods.add(body.memberDeclaration().methodDeclaration());
			}
		}

		return methods;
	}

	private ClassNode addChildStatements(ClassNode n) {
		List<BlockStatementContext> blocks = null;
		switch (n.getType()) {
			case 0:
				return n;
			case 1:
				blocks = ((MethodDeclarationContext) n.getNodeData()).methodBody().block().blockStatement();
				break;
			case 2:
				for (StatementContext s : ((StatementContext) n.getNodeData()).statement()) {
					ClassNode nt = new ClassNode(s, s.getText(), (byte) 2);
					n.addChild(addChildStatements(nt));
				}
				if (((StatementContext) n.getNodeData()).block() != null) {
					blocks = ((StatementContext) n.getNodeData()).block().blockStatement();
				}
				break;
		}

		if (blocks != null) {
			for (BlockStatementContext s : blocks) {
				if (s.localTypeDeclaration() != null) {
					n.addChild(new ClassNode(s.localTypeDeclaration(), s.getText(), (byte) 2));
				} else if (s.statement() != null) {
					ClassNode nt = new ClassNode(s.statement(), s.getText(), (byte) 2);
					n.addChild(addChildStatements(nt));
				}
			}
		}
		return n;
	}

	/**
	 * @return A list of the classes inside the file
	 */
	public ArrayList<ClassDeclarationContext> getClassList() {
		ArrayList<ClassDeclarationContext> classRoots = new ArrayList<>();
		ArrayList<ClassDeclarationContext> uncheckedRoots = new ArrayList<>();

		for (TypeDeclarationContext type : unit.typeDeclaration()) {
			if (type.classDeclaration() != null) {
				uncheckedRoots.add(type.classDeclaration());
			}
		}

		while (!uncheckedRoots.isEmpty()) {
			for (ClassBodyDeclarationContext body : uncheckedRoots.get(0).classBody().classBodyDeclaration()) {
				if (body.memberDeclaration().classDeclaration() != null) {
					uncheckedRoots.add(body.memberDeclaration().classDeclaration());
				}
			}
			classRoots.add(uncheckedRoots.get(0));
			uncheckedRoots.remove(0);
		}

		return classRoots;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.PluginInterface#getClasses()
	 */
	@Override
	public ArrayList<ClassNode> getClasses() {
		ASTExtractor visitor = new ASTExtractor();
		ArrayList<ClassNode> classes = visitor.visitCompilationUnit(unit).getChildrenAsCN();
		for (ClassNode c : classes) {
			c.compressClass();
		}
		return classes;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.PluginInterface#getParsedCode()
	 */
	@Override
	public ParserRuleContext getParsedCode() {
		return unit;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.PluginInterface#generateInstance()
	 */
	@Override
	public PluginInterface generateInstance() {
		return new JavaPlugin();
	}

	/*
	 * (non-Javadoc)
	 * @see parse.PluginInterface#preMerge()
	 */
	@Override
	public void preMerge(ClassNodeSkeleton root) {
	}


	/*
	 * (non-Javadoc)
	 * @see parse.PluginInterface#postMerge()
	 */
	@Override
	public void postMerge(ClassNodeSkeleton root) {
		// TODO Auto-generated method stub

	}

	private int countOccurences(String s, char target) {
		int i = 0;
		for (char c : s.toCharArray()) {
			if (c == target) {
				i++;
			}
		}
		return i;
	}

	@Override
	public int getClassStartLine(ClassNode classRoot, BufferedReader in) {
		PrettyPrinter p = new PrettyPrinter(false);
		String head = p.visitClassDeclaration((ClassDeclarationContext) classRoot.getNodeData());
		String regexp = head.replaceAll("[\\<\\(\\[\\{\\\\\\^\\-\\=\\$\\!\\|\\]\\}\\)\\?\\*\\+\\.\\>]", "\\\\$0\\\\s*")
				.replaceAll("\\s", "\\\\s+");
		Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);
		String line;
		int i = 0;
		loop: try {
			while ((line = in.readLine()) != null) {
				if (pattern.matcher(line).find()) {
					break loop;
				}
				i++;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return i;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.PluginInterface#getPositionInFile(dif.ClassNode,
	 * java.io.BufferedReader)
	 */
	@Override
	public FunctionPos getPositionInFile(ClassNode funcHead, BufferedReader in, int startPos) {
		// TODO optimise calling, so finding class is done once per file
		try {
			PrettyPrinter p = new PrettyPrinter(false);
			String head = p.visitMethodDeclaration((MethodDeclarationContext) funcHead.getNodeData());
			// Generate regex that ignores additional whitespace
			String regexp = head.replaceAll("[\\<\\(\\[\\{\\\\\\^\\-\\=\\$\\!\\|\\]\\}\\)\\?\\*\\+\\.\\>]", "\\\\$0\\\\s*")
					.replaceAll("\\s", "\\\\s+");
			Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);

			int start = -1;
			int end = -1;
			int depth = 0;
			Matcher m;

			for (int i = 0; i < startPos; i++) {
				in.readLine();
			}
			int i = startPos + 1;

			String line;
			loop: while ((line = in.readLine()) != null) {
				if (start == -1 && (m = pattern.matcher(line)).find()) {
					depth = 1;
					line = line.substring(m.end());
					start = i;
				}
				if (depth > 0) {
					for (char c : line.toCharArray()) {
						if (c == '{') {
							depth++;
						} else if (c == '}') {
							depth--;
							if (depth == 0) {
								end = i;
								break loop;
							}
						}
					}
				}
				i++;
			}

			return new FunctionPos(start, end);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see parse.PluginInterface#prettyPrint(dif.ClassNode)
	 */
	@Override
	public String prettyPrint(ClassNodeSkeleton c) {
		// PrettyPrinter p = new PrettyPrinter(true);
		// String print = "";
		// // get function head representation
		//
		// for (IntermdiateAST child : c.getChildren()) {
		// if (child instanceof ClassNodeSkeleton) {
		// print += prettyPrintSkeleton((ClassNodeSkeleton) child, p);
		// } else {
		// print += prettyPrintMerge((MergePoint) child, p);
		// }
		// }
		//
		// return print;

		PrettyPrinter p = new PrettyPrinter(false);
		String head = p.visit(c.getNode().getNodeData());
		// visit all posible heads

		head = head.substring(0, head.lastIndexOf(')')).trim() + ", int fID){";

		String body = "";
		for (IntermdiateAST child : c.getChildren()) {
			if (child instanceof ClassNodeSkeleton) {
				body += "\t" + prettyPrintBody((ClassNodeSkeleton) child, p).replaceAll("\n", "\n\t");
			} else {
				body += "\t" + prettyPrintMergePoint((MergePoint) child, p).replaceAll("\n", "\n\t");
			}
			body += "\n";
		}

		return head + "\n\t" + body.trim() + "\n}";

		// LazyPrettyPrinter lpp = new LazyPrettyPrinter();
		// return lpp.convertToString(c);
	}

	private String prettyPrintBody(ClassNodeSkeleton c, PrettyPrinter p) {
		ParserRuleContext ctx = c.getNode().getNodeData();
		if (ctx instanceof BlockStatementContext) {
			ctx = ((BlockStatementContext) ctx).statement();
		}
		if(ctx instanceof StatementContext) {
			if (((StatementContext) ctx).IF() != null)
				return prettyPrintBodyIf(c, p);

		}
		return prettyPrintBodyDefault(c, p);
	}

	private String prettyPrintBodyIf(ClassNodeSkeleton c, PrettyPrinter p) {
		if (c.getChildren().get(0) instanceof MergePoint) {
			boolean elseFlag = false;

			String head = p.visit(c.getNode().getNodeData());
			if(head.endsWith("else")) {
				head = head.substring(0,head.length()-4);
				elseFlag = true;
			}
			head += "{";

			// get list of cases for if and else
			HashMap<UniqueSet, ArrayList<ClassNodeSkeleton>> ifCases = new HashMap<>();
			HashMap<UniqueSet, ArrayList<ClassNodeSkeleton>> elseCases = new HashMap<>();

			for(Entry<UniqueSet, ArrayList<ClassNodeSkeleton>> options:((MergePoint)c.getChildren().get(0)).getMergeOptions().entrySet()) {
				if (elseFlag) {
					if (options.getValue().get(0).toString().startsWith("then")) {
						ArrayList<ClassNodeSkeleton> tmp = new ArrayList<>();
						tmp.add(options.getValue().get(0));
						ifCases.put(options.getKey(), tmp);

						tmp = new ArrayList<>();
						tmp.add(options.getValue().get(1));
						elseCases.put(options.getKey(), tmp);
					} else {
						ArrayList<ClassNodeSkeleton> tmp = new ArrayList<>();
						tmp.add(options.getValue().get(1));
						ifCases.put(options.getKey(), tmp);

						tmp = new ArrayList<>();
						tmp.add(options.getValue().get(0));
						elseCases.put(options.getKey(), tmp);
					}
				} else {
					ArrayList<ClassNodeSkeleton> tmp = new ArrayList<>();
					tmp.add(options.getValue().get(0));
					ifCases.put(options.getKey(), tmp);
				}
			}


			head += "\n\t" + prettyPrintMergePoint(new MergePoint(ifCases, c.getMergeGroup(), c, 0), p)
			.replaceAll("\n", "\n\t").trim();
			head += "\n}";
			if (elseFlag) {
				head += " else {";
				head += "\n\t" + prettyPrintMergePoint(new MergePoint(ifCases, c.getMergeGroup(), c, 0), p)
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
					head += p.visit(((ClassNodeSkeleton) c.getChildren().get(0)).getNode().getNodeData())
							.replaceAll("\n", "\n\t");
					head = head.trim() + "\n} else {\n\t";
					head += p.visit(((ClassNodeSkeleton) c.getChildren().get(1)).getNode().getNodeData())
							.replaceAll("\n", "\n\t");
				} else {
					head += p.visit(((ClassNodeSkeleton) c.getChildren().get(1)).getNode().getNodeData())
							.replaceAll("\n", "\n\t");
					head = head.trim() + "\n} else {\n";
					head += p.visit(((ClassNodeSkeleton) c.getChildren().get(0)).getNode().getNodeData())
							.replaceAll("\n", "\n\t");
				}
			} else {
				head += p.visit(((ClassNodeSkeleton) c.getChildren().get(0)).getNode().getNodeData()).replaceAll("\n",
						"\n\t");
			}
			return head.trim() + "\n}";
		}
	}

	private String prettyPrintBodyDefault(ClassNodeSkeleton c, PrettyPrinter p) {
		String body = p.visit(c.getNode().getNodeData());

		for (IntermdiateAST child : c.getChildren()) {
			if (child instanceof ClassNodeSkeleton) {
				body += prettyPrintBody((ClassNodeSkeleton) child, p);
			} else {
				body += prettyPrintMergePoint((MergePoint) child, p);
			}
			body += "\n";
		}
		return body.trim();
	}

	private String prettyPrintMergePoint(MergePoint c, PrettyPrinter p) {
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
			List<Integer> containerSet = c.getParent().getUniqueSet().getSetFuncIds();
			if (sets.size() == containerSet.size() && sets.containsAll(containerSet)) {
				useSwitch = false;
			}
		}

		return useSwitch ? prettyPrintMergePointSwitch(c, p) : prettyPrintMergePointIf(c, p);

	}

	private String prettyPrintMergePointIf(MergePoint c, PrettyPrinter p) {
		// get entries of the if and else cases
		Entry<UniqueSet, ArrayList<ClassNodeSkeleton>> caseIf = null;
		Entry<UniqueSet, ArrayList<ClassNodeSkeleton>> caseElse = null;
		Iterator<Entry<UniqueSet, ArrayList<ClassNodeSkeleton>>> iterator = c.getMergeOptions().entrySet().iterator();
		caseIf = iterator.next();
		if (iterator.hasNext()) {
			caseElse = iterator.next();
			if (caseElse.getKey().getSetFuncIds().size() < caseIf.getKey().getSetFuncIds().size()) {
				Entry<UniqueSet, ArrayList<ClassNodeSkeleton>> tmp = caseElse;
				caseElse = caseIf;
				caseIf = tmp;
			}
		}

		// init variables declared in if statement
		ArrayList<ClassNodeSkeleton> declarationsToInit = getDeclarationsToInit(c);
		String str = initVariableDecalarations(declarationsToInit, p);

		// add if head
		str += "\nif(";
		for (int id : caseIf.getKey().getSetFuncIds()) {
			str += "fID == " + id + " || ";
		}

		// add if body
		str = str.substring(0, str.length() - 3) + "){";
		for (ClassNodeSkeleton cnc : caseIf.getValue()) {
			if (declarationsToInit.contains(cnc)) {
				str += "\n\t" + printVariableDeclaration(cnc, p);
			} else {
				str += "\n\t" + prettyPrintBody(cnc, p);
			}
		}
		str = str.trim() + "\n}";

		// add else case if
		if (caseElse != null) {
			str += " else {";
			for (ClassNodeSkeleton cnc : caseElse.getValue()) {
				if (declarationsToInit.contains(cnc)) {
					str += "\n\t" + printVariableDeclaration(cnc, p);
				} else {
					str += "\n\t" + prettyPrintBody(cnc, p);
				}
			}
			str = str.trim() + "\n}";
		}
		return str.trim();
	}

	private String prettyPrintMergePointSwitch(MergePoint c, PrettyPrinter p) {
		// init variables declared in if statement
		ArrayList<ClassNodeSkeleton> declarationsToInit = getDeclarationsToInit(c);
		String str = initVariableDecalarations(declarationsToInit, p);

		// add switch head
		str += "\nswitch (fID) {";

		for (Entry<UniqueSet, ArrayList<ClassNodeSkeleton>> option : c.getMergeOptions().entrySet()) {
			// add cases
			for (int id : option.getKey().getSetFuncIds()) {
				str += "\n\tcase " + id + ":";
			}
			String body = "";
			// add body
			for (ClassNodeSkeleton cnc : option.getValue()) {
				if (declarationsToInit.contains(cnc)) {
					body += "\n\t\t" + printVariableDeclaration(cnc, p);
				} else {
					body += "\n\t\t" + prettyPrintBody(cnc, p).replaceAll("\n", "\n\t\t");
				}

			}
			String pattern = "[\\s.]*return.*;\\s*$";
			if (!body.matches(pattern)) {
				body += "\n\t\tbreak;";
			}
			str += body;
		}
		str += "\n}";
		return str.trim();
	}

	private String initVariableDecalarations(ArrayList<ClassNodeSkeleton> declarationsToInit, PrettyPrinter p) {
		p.setDoVariableInit(false);
		ArrayList<String> decs = new ArrayList<>();
		for (ClassNodeSkeleton node : declarationsToInit) {
			String str = p.visit(node.getNode().getNodeData());
			if (!decs.contains(str)) {
				decs.add(str);
			}
		}

		p.setDoVariableInit(true);
		String declarations = "";
		for (String s : decs) {
			declarations += s + "\n";
		}

		return declarations.trim();
	}

	private String printVariableDeclaration(ClassNodeSkeleton declaration, PrettyPrinter p) {
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
	private ArrayList<ClassNodeSkeleton> getDeclarationsToInit(MergePoint c) {
		// get LocalVariableDeclarationContext
		ArrayList<ClassNodeSkeleton> childDeclarations = new ArrayList<>();
		for (IntermdiateAST child : c.getChildren()) {
			if (child instanceof ClassNodeSkeleton) {
				ParserRuleContext ctx = ((ClassNodeSkeleton) child).getNode().getNodeData();
				if (ctx instanceof BlockStatementContext) {
					ctx = ((BlockStatementContext) ctx).localVariableDeclaration();
				}
				if (ctx instanceof LocalVariableDeclarationContext) {
					childDeclarations.add((ClassNodeSkeleton) child);
				}
			}
		}
		// TODO trim down list better

		return childDeclarations;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.PluginInterface#genFunctionName(dif.ClassNode[])
	 */
	@Override
	public String genFunctionName(ArrayList<ClassNode> classes) {
		String retString = "";
		for (ClassNode c : classes) {
			retString += c.getIdentifier().substring(0, 1).toUpperCase();
			if (c.getIdentifier().length() > 1) {
				retString += c.getIdentifier().substring(1);
			}
		}
		return retString;
	}

	/*
	 * (non-Javadoc)
	 * @see parse.PluginInterface#updateFunctionName(merge.ClassNodeSkeleton,
	 * merge.ClassNodeSkeleton, int, java.lang.String)
	 */
	@Override
	public String updateFunctionName(ClassNodeSkeleton originalRoot, ClassNodeSkeleton newRoot, int fID, String code) {
		return "";
	}

}