package txl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import util.FileUtil;
import util.Fragment;
import util.FragmentUtil;
import util.StreamGobbler;
import util.Token;
import util.TokenIterator;
import util.UnzipUtil;


/**
 * @author Jeff Svajlenko
 */
public class TXLUtil {
	
// --- Language Support -----------------------------------------------------------------------------------------------------------------------------
	public static int LANGUAGE_JAVA = 1;
	private static String JAVA_STRING = "java";
	public static int LANGUAGE_C = 2;
	private static String C_STRING = "c";
	public static int LANGUAGE_CS = 3;
	private static String CS_STRING = "cs";
	
	public static boolean doesLanguageHavePreProcessor(int language) {
		if(language == TXLUtil.LANGUAGE_C || language == TXLUtil.LANGUAGE_CS)
			return true;
		else
			return false;
	}
	
	public static boolean isLanguageSupported(int language) {
		if (language == LANGUAGE_JAVA) {
			return true;
		} else if (language == LANGUAGE_C) {
			return true;
		} else if (language == LANGUAGE_CS) {
			return true;
		} else
			return false;
	}
	
	public static void requireLanguageSupported(int language) {
		if(!isLanguageSupported(language)) {
			throw new LanguageNotSupportedException("Language: " + language + " is not supported by TxlUtil.");
		}
	}
	
// --- TXL Files ------------------------------------------------------------------------------------------------------------------------------------
	
	private static Path txl_directory = null;
	
	private static void initTxlDirectory() throws IOException {
		if(txl_directory == null) {
			txl_directory = Paths.get("txl/");
			
			if(Files.isDirectory(txl_directory)) {
				return;
			}
			
		//Extract zip file
			InputStream is = TXLUtil.class.getResourceAsStream("resources.zip");
			Path zipfile = Files.createTempFile("TXLUtil_", "_ResourcesZipFile");
			FileOutputStream fos = new FileOutputStream(zipfile.toFile());
			byte[] buffer = new byte[2048];
			int cbyte;
			cbyte = is.read(buffer, 0, 2048);
			while(cbyte != -1) {
				fos.write(buffer, 0, cbyte);
				cbyte = is.read(buffer, 0, 2048);
			}
			fos.flush(); fos.close();
			is.close();
			
		//Unzip it
			txl_directory = Files.createTempDirectory("TXLUtil_Resources");
			UnzipUtil.unzipIntoFolder(zipfile, txl_directory);
			System.out.println(txl_directory);
			
		//Cleanup
			Files.delete(zipfile);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						FileUtils.deleteDirectory(txl_directory.toFile());
					} catch (IOException e) {
					}
				}
		});
		}
	}
	
	public static Path getTxlDirectory(int language) throws IOException {
		initTxlDirectory();
		if(language == LANGUAGE_JAVA) {
			return txl_directory.resolve(JAVA_STRING);
		} else if (language == LANGUAGE_C) {
			return txl_directory.resolve(C_STRING);
		} else if (language == LANGUAGE_CS) {
			return txl_directory.resolve(CS_STRING);
		} else {
			throw new IllegalArgumentException("Language not supported.");
		}
	}
	
	/**
	 * Counts the number of tokens in a source file.  File needs to conform to the language's token grammar, but need not be complete syntax
	 * (or actually necessarily correct syntax...).
	 * @param file The file.
	 * @param language The language of the file's token grammar.
	 * @return The number of tokens in the file.
	 * @throws IOException If an IO error occurs.
	 * @throws TXLException If the TXL script used failed (either input file was malformed, or txl script has an error).
	 * @throws InterruptedException If a required TXL process is externally interrupted unexpectantly.
	 */
	public static int countTokens(Path file, int language) throws IOException, TXLException, InterruptedException {
		Objects.requireNonNull(file);
		if(!isLanguageSupported(language)) {
			throw new IllegalArgumentException("Language is not supported.");
		}
		if(!Files.exists(file)) {
			throw new IllegalArgumentException("File does not exist.");
		}
		if(!Files.isRegularFile(file)) {
			throw new IllegalArgumentException("File is not a regular file.");
		}
		if(!Files.isReadable(file)) {
			throw new IllegalArgumentException("File is not readable.");
		}
		
		//Tokenize the file
		Path tmpfile = Files.createTempFile("countTokens", null);
		try {
			TXLUtil.tokenize(file, tmpfile, language);
		} catch (InterruptedException e) {
			Files.deleteIfExists(tmpfile);
			throw e;
		} catch (TXLException e) {
			Files.deleteIfExists(tmpfile);
			throw new TXLException("Failed to tokenize file");
		}
		
		//Count the lines (=number tokens)
		int numtokens = FileUtil.countLines(tmpfile);
		Files.deleteIfExists(tmpfile);
		return numtokens;
	}
	
	/**
	 * Pretty prints the input file and stores the result in the output file.  input file must be a complete fragment of the Function or Block granularity.
	 * @param infile The input file to process.  Must exist, be a regular file, and be readable.
	 * @param outfile Where to store the token stream.  Must not be an existing directory (existing files are overwritten).  Must be writable.
	 * @param language The language of the file.
	 * @return True if successful, false if unsuccessful (TXL script failed for input).  False usually indicates incorrect language specified or syntax error in file.  There may be rare cases where TXL can not handle a valid file.
	 * @throws IllegalArgumentException If an argument's requirements are violated.
	 * @throws FileNotFoundException If input file is not found.
	 * @throws IOException If an IO error occurs.
	 * @throws InterruptedException If the TXL process executed by this function is externally interrupted.
	 */
	public static boolean prettyPrintSourceFragment(Path infile, Path outfile, int language) throws IllegalArgumentException, FileNotFoundException, IOException, InterruptedException {
	//Check Paramters
		//Paramters valid (not null, language exists)
		Objects.requireNonNull(infile);
		Objects.requireNonNull(outfile);
		isLanguageSupported(language);
		
		//Check input file (exists, is regular, is readable)
		if(!Files.exists(infile)) {
			throw new FileNotFoundException("File does not exist.");
		}
		if(!Files.isRegularFile(infile)) {
			throw new IllegalArgumentException("infile is not regular.");
		}
		if(!Files.isReadable(infile)) {
			throw new IOException("infile is not readable.");
		}
		
		//Check output file (is not a directory, is writable)
		if(Files.exists(outfile) && Files.isDirectory(outfile)) {
			throw new IllegalArgumentException("outfile is a directory and cannot be overriden.");
		}
		Files.deleteIfExists(outfile);
		Files.createFile(outfile);
		if(!Files.isWritable(outfile)) {
			throw new IOException("outfile is not writable.");
		}
		
		//txl
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("PrettyPrintFragment.txl"), infile, outfile);
		
		if(retval == 0) {
			return true;
		} else {
			return false;
		}
	}
	

	
