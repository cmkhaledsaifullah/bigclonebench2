package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Objects;

import txl.TXLException;
import txl.TXLUtil;

/**
 * @author Jeff Svajlenko
 */
public class TokenIterator implements Iterable<Token>  {
	
	private ArrayList<Token> tokens = new ArrayList<Token>();
	private ArrayList<Integer> lineIndex = new ArrayList<Integer>();
	private ListIterator<Token> iterator;
	int numLines = -1;
	
	public TokenIterator(Path file, int language) throws IOException, InterruptedException, TXLException {
		//Check Input
		Objects.requireNonNull(file);
		TXLUtil.requireLanguageSupported(language);
		if(!Files.exists(file)) {throw new FileNotFoundException("File does not exist.");}
		if(!Files.isRegularFile(file)) {throw new IllegalArgumentException("File is not a regular file.");}
		
		//Tokenize the file
		Path tmpfile = Files.createTempFile("TokenIterator_", "_TokenizedLines");
		TXLUtil.tokenizeLines(file, tmpfile, language);
		
		//Populate list
		BufferedReader br = new BufferedReader(new FileReader(tmpfile.toFile()));
		lineIndex.add(0);
		String line = br.readLine();
		int fileTokenNum = 1;
		int lineTokenNum = 1;
		int lineNum = 0;
		while(line != null) {
			if(!line.startsWith(" ")) {
				lineNum++;
				lineTokenNum = 1;
				lineIndex.add(fileTokenNum);
			}
			line = line.trim();
			if(!line.equals(""))
				tokens.add(new Token(line, fileTokenNum++, lineNum, lineTokenNum++));
			line = br.readLine();
		}
		br.close();
		
		//Init
		this.numLines = tokens.get(tokens.size()-1).getLineNumber();
		
		//Init iterator
		iterator = tokens.listIterator();
		
		//Cleanup
		Files.delete(tmpfile);
		
	}

	@Override
	public Iterator<Token> iterator() {
		return Collections.unmodifiableList(tokens).iterator();
	}
	
	public Token next() {
		return iterator.next();
	}
	
	public boolean hasNext() {
		return iterator.hasNext();
	}
	
	public Token previous() {
		return iterator.previous();
	}
	
	public boolean hasPrevious() {
		return iterator.hasPrevious();
	}
	
	public void reset() {
		iterator = tokens.listIterator();
	}
	
	public void gotoToken(int token) throws IndexOutOfBoundsException {
		iterator = tokens.listIterator(token - 1);
	}
	
	//throws element exception if no such line, if goto line that has no tokens, next token will be the next token after that line
	public void gotoLine(int line) {
		iterator = tokens.listIterator(lineIndex.get(line)-1);
	}
	
	// line the last token is on
	public int numLines() {
		return this.numLines;
	}
	
}
