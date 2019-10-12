package txl;


/**
 * @author Jeff Svajlenko
 */
public class TXLException extends Exception {
	private static final long serialVersionUID = -6011036810742187576L;

	/**
	 * 
	 */
	public TXLException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TXLException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TXLException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public TXLException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public TXLException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
}
