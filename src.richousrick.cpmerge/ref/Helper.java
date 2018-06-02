package ref;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import merge.MergedFunction;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public class Helper {

	private static HashMap<String, String> completedSourceFiles = new HashMap<String, String>();
	private static boolean sourceLock = false;

	private static ArrayList<MergedFunction<?>> updatedFunctions = new ArrayList<>();
	private static boolean functionLock = false;

	public static final boolean test = true;

	public static boolean verbose;

	public static boolean deleteOldFunctions = false;
	private static boolean forceExitEnabled = true;

	public static boolean parserContextEqual(ParserRuleContext p1, ParserRuleContext p2, boolean matchContent,
			boolean recursive) {
		if (!p1.getClass().equals(p2.getClass()))
			return false;

		if (!p1.toString().equals(p2.toString()))
			return false;

		if (p1.getChildCount() != p2.getChildCount())
			return false;

		int terminalCount = 0;
		for (int i = 0; i < p1.getChildCount(); i++) {
			ParserRuleContext p1c = p1.getChild(ParserRuleContext.class, i);
			ParserRuleContext p2c = p2.getChild(ParserRuleContext.class, i);
			if (recursive && p1c != null && p2c != null) {
				if (!parserContextEqual(p1c, p2c, matchContent, true))
					return false;
			} else {
				TerminalNodeImpl p1t = p1.getChild(TerminalNodeImpl.class, terminalCount);
				TerminalNodeImpl p2t = p2.getChild(TerminalNodeImpl.class, terminalCount);
				if (!matchContent)
					return p1t != null && p2t != null;
				if (!p1t.getText().equals(p2t.getText()))
					return false;
				terminalCount++;
			}

		}
		return true;
	}

	public synchronized static void addSourceFIle(String filename, String fileContent) throws IllegalAccessException {
		if (!sourceLock) {
			completedSourceFiles.put(filename, fileContent);
		} else
			throw new IllegalAccessException(
					"thread trying to insert completed sourceFile after registered thread has ended");
	}

	public synchronized static HashMap<String, String> getSourceFiles() {
		sourceLock = true;
		return completedSourceFiles;
	}

	public synchronized static void addUpdatedFunction(MergedFunction<?> function) throws IllegalAccessException {
		if (!functionLock) {
			updatedFunctions.add(function);
		} else
			throw new IllegalAccessException(
					"thread trying to insert merged function after registered thread has ended");
	}

	public synchronized static ArrayList<MergedFunction<?>> getUpdatedFunctions() {
		functionLock = true;
		return updatedFunctions;
	}

	public synchronized static void printToSTD(String stringToPrint, boolean checkVerbose) {
		if (checkVerbose && !verbose)
			return;
		System.out.println(stringToPrint);
		System.out.flush();
	}

	public synchronized static void printToSTD(String stringToPrint) {
		printToSTD(stringToPrint, true);
	}

	public synchronized static void printToSTD(String stringToPrint, String threadId) {
		if (!verbose)
			return;
		printToSTD(threadId + ":" + stringToPrint);
	}

	public synchronized static CharStream readFile(File f) throws IOException {
		return CharStreams.fromPath(f.toPath());
	}

	public synchronized static void exitProgram(String reason) {
		if (!forceExitEnabled)
			return;
		System.err.println("Fatal error: " + reason);
		System.exit(1);
	}

	public synchronized static void exitProgram(Exception e) {
		if (!forceExitEnabled)
			return;
		System.err.println("Fatal error:");
		e.printStackTrace();
		System.exit(1);
	}

	/**
	 * TODO Annotate method
	 */
	public static void disableForceExit() {
		forceExitEnabled = false;
	}

	private static Language<?> languageInstance = null;

	public static Language<?> getLanguageInstance() {
		return languageInstance;
	}

	public static void setLanguageInstance(Language<?> instance) {
		Helper.languageInstance = instance;
	}


}
