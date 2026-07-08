package ma.mizan.aianalysis.exception;

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
class AiAnalysisExceptionHandler {

	@ExceptionHandler(UnauthorizedInternalCallException.class)
	ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedInternalCallException ex) {
		return error(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
	}

	@ExceptionHandler(AnthropicApiException.class)
	ResponseEntity<ErrorResponse> handleAnthropicFailure(AnthropicApiException ex) {
		return error(HttpStatus.BAD_GATEWAY, "ANALYSIS_UPSTREAM_ERROR", ex.getMessage());
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
