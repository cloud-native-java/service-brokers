package cnj.s3;


import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * A service component for managing the lifecycle of AWS S3 credential bindings
 * for service instances in this service broker's catalog
 */
@Service
public class S3Service {

	private final Logger log = LoggerFactory.getLogger(S3Service.class);
	private final AmazonIdentityManagement identityManagement;
	private final AmazonS3 amazonS3;

	@Autowired
	public S3Service(@Value("${aws.access-key-id}") String awsAccessKeyId,
	                 @Value("${aws.secret-access-key}") String awsSecretAccessKey) {
		// Create identity management client
		this.identityManagement = new AmazonIdentityManagementClient(new BasicAWSCredentials(
				awsAccessKeyId, awsSecretAccessKey));

		// Create S3 client
		this.amazonS3 = new AmazonS3Client(new BasicAWSCredentials(awsAccessKeyId,
				awsSecretAccessKey));
	}


	private String getManageBucketPolicyDocument() throws IOException {
		URL policyDocumentUrl = new ClassPathResource("manage-bucket-policy.json").getURL();
		try (InputStream inputStream = policyDocumentUrl.openStream()) {
			return StreamUtils.copyToString(
					inputStream, StandardCharsets.UTF_8);
		}
	}

	public S3User createBucket(String applicationId) {
		return createUserResult(applicationId);
	}

	public boolean deleteServiceInstanceBucket(String serviceInstanceId, String accessKeyId,
	                                           String userName) {

		try {
			// Get all keys for objects in the service
			// instance's bucket
			List<String> objectKeys = amazonS3.listObjects(serviceInstanceId).getObjectSummaries()
					.stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());

			if (objectKeys.size() > 0)
				// Clear the contents of the service
				// instance's bucket
				amazonS3.deleteObjects(new DeleteObjectsRequest(serviceInstanceId)
						.withKeys(objectKeys.toArray(new String[objectKeys.size()])));

			// Delete the empty bucket for the service
			// instance
			amazonS3.deleteBucket(serviceInstanceId);

			// Detach the manage bucket user policy
			// before deleting the user
			identityManagement.detachUserPolicy(new DetachUserPolicyRequest().withPolicyArn(
					getOrCreateManageBucketPolicyArn()).withUserName(userName));

			// Delete the access key for the service
			// instance before deleting the user
			identityManagement
					.deleteAccessKey(new DeleteAccessKeyRequest(userName, accessKeyId));

			// Finally, delete the user for the service
			// instance that has been deleted by the
			// service broker
			identityManagement.deleteUser(new DeleteUserRequest(userName));

		} catch (Exception ex) {
			log.error("Could not delete instance bucket {}", ex);
			return false;
		}

		return true;
	}


	private String getOrCreateManageBucketPolicyArn()  {

		String manageBucketArn;

		try {
			// Get the IAM account identifier from the
			// user's resource name
			Pattern p = Pattern.compile("(?<=::)([\\d]*)(?=:)");
			Matcher m = p.matcher(identityManagement.getUser().getUser().getArn());

 			GetPolicyResult policyResult = identityManagement.getPolicy(new GetPolicyRequest()
					.withPolicyArn(String.format("arn:aws:iam::%s:policy/manage-bucket",
							m.find() ? m.group(1) : ":")));

			// Retrieve the manage bucket policy's ARN
			manageBucketArn = policyResult.getPolicy().getArn();
		} catch (NoSuchEntityException ex) {
			try {
				// If the manage bucket policy does not
				// exist, create one
				CreatePolicyResult createPolicyResult = identityManagement
						.createPolicy(new CreatePolicyRequest()
								.withPolicyDocument(getManageBucketPolicyDocument())
								.withPolicyName("manage-bucket")
								.withDescription(
										"Allows service instances to manage the content of an exclusive S3 bucket"));
				manageBucketArn = createPolicyResult.getPolicy().getArn();
			} catch (Exception exr) {
				String msg = String.format("arn:aws:iam::%s:policy/manage-bucket", identityManagement
						.getUser().getUser().getUserId());
				throw new RuntimeException(msg, exr);
			}
		}
		return manageBucketArn;
	}

	public S3User createUserResult(String serviceInstanceId)  {

		S3User user = new S3User(serviceInstanceId);

		// Create a new user for the service instance
		user.setCreateUserResult(identityManagement.createUser(new CreateUserRequest(
				serviceInstanceId)));

		// Create access key for new user
		CreateAccessKeyResult createAccessKeyResult = identityManagement
				.createAccessKey(new CreateAccessKeyRequest(serviceInstanceId).withUserName(user
						.getCreateUserResult().getUser().getUserName()));

		// Get access key and secret for new user
		user.setAccessKeyId(createAccessKeyResult.getAccessKey().getAccessKeyId());
		user.setAccessKeySecret(createAccessKeyResult.getAccessKey().getSecretAccessKey());

		// Create the bucket for the service instance
		amazonS3.createBucket(new CreateBucketRequest(serviceInstanceId));

		// Get or create the manage bucket policy for
		// the new user
		String manageBucketArn = getOrCreateManageBucketPolicyArn();

		// Attach the manage bucket policy to the new
		// user
		identityManagement.attachUserPolicy(new AttachUserPolicyRequest().withUserName(
				user.getCreateUserResult().getUser().getUserName())
				.withPolicyArn(manageBucketArn));

		return user;
	}

}
