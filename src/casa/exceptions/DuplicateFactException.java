package casa.exceptions;

public class DuplicateFactException extends Exception {

	private static final long serialVersionUID = -5951530043645362888L;

	public DuplicateFactException() {
	}

	public DuplicateFactException(String arg0) {
		super(arg0);
	}

	public DuplicateFactException(Throwable arg0) {
		super(arg0);
	}

	public DuplicateFactException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}