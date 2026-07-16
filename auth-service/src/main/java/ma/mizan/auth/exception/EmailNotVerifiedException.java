package ma.mizan.auth.exception;

public class EmailNotVerifiedException extends RuntimeException {

	public EmailNotVerifiedException() {
		super("Please verify your email address before signing in");
	}
}
