package ma.mizan.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
class MailService {

	private static final Logger log = LoggerFactory.getLogger(MailService.class);

	private final JavaMailSender mailSender;
	private final String fromAddress;
	private final String frontendOrigin;
	private final boolean logVerificationLinks;

	MailService(JavaMailSender mailSender, @Value("${mail.from-address}") String fromAddress,
			@Value("${app.frontend-origin}") String frontendOrigin,
			@Value("${app.log-verification-links:false}") boolean logVerificationLinks) {
		this.mailSender = mailSender;
		this.fromAddress = fromAddress;
		this.frontendOrigin = frontendOrigin;
		this.logVerificationLinks = logVerificationLinks;
	}

	void sendVerificationEmail(String toEmail, String rawToken) {
		String link = "%s/verify-email?token=%s".formatted(frontendOrigin, rawToken);

		// Dev-only convenience so the flow is testable without a real SMTP provider
		// configured.
		// Never gated "on" outside local/dev config — see .env.example.
		if (logVerificationLinks) {
			log.warn("LOG_VERIFICATION_LINKS is enabled — verification link for {}: {}", toEmail, link);
		}

		var message = new SimpleMailMessage();
		message.setFrom(fromAddress);
		message.setTo(toEmail);
		message.setSubject("Verify your Mizan.ma account");
		message.setText("Welcome to Mizan.ma!\n\nPlease verify your email address by clicking the link below:\n\n"
				+ link
				+ "\n\nThis link expires in 24 hours. If you didn't create this account, you can ignore this email.");

		try {
			mailSender.send(message);
		} catch (MailException e) {
			// Registration must not fail just because the mail provider is unreachable —
			// the user can request a new link via POST /auth/resend-verification.
			log.warn("Failed to send verification email to {}: {}", toEmail, e.getMessage());
		}
	}
}
