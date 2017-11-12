import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import parse.DummyPlugin;
import parse.PluginInterface;


/**
 * TODO Annotate class
 * @author Rikkey Paal
 */
public class Merge {
	
	String path = "";
	String language = "";
	boolean recursive = false;
	int maxThreads = 1;
	PluginInterface plugin = new DummyPlugin();
	
	/**
	 * Initializes the Merge class
	 * TODO Annotate constructor
	 */
	public Merge(String[] args) {
		parseOptions(args);
		// get list of files
		ArrayList<File> files = getFiles(new File(path));
		// dish out to threads
		System.out.println(Arrays.toString(files.toArray(new File[files.size()])));
		
		ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
		
	}
	
	private void parseOptions(String[] args){
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
			
			if(cmd.hasOption('t')){
				try {
					maxThreads = Integer.parseInt(cmd.getOptionValue('t'));
				} catch (NumberFormatException e) {
					System.out.println("Error: Thread parameter must be a positive integer");
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
		//args = new String[]{"-rl", "Java", "-p", "D:/Work/Coding/Java/CPMerge - Java/src", "-t", "12"};
		
		Merge m = new Merge(args);
	}
	
	private ArrayList<File> getFiles(File root){
		ArrayList<File> returnList = new ArrayList<>();
		if(root.isDirectory()){
			for(File f: root.listFiles()){
				if(f.isFile()&&plugin.validfile(f.getName())){
					System.out.println(f.getName());
					returnList.add(f);
				}else if(recursive){
					returnList.addAll(getFiles(f));
				}
			}
		}
		return returnList;
	}
	
	
	
	/*TODO:
	 * Create class for iteration
	 * search input ast for close matches
	 * 
	 */
	
}
