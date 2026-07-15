package ma.mizan.contracts.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

class ContractsExceptionHandlerTest {

	private final ContractsExceptionHandler handler = new ContractsExceptionHandler();

	@SuppressWarnings("unused")
	private void dummyEndpoint(String xUserId) {
	}

	@Test
	void missingUserHeaderMapsTo401() throws NoSuchMethodException {
		Method dummy = ContractsExceptionHandlerTest.class.getDeclaredMethod("dummyEndpoint", String.class);
		var parameter = new MethodParameter(dummy, 0);
		var ex = new MissingRequestHeaderException("X-User-Id", parameter);

		var response = handler.handleMissingUserHeader(ex);

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertEquals("UNAUTHORIZED", response.getBody().error().code());
	}

	@Test
	void invalidPdfMapsTo400() {
		var response = handler.handleInvalidPdf(new InvalidPdfException("File is empty"));

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("INVALID_PDF", response.getBody().error().code());
		assertEquals("File is empty", response.getBody().error().message());
	}

	@Test
	void missingFilePartMapsTo400() {
		var response = handler.handleMissingFile(new MissingServletRequestPartException("file"));

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("INVALID_PDF", response.getBody().error().code());
	}

	@Test
	void contractNotFoundMapsTo404() {
		var id = UUID.randomUUID();

		var response = handler.handleNotFound(new ContractNotFoundException(id));

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("CONTRACT_NOT_FOUND", response.getBody().error().code());
	}

	@Test
	void accessDeniedMapsTo403() {
		var response = handler.handleAccessDenied(new ContractAccessDeniedException());

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertEquals("FORBIDDEN", response.getBody().error().code());
	}

	@Test
	void adminRequiredMapsTo403() {
		var response = handler.handleAdminRequired(new AdminAccessRequiredException());

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertEquals("FORBIDDEN", response.getBody().error().code());
	}
}
