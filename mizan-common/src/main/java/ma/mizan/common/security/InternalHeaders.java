package ma.mizan.common.security;

/**
 * Trusted headers the Gateway sets after validating a JWT (ADR-3). Downstream
 * services trust these only because the internal network is not publicly
 * exposed.
 */
public final class InternalHeaders {

	public static final String USER_ID = "X-User-Id";
	public static final String USER_ROLE = "X-User-Role";

	/**
	 * Shared-secret header for direct service-to-service calls that bypass the
	 * Gateway entirely (e.g. contracts-service -> ai-analysis-service, which is not
	 * gateway-routed).
	 */
	public static final String INTERNAL_TOKEN = "X-Internal-Token";

	private InternalHeaders() {
	}
}
