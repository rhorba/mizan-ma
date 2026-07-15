package ma.mizan.aianalysis.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class AiAnalysisExceptionHandlerTest {

	private final AiAnalysisExceptionHandler handler = new AiAnalysisExceptionHandler();

	@SuppressWarnings("unused")
	private void dummyEndpoint(Object analyzeRequest) {
	}

	@Test
	void unauthorizedInternalCallMapsTo403() {
		var response = handler.handleUnauthorized(new UnauthorizedInternalCallException());

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertEquals("FORBIDDEN", response.getBody().error().code());
		assertEquals("Missing or invalid internal service token", response.getBody().error().message());
	}

	@Test
	void anthropicFailureMapsTo502() {
		var response = handler.handleAnthropicFailure(new AnthropicApiException("upstream unavailable"));

		assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
		assertEquals("ANALYSIS_UPSTREAM_ERROR", response.getBody().error().code());
		assertEquals("upstream unavailable", response.getBody().error().message());
	}

	@Test
	void validationFailureMapsTo400WithFieldErrorDetails() throws NoSuchMethodException {
		var bindingResult = new BeanPropertyBindingResult(new Object(), "analyzeRequest");
		bindingResult.addError(new FieldError("analyzeRequest", "contractId", "must not be null"));
		Method dummy = AiAnalysisExceptionHandlerTest.class.getDeclaredMethod("dummyEndpoint", Object.class);
		var ex = new MethodArgumentNotValidException(new MethodParameter(dummy, 0), bindingResult);

		var response = handler.handleValidation(ex);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("VALIDATION_ERROR", response.getBody().error().code());
		assertNotNull(response.getBody().error().details());
		assertEquals(1, response.getBody().error().details().size());
		assertEquals("contractId", response.getBody().error().details().get(0).field());
	}
}
