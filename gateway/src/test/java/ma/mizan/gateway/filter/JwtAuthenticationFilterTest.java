package ma.mizan.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import ma.mizan.common.security.InternalHeaders;
import ma.mizan.common.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class JwtAuthenticationFilterTest {

	private static final String SECRET = "test-secret-at-least-256-bits-long-for-hs256-signing";

	private JwtAuthenticationFilter filter;
	private JwtService jwtService;
	private AtomicReference<ServerHttpRequest> forwardedRequest;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService(SECRET, 3_600_000);
		filter = new JwtAuthenticationFilter(jwtService, new ObjectMapper());
		forwardedRequest = new AtomicReference<>();
	}

	@Test
	void publicLoginPathBypassesValidationAndStripsSpoofedHeaders() {
		var request = MockServerHttpRequest.post("/api/v1/auth/login")
				.header(InternalHeaders.USER_ID, "attacker-supplied-id").build();
		var exchange = MockServerWebExchange.from(request);

		filter.filter(exchange, capturingChain()).block();

		assertThat(forwardedRequest.get()).isNotNull();
		assertThat(forwardedRequest.get().getHeaders().getFirst(InternalHeaders.USER_ID)).isNull();
	}

	@Test
	void publicVerifyEmailAndResendVerificationPathsBypassValidation() {
		for (String path : new String[]{"/api/v1/auth/verify-email", "/api/v1/auth/resend-verification"}) {
			forwardedRequest.set(null);
			var exchange = MockServerWebExchange.from(MockServerHttpRequest.post(path));

			filter.filter(exchange, capturingChain()).block();

			assertThat(forwardedRequest.get()).as("path %s should bypass JWT validation", path).isNotNull();
		}
	}

	@Test
	void protectedPathWithoutTokenReturns401() {
		var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/contracts"));

		filter.filter(exchange, capturingChain()).block();

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(forwardedRequest.get()).isNull();
	}

	@Test
	void protectedPathWithExpiredTokenReturns401() {
		var expiredTokenService = new JwtService(SECRET, -1_000);
		String expiredToken = expiredTokenService.generateAccessToken(UUID.randomUUID(), "INDIVIDUAL");
		var exchange = MockServerWebExchange
				.from(MockServerHttpRequest.get("/api/v1/contracts").header("Authorization", "Bearer " + expiredToken));

		filter.filter(exchange, capturingChain()).block();

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(forwardedRequest.get()).isNull();
	}

	@Test
	void protectedPathWithValidTokenForwardsTrustedIdentityHeaders() {
		UUID userId = UUID.randomUUID();
		String token = jwtService.generateAccessToken(userId, "BUSINESS");
		var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/contracts")
				.header("Authorization", "Bearer " + token).header(InternalHeaders.USER_ID, "attacker-supplied-id"));

		filter.filter(exchange, capturingChain()).block();

		assertThat(forwardedRequest.get()).isNotNull();
		assertThat(forwardedRequest.get().getHeaders().getFirst(InternalHeaders.USER_ID)).isEqualTo(userId.toString());
		assertThat(forwardedRequest.get().getHeaders().getFirst(InternalHeaders.USER_ROLE)).isEqualTo("BUSINESS");
	}

	private org.springframework.cloud.gateway.filter.GatewayFilterChain capturingChain() {
		return exchange -> {
			forwardedRequest.set(exchange.getRequest());
			return Mono.empty();
		};
	}
}
