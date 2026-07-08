package ma.mizan.gateway.config;

import ma.mizan.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class JwtServiceConfig {

	@Bean
	JwtService jwtService(@Value("${jwt.secret}") String secret) {
		// The Gateway only ever parses tokens (ADR-3) — expiry is embedded in each
		// token,
		// so the expiration-ms constructor argument is irrelevant here.
		return new JwtService(secret, 0);
	}
}
