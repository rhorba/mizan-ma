package ma.mizan.auth.exception;

public class InvalidVerificationTokenException extends RuntimeException {

	public InvalidVerificationTokenException() {
		super("Verification link is invalid, expired, or already used");
	}
}
