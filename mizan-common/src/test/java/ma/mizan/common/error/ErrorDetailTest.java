package ma.mizan.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ErrorDetailTest {

	@Test
	void retainsFieldAndMessage() {
		var detail = new ErrorDetail("password", "must be at least 8 characters");

		assertEquals("password", detail.field());
		assertEquals("must be at least 8 characters", detail.message());
	}
}
