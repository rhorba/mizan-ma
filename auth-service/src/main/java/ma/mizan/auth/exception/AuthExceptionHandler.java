package ma.mizan.auth.exception;

import java.util.List;
import java.util.UUID;
import ma.mizan.common.error.ApiError;
import ma.mizan.common.error.ErrorDetail;
import ma.mizan.common.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class AuthExceptionHandler {

	@ExceptionHandler(EmailAlreadyExistsException.class)
	ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException ex) {
		return error(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", ex.getMessage());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
		return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage());
	}

	@ExceptionHandler(InvalidRefreshTokenException.class)
	ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
		return error(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> new ErrorDetail(fieldError.getField(), fieldError.getDefaultMessage())).toList();
		var apiError = new ApiError("VALIDATION_ERROR", "Request validation failed", details, requestId());
		return ResponseEntity.badRequest().body(ErrorResponse.of(apiError));
	}

	private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message) {
		var apiError = new ApiError(code, message, requestId());
		return ResponseEntity.status(status).body(ErrorResponse.of(apiError));
	}

	private String requestId() {
		return UUID.randomUUID().toString();
	}
}
