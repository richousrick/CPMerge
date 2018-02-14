package parse.parser.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import dif.ClassNode;
import parse.PluginInterface;
import parse.ResoultionPattern;
import parse.parser.java.comp.ASTExtractor;
import parse.parser.java.comp.JavaLexer;
import parse.parser.java.comp.JavaParser;
import parse.parser.java.comp.JavaParser.BlockStatementContext;
import parse.parser.java.comp.JavaParser.ClassBodyDeclarationContext;
import parse.parser.java.comp.JavaParser.ClassDeclarationContext;
import parse.parser.java.comp.JavaParser.CompilationUnitContext;
import parse.parser.java.comp.JavaParser.MethodDeclarationContext;
import parse.parser.java.comp.JavaParser.ModifierContext;
import parse.parser.java.comp.JavaParser.StatementContext;
import parse.parser.java.comp.JavaParser.TypeDeclarationContext;
import parse.parser.java.comp.PrettyPrinter;
import ref.FunctionPos;

/**
 * TODO Annotate class
 *
 * @author 146813
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
	public void preMerge() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see parse.PluginInterface#postMerge()
	 */
	@Override
	public void postMerge() {
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
	public String prettyPrint(ClassNode c) {
		// TODO Auto-generated method stub
		return null;
	}

}
