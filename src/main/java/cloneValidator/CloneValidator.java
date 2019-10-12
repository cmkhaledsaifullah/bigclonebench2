package cloneValidator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

import util.FileUtil;
import txl.TXLException;
import txl.TXLUtil;
import util.Clone;
import util.FragmentUtil;
import util.TokenIterator;

/**
 * author: Jeff Svajlenko
 */

public class CloneValidator {
	
// The Clone
	private Clone clone;
	private int language;
	
// Measured Information
	//type information
	private int type = Clone.TYPE_UNSPECIFIED;
	
	//similarity
	private double token_upi1 = -1;
	private double token_upi1_type2Normalized = -1;
	private double token_upi2 = -1;
	private double token_upi2_type2Normalized = -1;
	private double line_upi1 = -1;
	private double line_upi1_prettyPrinted = -1;
	private double line_upi1_prettyPrinted_type2Normalized = -1;
	private double line_upi2 = -1;
	private double line_upi2_prettyPrinted = -1;
	private double line_upi2_prettyPrinted_type2Normalized = -1;
	
	//iterators
	private TokenIterator tokenIterator_originalFile1;
	private TokenIterator tokenIterator_originalFile2;
	private TokenIterator tokenIterator_type2NormalizedFile1;
	private TokenIterator tokenIterator_type2NormalizedFile2;
	
	//expansions
		//raw expansion type 1
	private Clone type1Expansion_raw = null;
	
		//raw expansion type 2
	private Clone type2Expansion_raw = null;
	
	
	//trim
	private Clone type1Expansion_balancedBraces;
	private Clone type2Expansion_balancedBraces;
	
	//perfect
	private Clone type1Expansion_functionReplacable;
	private Clone type2Expansion_functionReplacable;

	
	//replacable by function

	
	
// State Information 
	
// Working Files
	private Path extractedFragment1;
	private Path extractedFragment2;
	private Path tokenizedFragment1;
	private Path tokenizedFragment2;
	private Path type2NormalizedTokenizedFragment1;
	private Path type2NormalizedTokenizedFragment2;
	private Path prettyPrintedFragment1;
	private Path prettyPrintedFragment2;
	private Path type2NormalizedPrettyPrintedFragment1;
	private Path type2NormalizedPrettyPrintedFragment2;
	private Path prettyPrintedFile1;
	private Path prettyPrintedFile2;
	private Path type2NormalizedFile1;
	private Path type2NormalizedFile2;
	
// Type 2 Normalization
	private boolean normalizeIdentifiers = true;
	private boolean normalizeLiterals = true;
	private boolean primitivesAreIdentifiers = true;
	private boolean isLiteralNormalizationBlind = true;
	
// Temporary Data 
	
	protected CloneValidator(Clone clone, int language) {
		if(!TXLUtil.isLanguageSupported(language)) {
			throw new IllegalArgumentException("Langauge is not supported.");
		}
		
		this.clone = clone;
		
		this.language = language;
		
	}
	
	protected CloneValidator(Clone clone, int language, boolean normalizeIdentifiers, boolean normalizeLiterals, boolean primitivesAreIdentifiers) {
		this(clone, language);
		this.normalizeIdentifiers = normalizeIdentifiers;
		this.normalizeLiterals = normalizeLiterals;
		this.primitivesAreIdentifiers = primitivesAreIdentifiers;
	}
	
	public void cleanUp() {
		
	}
	
	/**
	 * Returns the type of this clone as determined by the validator.
	 * @return the type of this clone.
	 * @throws IOException If an IO exception occurs while processing the files.
	 * @throws TXLException 
	 * @throws InterruptedException 
	 */
	public int getType() throws IOException, InterruptedException, TXLException {
		if(type == Clone.TYPE_UNSPECIFIED)
			typify();
		return type;
	}

// --- UPI Functions --------------------------------------------------------------------------------------------------------------------------------
	
	// --- Token ------------------------------------------------------------------------------------------------------------------------------------
	
