package util;
/**
 * @author Jeff Svajlenko
 */
public class Token {
	private String token;
	private int file_token_number;
	private int line_token_number;
	private int line_number;
	
	public Token(String token) {
		this.token = token;
		this.file_token_number = -1;
		this.line_number = -1;
	}
	
	public Token(String token, int file_token_number, int line_number, int line_token_number) {
		this.token = token;
		this.file_token_number = file_token_number;
		this.line_number = line_number;
		this.line_token_number = line_token_number;
	}
	
	public String getText() {
		return this.token;
	}
	
	public int getLineNumber() {
		return this.line_number;
	}

	public int getTokenNumber() {
		return this.file_token_number;
	}
	
	public int getLineTokenNumber() {
		return this.line_token_number;
	}
	
	public String toString() {
		return "Token(" + "File Token #: " + file_token_number + ", Line # "+ line_number + ", Line Token #: " + line_token_number + ", Value: " + token + ")";
	}
}