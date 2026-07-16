package ma.mizan.auth.exception;

public class VerificationResendTooSoonException extends RuntimeException {

	public VerificationResendTooSoonException() {
		super("A verification email was already sent recently — please check your inbox before requesting another");
	}
}
