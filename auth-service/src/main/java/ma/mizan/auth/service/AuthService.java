package ma.mizan.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import ma.mizan.auth.controller.dto.AuthResponse;
import ma.mizan.auth.controller.dto.LoginRequest;
import ma.mizan.auth.controller.dto.RegisterRequest;
import ma.mizan.auth.controller.dto.RegisterResponse;
import ma.mizan.auth.domain.EmailVerificationToken;
import ma.mizan.auth.domain.RefreshToken;
import ma.mizan.auth.domain.User;
import ma.mizan.auth.exception.EmailAlreadyExistsException;
import ma.mizan.auth.exception.EmailNotVerifiedException;
import ma.mizan.auth.exception.InvalidCredentialsException;
import ma.mizan.auth.exception.InvalidRefreshTokenException;
import ma.mizan.auth.exception.InvalidVerificationTokenException;
import ma.mizan.auth.exception.VerificationResendTooSoonException;
import ma.mizan.auth.repository.EmailVerificationTokenRepository;
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
	private final EmailVerificationTokenRepository verificationTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenHasher refreshTokenHasher;
	private final MailService mailService;
	private final long refreshExpirationMs;
	private final long verificationExpirationMs;
	private final long verificationResendCooldownMs;

	public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
			EmailVerificationTokenRepository verificationTokenRepository, PasswordEncoder passwordEncoder,
			JwtService jwtService, RefreshTokenHasher refreshTokenHasher, MailService mailService,
			@Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs,
			@Value("${app.email-verification-token-expiration-ms:86400000}") long verificationExpirationMs,
			@Value("${app.verification-resend-cooldown-ms:60000}") long verificationResendCooldownMs) {
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.verificationTokenRepository = verificationTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshTokenHasher = refreshTokenHasher;
		this.mailService = mailService;
		this.refreshExpirationMs = refreshExpirationMs;
		this.verificationExpirationMs = verificationExpirationMs;
		this.verificationResendCooldownMs = verificationResendCooldownMs;
	}

	@Transactional
	public RegisterResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new EmailAlreadyExistsException(request.email());
		}
		var user = new User(request.email(), passwordEncoder.encode(request.password()), request.role().toDomainRole());
		userRepository.save(user);
		issueVerificationToken(user);
		return new RegisterResponse(user.getId(), user.getEmail(), user.getRole());
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email()).filter(User::isActive)
				.orElseThrow(InvalidCredentialsException::new);
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}
		if (!user.isEmailVerified()) {
			throw new EmailNotVerifiedException();
		}
		return issueTokenPair(user);
	}

	@Transactional
	public void verifyEmail(String rawToken) {
		EmailVerificationToken stored = verificationTokenRepository.findByTokenHash(refreshTokenHasher.hash(rawToken))
				.filter(EmailVerificationToken::isUsable).orElseThrow(InvalidVerificationTokenException::new);
		User user = userRepository.findById(stored.getUserId()).orElseThrow(InvalidVerificationTokenException::new);

		user.markEmailVerified();
		stored.markUsed();
	}

	@Transactional
	public void resendVerification(String email) {
		// Silently no-op for an unknown/already-verified email rather than erroring, so
		// this
		// endpoint doesn't become a second account-enumeration surface alongside
		// register()'s
		// existing (and accepted) EMAIL_ALREADY_EXISTS leak.
		userRepository.findByEmail(email).filter(user -> !user.isEmailVerified()).ifPresent(user -> {
			verificationTokenRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId()).filter(
					token -> token.getCreatedAt().plusMillis(verificationResendCooldownMs).isAfter(Instant.now()))
					.ifPresent(token -> {
						throw new VerificationResendTooSoonException();
					});
			issueVerificationToken(user);
		});
	}

	private void issueVerificationToken(User user) {
		String rawToken = refreshTokenHasher.generateRawToken();
		var token = new EmailVerificationToken(user.getId(), refreshTokenHasher.hash(rawToken),
				Instant.now().plus(verificationExpirationMs, ChronoUnit.MILLIS));
		verificationTokenRepository.save(token);
		mailService.sendVerificationEmail(user.getEmail(), rawToken);
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