	public double getTokenUPI1_originalFragments() throws FileNotFoundException, IOException, InterruptedException, TXLException {
		if (token_upi1 < 0)
			token_upi1 = FileUtil.getUPL(getTokenizedFragment1(), getTokenizedFragment2());
		return token_upi1;
	}
	
	public double getTokenUPI1_type2NormalizedFragments() throws FileNotFoundException, IOException, InterruptedException, TXLException {
		if (token_upi1_type2Normalized < 0) {
			token_upi1_type2Normalized = FileUtil.getUPL(getType2NormalizedTokenizedFragment2(), getType2NormalizedTokenizedFragment1());
		}
		return token_upi1_type2Normalized;
	}
	
	public double getTokenUPI2_originalFragments() throws FileNotFoundException, IOException, InterruptedException, TXLException {
		if (token_upi2 < 0)
			token_upi2 = FileUtil.getUPL(getTokenizedFragment2(), getTokenizedFragment1());
		return token_upi2;
	}
	
	public double getTokenUPI2_type2NormalizedFragments() throws FileNotFoundException, IOException, InterruptedException, TXLException {
		if (token_upi2_type2Normalized < 0) {
			token_upi2_type2Normalized = FileUtil.getUPL(getType2NormalizedTokenizedFragment2(), getType2NormalizedTokenizedFragment1());
		}
		return token_upi2_type2Normalized;
	}
	
	// --- Line -------------------------------------------------------------------------------------------------------------------------------------
	
	public double getLineUPI1_originalFragments() throws FileNotFoundException, IOException {
		if (line_upi1 < 0)
			line_upi1 = FileUtil.getUPL(this.getExtractedFragment1(), this.getExtractedFragment2());
		return line_upi1;
	}
	
	public double getLineUPI1_prettyPrintedFragments() throws FileNotFoundException, IOException, InterruptedException {
		if(line_upi1_prettyPrinted < 0) {
			line_upi1_prettyPrinted = FileUtil.getUPL(getPrettyPrintedFragment1(), getPrettyPrintedFragment2());
		}
		return line_upi1_prettyPrinted;
	}
	
	public double getLineUPI1_type2NormalizedPrettyPrintedFragments() throws FileNotFoundException, IOException, InterruptedException, TXLException {
		if (line_upi1_prettyPrinted_type2Normalized < 0) {
			line_upi1_prettyPrinted_type2Normalized = FileUtil.getUPL(getType2NormalizedPrettyPrintedFragment1(), getType2NormalizedPrettyPrintedFragment2());
		}
		return line_upi1_prettyPrinted_type2Normalized;
	}
	
	public double getLineUPI2_originalFragments() throws FileNotFoundException, IOException {
		if (line_upi2 < 0)
			line_upi2 = FileUtil.getUPL(this.getExtractedFragment2(), this.getExtractedFragment1());
		return line_upi2;
	}
	
	public double getLineUPI2_prettyPrintedFragments() throws FileNotFoundException, IOException, InterruptedException {
		if(line_upi2_prettyPrinted < 0) {
			line_upi2_prettyPrinted = FileUtil.getUPL(getPrettyPrintedFragment2(), getPrettyPrintedFragment1());
		}
		return line_upi2_prettyPrinted;
	}
	
	public double getLineUPI2_type2NormalizedPrettyPrintedFragments() throws FileNotFoundException, IOException, InterruptedException, TXLException {
		if(line_upi2_prettyPrinted_type2Normalized < 0) {
			line_upi2_prettyPrinted_type2Normalized = FileUtil.getUPL(getType2NormalizedPrettyPrintedFragment2(), getType2NormalizedPrettyPrintedFragment1());
		}
		return line_upi2_prettyPrinted_type2Normalized;
	}
	
// --- Is Clone Functions ---------------------------------------------------------------------------------------------------------------------------
	
	// --- Maximum UPI Threshold --------------------------------------------------------------------------------------------------------------------
	
