package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author Jeff Svajlenko
 *
 * A collection of utility functions for files.
 * 
 */
public class FileUtil {
	
	/**
	 * 
	 * @param f1
	 * @param f2
	 * @return
	 * @throws IOException
	 */
	public static boolean isSameFile(Path f1, Path f2) throws IOException {
		return isSameFile(f1.toAbsolutePath().normalize().toFile(), f2.toAbsolutePath().normalize().toFile());
	}
	
	/**
	 * 
	 * @param f1
	 * @param f2
	 * @return
	 * @throws IOException
	 */
	public static boolean isSameFile(File f1, File f2) throws IOException {
		if (f1.getCanonicalFile().equals(f2.getCanonicalFile()))
			return true;
		else
			return false;
	}
	
	/**
	 * Returns a list of all files under the specified directory.  Symbolic links are neither included in the results, or followed in the search.
	 * Paths are absolute and normalised.
	 * @param directory the directory to inventory.
	 * @return a list of all files under the specified directory.  Each is specified by its absolute path.  (List is ArrayList).
	 * @throws IOException if an IOException occurs.
	 * @throws FileNotFoundException If the specified directory does not exist.
	 * @throws IllegalArgumentException If the specified path points to a file not a directory.
	 */
	public static List<Path> fileInventory(Path directory) throws IOException {
		if(!Files.exists(directory)) {
			throw new FileNotFoundException("Specified directory does not exist.");
		}
		if(!Files.isDirectory(directory)) {
			throw new IllegalArgumentException("Specified path is not a directory.");
		}
		InventoryFiles visitor = new InventoryFiles();
		EnumSet<FileVisitOption> opts = EnumSet.noneOf(FileVisitOption.class);
		Files.walkFileTree(directory, opts, Integer.MAX_VALUE, visitor);
		return visitor.getInventory();
	}
	private static class InventoryFiles extends SimpleFileVisitor<Path> {
		List<Path> inventory;
		
		InventoryFiles() {
			inventory = new ArrayList<Path>();
		}
		
		public List<Path> getInventory() {
			return this.inventory;
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
			if (!attr.isSymbolicLink() && attr.isRegularFile()) {
				inventory.add(file.toAbsolutePath().normalize());
			}
			return FileVisitResult.CONTINUE;
		}
	}
	
	/**
	 * Returns a list of all directories in the specified directory.  Symbolic links are neither included in the results, or followed in the search.
	 * Paths are absolute and normalised.
	 * @param directory the directory to inventory.
	 * @return a list of all directories in the specified directory.  Each is specified by its absolute path.  (List is ArrayList).
	 * @throws IOException if an IOException occurs.
	 * @throws FileNotFoundException If the specified directory does not exist.
	 * @throws IllegalArgumentException If the specified path points to a file not a directory.
	 */
	public static List<Path> directoryInventory(Path directory) throws IOException {
		if(!Files.exists(directory)) {
			throw new FileNotFoundException("Specified directory does not exist.");
		}
		if(!Files.isDirectory(directory)) {
			throw new IllegalArgumentException("Specified path is not a directory.");
		}
		InventoryDirectories visitor = new InventoryDirectories(directory);
		EnumSet<FileVisitOption> opts = EnumSet.noneOf(FileVisitOption.class);
		Files.walkFileTree(directory, opts, Integer.MAX_VALUE, visitor);
		return visitor.getInventory();
	}
	//Class used by directoryInventory to collect all found directories which are not symbolic links.
	private static class InventoryDirectories extends SimpleFileVisitor<Path> {
		List<Path> inventory;
		Path root;
		
		InventoryDirectories(Path root) {
			inventory = new ArrayList<Path>();
			this.root = root.toAbsolutePath().normalize();
		}
		
		public List<Path> getInventory() {
			return this.inventory;
		}
		
