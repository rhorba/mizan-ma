package ma.mizan.contracts.exception;

public class AdminAccessRequiredException extends RuntimeException {

	public AdminAccessRequiredException() {
		super("This endpoint requires the ADMIN role");
	}
}