		// --- Token --------------------------------------------------------------------------------------------------------------------------------
	
	public boolean isClone_maxTokenUPI_originalFragments(double threshold) throws FileNotFoundException, IOException, InterruptedException, TXLException {
		if (getTokenUPI1_originalFragments() <= threshold && getTokenUPI2_originalFragments() <= threshold)
			return true;
		else 
			return false;
	}
	
	public boolean isClone_maxTokenUPI_type2NormalizedFragments(double threshold) throws FileNotFoundException, IllegalArgumentException, IOException, InterruptedException, TXLException {
		if (getTokenUPI1_type2NormalizedFragments() <= threshold && getTokenUPI2_type2NormalizedFragments() <= threshold)
			return true;
		else 
			return false;
	}
	
		// --- Line ---------------------------------------------------------------------------------------------------------------------------------
	
	public boolean isClone_maxLineUPI_originalFragments(double threshold) throws FileNotFoundException, IOException {
		if (getLineUPI1_originalFragments() <= threshold && getLineUPI2_originalFragments() <= threshold)
			return true;
		else
			return false;
	}
	
	public boolean isClone_maxLineUPI_prettyPrintedFragments(double threshold) throws FileNotFoundException, IOException, InterruptedException {
		if (getLineUPI1_prettyPrintedFragments() <= threshold && getLineUPI2_prettyPrintedFragments() <= threshold)
			return true;
		else
			return false;
	}
	
	public boolean isClone_maxLineUPI_type2NormalizedPrettyPrintedFragments(double threshold) throws FileNotFoundException, IOException, InterruptedException, TXLException {
		if (getLineUPI1_type2NormalizedPrettyPrintedFragments() <= threshold && getLineUPI2_type2NormalizedPrettyPrintedFragments() <= threshold)
			return true;
		else
			return false;
	}
	
		// --- Token and Line -----------------------------------------------------------------------------------------------------------------------
	
	//uses the line (original) and token UPI
	public boolean isClone_maxLineAndTokenUPI_originalFragments(double threshold) throws FileNotFoundException, IOException, InterruptedException, TXLException {
		if (isClone_maxLineUPI_originalFragments(threshold) && isClone_maxTokenUPI_originalFragments(threshold))
			return true;
		else
			return false;
	}
	
	//uses the line (pretty print) and token UPI
	public boolean isClone_maxLineAndTokenUPI_prettyPrintedFragments(double threshold) throws FileNotFoundException, IOException, InterruptedException, TXLException {
		if(this.isClone_maxLineUPI_prettyPrintedFragments(threshold) && this.isClone_maxTokenUPI_originalFragments(threshold))
			return true;
		else
			return false;
	}
	
	//uses the line (type2/prettyprint) and token (type2) UPI
	public boolean isClone_maxLineAndTokenUPI_type2NormalizedPrettyPrintedFragments(double threshold) throws FileNotFoundException, IllegalArgumentException, IOException, InterruptedException, TXLException {
		if (this.isClone_maxLineUPI_type2NormalizedPrettyPrintedFragments(threshold) && this.isClone_maxTokenUPI_type2NormalizedFragments(threshold))
			return true;
		else
			return false;
	}
	
	
// --- Typify ---------------------------------------------------------------------------------------------------------------------------------------
	
	private void typify() throws IOException, InterruptedException, TXLException {
		if (FileUtils.contentEquals(getTokenizedFragment1().toFile(), getTokenizedFragment2().toFile()))
			type = Clone.TYPE_1;
		else if (FileUtils.contentEquals(getType2NormalizedTokenizedFragment1().toFile(), getType2NormalizedTokenizedFragment2().toFile()))
			type = Clone.TYPE_2;
		else
			type = Clone.TYPE_3;
	}
	
// --- Get Processed Files --------------------------------------------------------------------------------------------------------------------------
		