		@Override
		public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attr) {
			if (!attr.isSymbolicLink() && attr.isDirectory()) {
				if(!file.toAbsolutePath().equals(root)) {
					inventory.add(file.toAbsolutePath());
				}
			}
			return FileVisitResult.CONTINUE;
		}
	}
	
	/**
	 * Returns if specified path denotes a leaf directory (a directory which contains no directories).
	 * Paths are absolute and normalised.
	 * @param directory Path of directory to test for leaf status.
	 * @return if specified path denotes a leaf directory.
	 * @throws FileNotFoundException If the specified directory does not exist.
	 * @throws IOException If an IOException occurs when investigating the directory and its children.
	 */
	public static boolean isLeafDirectory(Path directory) throws FileNotFoundException, IOException {
		if(!Files.exists(directory)) {
			throw new FileNotFoundException("Specified directory does not exist.");
		}
		if(!Files.isDirectory(directory)) {
			throw new IllegalArgumentException("Specified path is not a directory.");
		}
		
		DirectoryStream<Path> ds = null;
		try {
			ds = Files.newDirectoryStream(directory);
			for(Path p : ds) {
				if(Files.isDirectory(p)) {
					ds.close();
					return false;
				}
			}
			ds.close();
		} catch (IOException e) {
			if(ds != null) {
				try {ds.close();} catch (IOException ee){};
			}
			throw e;
		}
		return true;
	}
	
	/**
	 * Returns the number of lines in the file specified.
	 * @param file The location of the file.
	 * @return The number of lines in the file.
	 * @throws IOException If an I/O exception occurs while counting the lines.
	 * @throws FileNotFoundException If path points to a file that does not exist.
	 * @throws IllegalArgumentException If the specified file is not a regular file (symbolic links are followed), or if the file is not readable.
	 */
	public static int countLines(Path file) throws FileNotFoundException, IOException {
		//Check input
		Objects.requireNonNull(file);
		if(!Files.exists(file)) {
			throw new FileNotFoundException("Path file points to a file that does not exist.");
		}
		if(!Files.isRegularFile(file)) {
			throw new IllegalArgumentException("Path file points to a file that is not regular.");
		}
		if(!Files.isReadable(file)) {
			throw new IllegalArgumentException("Path file points to a file that is not readable.");
		}
		
		//Count lines
		int countRec=0;
		LineNumberReader reader = null;
		try {
			reader = new LineNumberReader(new FileReader(file.toFile()));
			while(reader.readLine() != null);
			countRec = reader.getLineNumber();
			reader.close();
		}
		catch(IOException e)
		{
			if(reader != null) {
				try{reader.close();} catch (IOException ee) {}
			}
			throw new IOException();
		}
		return countRec;
	}
	
	
	
	/**
	 * Returns the % similarity (by line) between the two files.  Similarity is measured by first finding the ratio of the lines in each
	 * fragment which is unique to that fragment.  Similarity is reported as one minus this ratio from the fragment with the largest ratio
	 * of unique lines.  Empty lines are ignored during this measurement.  Lines which differ by extra whitespace are considered equivalent.
	 * @param file1 The first file.  Must exist, be a regular file, and be readable.
	 * @param file2 The second file.  Must exist, be a regular file, and be readable.
	 * @return the similarity between the fragments.
	 * @throws FileNotFoundException If either of file1 or file2 does not exist.
	 * @throws IllegalArgumentException If either of file1 or file 2 are not regular files or are not readable.
	 * @throws IOException If an IO exception occurs during the reading of these files.
	 */
	public static double getSimilarity(Path file1, Path file2) throws FileNotFoundException, IOException {
		//Check input
		Objects.requireNonNull(file1);
		Objects.requireNonNull(file2);
		if(!Files.exists(file1)) {
			throw new FileNotFoundException("File1 does not exist.");
		}
		if(!Files.exists(file2)) {
			throw new FileNotFoundException("File2 does not exist.");
		}
		if(!Files.isRegularFile(file1)) {
			throw new IllegalArgumentException("File 1 is not a regular file.");
		}
		if(!Files.isRegularFile(file2)) {
			throw new IllegalArgumentException("File 2 is not a regular file.");
		}
		if(!Files.isReadable(file1)) {
			throw new IllegalArgumentException("File 1 is not readable.");
		}
		if(!Files.isReadable(file2)) {
			throw new IllegalArgumentException("File 2 is not readable.");
		}
		
		// Get line counts
		int fileSize1 = FileUtil.countLines(file1);
		int fileSize2 = FileUtil.countLines(file2);
		
		//Check similarity
		double leftUnique=0.0;
		double rightUnique=0.0;
		String command;
		if (SystemUtils.IS_OS_WINDOWS) {
			 command = "cmd /c diff \"" + file1.toAbsolutePath().normalize().toString() + "\" \"" + file2.toAbsolutePath().normalize().toString() + "\"";
		} else {
			 command = "diff \"" + file1.toAbsolutePath().normalize().toString() + "\" \"" + file2.toAbsolutePath().normalize().toString() + "\"";
		}
		Process p = Runtime.getRuntime().exec(command);
		int leftDistinct=0; 
		int rightDistinct=0;
		
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		new StreamGobbler(p.getErrorStream()).start();
		
		String line = null;
		
		while ((line = in.readLine()) != null) {
			if (line.substring(0,1).contains("<")) {
				leftDistinct++;
			}
			if(line.substring(0,1).contains(">")) {
				rightDistinct++;
			}
		}          
		leftUnique = ((double) ((double)leftDistinct / (double)(fileSize1)));
		rightUnique = ((double) ((double)rightDistinct / (double)(fileSize2)));

		//Return similarity
		System.out.println(leftDistinct);
		System.out.println(fileSize1);
		System.out.println(leftUnique + " " +  rightUnique);
		return 1.0d - Math.max(leftUnique, rightUnique);
	}
	

	public static double getUPL(Path file1, Path file2) throws FileNotFoundException, IOException {
		//Check input
		Objects.requireNonNull(file1);
		Objects.requireNonNull(file2);
		if(!Files.exists(file1)) {
			throw new FileNotFoundException("File1 does not exist.");
		}
		if(!Files.exists(file2)) {
			throw new FileNotFoundException("File2 does not exist.");
		}
		if(!Files.isRegularFile(file1)) {
			throw new IllegalArgumentException("File 1 is not a regular file.");
		}
		if(!Files.isRegularFile(file2)) {
			throw new IllegalArgumentException("File 2 is not a regular file.");
		}
		if(!Files.isReadable(file1)) {
			throw new IllegalArgumentException("File 1 is not readable.");
		}
		if(!Files.isReadable(file2)) {
			throw new IllegalArgumentException("File 2 is not readable.");
		}
		
		// Get line counts
		int fileSize1 = FileUtil.countLines(file1);
		
		//Check similarity
		double leftUnique=0.0;
		Process p = Runtime.getRuntime().exec("diff " + file1.toAbsolutePath().normalize().toString() + " " + file2.toAbsolutePath().normalize().toString());
		int leftDistinct=0; 
		
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		new StreamGobbler(p.getErrorStream()).start();
		
		String line = null;
		
		while ((line = in.readLine()) != null) {
			if (line.substring(0,1).contains("<")) {
				leftDistinct++;
			}
		}          
		
		leftUnique = ((double) ((double)leftDistinct / (double)(fileSize1)));
		
		return leftUnique;
	}
	
	/**
	 * Removes empty lines from a file (files containing only whitespace).
	 * Input and output files can be the same file (this buffers the file's contents).
	 * @param infile The input file.
	 * @param outfile The output file, must not be the same as the input file.
	 * @throws FileNotFoundException If the input file was not found.
	 * @throws IOException If an IO error occurs during the process.
	 */
	public static void removeEmptyLines(Path infile, Path outfile) throws FileNotFoundException, IOException{
	//Check Input
		Objects.requireNonNull(infile);
		Objects.requireNonNull(infile);
		infile = infile.toAbsolutePath().normalize();
		outfile = outfile.toAbsolutePath().normalize();
		if(!Files.exists(infile)) {
			throw new IllegalArgumentException("infile does not exist.");
		}
		if(!Files.isRegularFile(infile)) {
			throw new IllegalArgumentException("infile does not denote a regular file.");
		}
		if(!Files.isReadable(infile)) {
			throw new IllegalArgumentException("infile is not readable.");
		}
		//if(infile.equals(outfile)) {
		//	throw new IllegalArgumentException("Input and output file are the same.");
		//}
		
	//Perform removeEmptyLines
		LinkedList<String> contents = new LinkedList<String>();
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(infile.toFile()));
			String line = br.readLine();
			while(line != null) {
				if(!line.matches("\\s*")) {
					contents.add(line);
				}
				line = br.readLine();
			}
			br.close();
		} catch(FileNotFoundException e) {
			if(br != null) {
				try {br.close();} catch (IOException ee) {}
				br = null;
			}
			throw e;
		} catch (IOException e) {
			if(br != null) {
				try {br.close();} catch (IOException ee) {}
				br = null;
			}
			throw e;
		}
		
		if(Files.exists(outfile)) {
			Files.deleteIfExists(outfile);
			
		}
		Files.createFile(outfile);
		if(!Files.isWritable(outfile)) {
			throw new IllegalArgumentException("outfile not writeable.");
		}
		
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(outfile.toFile()));
			for (String s : contents) {
				pw.println(s);
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		pw.flush();
		pw.close();
	}
}
