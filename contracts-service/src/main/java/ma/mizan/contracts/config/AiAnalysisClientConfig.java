package ma.mizan.contracts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class AiAnalysisClientConfig {

	@Bean
	RestClient aiAnalysisRestClient(@Value("${ai-analysis.service-url:http://localhost:8084}") String serviceUrl) {
		return RestClient.builder().baseUrl(serviceUrl).build();
	}
}