	protected Path getFile1_prettyPrinted() throws IOException, IllegalArgumentException, InterruptedException, TXLException {
		if (prettyPrintedFile1 == null) {
			prettyPrintedFile1 = Files.createTempFile("CloneValidator_", "_PrettyPrintedFile1");
			TXLUtil.prettyPrintSourceFile(clone.getFile1(), prettyPrintedFile1, language);
		}
		return prettyPrintedFile1;
	}
	
	protected Path getFile2_prettyPrinted() throws IOException, IllegalArgumentException, InterruptedException, TXLException {
		if (prettyPrintedFile2 == null) {
			prettyPrintedFile2 = Files.createTempFile("CloneValidator_", "_PrettyPrintedFile2");
			TXLUtil.prettyPrintSourceFile(clone.getFile2(), prettyPrintedFile2, language);
		}
		return prettyPrintedFile2;
	}
	
	protected Path getFile1_type2Normalized() throws IOException, IllegalArgumentException, InterruptedException, TXLException {
		if (type2NormalizedFile1 == null) {
			type2NormalizedFile1 = Files.createTempFile("CloneValidator_", "_Type2NormalizedFile1");
			//TXLUtil.blindType2Normalize(clone.getFile1(), type2NormalizedFile1, language, normalizeIdentifiers, normalizeLiterals, primitivesAreIdentifiers, isLiteralNormalizationBlind);
		}
		return type2NormalizedFile1;
	}
	
	protected Path getFile2_type2Normalized() throws IOException, IllegalArgumentException, InterruptedException, TXLException {
		if (type2NormalizedFile2 == null) {
			type2NormalizedFile2 = Files.createTempFile("CloneValidator_", "_Type2NormalizedFile2");
			//TXLUtil.blindType2Normalize(clone.getFile2(), type2NormalizedFile2, language, normalizeIdentifiers, normalizeLiterals, primitivesAreIdentifiers, isLiteralNormalizationBlind);
		}
		return type2NormalizedFile2;
	}
		
	protected Path getExtractedFragment1() throws IOException {
		if(extractedFragment1 == null) {
			extractedFragment1 = Files.createTempFile("CloneValidator_", "_ExtractedFragment1");
			FragmentUtil.extractFragment(clone.getFragment1(), extractedFragment1);
		}
		return extractedFragment1;
	}
	
	protected Path getExtractedFragment2() throws IOException {
		if(extractedFragment2 == null) {
			extractedFragment2 = Files.createTempFile("CloneValidator_", "_ExtractedFragment2");
			FragmentUtil.extractFragment(clone.getFragment2(), extractedFragment2);
		}
		return extractedFragment2;
	}
	
	protected Path getTokenizedFragment1() throws IOException, IllegalArgumentException, InterruptedException, TXLException {
		if(tokenizedFragment1 == null) {
			tokenizedFragment1 = Files.createTempFile("CloneValidator_", "_TokenizedFragment1");
			TXLUtil.tokenize(getExtractedFragment1(), tokenizedFragment1, language);
		}
		return tokenizedFragment1;
	}
	
	protected Path getTokenizedFragment2() throws IOException, InterruptedException, TXLException {
		if(tokenizedFragment2 == null) {
			tokenizedFragment2 = Files.createTempFile("CloneValidator_", "_TokenizedFragment2");
			TXLUtil.tokenize(getExtractedFragment2(), tokenizedFragment2, language);
		}
		return tokenizedFragment2;
	}
		
	protected Path getType2NormalizedTokenizedFragment1() throws IOException, InterruptedException, TXLException {
		if(type2NormalizedTokenizedFragment1 == null) {
			type2NormalizedTokenizedFragment1 = Files.createTempFile("CloneValidator_", "_Type2NormalizedTokenizedFragment1");
			//TXLUtil.blindType2Normalize(getTokenizedFragment1(), type2NormalizedTokenizedFragment1, language, normalizeIdentifiers, normalizeLiterals, primitivesAreIdentifiers, isLiteralNormalizationBlind);
		}
		return type2NormalizedTokenizedFragment1;
	}
	
