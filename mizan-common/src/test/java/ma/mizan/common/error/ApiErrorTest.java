package ma.mizan.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ApiErrorTest {

	@Test
	void fullConstructorRetainsAllFields() {
		var detail = new ErrorDetail("email", "must not be blank");
		var error = new ApiError("VALIDATION_ERROR", "Invalid request", List.of(detail), "req-1");

		assertEquals("VALIDATION_ERROR", error.code());
		assertEquals("Invalid request", error.message());
		assertEquals(List.of(detail), error.details());
		assertEquals("req-1", error.requestId());
	}

	@Test
	void shortConstructorDefaultsDetailsToEmptyList() {
		var error = new ApiError("NOT_FOUND", "Resource not found", "req-2");

		assertTrue(error.details().isEmpty());
	}
}
