package ma.mizan.aianalysis.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class AnthropicApiExceptionTest {

	@Test
	void messageOnlyConstructorSetsMessageAndNoCause() {
		var ex = new AnthropicApiException("upstream timed out");

		assertEquals("upstream timed out", ex.getMessage());
		assertNull(ex.getCause());
	}

	@Test
	void messageAndCauseConstructorSetsBoth() {
		var cause = new RuntimeException("connection reset");

		var ex = new AnthropicApiException("upstream failed", cause);

		assertEquals("upstream failed", ex.getMessage());
		assertSame(cause, ex.getCause());
	}
}