	protected Path getType2NormalizedTokenizedFragment2() throws FileNotFoundException, IOException, InterruptedException, TXLException {
		if(type2NormalizedTokenizedFragment2 == null) {
			type2NormalizedTokenizedFragment2 = Files.createTempFile("CloneValidator_", "_Type2NormalizedTokenizedFragment2");
			//TXLUtil.blindType2Normalize(getTokenizedFragment2(), type2NormalizedTokenizedFragment2, language, normalizeIdentifiers, normalizeLiterals, primitivesAreIdentifiers, isLiteralNormalizationBlind);
		}
		return type2NormalizedTokenizedFragment2;
	}
	
	protected Path getPrettyPrintedFragment1() throws FileNotFoundException, IOException, InterruptedException {
		if (prettyPrintedFragment1 == null) {
			prettyPrintedFragment1 = Files.createTempFile("CloneValidator_", "_PrettyPrintedFragment1");
			TXLUtil.prettyprint(clone.getFile1(), prettyPrintedFragment1, clone.getStartLine1(), clone.getEndLine1(), language);
		}
		return prettyPrintedFragment1;
	}
	
	protected Path getPrettyPrintedFragment2() throws FileNotFoundException, IOException, InterruptedException {
		if (prettyPrintedFragment2 == null) {
			prettyPrintedFragment2 = Files.createTempFile("CloneValidator_", "_PrettyPrintedFragment2");
			TXLUtil.prettyprint(clone.getFile2(), prettyPrintedFragment2, clone.getStartLine2(), clone.getEndLine2(), language);
		}
		return prettyPrintedFragment2;
	}
	
	protected Path getType2NormalizedPrettyPrintedFragment1() throws IOException, InterruptedException, TXLException {
		if (type2NormalizedPrettyPrintedFragment1 == null) {
			type2NormalizedPrettyPrintedFragment1 = Files.createTempFile("CloneValidator_", "_Type2NormalizedPrettyPrintedFragment1");
			//TXLUtil.blindType2Normalize(getPrettyPrintedFragment1(), type2NormalizedPrettyPrintedFragment1, language, normalizeIdentifiers, normalizeLiterals, primitivesAreIdentifiers, isLiteralNormalizationBlind);
		}
		return type2NormalizedPrettyPrintedFragment1;
	}
	
	protected Path getType2NormalizedPrettyPrintedFragment2() throws IOException, InterruptedException, TXLException {
		if (type2NormalizedPrettyPrintedFragment2 == null) {
			type2NormalizedPrettyPrintedFragment2 = Files.createTempFile("CloneValidator_", "_Type2NormalizedPrettyPrintedFragment2");
			//TXLUtil.blindType2Normalize(getPrettyPrintedFragment2(), type2NormalizedPrettyPrintedFragment2, language, normalizeIdentifiers, normalizeLiterals, primitivesAreIdentifiers, isLiteralNormalizationBlind);
		}
		//A Comment!
		return type2NormalizedPrettyPrintedFragment2;
	}

// --- Token Iterators ------------------------------------------------------------------------------------------------------------------------------
	//resets before return
	private TokenIterator getTokenIterator_OriginalFile1() throws IOException, InterruptedException, TXLException {
		if (tokenIterator_originalFile1 == null)
			tokenIterator_originalFile1 = new TokenIterator(clone.getFile1(), language);
		tokenIterator_originalFile1.reset();
		return tokenIterator_originalFile1;
	}
	
	//resets before return
	private TokenIterator getTokenIterator_type2NormalizedFile1() throws IOException, InterruptedException, TXLException {
		if (tokenIterator_type2NormalizedFile1 == null) 
			tokenIterator_type2NormalizedFile1 = new TokenIterator(this.getFile1_type2Normalized(), language);
		tokenIterator_type2NormalizedFile1.reset();
		return tokenIterator_type2NormalizedFile1;
	}
	
