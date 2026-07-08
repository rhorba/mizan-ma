package ma.mizan.user.exception;

import java.util.List;
import java.util.UUID;
import ma.mizan.common.error.ApiError;
import ma.mizan.common.error.ErrorDetail;
import ma.mizan.common.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class UserExceptionHandler {

	@ExceptionHandler(MissingRequestHeaderException.class)
	ResponseEntity<ErrorResponse> handleMissingUserHeader(MissingRequestHeaderException ex) {
		var apiError = new ApiError("UNAUTHORIZED", "Request is missing trusted identity headers", requestId());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.of(apiError));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> new ErrorDetail(fieldError.getField(), fieldError.getDefaultMessage())).toList();
		var apiError = new ApiError("VALIDATION_ERROR", "Request validation failed", details, requestId());
		return ResponseEntity.badRequest().body(ErrorResponse.of(apiError));
	}

	private String requestId() {
		return UUID.randomUUID().toString();
	}
}
