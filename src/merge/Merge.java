package merge;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import parse.PluginInterface;
import parse.parser.java.JavaPlugin;
import ref.Helper;
import ref.Language;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public class Merge {

	public static void main(String[] args) {
//		final boolean runOnSmall = true;
//		if (runOnSmall) {
//			args = new String[] { "-rkl", "Java", "-p",
//					"Test\\com\\", "-t",
//			"12" };
//		} else {
//			args = new String[] { "-rl", "Java", "-p",
//					"E:\\University\\Workspace\\CPMerge\\src\\parse\\parser\\java\\comp\\JavaParser.java", "-t", "12" };
//		}
		new Merge(args);
	}

	String path = "";
	String language = "";
	boolean recursive = false;
	int maxThreads = 1;
	// 1 hour
	long timeout = 3600000000000L;

	private double minPer;

	PluginInterface<?> plugin;

	/**
	 * Initializes the Merge class TODO Annotate constructor
	 */
	public Merge(String[] args) {
		parseOptions(args);

		plugin = Helper.getLanguageInstance().generatePluginInstance();

		// get list of files
		final ArrayList<File> files = getFiles(new File(path));
		maxThreads = Math.min(maxThreads, files.size());
		// dish out to threads

		final long startTime = System.nanoTime();
		if (Helper.verbose) {
			System.out.println("Reading files and distributing threads");
		}

		ArrayList<MergeThread<?>> threads = mergeFiles(files);

		threads = extractReferences(threads);



		// TODO update references

		Helper.disableForceExit();
		saveUpdatedFiles(files);

		long duration = System.nanoTime() - startTime;
		if (duration < 1000000000) {
			System.out.println("Done in " + duration + "ns");
		} else {
			duration = TimeUnit.SECONDS.convert(duration, TimeUnit.NANOSECONDS);
			System.out.printf("Done in %02d:%02d:%02d%n", duration / 3600, duration % 3600 / 60, duration % 60);
		}

	}

	/**
	 * TODO Annotate method
	 *
	 * @param threads
	 * @return
	 */
	private ArrayList<MergeThread<?>> extractReferences(ArrayList<MergeThread<?>> threads) {
		if (Helper.getUpdatedFunctions().size() != 0) {
			ArrayList<Runnable> initRunnable = new ArrayList<>();
			ArrayList<Runnable> mainRunnable = new ArrayList<>();
			ArrayList<Runnable> postRunnable = new ArrayList<>();
			for (MergeThread<?> t : threads) {
				initRunnable.add(new Runnable() {
					@Override
					public void run() {
						t.initUpdateReferences();
					}
				});

				mainRunnable.add(new Runnable() {
					@Override
					public void run() {
						t.updateReferences();
					}
				});

				postRunnable.add(new Runnable() {
					@Override
					public void run() {
						t.postUpdateReferences();
					}
				});
			}

			processThreads(initRunnable, "timed out initalising reference updating", timeout);
			Helper.printToSTD("Updating References", false);
			Helper.printToSTD("\t[startPos,endPos] callPath.reference > newReference", false);
			processThreads(mainRunnable, "timed out updating the references", timeout);
			processThreads(postRunnable, "timed out cleaning up after updating the references", timeout);

		}
		return threads;
	}

	private ArrayList<MergeThread<?>> mergeFiles(ArrayList<File> files) {

		ArrayList<MergeThread<?>> threads = new ArrayList<>();
		for (final File f : files) {
			Helper.printToSTD("Generating thread for " + f.getName(), "Main");
			final MergeThread<?> t = Helper.getLanguageInstance().generateMergeThreadInstance(f, minPer);
			threads.add(t);
		}

		processThreads(threads, "Parsing files timed out", timeout);

		Helper.printToSTD("Merged Functions", false);
		for (MergedFunction<?> r : Helper.getUpdatedFunctions()) {
			Helper.printToSTD("\t" + r.toString().replaceAll("\n", "\n\t"), false);
		}

		return threads;
	}

	/**
	 * Processes the runnables, if they take longer than the timeout specified
	 * then they will print the timeoutMsg and kill the process
	 *
	 * @param runnables
	 *            to be processed
	 * @param timeoutMsg
	 *            to print if the timeout is exceeded
	 * @param timeout
	 *            max ms to wait for the runnables to complete
	 */
	private void processThreads(ArrayList<? extends Runnable> runnables, String timeoutMsg, long timeout) {
		ExecutorService executor = null;
		try {
			executor = Executors.newFixedThreadPool(Math.max(runnables.size(), maxThreads));
		} catch (Throwable e) {
			Helper.exitProgram(e.getMessage());
		}
		for (Runnable runnable : runnables) {
			executor.execute(new Thread(runnable));
		}
		executor.shutdown();
		try {
			executor.awaitTermination(timeout, TimeUnit.NANOSECONDS);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		if (!executor.isTerminated()) {
			System.err.println(timeoutMsg);
			System.exit(0);
		}

	}

	private String backupFiles(ArrayList<File> files) {
		String containerName;
		File tmp = new File(path);
		if (tmp.isDirectory()) {
			containerName = tmp.getPath();
		} else {
			containerName = tmp.getAbsoluteFile().getParent();
		}

		String backupDirName = containerName + "/.CPMerge_Backup/"
				+ new Timestamp(System.currentTimeMillis()).toString().replaceAll(" ", "_").replaceAll(":", "-") + "/";

		boolean sucessful = new File(backupDirName).mkdirs();

		if (sucessful) {
			for (File f : files) {
				File newFile = new File(backupDirName + f.getName());
				try {
					Files.move(f.toPath(), newFile.toPath());
				} catch (IOException e) {
					e.printStackTrace();
					sucessful = false;
				}

				if (!sucessful) {
					break;
				}
			}
		}
		return sucessful ? backupDirName : null;

	}

	private boolean saveUpdatedFiles(ArrayList<File> files) {
		String backupLocation = backupFiles(files);
		boolean sucessful = backupLocation != null;
		if (sucessful) {
			// update files
			FileWriter out;
			for (Entry<String, String> file : Helper.getSourceFiles().entrySet()) {
				File f = new File(file.getKey());
				try {
					sucessful = f.createNewFile();
					if (sucessful) {
						out = new FileWriter(f);
						out.write(file.getValue());
						out.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
					sucessful = false;
				}

				if (!sucessful) {
					break;

				}

			}
		}

		return sucessful;
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
				} else if (recursive && f.isDirectory()) {
					if (!f.getName().equals(".CPMerge_Backup")) {
						returnList.addAll(getFiles(f));
					}
				}
			}
		} else {
			returnList.add(root);
		}
		return returnList;
	}

	private void parseOptions(String[] args) {
		language = "java";
		minPer = 0.2;

		final Options options = new Options();

		final Option pathOpt = new Option("p", "path", true, "Path to the file or directory the code is in.");
		pathOpt.setRequired(true);
		pathOpt.setArgName("path");
		options.addOption(pathOpt);

		final Option languageOpt = new Option("l", "language", true, "Language the code is written in.");
		languageOpt.setRequired(true);
		languageOpt.setArgName("language");
		options.addOption(languageOpt);

		options.addOption("r", "recursive", false, "Locate files in specified path recursively.");
		options.addOption("t", "max-threads", true,
				"Maximum number of threads to use.\nThreads used = min(max threads, num files).\nDefault is 1.");
		options.addOption("s", "min-per", true, "Minimum percentage of body blocks that must be shared.");
		options.addOption("v", "verbose", false, "Display additional logging information.");
		options.addOption("k", "remove-functions", false, "Remove the original copies of the merged functions.");
		options.addOption("c", "timeout", true, "Number of nanoseconds to wait for merge.");

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

			if (cmd.hasOption('k')) {
				Helper.deleteOldFunctions = true;
			}

			if (cmd.hasOption('t')) {
				try {
					maxThreads = Integer.parseInt(cmd.getOptionValue('t'));
				} catch (final NumberFormatException e) {
					System.out.println("Error: Thread parameter must be a positive integer");
					showUsageExit(options);
				}
			}

			if (cmd.hasOption('c')) {
				try {
					timeout = Long.parseLong(cmd.getOptionValue("timeout"));
				} catch (final NumberFormatException e) {
					System.out.println("Error: Timeout parameter must be a positive integer");
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

		} catch (final ParseException e) {
			showUsageExit(options);
		}

		setLanguage();
	}

	private void showUsageExit(Options options) {
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("CPMerge [options] -l <language> -p <path to files>", options);
		System.exit(0);
	}

	private void setLanguage() {
		switch (language.toLowerCase()) {
			case "java":
				// change ParserRuleContext to whatever type the
				Helper.setLanguageInstance(new Language<ParserRuleContext>() {

					@Override
					public PluginInterface<ParserRuleContext> generatePluginInstance() {
						return new JavaPlugin();
					}

				});
				break;
			default:
				Helper.exitProgram("Invalid language specified");
		}
	}


}
