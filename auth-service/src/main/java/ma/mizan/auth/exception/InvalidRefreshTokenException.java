package ma.mizan.auth.exception;

public class InvalidRefreshTokenException extends RuntimeException {

	public InvalidRefreshTokenException() {
		super("Refresh token is invalid, expired, or revoked");
	}
}
