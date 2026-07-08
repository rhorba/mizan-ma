package ma.mizan.auth.config;

import ma.mizan.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * auth-service only needs the password encoder + JWT issuance; it does not run
 * Spring Security's filter chain — JWT validation happens at the Gateway
 * (ADR-3).
 */
@Configuration
class SecurityBeansConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	JwtService jwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expirationMs) {
		return new JwtService(secret, expirationMs);
	}
}
