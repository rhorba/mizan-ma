package ma.mizan.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import ma.mizan.auth.controller.dto.AuthResponse;
import ma.mizan.auth.controller.dto.LoginRequest;
import ma.mizan.auth.controller.dto.RegisterRequest;
import ma.mizan.auth.controller.dto.RegisterResponse;
import ma.mizan.auth.domain.RefreshToken;
import ma.mizan.auth.domain.User;
import ma.mizan.auth.exception.EmailAlreadyExistsException;
import ma.mizan.auth.exception.InvalidCredentialsException;
import ma.mizan.auth.exception.InvalidRefreshTokenException;
import ma.mizan.auth.repository.RefreshTokenRepository;
import ma.mizan.auth.repository.UserRepository;
import ma.mizan.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenHasher refreshTokenHasher;
	private final long refreshExpirationMs;

	public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
			PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenHasher refreshTokenHasher,
			@Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshTokenHasher = refreshTokenHasher;
		this.refreshExpirationMs = refreshExpirationMs;
	}

	@Transactional
	public RegisterResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new EmailAlreadyExistsException(request.email());
		}
		var user = new User(request.email(), passwordEncoder.encode(request.password()), request.role().toDomainRole());
		userRepository.save(user);
		return new RegisterResponse(user.getId(), user.getEmail(), user.getRole());
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email()).filter(User::isActive)
				.orElseThrow(InvalidCredentialsException::new);
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}
		return issueTokenPair(user);
	}

	@Transactional
	public AuthResponse refresh(String rawRefreshToken) {
		RefreshToken stored = refreshTokenRepository.findByTokenHash(refreshTokenHasher.hash(rawRefreshToken))
				.filter(RefreshToken::isUsable).orElseThrow(InvalidRefreshTokenException::new);
		User user = userRepository.findById(stored.getUserId()).filter(User::isActive)
				.orElseThrow(InvalidRefreshTokenException::new);

		stored.revoke();
		return issueTokenPair(user);
	}

	@Transactional
	public void logout(String rawRefreshToken) {
		refreshTokenRepository.findByTokenHash(refreshTokenHasher.hash(rawRefreshToken))
				.ifPresent(RefreshToken::revoke);
	}

	private AuthResponse issueTokenPair(User user) {
		String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name());

		String rawRefreshToken = refreshTokenHasher.generateRawToken();
		var refreshToken = new RefreshToken(user.getId(), refreshTokenHasher.hash(rawRefreshToken),
				Instant.now().plus(refreshExpirationMs, ChronoUnit.MILLIS));
		refreshTokenRepository.save(refreshToken);

		return new AuthResponse(accessToken, rawRefreshToken, jwtService.getExpirationMs());
	}
}
