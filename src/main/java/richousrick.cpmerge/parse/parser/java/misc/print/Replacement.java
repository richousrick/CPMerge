package richousrick.cpmerge.parse.parser.java.misc.print;

import richousrick.cpmerge.ref.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

public class Replacement implements Comparable<Replacement> {
	final int start;
	final int end;
	private String replacement;
	private final String originalRepresentation;
	Replacement enclosingReplacement;
	final String path;

	/**
	 * Initializes the replacement class
	 * TODO Annotate constructor
	 *
	 * @param start
	 * @param end
	 * @param replacement
	 */
	public Replacement(int start, int end, String replacement, String original, String path) {
		this.start = start;
		this.end = end;
		this.replacement = replacement;
		originalRepresentation = original;
		this.path = path;
	}

	/**
	 * TODO Annotate method
	 */
	public void updateEnclosingText(boolean overriteEmptyEdits) {
		if (enclosingReplacement.getReplacement().contains(originalRepresentation)) {
			enclosingReplacement.updateReplacement(originalRepresentation, replacement);
		} else if (overriteEmptyEdits && enclosingReplacement.getReplacement().length() == 0) {
			enclosingReplacement.replacement = replacement;
		} else {
			Helper.exitProgram("Edit colision detected");
		}

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Replacement o) {
		int enc = checkEnclosing(this, o);
		switch (enc) {
			case 0:
				return Integer.compare(start, o.start);
			case 1:
				return -1;
			case 2:
				return 1;
		}

		// not reachable
		return 0;
	}


	private void updateReplacement(String original, String replacement){
		this.replacement = this.replacement.replaceFirst(Pattern.quote(original), replacement);
	}

	void setEnclosingReplacement(Replacement rep) {
		if (enclosingReplacement == null) {
			enclosingReplacement = rep;
		} else {
			// if already has enclosing Replacement
			// set enclosing replacement to the smaller
			if (rep.getSize() < enclosingReplacement.getSize()) {
				enclosingReplacement = rep;
			}
		}
	}

	int getSize(){
		return end - start;
	}


	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @return the end
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * @return the replacement
	 */
	public String getReplacement() {
		return replacement;
	}

	boolean hasEnclosingReplacement(){
		return enclosingReplacement != null;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + start + "," + end + "] " + path + "." + originalRepresentation + " > " + replacement;
	}

	public static int checkEnclosing(Replacement r1, Replacement r2) {
		// see if the smallest replacement is contained within the other
		if (r1.getSize() < r2.getSize()) {
			if (r1.start >= r2.start && r1.end <= r2.end) {
				r1.setEnclosingReplacement(r2);
				return 2;
			}
		} else {
			if (r2.start >= r1.start && r2.end <= r1.end) {
				r2.setEnclosingReplacement(r1);
				return 1;
			}
		}
		return 0;
	}

	public static String replace(ArrayList<Replacement> replacements, String string, boolean verbose) {
		return Replacement.replace(replacements, string, verbose, false);
	}

	public static String replace(ArrayList<Replacement> replacements, String string, boolean verbose,
			boolean renameMatches) {
		Collections.sort(replacements, Collections.reverseOrder());
		for (Replacement replacement : replacements) {
			// update final String
			if (replacement.hasEnclosingReplacement()) {
				replacement.updateEnclosingText(renameMatches);
			} else {
				if (verbose) {
					Helper.printToSTD("\t" + replacement.toString(), false);
				}
				String start = string.substring(0, replacement.start);
				String end = string.substring(replacement.end + 1);
				string = start + replacement.getReplacement() + end;
			}
		}
		return string;
	}
}