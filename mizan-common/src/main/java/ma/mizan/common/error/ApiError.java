package ma.mizan.common.error;

import java.util.List;

public record ApiError(String code, String message, List<ErrorDetail> details, String requestId) {

	public ApiError(String code, String message, String requestId) {
		this(code, message, List.of(), requestId);
	}
}
