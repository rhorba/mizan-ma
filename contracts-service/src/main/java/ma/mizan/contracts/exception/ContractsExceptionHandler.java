package ma.mizan.contracts.exception;

import java.util.UUID;
import ma.mizan.common.error.ApiError;
import ma.mizan.common.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
class ContractsExceptionHandler {

	@ExceptionHandler(MissingRequestHeaderException.class)
	ResponseEntity<ErrorResponse> handleMissingUserHeader(MissingRequestHeaderException ex) {
		return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Request is missing trusted identity headers");
	}

	@ExceptionHandler(InvalidPdfException.class)
	ResponseEntity<ErrorResponse> handleInvalidPdf(InvalidPdfException ex) {
		return error(HttpStatus.BAD_REQUEST, "INVALID_PDF", ex.getMessage());
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	ResponseEntity<ErrorResponse> handleMissingFile(MissingServletRequestPartException ex) {
		return error(HttpStatus.BAD_REQUEST, "INVALID_PDF", "No file part found in the request");
	}

	@ExceptionHandler(ContractNotFoundException.class)
	ResponseEntity<ErrorResponse> handleNotFound(ContractNotFoundException ex) {
		return error(HttpStatus.NOT_FOUND, "CONTRACT_NOT_FOUND", ex.getMessage());
	}

	@ExceptionHandler(ContractAccessDeniedException.class)
	ResponseEntity<ErrorResponse> handleAccessDenied(ContractAccessDeniedException ex) {
		return error(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
	}

	private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message) {
		var apiError = new ApiError(code, message, UUID.randomUUID().toString());
		return ResponseEntity.status(status).body(ErrorResponse.of(apiError));
	}
}
