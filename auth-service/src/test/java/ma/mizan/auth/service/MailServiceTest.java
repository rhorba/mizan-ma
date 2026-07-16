package ma.mizan.auth.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class MailServiceTest {

	@Test
	void sendingIsAttemptedRegardlessOfTheLogVerificationLinksFlag() {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		var service = new MailService(mailSender, "noreply@mizan.ma", "http://localhost:4200", false);

		service.sendVerificationEmail("user@example.com", "raw-token-value");

		verify(mailSender).send(any(SimpleMailMessage.class));
	}

	@Test
	void aMailProviderFailureDoesNotPropagate() {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		doThrow(new MailSendException("SMTP unreachable")).when(mailSender).send(any(SimpleMailMessage.class));
		var service = new MailService(mailSender, "noreply@mizan.ma", "http://localhost:4200", true);

		// Registration must succeed even when the mail provider is down — the resend
		// endpoint is the recovery path, not a 500 on register().
		assertDoesNotThrow(() -> service.sendVerificationEmail("user@example.com", "raw-token-value"));
	}
}
