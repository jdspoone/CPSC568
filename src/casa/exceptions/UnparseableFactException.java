package casa.exceptions;

public class UnparseableFactException extends Exception {

	private static final long serialVersionUID = -8015316219453539940L;

	public UnparseableFactException() {
	}

	public UnparseableFactException(String arg0) {
		super(arg0);
	}

	public UnparseableFactException(Throwable arg0) {
		super(arg0);
	}

	public UnparseableFactException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}