// --- Source File Normalizers ----------------------------------------------------------------------------------------------------------------------
	
	private static void validateTXLEnd(int retval) throws TXLException {
		if(retval != 0)
			throw new TXLException("TXL program failed to execute properly.");
	}
	
	private static void checkAndPrepTxlInOutLanguageFunction(Path infile, Path outfile, int language) throws FileNotFoundException, IOException {
		// infile can not be null
		Objects.requireNonNull(infile);
		
		// outfile can not be null
		Objects.requireNonNull(outfile);
		
		// language must be supported
		requireLanguageSupported(language);
		
		// infile must exist
		if(!Files.exists(infile)) {throw new FileNotFoundException("infile does not exist.");}
		
		// infile must be a regular file
		if(!Files.isRegularFile(infile)) {throw new IllegalArgumentException("infile is not a regular file.");}
		
		// infile must be readable
		if(!Files.isReadable(infile)) {throw new IOException("infile is not readable.");}
		
		// if outfile is a directory, it must not be a non-empty directory, if empty it is deleted
		if(Files.isDirectory(outfile)) { //outfile can not be a non-empty directory
			if(!outfile.toFile().delete())
				throw new DirectoryNotEmptyException("outfile is a non-empty directory.");
		}
		// if regular file, and is not the infile, delete the file 
		else if (Files.isRegularFile(outfile) && !FileUtil.isSameFile(infile, outfile)) {
			if(!Files.isWritable(outfile))
				throw new IOException("outfile is not writable.");
			Files.delete(outfile);
		}
		// if regular file, and is the infile, check writable
		else if (Files.isRegularFile(outfile) && FileUtil.isSameFile(infile, outfile)) {
			if(!Files.isWritable(outfile)) {
				throw new IOException("outfile is not writable.");
			}
		}
		// if file does not exist, test writability
		else if (!Files.exists(outfile)) {
			try {
				Files.createFile(outfile);
			} catch (IOException e) {
				throw new IOException("outfile is not writable.");
			}
			Files.delete(outfile);
		}
	}
	
	/**
	 * Pretty prints complete source file.
	 * @param infile
	 * @param outfile
	 * @param language
	 * @throws IllegalArgumentException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TXLException
	 */
	public static void prettyPrintSourceFile(Path infile, Path outfile, int language) throws IllegalArgumentException, FileNotFoundException, IOException, InterruptedException, TXLException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("PrettyPrint.txl"), infile, outfile);
		validateTXLEnd(retval);
	}
	
	/**
	 * Tokenizes the source code in the input file.  Tokens seperated by newline.
	 * @param infile
	 * @param outfile
	 * @param language
	 * @throws IllegalArgumentException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TXLException
	 */
	public static void tokenize(Path infile, Path outfile, int language) throws IllegalArgumentException, FileNotFoundException, IOException, InterruptedException, TXLException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("tokenize.txl"), infile, outfile);
		validateTXLEnd(retval);
	}
	
	/**
	 * Tokenizes the source code in the input file.  Tokens seperated by spaces.  Tokens are left on their original source line.
	 * @param infile
	 * @param outfile
	 * @param language
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TXLException
	 */
	public static void tokenizeLines(Path infile, Path outfile, int language) throws IOException, InterruptedException, TXLException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		removeComments(infile, outfile, language);
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("tokenizelines.txl"), outfile, outfile);
		validateTXLEnd(retval);
	}
	
	/**
	 * All of the identifiers are replaced by X.
	 * @param infile
	 * @param outfile
	 * @param language
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TXLException
	 */
	public static void blindRenameIdentifiers(Path infile, Path outfile, int language) throws FileNotFoundException, IOException, InterruptedException, TXLException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("blindRenameIdentifiers.txl"), infile, outfile);
		validateTXLEnd(retval);
	}
	
	/**
	 * All primitives are replaced by X.
	 * @param infile
	 * @param outfile
	 * @param language
	 * @throws TXLException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void blindRenamePrimitives(Path infile, Path outfile, int language) throws TXLException, FileNotFoundException, IOException, InterruptedException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("blindRenamePrimitives.txl"), infile, outfile);
		validateTXLEnd(retval);
	}
	
	/**
	 * All literals are normalized to uniform "default" values.
	 * @param infile
	 * @param outfile
	 * @param language
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TXLException
	 */
	public static void normalizeLiteralsToDefault(Path infile, Path outfile, int language) throws FileNotFoundException, IOException, InterruptedException, TXLException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("normalizeLiteralsToDefault.txl"), infile, outfile);
		validateTXLEnd(retval);
	}
	
	/**
	 * All literals are normalized to 0.
	 * @param infile
	 * @param outfile
	 * @param language
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws TXLException
	 * @throws InterruptedException
	 */
	public static void normalizeLiteralsToZero(Path infile, Path outfile, int language) throws FileNotFoundException, IOException, TXLException, InterruptedException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("normalizeLiteralsToZero.txl"), infile, outfile);
		validateTXLEnd(retval);
	}
	
	/**
	 * Identifiers are consistently renamed to x#, with # in order of the identifiers' first apperance in the fragment.
	 * @param infile
	 * @param outfile
	 * @param language
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TXLException
	 */
	public static void consistentRenameIdentifiers(Path infile, Path outfile, int language) throws FileNotFoundException, IOException, InterruptedException, TXLException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		int retval = runTxl(getTxlDirectory(language).resolve("consistentRenameIdentifiers.txl"), infile, outfile);
		validateTXLEnd(retval);
	}

	/**
	 * Removes all comments.  Source code is otherwise left unchanged.
	 * @param infile
	 * @param outfile
	 * @param language
	 * @throws IllegalArgumentException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TXLException
	 */
	public static void removeComments(Path infile, Path outfile, int language) throws IllegalArgumentException, FileNotFoundException, IOException, InterruptedException, TXLException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("removecomments.txl"), infile, outfile);
		validateTXLEnd(retval);
	}
	
	/**
	 * Pretty prints a source code function.  Will not work on any other granularity.
	 * @param infile
	 * @param outfile
	 * @param language
	 * @throws IllegalArgumentException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TXLException
	 */
	public static void prettyPrintFunction(Path infile, Path outfile, int language) throws IllegalArgumentException, FileNotFoundException, IOException, InterruptedException, TXLException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("function.txl"), infile, outfile);
		validateTXLEnd(retval);
	}
	
	/**
	 * Pretty prints a source code block.  Will not work on any other granularity.
	 * @param infile
	 * @param outfile
	 * @param language
	 * @throws IllegalArgumentException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TXLException
	 */
	public static void prettyPrintBlock(Path infile, Path outfile, int language) throws IllegalArgumentException, FileNotFoundException, IOException, InterruptedException, TXLException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("block.txl"), infile, outfile);
		validateTXLEnd(retval);
	}
	
	/**
	 * Comments out the if block of if/else preprocessor blocks.  
	 * @param infile
	 * @param outfile
	 * @param language the language of the source file.  Must be C or C#.  Java does not have ifdef preprocessor.
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws TXLException
	 */
	public static void commentOutIfBlockOfIfDefPreProcessor(Path infile, Path outfile, int language) throws InterruptedException, IOException, TXLException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		if(Files.exists(TXLUtil.getTxlDirectory(language).resolve("ifdef.txl"))) {
			throw new IllegalArgumentException("This langauge does not have ifdef.");
		}
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("ifdef.txl"), infile, outfile);
		validateTXLEnd(retval);
	}
	
	public static void extractFunctions(Path infile, Path outfile, int language) throws InterruptedException, IOException, TXLException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		if (doesLanguageHavePreProcessor(language))
			commentOutIfBlockOfIfDefPreProcessor(infile, outfile, language);
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("extract-functions.txl"), outfile, outfile);
		validateTXLEnd(retval);
	}
	
	public static void extractBlocks(Path infile, Path outfile, int language) throws InterruptedException, IOException, TXLException {
		checkAndPrepTxlInOutLanguageFunction(infile, outfile, language);
		if (doesLanguageHavePreProcessor(language))
			commentOutIfBlockOfIfDefPreProcessor(infile, outfile, language);
		int retval = TXLUtil.runTxl(getTxlDirectory(language).resolve("extract-blocks.txl"), outfile, outfile);
		validateTXLEnd(retval);
	}
	
	public static List<Fragment> extractBlocks(Path infile, int language) throws IOException, InterruptedException, TXLException {
		Path originalLines = Files.createTempFile("TXLUtil_", "_extractedOriginal");
		Path prettyPrinted = Files.createTempFile("TXLUtil_", "_prettyPrinted");
		Path prettyPrintedLines = Files.createTempFile("TXLUtil_", "_extractedPretty");
		
		try {
			TXLUtil.extractBlocks(infile, originalLines, language);
			TXLUtil.prettyPrintSourceFile(infile, prettyPrinted, language);
			TXLUtil.extractBlocks(prettyPrinted, prettyPrintedLines, language);
		} catch (TXLException e) {
			Files.deleteIfExists(originalLines);
			Files.delete(prettyPrinted);
			throw e;
		}
		
		List<Fragment> originalBlocks = parseExtracted(originalLines);
		List<Fragment> prettyFragments = parseExtracted(prettyPrintedLines);
		List<Fragment> list = new ArrayList<Fragment>(originalBlocks.size());
		
		TokenIterator ti = new TokenIterator(prettyPrinted, language);
		
		for(int i = 0; i < originalBlocks.size(); i++) {
			Fragment original = originalBlocks.get(i);
			Fragment pretty = prettyFragments.get(i);
			
			ti.gotoLine(pretty.getStartLine());
			Token startToken = ti.next();
			
			ti.gotoLine(pretty.getEndLine());
			Token endToken = ti.next();
					
			list.add(Fragment.createFragmentByLineAndToken(infile, original.getStartLine(), startToken.getTokenNumber(), original.getEndLine(), endToken.getTokenNumber()));
		}
		
		return list;
	} 
	
	public static List<Fragment> extractFunctions(Path infile, int language) throws IOException, InterruptedException, TXLException {
		Path originalLines = Files.createTempFile("TXLUtil_", "_extractedOriginal");
		Path prettyPrinted = Files.createTempFile("TXLUtil_", "_prettyPrinted");
		Path prettyPrintedLines = Files.createTempFile("TXLUtil_", "_extractedPretty");
		
		try {
			TXLUtil.extractFunctions(infile, originalLines, language);
			TXLUtil.prettyPrintSourceFile(infile, prettyPrinted, language);
			TXLUtil.extractFunctions(prettyPrinted, prettyPrintedLines, language);
		} catch (TXLException e) {
			Files.deleteIfExists(originalLines);
			Files.delete(prettyPrinted);
			throw e;
		}
		
		List<Fragment> originalFragments = parseExtracted(originalLines);
		List<Fragment> prettyFragments = parseExtracted(prettyPrintedLines);
		List<Fragment> list = new ArrayList<Fragment>(originalFragments.size());
		
		TokenIterator ti = new TokenIterator(prettyPrinted, language);
		
		for(int i = 0; i < originalFragments.size(); i++) {
			Fragment original = originalFragments.get(i);
			Fragment pretty = prettyFragments.get(i);
			
			ti.gotoLine(pretty.getStartLine());
			Token startToken = ti.next();
			
			ti.gotoLine(pretty.getEndLine());
			Token endToken = ti.next();
					
			list.add(Fragment.createFragmentByLineAndToken(infile, original.getStartLine(), startToken.getTokenNumber(), original.getEndLine(), endToken.getTokenNumber()));
		}
		
		return list;
	}
	
	private static List<Fragment> parseExtracted(Path extracted) throws IOException, FileNotFoundException {
		Objects.requireNonNull(extracted);
		if(!Files.exists(extracted)) { throw new FileNotFoundException("extracted does not exist."); }
		if(!Files.isRegularFile(extracted)) { throw new IllegalArgumentException("extracted does not denote a regular file."); }
		
		List<Fragment> list = new ArrayList<Fragment>();
		BufferedReader br = new BufferedReader(new FileReader(extracted.toFile()));
		String line = br.readLine();
		while(line != null) {
			if(line.startsWith("<source file")) {
				String parts[] = line.split("\"");
				Path file = Paths.get(parts[1]);
				int startline = Integer.parseInt(parts[3]);
				int endline = Integer.parseInt(parts[5]);
				list.add(Fragment.createFragmentByLines(file, startline, endline));
			}
			line = br.readLine();
		}
		br.close();
		
		return list;
	}

	public static int runTxl(Path txlScript, Path inputFile, Path outputFile) throws InterruptedException, IOException {
		//Check input
		Objects.requireNonNull(txlScript);
		Objects.requireNonNull(inputFile);
		Objects.requireNonNull(outputFile);
		if(!Files.exists(txlScript)) {
			throw new FileNotFoundException("Specified txl script does not exist.  Script: " + txlScript + ", input: " + inputFile + ", output: " + outputFile);
		}
		if(!Files.isReadable(txlScript)) {
			throw new IOException("Specified txl script is not readable.");
		}
		if(!Files.isRegularFile(txlScript)) {
			throw new IllegalArgumentException("txlScript must be a regular file.");
		}
		if(!Files.exists(inputFile)) {
			throw new FileNotFoundException("Input file does not exist.");
		}
		if(!Files.isReadable(inputFile)) {
			throw new IOException("Input file is not readable or does not exist.");
		}
		if(!Files.exists(outputFile)) {
			try {
				Files.createFile(outputFile);
			} catch (IOException e) {
				throw new IllegalArgumentException("Output file can not be written to.");
			}
		}
		if(!Files.isWritable(outputFile)) {
			throw new IOException("Output file can not be written to.");
		}
		
		//Execute TXL
		List<String> command = new LinkedList<String>();
		if(SystemUtils.IS_OS_WINDOWS) {
			command.add("cmd.exe");
			command.add("/c");
			command.add("txl");
			//command += " -o \"" + outputFile.toAbsolutePath().normalize().toString() + "\" ";
			//command += " \"" + inputFile.toAbsolutePath().normalize().toString() + "\" ";
			//command += " \"" + txlScript.toAbsolutePath().normalize().toString() + "\" ";
		}
		else if (SystemUtils.IS_OS_MAC_OSX) {
			command.add("txl");
			//command += " -o " + outputFile.toAbsolutePath().normalize().toString() + " ";
			//command += " " + inputFile.toAbsolutePath().normalize().toString() + " ";
			//command += " " + txlScript.toAbsolutePath().normalize().toString() + " ";
		}
		else {
			command.add("txl");
			//command += " -o " + outputFile.toAbsolutePath().normalize().toString() + " ";
			//command += " " + inputFile.toAbsolutePath().normalize().toString() + " ";
			//command += " " + txlScript.toAbsolutePath().normalize().toString() + " ";
		}

		command.add("-s");
		command.add("4000");
		command.add("-o");
		command.add(outputFile.toAbsolutePath().normalize().toString());
		command.add(inputFile.toAbsolutePath().normalize().toString());
		command.add(txlScript.toAbsolutePath().normalize().toString());

		//System.out.println(command);
		Process process = null;
		int retval;
		try {
			process = Runtime.getRuntime().exec(command.toArray(new String[command.size()]));
			new StreamGobbler(process.getInputStream()).start();
			new StreamGobbler(process.getErrorStream()).start();
			retval = process.waitFor();
		} catch (IOException e) {
			throw e;
		} catch (InterruptedException e) {
			if(process != null) {
				try {process.getErrorStream().close();} catch (IOException e1) {}
				try {process.getInputStream().close();} catch (IOException e1) {}
				try {process.getOutputStream().close();} catch (IOException e1) {}
				process.destroy();
				process = null;
			}
			throw e;
		} finally {
			if(process != null) {
				try {process.getErrorStream().close();} catch (IOException e) {}
				try {process.getInputStream().close();} catch (IOException e) {}
				try {process.getOutputStream().close();} catch (IOException e) {}
				process.destroy();
				process = null;
			}
		}
		
		return retval;
	}
	
	public static boolean isFunction(Path function, int language) throws FileNotFoundException, IOException, InterruptedException {
		//Check Input
		Objects.requireNonNull(function);
		Objects.requireNonNull(language);
		requireLanguageSupported(language);
		if(!Files.exists(function)) {throw new FileNotFoundException("Function does not exist.");}
		if(!Files.isReadable(function)) {throw new IllegalArgumentException("Function is not readable.");}
		
		Path tmpfile = Files.createTempFile("TXLUtil_", "_isFunction");
		boolean isFunction;
		
		try {
			TXLUtil.prettyPrintFunction(function, tmpfile, language);
			isFunction = true;
		} catch (TXLException e) {
			isFunction = false;
		}
		
		Files.deleteIfExists(tmpfile);
		
		return isFunction;
	}
	
	public static boolean isBlock(Path function, int language) throws FileNotFoundException, IOException, InterruptedException {
		//Check Input
		Objects.requireNonNull(function);
		Objects.requireNonNull(language);
		requireLanguageSupported(language);
		if(!Files.exists(function)) {throw new FileNotFoundException("Function does not exist.");}
		if(!Files.isReadable(function)) {throw new IllegalArgumentException("Function is not readable.");}
		
		Path tmpfile = Files.createTempFile("TXLUtil_", "_isBlock");
		boolean isFunction;
		
		try {
			TXLUtil.prettyPrintBlock(function, tmpfile, language);
			isFunction = true;
		} catch (TXLException e) {
			isFunction = false;
		}
		
		Files.deleteIfExists(tmpfile);
		
		return isFunction;
	}
	
	/**
	 * Pretty prints an arbitrary code fragment.  Reliable, and consistent.  However, the output is not pretty to humans.  Indentation is destroyed and tokens are spaced.
	 * @param fragment
	 * @param outfile
	 * @param language
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TXLException
	 */
	public static void prettyPrintFragment(Fragment fragment, Path outfile, int language) throws IOException, InterruptedException, TXLException {
		Objects.requireNonNull(fragment);
		Objects.requireNonNull(outfile);
		
		if(!fragment.isTokenSpecified()) {
			TokenIterator ti = new TokenIterator(fragment.getFile(), language);
			ti.gotoLine(fragment.getStartLine());
			Token token = ti.next();
			int startToken = token.getTokenNumber();
			ti.gotoLine(fragment.getEndLine()+1);
			ti.next();
			token = ti.previous();
			int endToken = token.getTokenNumber();
			fragment = Fragment.createFragmentByLineAndToken(fragment.getFile(), fragment.getStartLine(), startToken, fragment.getEndLine(), endToken);
		}
		
		Path pretty = Files.createTempFile("TXLUtil_", "_prettyPrintFragment_prettyFile");
		TXLUtil.prettyPrintBlock(fragment.getFile(), pretty, language);
		TokenIterator ti = new TokenIterator(pretty, language);
		Files.delete(pretty);
		
		Files.createFile(outfile);
		PrintWriter pw = new PrintWriter(new FileWriter(outfile.toFile()));
		ti.gotoToken(fragment.getStartToken());
		
		Token token = ti.next();
		int line = token.getLineNumber();
		while(token.getTokenNumber() <= fragment.getEndLine()) {
			if(line < token.getLineNumber())
				pw.println();
			else
				pw.print(token.getText() + " ");
		}
		pw.flush();
		pw.close();
	}
	
	/**
	 * Pretty prints a code fragment in a source file.  Preserves pretty-printed formatting (pretty for humans).  Is slow, and may not be reliable.  Tokens are spaced.
	 * @param infile
	 * @param outfile
	 * @param startline
	 * @param endline
	 * @param language
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static boolean prettyprint(Path infile, Path outfile, int startline, int endline, int language) throws FileNotFoundException, IOException, InterruptedException {
	//Check Arguments
		//Objects
		Objects.requireNonNull(infile);
		Objects.requireNonNull(outfile);
	
		//Language
		isLanguageSupported(language);
		
		//Input File
		if(!Files.exists(infile)) { //should exist
			throw new FileNotFoundException("Input file does not exist.");
		}
		if(!Files.isRegularFile(infile)) {
			throw new IllegalArgumentException("infile is not a regular file.");
		}
		if(!Files.isReadable(infile)) { //should be readable
			throw new IOException("Input file can not be read.");
		}
		
		//Output File
		Files.deleteIfExists(outfile);
		Files.createFile(outfile);
		if(!Files.exists(outfile)) { //create output file
			throw new IOException("Output file could not be created.");
		}
		if(!Files.isWritable(outfile)) { //should be able to write
			throw new IOException("Output file is not writable.");
		}
		
		//Startline/Endline are appropraite
		if(endline < startline) {
			throw new IllegalArgumentException("Endline preceeds startline.");
		}
		int numlines = FileUtil.countLines(infile);
		if(endline > numlines) {
			throw new IllegalArgumentException("Endline exceeds length of file.");
		}
		
		Path prettyfile=null;
		Path prettyfile_spaced=null;
		Path uncommented=null;
		Path fragment=null;
		Path fragment_tokstream=null;
		Path fragment_beforeRemoveEmptyLines = null;
		try {
			prettyfile = Files.createTempFile("LineValidator_prettyfile", null);
			prettyfile_spaced = Files.createTempFile("LineValidator_prettyfile_spaced", null);
			uncommented = Files.createTempFile("LineValidator_uncommented", null);
			fragment = Files.createTempFile("LineValidator_fragment", null);
			fragment_tokstream = Files.createTempFile("LineValidator_fragment_tokstream", null);
			fragment_beforeRemoveEmptyLines = Files.createTempFile("LineValidator_fragment_beforeRemoveEmptyLine", null);
		} catch (IOException e) {
			try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
			try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
			try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
			try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
			try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
			//try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
			throw e;
		}
		
// Perform pretty-printing
	//PrettyPrint file containing the fragment
		try {
			if(0 != runTxl(getTxlDirectory(language).resolve("PrettyPrint.txl"), infile, prettyfile)) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				//System.out.println("here1");
				return false;
			}
		} catch (InterruptedException e1) {
			try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
			try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
			try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
			try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
			try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
			try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
			throw e1;
		}
		
	//Add spaces between each token of the pretty-printed file
		try {
			if(0 != runTxl(getTxlDirectory(language).resolve("spacer.txl"), prettyfile, prettyfile_spaced)) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				return false;
			}
		} catch (InterruptedException e1) {
			try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
			try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
			try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
			try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
			try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
			try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
			throw e1;
		}
		
	//Load space adjusted pretty-printed file into a string, fix whitespace so all tokens are 1 space apart, no tabs, newlines persist
		String file = new String();
		try {
			Scanner scanner = new Scanner(prettyfile_spaced);
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
					file = file + line + "\n";
			}
			scanner.close();
		} catch (IOException e) {
			try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
			try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
			try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
			try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
			try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
			try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
			throw e;
		}
		
//Create fragment search pattern
		//Remove comments from original file
		try {
			if(0 != runTxl(getTxlDirectory(language).resolve("removecomments.txl"), infile, uncommented)) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				return false;
			}
		} catch (InterruptedException e1) {
			try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
			try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
			try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
			try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
			try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
			try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
			throw e1;
		}
		
		//check same length (not perfect check, but catch some error cases)
		int numlinesu;
		try {
			numlinesu = FileUtil.countLines(uncommented);
		} catch (IOException e) {
			try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
			try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
			try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
			try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
			try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
			try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
			throw e;
		}
		if(numlinesu != numlines) {
			try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
			try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
			try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
			try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
			try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
			try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
			return false;
		}
		
		//Extract fragment form comment removed file
		try {
			FragmentUtil.extractFragment(Fragment.createFragmentByLines(uncommented, startline, endline), fragment);
		} catch (IOException e) {
			try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
			try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
			try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
			try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
			try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
			try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
			throw e;
		}
		
		//Transform fragment into a space delimited token stream (embeded in first line of a file)
		
		try {
			if(0!=runTxl(getTxlDirectory(language).resolve("tokenize.txl"), fragment, fragment_tokstream)) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				return false;
			}
		} catch (InterruptedException e1) {
			try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
			try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
			try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
			try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
			try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
			try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
			throw e1;
		}
		
		//Extract token stream from file and turn it into a regex search pattern
		String fragmentpattern = "";
		try {
			Scanner scanner = new Scanner(fragment_tokstream);
			while(scanner.hasNextLine()) {
				String tok = scanner.nextLine();
				//System.out.println(tok);
				tok = Pattern.quote(tok); //escape for regex
				if(scanner.hasNext()) {
					fragmentpattern = fragmentpattern + tok + "\\s+";
				} else {
					fragmentpattern = fragmentpattern + tok;
				}
			}
			scanner.close();
		} catch (IOException e) {
			try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
			try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
			try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
			try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
			try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
			try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
			throw e;
		}
		
		//System.out.println(fragmentpattern);
		
	//Search space adjusted pretty-printed version for the fragment
		Pattern p = Pattern.compile(fragmentpattern);
		Matcher m = p.matcher(file);
		if(!m.find()) {
			//System.out.println(fragmentpattern);
			//System.out.println(file);
			//System.out.println("here6");
			return false;
		}
		String pfragment = m.group();
		
	//Write the fragment to output file
		PrintWriter pw = new PrintWriter(new FileWriter(fragment_beforeRemoveEmptyLines.toFile()));
		pw.print(pfragment);
		pw.print("\n");
		pw.flush();
		pw.close();
	
	//Remove empty lines from fragment
		try {
			FileUtil.removeEmptyLines(fragment_beforeRemoveEmptyLines, outfile);
		} catch(IOException e) {
			try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
			try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
			try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
			try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
			try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
			try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
			throw e;
		}
		
	//Cleanup
		try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
		try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
		try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
		try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
		try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
		try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
		
		return true;
	}
	
	/**
	 * Pretty prints a code fragment in a complete source file.  Maintains original formatting.  Slow and possibly not perfectly reliable.
	 * @param infile
	 * @param outfile
	 * @param startline
	 * @param endline
	 * @param language
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static boolean prettyprint_keepformat(Path infile, Path outfile, int startline, int endline, int language) throws FileNotFoundException, IOException, InterruptedException {
		//Check Arguments
			//Objects
			Objects.requireNonNull(infile);
			Objects.requireNonNull(outfile);
		
			//Language
			isLanguageSupported(language);
			
			//Input File
			if(!Files.exists(infile)) { //should exist
				throw new FileNotFoundException("Input file does not exist.");
			}
			if(!Files.isRegularFile(infile)) {
				throw new IllegalArgumentException("infile is not a regular file.");
			}
			if(!Files.isReadable(infile)) { //should be readable
				throw new IOException("Input file can not be read.");
			}
			
			//Output File
			Files.deleteIfExists(outfile);
			Files.createFile(outfile);
			if(!Files.exists(outfile)) { //create output file
				throw new IOException("Output file could not be created.");
			}
			if(!Files.isWritable(outfile)) { //should be able to write
				throw new IOException("Output file is not writable.");
			}
			
			//Startline/Endline are appropraite
			if(endline < startline) {
				throw new IllegalArgumentException("Endline preceeds startline.");
			}
			int numlines = FileUtil.countLines(infile);
			if(endline > numlines) {
				throw new IllegalArgumentException("Endline exceeds length of file.");
			}
			
			Path prettyfile=null;
			Path prettyfile_spaced=null;
			Path uncommented=null;
			Path fragment=null;
			Path fragment_tokstream=null;
			Path fragment_beforeRemoveEmptyLines = null;
			try {
				prettyfile = Files.createTempFile("LineValidator_prettyfile", null);
				prettyfile_spaced = Files.createTempFile("LineValidator_prettyfile_spaced", null);
				uncommented = Files.createTempFile("LineValidator_uncommented", null);
				fragment = Files.createTempFile("LineValidator_fragment", null);
				fragment_tokstream = Files.createTempFile("LineValidator_fragment_tokstream", null);
				fragment_beforeRemoveEmptyLines = Files.createTempFile("LineValidator_fragment_beforeRemoveEmptyLine", null);
			} catch (IOException e) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				//try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				throw e;
			}
			
	// Perform pretty-printing
		//PrettyPrint file containing the fragment
			try {
				if(0 != runTxl(getTxlDirectory(language).resolve("PrettyPrint.txl"), infile, prettyfile)) {
					try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
					try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
					try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
					try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
					try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
					try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
					//System.out.println("here1");
					return false;
				}
			} catch (InterruptedException e1) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				throw e1;
			}
			
			FileUtils.writeLines(prettyfile_spaced.toFile(), FileUtils.readLines(prettyfile.toFile()));
			
		//Add spaces between each token of the pretty-printed file
//			try {
//				if(0 != runTxl(getTxlDirectory(language).resolve("spacer.txl"), prettyfile, prettyfile_spaced)) {
//					try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
//					try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
//					try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
//					try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
//					try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
//					try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
//					return false;
//				}
//			} catch (InterruptedException e1) {
//				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
//				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
//				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
//				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
//				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
//				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
//				throw e1;
//			}
			
		//Load space adjusted pretty-printed file into a string, fix whitespace so all tokens are 1 space apart, no tabs, newlines persist
			String file = new String();
			try {
				Scanner scanner = new Scanner(prettyfile_spaced);
				while(scanner.hasNextLine()) {
					String line = scanner.nextLine();
						file = file + line + "\n";
				}
				scanner.close();
			} catch (IOException e) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				throw e;
			}
			
	//Create fragment search pattern
			//Remove comments from original file
			try {
				if(0 != runTxl(getTxlDirectory(language).resolve("removecomments.txl"), infile, uncommented)) {
					try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
					try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
					try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
					try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
					try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
					try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
					return false;
				}
			} catch (InterruptedException e1) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				throw e1;
			}
			
			//check same length (not perfect check, but catch some error cases)
			int numlinesu;
			try {
				numlinesu = FileUtil.countLines(uncommented);
			} catch (IOException e) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				throw e;
			}
			if(numlinesu != numlines) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				return false;
			}
			
			//Extract fragment from comment removed file
			try {
				FragmentUtil.extractFragment(Fragment.createFragmentByLines(uncommented, startline, endline), fragment);
			} catch (IOException e) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				throw e;
			}
			
			//Transform fragment into a space delimited token stream (embeded in first line of a file)
			
			try {
				if(0!=runTxl(getTxlDirectory(language).resolve("tokenize.txl"), fragment, fragment_tokstream)) {
					try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
					try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
					try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
					try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
					try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
					try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
					return false;
				}
			} catch (InterruptedException e1) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				throw e1;
			}
			
			//Extract token stream from file and turn it into a regex search pattern
			String fragmentpattern = "";
			try {
				Scanner scanner = new Scanner(fragment_tokstream);
				while(scanner.hasNextLine()) {
					String tok = scanner.nextLine();
					//System.out.println(tok);
					tok = Pattern.quote(tok); //escape for regex
					if(scanner.hasNext()) {
						fragmentpattern = fragmentpattern + tok + "\\s*";
					} else {
						fragmentpattern = fragmentpattern + tok;
					}
				}
				scanner.close();
			} catch (IOException e) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				throw e;
			}
			
			//System.out.println(fragmentpattern);
			
		//Search space adjusted pretty-printed version for the fragment
			Pattern p = Pattern.compile(fragmentpattern, Pattern.DOTALL);
			Matcher m = p.matcher(file);
			if(!m.find()) {
				//System.out.println(fragmentpattern);
				//System.out.println(file);
				//System.out.println("here6");
				return false;
			}
			String pfragment = m.group();
			
		//Write the fragment to output file
			PrintWriter pw = new PrintWriter(new FileWriter(fragment_beforeRemoveEmptyLines.toFile()));
			pw.print(pfragment);
			pw.print("\n");
			pw.flush();
			pw.close();
		
		//Remove empty lines from fragment
			try {
				FileUtil.removeEmptyLines(fragment_beforeRemoveEmptyLines, outfile);
			} catch(IOException e) {
				try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
				try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
				try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
				try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
				try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
				try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
				throw e;
			}
			
		//Cleanup
			try {if (prettyfile != null) Files.deleteIfExists(prettyfile);} catch (Exception exception) {};
			try {if (prettyfile_spaced != null) Files.deleteIfExists(prettyfile_spaced);} catch (Exception exception) {};
			try {if (uncommented != null) Files.deleteIfExists(uncommented);} catch (Exception exception) {};
			try {if (fragment != null) Files.deleteIfExists(fragment);} catch (Exception exception) {};
			try {if (fragment_tokstream != null) Files.deleteIfExists(fragment_tokstream);} catch (Exception exception) {};
			try {if (fragment_beforeRemoveEmptyLines != null) Files.deleteIfExists(fragment_beforeRemoveEmptyLines);} catch (Exception exception) {};
			
			return true;
		}
}
