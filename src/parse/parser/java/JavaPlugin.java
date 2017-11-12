package parse.parser.java;
import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import parse.PluginInterface;
import parse.ResoultionPattern;
import parse.parser.java.comp.JavaLexer;
import parse.parser.java.comp.JavaParser;
import parse.parser.java.comp.JavaParser.ClassBodyDeclarationContext;
import parse.parser.java.comp.JavaParser.ClassDeclarationContext;
import parse.parser.java.comp.JavaParser.CompilationUnitContext;
import parse.parser.java.comp.JavaParser.ModifierContext;
import parse.parser.java.comp.JavaParser.TypeDeclarationContext;

/**
 * TODO Annotate class
 * @author 146813
 */
public class JavaPlugin implements PluginInterface{

	private CompilationUnitContext unit;
	
	/* (non-Javadoc)
	 * @see PluginInterface#getLanguageName()
	 */
	@Override
	public String getLanguageName() {
		return "Java";
	}

	/* (non-Javadoc)
	 * @see PluginInterface#getPluginVersion()
	 */
	@Override
	public String getPluginVersion() {
		return "0.0.1";
	}

	
	public void parse(CharStream stream){
		JavaLexer lexer = new JavaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JavaParser parser = new JavaParser(tokens);
		unit = parser.compilationUnit();
	}

	public String generateCode(){
		System.err.println("generateCode not implmented yet");
		return "";
	}

	/* (non-Javadoc)
	 * @see parse.PluginInterface#validfile(java.lang.String)
	 */
	@Override
	public boolean validfile(String filename) {
		return filename.substring(filename.lastIndexOf('.')+1).equalsIgnoreCase("java");
	}

	/* (non-Javadoc)
	 * @see parse.PluginInterface#getPatterns()
	 */
	@Override
	public HashMap<Class<? extends ParserRuleContext>, ResoultionPattern> getPatterns() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see parse.PluginInterface#getMethods()
	 */
	@Override
	public ArrayList<ArrayList<ParserRuleContext>> getMethods() {
		ArrayList<ArrayList<ParserRuleContext>> classMethods = new ArrayList<>();
		
		for(ClassDeclarationContext c: getClasses()){
			ArrayList<ParserRuleContext> methods = new ArrayList<>();
			MethodLoop: for(ClassBodyDeclarationContext body: c.classBody().classBodyDeclaration()){
				for(ModifierContext m:body.getRuleContexts(ModifierContext.class)){
					if(m.classOrInterfaceModifier()!=null && m.classOrInterfaceModifier().STATIC()!=null){
						System.out.println("yo");
						continue MethodLoop;
					}
				}
				if(body.memberDeclaration().methodDeclaration()!=null){
					methods.add(body.memberDeclaration().methodDeclaration());
				}
			}
			if(!methods.isEmpty())
				classMethods.add(methods);
		}
		
		return classMethods;
	}
	
	/**
	 * @return A list of the classes inside the file
	 */
	public ArrayList<ClassDeclarationContext> getClasses(){
		ArrayList<ClassDeclarationContext> classRoots = new ArrayList<>();
		ArrayList<ClassDeclarationContext> uncheckedRoots = new ArrayList<>();
		
		for(TypeDeclarationContext type : unit.typeDeclaration()){
			if(type.classDeclaration()!=null){
				uncheckedRoots.add(type.classDeclaration());
			}
		}
		
		while (!uncheckedRoots.isEmpty()){
			for(ClassBodyDeclarationContext body: uncheckedRoots.get(0).classBody().classBodyDeclaration()){
				if(body.memberDeclaration().classDeclaration()!=null){
					uncheckedRoots.add(body.memberDeclaration().classDeclaration());
				}
			}
			classRoots.add(uncheckedRoots.get(0));
			uncheckedRoots.remove(0);
		}
		
		return classRoots;
	}

	/* (non-Javadoc)
	 * @see parse.PluginInterface#getParsedCode()
	 */
	@Override
	public ParserRuleContext getParsedCode() {
		return unit;
	}

	/* (non-Javadoc)
	 * @see parse.PluginInterface#generateInstance()
	 */
	@Override
	public PluginInterface generateInstance() {
		return new JavaPlugin();
	}
	
}
