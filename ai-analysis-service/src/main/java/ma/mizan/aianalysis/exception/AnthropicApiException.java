package ma.mizan.aianalysis.exception;

public class AnthropicApiException extends RuntimeException {

	public AnthropicApiException(String message) {
		super(message);
	}

	public AnthropicApiException(String message, Throwable cause) {
		super(message, cause);
	}
}
