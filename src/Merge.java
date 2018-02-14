import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
 *
 * @author Rikkey Paal
 */
public class Merge {

	public static void main(String[] args) {
		final boolean runOnSmall = true;
		if (runOnSmall) {
			args = new String[] { "-rl", "Java", "-p", "testcode/", "-t", "12" };
		} else {
			args = new String[] { "-rl", "Java", "-p",
					"E:\\University\\Workspace\\CPMerge\\src\\parse\\parser\\java\\comp\\JavaParser.java", "-t", "12" };
		}

		new Merge(args);
	}
	String path = "";
	String language = "";
	boolean recursive = false;
	int maxThreads = 1;
	PluginInterface plugin = new JavaPlugin();
	private int minMatch;
	private double minPer;

	private double minPerDelta;

	/**
	 * Initializes the Merge class TODO Annotate constructor
	 */
	public Merge(String[] args) {
		parseOptions(args);
		// get list of files
		final ArrayList<File> files = getFiles(new File(path));
		maxThreads = Math.min(maxThreads, files.size());
		// dish out to threads

		final ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
		final long startTime = System.nanoTime();
		if (Helper.verbose) {
			System.out.println("Reading files and distributing threads");
		}
		for (final File f : files) {
			Helper.printToSTD("Generating thread for " + f.getName(), "Main");
			final MergeThread t = new MergeThread(f, plugin.generateInstance(), minMatch, minPer, minPerDelta);
			executor.execute(t);
		}
		Helper.printToSTD("Thread Assigned, Waiting for completion", "Main");
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		long duration = System.nanoTime() - startTime;
		if (duration < 1000000000) {
			System.out.println("Done in " + duration + "ns");
		} else {
			duration = TimeUnit.SECONDS.convert(duration, TimeUnit.NANOSECONDS);
			System.out.printf("Done in %02d:%02d:%02d%n", duration / 3600, duration % 3600 / 60, duration % 60);
		}

	}

	private ArrayList<File> getFiles(File root) {
		final ArrayList<File> returnList = new ArrayList<>();
		if (root.isDirectory()) {
			if (Helper.verbose) {
				System.out.println("Searching for files in \"" + root.getAbsolutePath() + "\"");
			}
			for (final File f : root.listFiles()) {
				if (f.isFile() && plugin.validfile(f.getName())) {
					if (Helper.verbose) {
						System.out.println("found file \"" + f.getName() + "\"");
					}
					returnList.add(f);
				} else if (recursive) {
					returnList.addAll(getFiles(f));
				}
			}
		} else {
			returnList.add(root);
		}
		return returnList;
	}

	private void parseOptions(String[] args) {

		minMatch = 1;
		minPer = 0.2;
		minPerDelta = 0;

		final Options options = new Options();

		final Option pathOpt = new Option("p", "path", true, "Path to the file or directory the code is in.");
		pathOpt.setRequired(true);
		pathOpt.setArgName("path");
		options.addOption(pathOpt);

		final Option languageOpt = new Option("l", "language", true, "Language the code is written in.");
		languageOpt.setRequired(true);
		languageOpt.setArgName("language");
		options.addOption(languageOpt);

		options.addOption("r", "recursive", false, "Locate files in specified path recursevly.");
		options.addOption("t", "max-threads", true,
				"Maximum number of threads to use.\nThreads used = min(max-threads, num files).\nDefault is 1.");
		options.addOption("s", "minPer", true, "Minimum percentage of body blocks that must be shared.");
		options.addOption("d", "minPerDelta", true, "Added to minPer to compare the larger method.\nDefault is 0");
		options.addOption("m", "minMatch", true, "minimum number of statemetns to consititute a match.\n Defualt is 1");
		options.addOption("v", "verbose", false, "display logging information");

		final CommandLineParser parser = new DefaultParser();
		try {
			final CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("path")) {
				path = cmd.getOptionValue("path");
			}

			if (cmd.hasOption("language")) {
				language = cmd.getOptionValue("language");
			}

			if (cmd.hasOption('r')) {
				recursive = true;
			}

			if (cmd.hasOption('v')) {
				Helper.verbose = true;
			}

			if (cmd.hasOption('t')) {
				try {
					maxThreads = Integer.parseInt(cmd.getOptionValue('t'));
				} catch (final NumberFormatException e) {
					System.out.println("Error: Thread parameter must be a positive integer");
					showUsageExit(options);
				}
			}

			if (cmd.hasOption('s')) {
				try {
					minPer = Double.parseDouble(cmd.getOptionValue('s'));
					if (minPer <= 0 || minPer > 1)
						throw new NumberFormatException();
				} catch (final NumberFormatException e) {
					System.out.println("Error: MinPer parameter must be in range 0>minPer>=1");
					showUsageExit(options);
				}
			}

			if (cmd.hasOption('d')) {
				try {
					minPerDelta = Double.parseDouble(cmd.getOptionValue('d'));
					if (minPerDelta + minPer <= 0 || minPerDelta + minPer > 1)
						throw new NumberFormatException();
				} catch (final NumberFormatException e) {
					System.out.println("Error: MinPerDelta parameter must be in range 0>minPerDelta-minPer>=1");
					showUsageExit(options);
				}
			}

			if (cmd.hasOption('m')) {
				try {
					minMatch = Integer.parseInt(cmd.getOptionValue('m'));
				} catch (final NumberFormatException e) {
					System.out.println("Error: MinMatch parameter must be a positive integer");
					showUsageExit(options);
				}
			}

		} catch (final ParseException e) {
			if (e.getClass() != MissingOptionException.class) {
				e.printStackTrace();
			}

			showUsageExit(options);
		}
	}

	private void showUsageExit(Options options) {
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("CPMerge [options] -l language -p path", options);
		System.exit(0);
	}

	/*
	 * TODO: Create class for iteration search input ast for close matches
	 */

}
