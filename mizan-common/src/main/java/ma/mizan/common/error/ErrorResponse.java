package ma.mizan.common.error;

public record ErrorResponse(ApiError error) {

	public static ErrorResponse of(ApiError error) {
		return new ErrorResponse(error);
	}
}
