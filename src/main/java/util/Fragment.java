package util;

import java.nio.file.Path;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Jeff Svajlenko
 * A source code fragment.  Specified by source file and boundaries.  Boundaries can be specified by line and/or by token.
 *
 */
public class Fragment implements Comparable<Fragment> {
	/** The file containing the fragment */
	private Path srcFile;
	
	/** The start line of the fragment (inclusive) */
	private int startLine;
	
	/** The start token of the fragment (inclusive) */
	private int startToken;
	
	/** The end line of the fragment (inclusive) */
	private int endLine;
	
	/** The end token of the fragment (inclusive) */
	private int endToken;
	
	/**
	 * 
	 * @param srcfile The source file containing the fragment.  Path is modified by toAbsolutepath() and normalize() before storage.
	 * @param startLine
	 * @param startToken
	 * @param endLine
	 * @param endToken
	 */
	private Fragment(Path srcFile, int startLine, int startToken, int endLine, int endToken) {
		Objects.requireNonNull(srcFile);
		
		boolean isLines = false;
		boolean isTokens = false;
		
		if(endLine != 0 && startLine != 0) {
			isLines = true;
			if(endLine < startLine) {
				throw new IllegalArgumentException("Fragment created with invalid start/end lines.");
			}
		}
		
		if(startToken != 0 && endToken != 0) {
			isTokens = true;
			if(endToken < startToken) {
				throw new IllegalArgumentException("Fragment created with invalid start/end tokens.");
			}
		}
		
		if(!isLines && !isTokens) {
			throw new IllegalArgumentException("Fragment must be specified by either start/end lines or start/end tokens (or both).");
		}
		
		this.srcFile = srcFile.toAbsolutePath().normalize();
		this.startLine = startLine;
		this.endLine = endLine;
		this.startToken = startToken;
		this.endToken = endToken;
	}
	
	/**
	 * Creates a source fragment with line boundaries.
	 * @param srcFile
	 * @param startLine
	 * @param endLine
	 * @return the fragment.
	 * @throws IllegalArgumentException If boundaries are invalid.  Start line must precede (or be the same line as) the end line.
	 */
	public static Fragment createFragmentByLines(Path srcFile, int startLine, int endLine) {
		return new Fragment(srcFile, startLine, 0, endLine, 0);
	}
	
	/**
	 * Creates a source fragment with token boundaries.
	 * @param srcfile
	 * @param startToken
	 * @param endToken
	 * @return the fragment.
	 * @throws IllegalArgumentException if boundaries are invalid.  Start token must precede (or be the same token) as the end token.
	 */
	public static Fragment createFragmentByTokens(Path srcfile, int startToken, int endToken) {
		return new Fragment(srcfile, 0, startToken, 0, endToken);
	}
	
	/**
	 * Creates a source fragment with both line and token boundaries.
	 * @param srcfile
	 * @param startLine
	 * @param startToken
	 * @param endLine
	 * @param endToken
	 * @return the fragment.
	 * @throws IllegalArgumentException if boundaries are invalid.  Start line/token must precede (or be the same line/token) as the end line/token.
	 */
	public static Fragment createFragmentByLineAndToken(Path srcfile, int startLine, int startToken, int endLine, int endToken) {
		return new Fragment(srcfile, startLine, startToken, endLine, endToken);
	}
	
	/**
	 * Returns the file containing the fragment.  May not be absolute.
	 * @return The file containing the fragment.
	 */
	public Path getFile() {
		return srcFile;
	}
	
	/**
	 * Returns the start line of the fragment.
	 * @return The fragment's start line.
	 * @throws UnsupportedOperationException if the fragment does not specify line boundaries.
	 */
	public int getStartLine() {
		if(startLine != 0)
			return startLine;
		else
			throw new UnsupportedOperationException("Fragment does not specify line boundaries.");
	}
	
	/**
	 * Returns the start token of the fragment.
	 * @return the start token of the fragment.
	 * @throws UnsupportedOperationException if the fragment does not specify token boundaries.
	 */
	public int getStartToken() {
		if(startToken != 0)
			return startToken;
		else
			throw new UnsupportedOperationException("Fragment does not specify token boundaries.");
	}
	
	/**
	 * Returns the end line of the fragment.
	 * @return The fragment's end line.
	 * @throws UnsupportedOperationException if the fragment does not specify line boundaries.
	 */
	public int getEndLine() {
		if(endLine != 0)
			return endLine;
		else
			throw new UnsupportedOperationException("Fragment does not specify line boundaries.");
	}
	
	/**
	 * Returns the end token.
	 * @return the end token.
	 * @throws UnsupportedOperationException if the fragment does not specify token boundaries.
	 */
	public int getEndToken() {
		if(endToken != 0)
			return endToken;
		else
			throw new UnsupportedOperationException("Fragment does not specify token boundaries.");
	}
	
	/**
	 * Returns if this fragment is specified by line boundaries.
	 * @return if this fragment is specified by line boundaries.
	 */
	public boolean isLineSpecified() {
		if(startLine != 0 && endLine != 0)
			return true;
		else 
			return false;
	}
	
	/**
	 * Returns if this fragment is specified by token boundaries.
	 * @return if this fragment is specified by token boundaries.
	 */
	public boolean isTokenSpecified() {
		if(startToken != 0 && endToken != 0)
			return true;
		else
			return false;
	}
	