	//resets before return
	private TokenIterator getTokenIterator_OriginalFile2() throws IOException, InterruptedException, TXLException {
		if (tokenIterator_originalFile2 == null)
			tokenIterator_originalFile2 = new TokenIterator(clone.getFile2(), language);
		tokenIterator_originalFile2.reset();
		return tokenIterator_originalFile2;
	}
	
	//resets before return
	private TokenIterator getTokenIterator_type2NormalizedFile2() throws IOException, InterruptedException, TXLException {
		if (tokenIterator_type2NormalizedFile2 == null) 
			tokenIterator_type2NormalizedFile2 = new TokenIterator(this.getFile2_type2Normalized(), language);
		tokenIterator_type2NormalizedFile2.reset();
		return tokenIterator_type2NormalizedFile2;
	}
	
// --- Clone Expansion ------------------------------------------------------------------------------------------------------------------------------
//	public Clone getType1Expansion_raw() throws IOException, InterruptedException, TXLException {
//		if (type1Expansion_raw == null)
//			type1Expansion_raw = fragmentExpansion_raw(getTokenIterator_OriginalFile1(), getTokenIterator_OriginalFile2());
//		return type1Expansion_raw;
//	}
//	
//	public Clone getType2Expansion_raw() throws IOException, InterruptedException, TXLException {
//		if (type2Expansion_raw == null)
//			type2Expansion_raw = fragmentExpansion_raw(this.getTokenIterator_type2NormalizedFile1(), this.getTokenIterator_type2NormalizedFile2());
//		return type2Expansion_raw;
//	}
//	
//	public Clone getType1Expansion_balancedBraces() throws IOException, InterruptedException, TXLException {
//		if (type1Expansion_balancedBraces == null) {
//			Clone type1expansion = this.getType1Expansion_raw();
//			type2Expansion_balancedBraces = new Clone(fragmentExpansion_balanceBraces(type1expansion.getFragment1(), getTokenIterator_OriginalFile1()),
//					this.fragmentExpansion_balanceBraces(type1expansion.getFragment2(), getTokenIterator_OriginalFile2()));
//		}
//		return type1Expansion_balancedBraces;
//	}
//	
//	public Clone getType2Expansion_balacedBraces() throws IOException, InterruptedException, TXLException {
//		if(type2Expansion_balancedBraces == null) {
//			Clone type2expansion = this.getType2Expansion_raw();
//			type2Expansion_balancedBraces = new Clone(fragmentExpansion_balanceBraces(type2expansion.getFragment1(), getTokenIterator_type2NormalizedFile1()),
//					this.fragmentExpansion_balanceBraces(type2expansion.getFragment2(), getTokenIterator_type2NormalizedFile2()));
//		}
//		return type2Expansion_balancedBraces;
//	}
//	
//	private Fragment fragmentExpansion_balanceBraces(Fragment raw, TokenIterator ti) {
//		ti.reset();
//		
//		int leftBraces = 0;
//		int rightBraces = 0;
//		
//		//Goto Start of Token
//		if(raw.isTokenSpecified()) {
//			ti.gotoToken(raw.getStartToken());
//		} else {
//			ti.gotoLine(raw.getStartLine());
//		};
//		
//		//Count Left and Right braces
//		Token tok = ti.next();
//		while(tok.getTokenNumber() <= raw.getEndToken()) {
//			if(tok.getText().equals("{"))
//				leftBraces++;
//			if(tok.getText().equals("}"))
//				rightBraces++;
//		}
//		
//		//If balanced, no change required
//		if(leftBraces == rightBraces) {
//			return raw;
//			
//		//If more left braces, trim from start until balanced
//		} else if (leftBraces > rightBraces) {
//			//Goto start of fragment
//			if(raw.isTokenSpecified()) {
//				ti.gotoToken(raw.getStartToken());
//			} else {
//				ti.gotoLine(raw.getStartLine());
//			};
//			
//			//Trim
//			int numTrim = leftBraces - rightBraces;
//			int trimmed = 0;
//			tok = ti.next();
//			while(trimmed != numTrim) {
//				if(tok.getText().equals("{"))
//					trimmed++;
//				tok = ti.next();
//			}
//			
//			//Return new fragment
//			return Fragment.createFragmentByLineAndToken(raw.getFile(), tok.getLineNumber(), tok.getTokenNumber(), raw.getEndLine(), raw.getEndToken());
//			
//		//If more right braces, trim from end until balanced
//		} else if (leftBraces < rightBraces) {
//			//Goto end of fragment
//			if(raw.isTokenSpecified()) {
//				ti.gotoToken(raw.getEndToken());
//			} else {
//				ti.gotoLine(raw.getEndLine());
//			};
//			
//			//Trim
//			int numTrim = rightBraces - leftBraces;
//			int trimmed = 0;
//			tok = ti.previous();
//			while(trimmed != numTrim) {
//				if(tok.getText().equals("}"))
//					trimmed++;
//				tok = ti.previous();
//			}
//			
//			//Return new fragment
//			return Fragment.createFragmentByLineAndToken(raw.getFile(), raw.getStartLine(), raw.getStartToken(), tok.getLineNumber(), tok.getTokenNumber());
//			
//		} else {
//			throw new RuntimeException("CloneValidator.fragmentExpansion_balanceBraces contains a bug!");
//		}
//	}
//	
//	private Clone fragmentExpansion_raw(TokenIterator ti1, TokenIterator ti2) throws IOException, InterruptedException, TXLException {
//		ti1.reset();
//		ti2.reset();
//		
//	//Start
//		if(clone.isTokenBoundariesSpecified()) {
//			ti1.gotoToken(clone.getFragment1().getStartToken());
//			ti2.gotoToken(clone.getFragment2().getStartToken());
//		} else {
//			ti1.gotoLine(clone.getFragment1().getStartLine());
//			ti2.gotoLine(clone.getFragment2().getStartLine());
//		}
//		
//		Token startToken1 = ti1.previous();
//		Token startToken2 = ti2.previous();
//		while(startToken1.getText().equals(startToken2.getText())) {
//			startToken1 = ti1.previous();
//			startToken2 = ti2.previous();
//		}
//		startToken1 = ti1.next();
//		startToken2 = ti2.next();
//		
//	//End
//		if(clone.isTokenBoundariesSpecified()) {
//			ti1.gotoToken(clone.getFragment1().getEndToken());
//			ti2.gotoToken(clone.getFragment2().getEndToken());
//		} else {
//			ti1.gotoToken(clone.getFragment1().getEndLine());
//			ti2.gotoToken(clone.getFragment2().getEndLine());
//		}
//		
//		Token endToken1 = ti1.next();
//		Token endToken2 = ti2.next();
//		while(endToken1.getText().equals(endToken2.getText())) {
//			endToken1 = ti1.next();
//			endToken2 = ti2.next();
//		}
//		endToken1 = ti1.previous();
//		endToken2 = ti2.previous();
//		
//	//Store
//		int startLine_1 = startToken1.getLineNumber();
//		int startToken_1 = startToken1.getTokenNumber();
//		
//		int endLine_1 = endToken1.getLineNumber();
//		int endToken_1 = endToken1.getTokenNumber();
//		
//		int startLine_2 = startToken2.getLineNumber();
//		int startToken_2 = startToken2.getTokenNumber();
//		
//		int endLine_2 = endToken2.getLineNumber();
//		int endToken_2 = endToken2.getTokenNumber();
//		
//		
//		return new Clone(Fragment.createFragmentByLineAndToken(clone.getFile1(), startLine_1, startToken_1, endLine_1, endToken_1),
//				Fragment.createFragmentByLineAndToken(clone.getFile2(), startLine_2, startToken_2, endLine_2, endToken_2));
//	}
}
