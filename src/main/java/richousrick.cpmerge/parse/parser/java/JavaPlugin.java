package richousrick.cpmerge.parse.parser.java;

import costmodel.CostModel;
import distance.APTED;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import richousrick.cpmerge.dif.ASTNode;
import richousrick.cpmerge.merge.ClassNodeSkeleton;
import richousrick.cpmerge.merge.FunctionMappings;
import richousrick.cpmerge.merge.MergeThread;
import richousrick.cpmerge.merge.MergedFunction;
import richousrick.cpmerge.parse.PluginInterface;
import richousrick.cpmerge.parse.ResoultionPattern;
import richousrick.cpmerge.parse.parser.java.comp.JavaLexer;
import richousrick.cpmerge.parse.parser.java.comp.JavaParser;
import richousrick.cpmerge.parse.parser.java.comp.JavaParser.*;
import richousrick.cpmerge.parse.parser.java.misc.merge.APTEDCostModel;
import richousrick.cpmerge.parse.parser.java.misc.merge.ClassNode;
import richousrick.cpmerge.parse.parser.java.misc.merge.MethodReferenceUpdater;
import richousrick.cpmerge.parse.parser.java.misc.print.FunctionPrinter;
import richousrick.cpmerge.parse.parser.java.misc.print.Replacement;
import richousrick.cpmerge.parse.parser.java.misc.reference.FunctionCall;
import richousrick.cpmerge.parse.parser.java.misc.reference.MergedFunctionPass;
import richousrick.cpmerge.parse.parser.java.misc.reference.scopes.Scope;
import richousrick.cpmerge.parse.parser.java.misc.validate.FunctionValidator;
import richousrick.cpmerge.parse.parser.java.visitors.ASTExtractor;
import richousrick.cpmerge.parse.parser.java.visitors.PrettyPrinter;
import richousrick.cpmerge.parse.parser.java.visitors.SymbolTableGenerator;
import richousrick.cpmerge.ref.FunctionPos;
import richousrick.cpmerge.ref.Helper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public class JavaPlugin implements PluginInterface<ParserRuleContext> {

	private CompilationUnitContext unit;

	private MergeThread<ParserRuleContext> mergeThread;

	static MethodReferenceUpdater referenceUpdater;

	private final HashMap<ClassNode, FunctionValidator> validators = new HashMap<>();

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
		parser.setErrorHandler(new DefaultErrorStrategy() {
			/*
			 * (non-Javadoc)
			 * @see
			 * org.antlr.v4.runtime.DefaultErrorStrategy#recover(org.antlr.v4.
			 * runtime.Parser, org.antlr.v4.runtime.RecognitionException)
			 */
			@Override
			public void recover(Parser recognizer, RecognitionException e) {
				Helper.exitProgram("File does not match expected format");
			}

		});
		unit = parser.compilationUnit();
	}

	public String generateCode() {
		System.err.println("generateCode not implmented yet");
		return "";
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#validfile(java.lang.String)
	 */
	@Override
	public boolean validfile(String filename) {
		String name = filename.substring(filename.lastIndexOf('.') + 1);
		return name.equalsIgnoreCase("java");
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#getPatterns()
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
	 * @see richousrick.cpmerge.parse.PluginInterface#getClasses()
	 */
	@Override
	public ArrayList<ClassNode> getClasses() {
		ASTExtractor visitor = new ASTExtractor();
		ArrayList<ClassNode> classes = null;
		try {
			classes = visitor.visitCompilationUnit(unit).getChildrenAsASTNode();
		} catch (Exception e) {
			Helper.exitProgram(e);
		}
		// for (ClassNode c : classes) {
		// c.compressClass();
		// }
		return classes;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#getParsedCode()
	 */
	@Override
	public ParserRuleContext getParsedCode() {
		return unit;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#generateInstance()
	 */
	@Override
	public PluginInterface<ParserRuleContext> generateInstance() {
		return new JavaPlugin();
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#preMerge()
	 */
	@Override
	public void preMerge(ClassNodeSkeleton<ParserRuleContext> root) {
	}


	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#postMerge()
	 */
	@Override
	public void postMerge(ClassNodeSkeleton<ParserRuleContext> root) {
		// TODO Auto-generated method stub

	}


	@Override
	public int getClassStartLine(ASTNode<ParserRuleContext> classRoot, BufferedReader in) {
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
	 * @see richousrick.cpmerge.parse.PluginInterface#getPositionInFile(richousrick.cpmerge.dif.ClassNode,
	 * java.io.BufferedReader)
	 */
	@Override
	public FunctionPos getPositionInFile(ASTNode<ParserRuleContext> funcHead, BufferedReader in, int startPos) {
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
	 * @see richousrick.cpmerge.parse.PluginInterface#prettyPrint(richousrick.cpmerge.dif.ClassNode)
	 */
	@Override
	public String prettyPrint(MergedFunction<ParserRuleContext> rootNode) {
		FunctionPrinter printer = new FunctionPrinter();
		FunctionValidator fv = validators.get(rootNode.getOriginalFunctionRoots().get(0).getParent());
		if (fv == null) {
			Helper.exitProgram("");
		}
		return printer.prettyPrintFunction(rootNode,
				fv.getFunctionSymbols(rootNode.getID()));
	}


	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#genFunctionName(richousrick.cpmerge.dif.ClassNode[])
	 */
	@Override
	public String genFunctionName(ArrayList<ASTNode<ParserRuleContext>> classes) {
		String retString = "";
		for (ASTNode<ParserRuleContext> c : classes) {
			retString += c.getIdentifier().substring(0, 1).toUpperCase();
			if (c.getIdentifier().length() > 1) {
				retString += c.getIdentifier().substring(1);
			}
		}
		return retString;
	}



	private int getFuncPosition(MergedFunction<ParserRuleContext> func, String fileContent) {
		ParserRuleContext data = func.getRootNode().getNode().getNodeData().getParent().getParent();
		String funcRep = getSourceRepresentation(data);
		// TODO use startIndex of classNameIndex
		int pos = fileContent.indexOf(funcRep);
		return pos;
	}

	private String getSourceRepresentation(ParserRuleContext data) {
		int startPos = data.start.getStartIndex();
		int endPos = data.stop.getStopIndex();
		CharStream cs = data.start.getInputStream();
		return cs.getText(new Interval(startPos, endPos));
	}

	private int getStartPos(ClassBodyDeclarationContext data) {
		int startPos = -1;
		int childIndex = data.getParent().children.indexOf(data);
		if (childIndex == 0) {
			startPos = data.getParent().start.getStopIndex();
		} else {
			Object child = data.getParent().getChild(childIndex - 1);
			if (child instanceof TerminalNodeImpl) {
				startPos = ((TerminalNodeImpl) child).symbol.getStopIndex();
			} else {
				startPos = ((ParserRuleContext) child).stop.getStopIndex();
			}
		}
		startPos++;
		return startPos;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#startWriting()
	 */
	@Override
	public void startWriting() {

	}



	static synchronized void setMergedFunctions(ArrayList<MergedFunction<ParserRuleContext>> functions) {
		if (MethodReferenceUpdater.initMethodReferenceUpdater(functions)) {
			referenceUpdater = new MethodReferenceUpdater();
			PrettyPrinter p = new PrettyPrinter(false);
			HashMap<String, MergedFunctionPass> newFunctionPaths = new HashMap<>();
			// get mapping of changed functions to new
			for (MergedFunction<ParserRuleContext> r : functions) {
				for (ASTNode<ParserRuleContext> c : r.getOriginalFunctionRoots()) {
					newFunctionPaths.put(p.getNodeName(c.getNodeData()),
							new MergedFunctionPass((MethodDeclarationContext) c.getNodeData(), r));
				}
			}
			if (Scope.needResolving()) {
				Scope.resoveAllReferences(newFunctionPaths);
			}

		}
	}


	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#updateReferences(java.util.ArrayList)
	 */
	@Override
	public String updateReferences(String fileContnent, ArrayList<MergedFunction<ParserRuleContext>> mergedFunctions) {
		try {
			JavaPlugin.setMergedFunctions(mergedFunctions);
			String name = mergeThread.getFile().getName();
			name = name.substring(0, name.indexOf("."));
			ArrayList<FunctionCall> functionCalls = Scope.getFunctionCallsToUpdate(name);
			if(functionCalls.isEmpty())
				return fileContnent;
			else
				return referenceUpdater.updateReferences(functionCalls, fileContnent);
		} catch (Throwable e) {
			e.printStackTrace();
			Helper.exitProgram("");
		}
		// remove all symbols that are of unparsed type + functions referencing
		// those symbols

		return fileContnent;
	}


	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#initUpdateReferences(java.lang.String,
	 * java.util.ArrayList)
	 */
	@Override
	public String initUpdateReferences(String currFileRep, ArrayList<MergedFunction<ParserRuleContext>> updatedFunctions) {
		// if no updated functions no need to init
		if (updatedFunctions.size() != 0) {

			try {
				parse(CharStreams.fromStream(new ByteArrayInputStream(currFileRep.getBytes())));
			} catch (IOException e) {
				e.printStackTrace();
			}

			SymbolTableGenerator gen = new SymbolTableGenerator();
			String name = mergeThread.getFile().getName();
			name = name.substring(0, name.indexOf("."));
			try {
				gen.processClass(unit, name);
			} catch (Throwable e) {
				Helper.exitProgram(e.getMessage());
			}
		}
		return currFileRep;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#destroyUpdateReferences(java.lang.String,
	 * java.util.ArrayList)
	 */
	@Override
	public String destroyUpdateReferences(String currFileRep, ArrayList<MergedFunction<ParserRuleContext>> updatedFunctions) {
		// TODO Auto-generated method stub
		return currFileRep;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#setMergeThread(richousrick.cpmerge.merge.MergeThread)
	 */
	@Override
	public void setMergeThread(MergeThread<ParserRuleContext> thread) {
		mergeThread = thread;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#copyNode(richousrick.cpmerge.dif.ASTNode, boolean)
	 */
	@Override
	public ASTNode<?> copyNode(ASTNode<?> node, boolean copyChildren) {
		if (copyChildren)
			return new ClassNode((ClassNode) node);
		else
			return new ClassNode((ClassNode) node, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#getApted()
	 */
	@Override
	public APTED<? extends CostModel<ParserRuleContext>, ParserRuleContext> getApted() {
		return new APTED<CostModel<ParserRuleContext>, ParserRuleContext>(new APTEDCostModel());
	}

	private final ArrayList<Replacement> replacements = new ArrayList<>();

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#updateFunctionBody(richousrick.cpmerge.merge.RootNode,
	 * java.lang.String)
	 */
	@Override
	public String updateFunctionBodies(MergedFunction<ParserRuleContext> nodes, String currFileRep) {
		for(ASTNode<ParserRuleContext> node: nodes.getOriginalFunctionRoots()){
			// get representation of function
			ClassBodyDeclarationContext functionCtx = (ClassBodyDeclarationContext) node.getNodeData().getParent()
					.getParent();
			String function = getSourceRepresentation(functionCtx);

			BlockContext bodyCtx = ((MethodDeclarationContext) node.getNodeData()).methodBody().block();
			String functionBody = getSourceRepresentation(bodyCtx);
			functionBody = functionBody.substring(1, functionBody.length() - 1).trim();

			MethodDeclarationContext meth = (MethodDeclarationContext) node.getNodeData();
			PrettyPrinter p = new PrettyPrinter(false);

			boolean hasReturn = !p.visit(meth.typeTypeOrVoid()).equals("void");

			String returnStament = (hasReturn ? "return " : "") + meth.IDENTIFIER().getText() + "("
					+ p.printParameterListNames(meth.formalParameters()) + ");";

			String emptyFunction = function.replaceAll(Pattern.quote(functionBody), returnStament);
			replacements.add(new Replacement(functionCtx.start.getStartIndex(), functionCtx.stop.getStopIndex(),
					emptyFunction, function, ""));
		}
		return currFileRep;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#removeFunction(richousrick.cpmerge.dif.ClassNode,
	 * java.lang.String)
	 */
	@Override
	public String removeFunctions(MergedFunction<ParserRuleContext> functions, String fileContent) {
		for(ASTNode<ParserRuleContext> function: functions.getOriginalFunctionRoots()){
			ClassBodyDeclarationContext data = (ClassBodyDeclarationContext) function.getNodeData().getParent()
					.getParent();
			// TODO replace first instance in class
			String s = data.start.getInputStream()
					.getText(new Interval(getStartPos(data), data.stop.getStopIndex()));
			replacements.add(new Replacement(getStartPos(data), data.stop.getStopIndex(), "", s, ""));
		}
		return fileContent;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#insertFunction(richousrick.cpmerge.merge.ClassNodeSkeleton)
	 */
	@Override
	public String insertFunction(MergedFunction<ParserRuleContext> root, String fileContent) {
		int pos = getFuncPosition(root, fileContent);

		String funcRep = root.getStringRepresentation();
		if (funcRep == null) {
			funcRep = prettyPrint(root);
			root.setStringRepresentation(funcRep.substring(0, funcRep.indexOf('{')));
		}
		// get num tabs
		int retPos = fileContent.lastIndexOf('\n', pos);
		String indent = "";
		try {
			indent = fileContent.substring(retPos + 1, pos);
		} catch (StringIndexOutOfBoundsException e) {

		}
		funcRep = "\n\n" + indent + funcRep.replaceAll("\n", "\n" + indent) + "\n";

		ParserRuleContext data = root.getOriginalFunctionRoots().get(0).getNodeData().getParent().getParent();
		String function = getSourceRepresentation(data);

		int insertPos = getStartPos((ClassBodyDeclarationContext) data);
		replacements.add(new Replacement(insertPos, insertPos,
				funcRep,
				function, ""));

		return fileContent;
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#postProcessFile(java.lang.String)
	 */
	@Override
	public String postProcessFile(String currFileRep) {
		return Replacement.replace(replacements, currFileRep, false, Helper.deleteOldFunctions);
	}

	/*
	 * (non-Javadoc)
	 * @see richousrick.cpmerge.parse.PluginInterface#validateMergeGroup(java.util.ArrayList,
	 * richousrick.cpmerge.merge.FunctionMappings)
	 */
	@Override
	public ArrayList<ArrayList<Integer>> validateMergeGroup(ArrayList<ArrayList<Integer>> groups,
			FunctionMappings<ParserRuleContext> mappings, ArrayList<? extends ASTNode<ParserRuleContext>> functions) {
		if (functions.size() > 0) {
			FunctionValidator validator = new FunctionValidator(functions);
			validators.put((ClassNode) functions.get(0).getParent(),
					validator);
			return validator.validateMergeGroup(groups, mappings);
		} else
			return new ArrayList<>();

	}


}