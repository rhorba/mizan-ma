package ma.mizan.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ErrorResponseTest {

	@Test
	void ofWrapsGivenError() {
		var error = new ApiError("INTERNAL_ERROR", "Something went wrong", "req-3");

		var response = ErrorResponse.of(error);

		assertEquals(error, response.error());
	}
}
