package ma.mizan.aianalysis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class AnthropicConfig {

	@Bean
	RestClient anthropicRestClient(@Value("${anthropic.base-url:https://api.anthropic.com}") String baseUrl,
			@Value("${anthropic.api-key}") String apiKey) {
		return RestClient.builder().baseUrl(baseUrl).defaultHeader("x-api-key", apiKey)
				.defaultHeader("anthropic-version", "2023-06-01").defaultHeader("content-type", "application/json")
				.build();
	}
}
