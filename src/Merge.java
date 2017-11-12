import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.antlr.v4.parse.ANTLRParser.throwsSpec_return;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import merge.MergeThread;
import parse.PluginInterface;
import parse.parser.java.JavaPlugin;
import ref.Helper;


/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class Merge {
	
	String path = "";
	String language = "";
	boolean recursive = false;
	int maxThreads = 1;
	PluginInterface plugin = new JavaPlugin();
	private int minMatch;
	private double minPer;
	private double minPerDelta;
	
	/**
	 * Initializes the Merge class
	 * TODO Annotate constructor
	 */
	public Merge(String[] args) {
		parseOptions(args);
		// get list of files
		ArrayList<File> files = getFiles(new File(path));
		maxThreads = Math.min(maxThreads, files.size());
		// dish out to threads
		
		ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
		
		if(Helper.verbose)
				System.out.println("Reading files and distributing threads");
		for(File f: files){
			Helper.printToSTD("Generating thread for "+f.getName(), "Main");
			MergeThread t = new MergeThread(f, plugin.generateInstance(), minMatch, minPer, minPerDelta);
			executor.execute(t);
		}
		Helper.printToSTD("Thread Assigned, Waiting for completion", "Main");
		executor.shutdown();
		
	}
	
	private void parseOptions(String[] args){
		
		minMatch = 1;
		minPer = 0.2;
		minPerDelta = 0;

		
		Options options = new Options();
		
		Option pathOpt = new Option("p", "path", true, "Path to the file or directory the code is in.");
		pathOpt.setRequired(true);
		pathOpt.setArgName("path");
		options.addOption(pathOpt);
		
		Option languageOpt = new Option("l", "language", true, "Language the code is written in.");
		languageOpt.setRequired(true);
		languageOpt.setArgName("language");
		options.addOption(languageOpt);
		
		options.addOption("r", "recursive", false, "Locate files in specified path recursevly.");
		options.addOption("t", "max-threads", true, "Maximum number of threads to use.\nThreads used = min(max-threads, num files).\nDefault is 1.");
		options.addOption("s", "minPer", true, "Minimum percentage of body blocks that must be shared.");
		options.addOption("d", "minPerDelta", true, "Added to minPer to compare the larger method.\nDefault is 0");
		options.addOption("m", "minMatch", true, "minimum number of statemetns to consititute a match.\n Defualt is 1");
		options.addOption("v", "verbose", false, "display logging information");
		
		
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			
			if (cmd.hasOption("path")){
				path = cmd.getOptionValue("path");
			}
			
			if (cmd.hasOption("language")){
				language = cmd.getOptionValue("language");
			}
			
			if(cmd.hasOption('r')){
				recursive = true;
			}
			
			if(cmd.hasOption('v')){
				Helper.verbose = true;
			}
			
			if(cmd.hasOption('t')){
				try {
					maxThreads = Integer.parseInt(cmd.getOptionValue('t'));
				} catch (NumberFormatException e) {
					System.out.println("Error: Thread parameter must be a positive integer");
					showUsageExit(options);
				}
			}
			
			if(cmd.hasOption('s')){
				try {
					minPer = Double.parseDouble(cmd.getOptionValue('s'));
					if(minPer<=0||minPer>1){
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e) {
					System.out.println("Error: MinPer parameter must be in range 0>minPer>=1");
					showUsageExit(options);
				}
			}
			
			if(cmd.hasOption('d')){
				try {
					minPerDelta = Double.parseDouble(cmd.getOptionValue('d'));
					if(minPerDelta+minPer<=0||minPerDelta+minPer>1){
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e) {
					System.out.println("Error: MinPerDelta parameter must be in range 0>minPerDelta-minPer>=1");
					showUsageExit(options);
				}
			}
			
			if(cmd.hasOption('m')){
				try {
					minMatch = Integer.parseInt(cmd.getOptionValue('m'));
				} catch (NumberFormatException e) {
					System.out.println("Error: MinMatch parameter must be a positive integer");
					showUsageExit(options);
				}
			}
			
		} catch (ParseException e) {
			if(e.getClass() != MissingOptionException.class)
				e.printStackTrace();
			
			showUsageExit(options);
		}
	}
	
	private void showUsageExit(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("CPMerge [options] -l language -p path", options);
		System.exit(0);
	}
	
	

	public static void main(String[] args) {
		args = new String[]{"-rvl", "Java", "-p", "testcode/", "-t", "12"};
		Merge m = new Merge(args);
	}
	
	private ArrayList<File> getFiles(File root){
		ArrayList<File> returnList = new ArrayList<>();
		if(root.isDirectory()){
			if(Helper.verbose){
				System.out.println("Searching for files in \""+root.getAbsolutePath()+"\"");
			}
			for(File f: root.listFiles()){
				if(f.isFile()&&plugin.validfile(f.getName())){
					if(Helper.verbose){
						System.out.println("found file \"" +f.getName()+"\"");	
					}
					returnList.add(f);
				}else if(recursive){
					returnList.addAll(getFiles(f));
				}
			}
		}else{
			returnList.add(root);
		}
		return returnList;
	}
	
	
	/*TODO:
	 * Create class for iteration
	 * search input ast for close matches
	 * 
	 */
	
}
