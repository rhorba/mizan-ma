package ma.mizan.contracts.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class R2StorageService {

	private final S3Client r2Client;
	private final String bucketName;

	public R2StorageService(S3Client r2Client, @Value("${r2.bucket-name}") String bucketName) {
		this.r2Client = r2Client;
		this.bucketName = bucketName;
	}

	public void upload(String objectKey, byte[] content, String contentType) {
		r2Client.putObject(
				PutObjectRequest.builder().bucket(bucketName).key(objectKey).contentType(contentType).build(),
				RequestBody.fromBytes(content));
	}

	public void delete(String objectKey) {
		r2Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
	}
}
