package util;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Jeff Svajlenko
 * Represents a clone pair, containing two code fragments.
 */
public class Clone {

	/**
	 * Constant specifying the clone's type is unknown or unspecified.
	 */
	public final static int TYPE_UNSPECIFIED = 0;
	
	/**
	 * Constant specifying the clone is type 1.
	 */
	public final static int TYPE_1 = 1;
	
	/**
	 * Constant specifying the clone is type 2.
	 */
	public final static int TYPE_2 = 2;
	
	/**
	 * Constant specifying the clone is type 3.
	 */
	public final static int TYPE_3 = 3;
	
	
	/** Fragment 1 */
	private Fragment fragment1;
	
	/** Fragment 2 */
	private Fragment fragment2;
	
	
	/** The clone's type */
	private int type;
	
	
	/**
	 * Creates a new clone.
	 * @param fragment1 The first fragment.
	 * @param fragment2 The second fragment.
	 * @param type The clone's type.  Set by one of the TYPE_* constants.
	 * @throws IllegalArgumentException If one of the specified fragments is null, or if the type is invalid.
	 */
	public Clone(Fragment fragment1, Fragment fragment2, int type) {
	//Check Parameters
		//Check parameters not null
		Objects.requireNonNull(fragment1);
		Objects.requireNonNull(fragment2);
		
		//Check valid type
		if(type != TYPE_UNSPECIFIED && type != TYPE_1 && type != TYPE_2 && type != TYPE_3) {
			throw new IllegalArgumentException("Clone's type is invalid.");
		}
		
		//Fragments must share a specification
		if( (fragment1.isLineSpecified() && fragment2.isLineSpecified() ) || 
				(fragment1.isTokenSpecified() && fragment2.isTokenSpecified()) ) {
			throw new IllegalArgumentException("Fragments must have the same specification.");
		}
		
	//Initialize Object
		if(fragment1.compareTo(fragment2) < 0) {
			this.fragment1 = fragment1;
			this.fragment2 = fragment2;
		} else {
			this.fragment1 = fragment2;
			this.fragment2 = fragment1;
		}
		
		this.type = type;
	}
	
	/**
	 * Creates a new clone.
	 * @param fragment1 The first fragment.
	 * @param fragment2 The second fragment.
	 * @param type The clone's type.  Set by one of the TYPE_* constants.
	 * @throws IllegalArgumentException If one of the specified fragments is null, or if the type is invalid.
	 */
	public Clone(Fragment fragment1, Fragment fragment2) {
		this(fragment1, fragment2, Clone.TYPE_UNSPECIFIED);
	}
	
	/**
	 * Creates a new clone.
	 * @param srcfile1
	 * @param startline1
	 * @param endline1
	 * @param srcfile2
	 * @param startline2
	 * @param endline2
	 */
	public Clone(Path srcfile1, int startline1, int endline1, Path srcfile2, int startline2, int endline2) {
		this(Fragment.createFragmentByLines(srcfile1, startline1, endline1), Fragment.createFragmentByLines(srcfile2, startline2, endline2));
	}
	
	/**
	 * Creates a new clone.
	 * @param srcfile1
	 * @param startline1
	 * @param endline1
	 * @param srcfile2
	 * @param startline2
	 * @param endline2
	 * @param type
	 */
	public Clone(Path srcfile1, int startline1, int endline1, Path srcfile2, int startline2, int endline2, int type) {
		this(Fragment.createFragmentByLines(srcfile1, startline1, endline1), Fragment.createFragmentByLines(srcfile2, startline2, endline2), type);
	}
		
	/**
	 * Returns the first code fragment.
	 * @return the first code fragment.
	 */
	public Fragment getFragment1() {
		return this.fragment1;
	}
	
	/**
	 * Returns the second code fragment.
	 * @return the second code fragment.
	 */
	public Fragment getFragment2() {
		return this.fragment2;
	}
	
	public boolean isLineBoundariesSpecified() {
		if(fragment1.isLineSpecified() && fragment2.isLineSpecified())
			return true;
		else
			return false;
	}
	
	public boolean isTokenBoundariesSpecified() {
		if(fragment1.isTokenSpecified() && fragment2.isTokenSpecified())
			return true;
		else
			return false;
	}
	
	public Path getFile1() {
		return this.fragment1.getFile();
	}
	
	public Path getFile2() {
		return this.fragment2.getFile();
	}
	
	public int getStartLine1() {
		return this.fragment1.getStartLine();
	}
	
	public int getStartLine2() {
		return this.fragment2.getStartLine();
	}
	
	public int getStartToken1() {
		return this.fragment1.getStartToken();
	}
	
	public int getStartToken2() {
		return this.fragment2.getStartToken();
	}
	
	public int getEndLine1() {
		return this.fragment1.getEndLine();
	}
	
	public int getEndLine2() {
		return this.fragment2.getEndLine();
	}
	
	public int getEndToken1() {
		return this.fragment1.getEndToken();
	}
	
	public int getEndToken2() {
		return this.fragment2.getEndToken();
	}
	
	/**
	 * Returns the clone's type.
	 * @return the clone's type.
	 */
	public int getType() {
		return this.type;
	}
	
	public boolean equals(Object obj) {
		//If obj null, then false
		if(obj == null) {
			return false;
		}
		
		//If obj not the same type as this, then false
		if(!this.getClass().equals(obj.getClass())) {
			return false;
		}
		
		//Cast and check
		Clone clone = (Clone) obj;		
		if(this.fragment1.equals(clone.fragment1) && this.fragment2.equals(clone.fragment2)) {
			return true;
		} else {
			return false;
		}
	}
	
	public String toString() {
		String string;
		string = "Fragment 1:" + this.fragment1 + " Fragment 2:" + this.fragment2 + " Type: " + this.type;
		return string;
	}
}