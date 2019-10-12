package txl;

/**
 * @author Jeff Svajlenko
 */
public class LanguageNotSupportedException extends RuntimeException {
	private static final long serialVersionUID = -8139507978746432424L;
	public LanguageNotSupportedException() {
		super();
	}
	public LanguageNotSupportedException(String str) {
		super(str);
	}
}
