package ma.mizan.contracts.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Cloudflare R2 is S3-compatible; the same client works against LocalStack in
 * tests by overriding {@code r2.endpoint-url}.
 */
@Configuration
class R2Config {

	@Bean
	S3Client r2Client(@Value("${r2.endpoint-url}") String endpointUrl, @Value("${r2.access-key-id}") String accessKeyId,
			@Value("${r2.secret-access-key}") String secretAccessKey) {
		return S3Client.builder().endpointOverride(URI.create(endpointUrl)).region(Region.of("auto"))
				.credentialsProvider(
						StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
				.forcePathStyle(true).build();
	}
}