	/**
	 * Returns the length of the fragment in lines.
	 * @return the length of the fragment in lines.
	 * @throws UnsupportedOperationException If the fragment is not specified by line boundaries (check using isLineSpecified()).
	 */
	public int getNumberOfLines() {
		if (isLineSpecified())
			return this.endLine - this.startLine + 1;
		else
			throw new UnsupportedOperationException("getNumberOfLines is unsupported when the fragment is not specified by lines.");
	}
	
	/**
	 * Returns the length of the fragment in tokens.
	 * @return the length of the fragment in tokens.
	 * @throws UnsupportedOperationException If the fragment is not specified by token boundaries (check using istokenSpecified()).
	 */
	public int getNumberOfTokens() {
		if (isTokenSpecified())
			return this.endToken - this.startToken + 1;
		else
			throw new UnsupportedOperationException("getNumberOfTokens is unsupported when the fragment is not specified by tokens.");
	}
	
	/**
	 * Returns if this fragment subsumes another given a specified threshold.
	 * The specification of this and another fragment must be compatible, i.e., both must be specified either by line or by token, with token taking preference.
	 * @param another
	 * @param threshold The subsume threshold, specified by the number of lines or tokens.
	 * @return
	 */
	public boolean subsumes(Fragment another, int threshold){
		Objects.requireNonNull(another);
		if(threshold < 0) {throw new IllegalArgumentException("relaxer must be >= 0.");}
		if (this.isTokenSpecified() && another.isTokenSpecified()) { // If Token Specification Compatible
			if(this.getFile().toAbsolutePath().normalize().equals(another.getFile().toAbsolutePath().normalize()) 
					&& this.startLine - threshold <= another.getStartLine() 
					&& this.getEndLine() + threshold >= another.getEndLine()){
				return true;
			} else 
				return false;
		} else if (this.isLineSpecified() && another.isLineSpecified()) { // If Line Specification Compatible
			if(this.getFile().toAbsolutePath().normalize().equals(another.getFile().toAbsolutePath().normalize())
					&& this.startToken - threshold <= another.getStartToken()
					&& this.endToken + threshold >= another.getEndToken()) {
				return true;
			} else
				return false;
		} else { //if incompatible
			throw new IllegalArgumentException("Can not determine if this fragment subsumes another with an incompatible specification.");
		}
	}
	
	/**
	 * Returns if this fragment subsumes another given a specified tolerance.
	 * The specification of this and another fragment must be compatible, i.e., both must be specified either by line or by token, with token taking preference.
	 * @param another
	 * @param tolerance
	 * @throws IllegalArgumentException If the specification of this and another fragment are incompatible.
	 * @return
	 */
	public boolean subsumes(Fragment another, double tolerance) {
		Objects.requireNonNull(another);
		if(tolerance < 0.0 || tolerance > 1.0) {
			throw new IllegalArgumentException("Tolerance must be a percetange ( 0.0 <= x <= 1.0 )");
		}
		
		int size = another.endLine - another.startLine + 1;
		int relaxer = (int)(size * tolerance);
		
		return subsumes(another, relaxer);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) { return false; }
		
		Fragment rhs = (Fragment) obj;
		return new EqualsBuilder()
						//.appendSuper(super.equals(obj))
						.append(this.srcFile, rhs.srcFile)
						.append(this.startLine, rhs.startLine)
						.append(this.endLine, rhs.endLine)
						.append(this.startToken, rhs.startToken)
						.append(this.endToken, rhs.endToken)
						.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
						.append(this.srcFile)
						.append(this.startLine)
						.append(this.endLine)
						.append(this.startToken)
						.append(this.endToken)
						.toHashCode();
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
						.append("srcFile", this.srcFile)
						.append("startLine", this.startLine)
						.append("endLine", this.endLine)
						.append("startToken", this.startToken)
						.append("endToken", this.endToken)
						.toString();
	}

	/**
	 * Compares this fragment to other.  Only possible if both fragments have compatible specifications 
	 * (i.e., both have either line or token specification).  Comparison prefers token specification if possible.
	 * @param other
	 * @return
	 */
	@Override
	public int compareTo(Fragment other) {
		Objects.requireNonNull(other);
		
		if(this.isTokenSpecified() && other.isTokenSpecified()) {
			if(this.getFile().compareTo(other.getFile()) < 0) {
				return -1;
			} else if (this.getFile().compareTo(other.getFile()) > 0) {
				return 1;
			} else { // this.getStartToken() == o.getStartToken()
				if(this.getStartToken() < other.getStartToken()) {
					return -1;
				} else if (this.getStartToken() > other.getStartToken()) {
					return 1;
				} else { // this.getStartToken() == o.getStartToken()
					if (this.getEndToken() < other.getEndToken()) {
						return -1;
					} else if (this.getEndToken() > other.getEndToken()) {
						return 1;
					} else { // this.getEndToken() == o.getEndToken()
						return 0;
					}
				}
			}
		} else if (this.isLineSpecified() && other.isLineSpecified()) {
			if(this.getFile().compareTo(other.getFile()) < 0) {
				return -1;
			} else if (this.getFile().compareTo(other.getFile()) > 0) {
				return 1;
			} else { // this.getSrcFile() == o.getSrcFile()
				if(this.getStartLine() < other.getStartLine()) {
					return -1;
				} else if (this.getStartLine() > other.getStartLine()) {
					return 1;
				} else { // this.getStartLine() == o.getStartLine()
					if (this.getEndLine() < other.getEndLine()) {
						return -1;
					} else if (this.getEndLine() > other.getEndLine()) {
						return 1;
					} else { // this.getEndLine() == o.getEndLine()
						return 0;
					}
				}
			}
		} else {
			throw new IllegalArgumentException("Can not compare Fragments with incompatible specifications.");
		}
	}
}