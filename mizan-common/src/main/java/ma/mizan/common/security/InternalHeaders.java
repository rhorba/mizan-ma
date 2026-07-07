package ma.mizan.common.security;

/**
 * Trusted headers the Gateway sets after validating a JWT (ADR-3). Downstream
 * services trust these only because the internal network is not publicly
 * exposed.
 */
public final class InternalHeaders {

	public static final String USER_ID = "X-User-Id";
	public static final String USER_ROLE = "X-User-Role";

	private InternalHeaders() {
	}
}
