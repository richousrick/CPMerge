package ref;

/**
 * TODO Annotate class
 *
 * @author Rikkey Paal
 */
public class FunctionPos {

	private final int startLine;
	private final int endLine;

	/**
	 * Initializes the FunctionPos class TODO Annotate constructor
	 *
	 * @param startLine
	 * @param endLine
	 */
	public FunctionPos(int startLine, int endLine) {
		this.startLine = startLine;
		this.endLine = endLine;
	}

	/**
	 * @return the startLine
	 */
	public int getStartLine() {
		return startLine;
	}

	/**
	 * @return the endLine
	 */
	public int getEndLine() {
		return endLine;
	}

}
