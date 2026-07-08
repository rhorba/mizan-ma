package ma.mizan.aianalysis.exception;

public class UnauthorizedInternalCallException extends RuntimeException {

	public UnauthorizedInternalCallException() {
		super("Missing or invalid internal service token");
	}
}
