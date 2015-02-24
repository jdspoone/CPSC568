package casa.exceptions;

public class ConflictingFactException extends Exception {

	private static final long serialVersionUID = -5951530043645362888L;

	public ConflictingFactException() {
	}

	public ConflictingFactException(String arg0) {
		super(arg0);
	}

	public ConflictingFactException(Throwable arg0) {
		super(arg0);
	}

	public ConflictingFactException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
