package ma.mizan.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import ma.mizan.common.error.ApiError;
import ma.mizan.common.error.ErrorResponse;
import ma.mizan.common.security.InternalHeaders;
import ma.mizan.common.security.JwtService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Validates the JWT for every request and forwards identity via trusted
 * internal headers (ADR-3) — downstream services never see or parse the token
 * themselves. Public auth endpoints (register/login/refresh) and actuator
 * health checks bypass validation.
 *
 * <p>
 * Any client-supplied {@code X-User-Id}/{@code X-User-Role} header is always
 * stripped first, even on public routes — otherwise a caller could forge
 * identity directly and skip the JWT check entirely, since downstream services
 * trust these headers unconditionally.
 */
@Component
class JwtAuthenticationFilter implements GlobalFilter, Ordered {

	private static final List<String> PUBLIC_PATHS = List.of("/api/v1/auth/register", "/api/v1/auth/login",
			"/api/v1/auth/refresh", "/actuator/**");

	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private final JwtService jwtService;
	private final ObjectMapper objectMapper;

	JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
		this.jwtService = jwtService;
		this.objectMapper = objectMapper;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest.Builder strippedRequest = exchange.getRequest().mutate().headers(headers -> {
			headers.remove(InternalHeaders.USER_ID);
			headers.remove(InternalHeaders.USER_ROLE);
		});

		String path = exchange.getRequest().getURI().getPath();
		if (isPublic(path)) {
			return chain.filter(exchange.mutate().request(strippedRequest.build()).build());
		}

		String token = extractBearerToken(exchange.getRequest().getHeaders());
		if (token == null) {
			return unauthorized(exchange, "Missing bearer token");
		}

		Claims claims;
		try {
			claims = jwtService.parseClaims(token);
		} catch (JwtException | IllegalArgumentException e) {
			return unauthorized(exchange, "Invalid or expired token");
		}

		ServerHttpRequest authenticatedRequest = strippedRequest.header(InternalHeaders.USER_ID, claims.getSubject())
				.header(InternalHeaders.USER_ROLE, claims.get(JwtService.ROLE_CLAIM, String.class)).build();
		return chain.filter(exchange.mutate().request(authenticatedRequest).build());
	}

	private boolean isPublic(String path) {
		return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	private String extractBearerToken(HttpHeaders headers) {
		String header = headers.getFirst(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith("Bearer ")) {
			return header.substring("Bearer ".length());
		}
		return null;
	}

	private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		var apiError = new ApiError("UNAUTHORIZED", message, UUID.randomUUID().toString());
		byte[] body;
		try {
			body = objectMapper.writeValueAsBytes(ErrorResponse.of(apiError));
		} catch (Exception e) {
			body = ("{\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"" + message + "\"}}")
					.getBytes(StandardCharsets.UTF_8);
		}
		DataBuffer buffer = response.bufferFactory().wrap(body);
		return response.writeWith(Mono.just(buffer));
	}
}
