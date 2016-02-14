package org.cloudfoundry.community.servicebroker.service;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.cloudfoundry.community.servicebroker.model.S3User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A service component for managing the lifecycle of AWS S3 credential bindings for service instances in this
 * service broker's catalog
 *
 * @author kbastani
 */
@Service
public class S3Service {

    private final Logger log = LoggerFactory.getLogger(S3Service.class);
    private AmazonIdentityManagement identityManagement;
    private AmazonS3 amazonS3;

    @Autowired
    public S3Service(@Value("${aws.accessKeyId}") String awsAccessKeyId,
                     @Value("${aws.secretAccessKey}") String awsSecretAccessKey) {

        // Create identity management client
        this.identityManagement =
                new AmazonIdentityManagementClient(
                        new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey));

        // Create S3 client
        this.amazonS3 = new AmazonS3Client(new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey));
    }

    /**
     * Get the policy document JSON from classpath resources
     *
     * @return a resource policy for managing buckets on S3
     */
    private String getManageBucketPolicyDocument() {
        String policyDocument = null;

        try {
            URL policyDocumentUrl = new ClassPathResource("manage-bucket-policy.json").getURL();
            policyDocument = Resources.toString(policyDocumentUrl, Charsets.UTF_8);
        } catch (IOException ex) {
            log.error("Error retrieving manage bucket policy from resources {}", ex);
        }

        return policyDocument;
    }

    /**
     * Create a new S3 bucket for the service instance using the unique application id
     *
     * @param applicationId is the unique application id of the service instance in the broker's catalog
     * @return an instance of {@link S3User} containing credentials or null if an issue was encountered
     */
    public S3User createBucket(String applicationId) {
        S3User user;

        try {
            user = createUserResult(applicationId);
        } catch (Exception ex) {
            log.error("Error creating IAM user {}", ex);
            return null;
        }

        // TODO: Remove clean up

        // Wait for the policy to be attached before testing session-based management
        // cleanUp(applicationId, user);

        return user;
    }

    private void cleanUp(String applicationId, S3User user) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Test object creation using the IAM user that was just created
        manageServiceInstanceBucket(applicationId, user.getAccessKeyId(), user.getAccessKeySecret());

        // Delete the service instance's S3 bucket and user account
        deleteServiceInstanceBucket(applicationId, user.getAccessKeyId(), user.getCreateUserResult().getUser().getUserName());
    }

    /**
     * Delete a service instance's bucket and user, used when a service instance has been deleted from the catalog
     *
     * @param applicationId is the id of the service instance from the catalog that is being deleted
     * @param accessKeyId   is the access key for the service instance's IAM user
     * @param userName      is the service instance's IAM user name
     */
    public boolean deleteServiceInstanceBucket(String applicationId, String accessKeyId, String userName) {

        try {
            // Get all keys for objects in the service instance's bucket
            List<String> objectKeys = amazonS3.listObjects(applicationId).getObjectSummaries()
                    .stream()
                    .map(S3ObjectSummary::getKey)
                    .collect(Collectors.toList());

            if (objectKeys.size() > 0)
                // Clear the contents of the service instance's bucket
                amazonS3.deleteObjects(new DeleteObjectsRequest(applicationId)
                        .withKeys(objectKeys
                                .toArray(new String[objectKeys.size()])));

            // Delete the empty bucket for the service instance
            amazonS3.deleteBucket(applicationId);

            // Detach the manage bucket user policy before deleting the user
            identityManagement.detachUserPolicy(new DetachUserPolicyRequest()
                    .withPolicyArn(getManageBucketPolicyArn())
                    .withUserName(userName));

            // Delete the access key for the service instance before deleting the user
            identityManagement.deleteAccessKey(new DeleteAccessKeyRequest(userName, accessKeyId));

            // Finally, delete the user for the service instance that has been deleted by the service broker
            identityManagement.deleteUser(new DeleteUserRequest(userName));

        } catch (Exception ex) {
            log.error("Could not delete instance bucket {}", ex);
            return false;
        }

        return true;
    }

    /**
     * Get or create an AWS resource policy name for managing a service instance's bucket
     *
     * @return a resource policy name for the AWS manage bucket policy
     */
    private String getManageBucketPolicyArn() {
        String manageBucketArn;
        try {
            GetPolicyResult policyResult = identityManagement
                    .getPolicy(new GetPolicyRequest().withPolicyArn(String.format("arn:aws:iam::%s:policy/manage-bucket", identityManagement.getUser().getUser().getUserId())));
            manageBucketArn = policyResult.getPolicy().getArn();
        } catch (NoSuchEntityException ex) {
            CreatePolicyResult createPolicyResult = identityManagement
                    .createPolicy(new CreatePolicyRequest()
                            .withPolicyDocument(getManageBucketPolicyDocument())
                            .withPolicyName("manage-bucket")
                            .withDescription("Allows service instances to manage the content of an exclusive S3 bucket"));
            manageBucketArn = createPolicyResult.getPolicy().getArn();
        }
        return manageBucketArn;
    }

    /**
     * Manage the service instance's S3 bucket using the IAM user that was created by the service broker
     *
     * @param applicationId   is the id of the service instance
     * @param accessKeyId     is the access key id of the service instance's S3 credentials
     * @param accessKeySecret is the access key secret of the service instance's S3 credentials
     */
    private void manageServiceInstanceBucket(String applicationId, String accessKeyId, String accessKeySecret) {
        // Create a new session with the user credentials for the service instance
        AWSSecurityTokenServiceClient stsClient =
                new AWSSecurityTokenServiceClient(new BasicAWSCredentials(accessKeyId, accessKeySecret));

        // Start a new session for managing a service instance's bucket
        GetSessionTokenRequest getSessionTokenRequest =
                new GetSessionTokenRequest().withDurationSeconds(7200);

        // Get the session token for the service instance's bucket
        GetSessionTokenResult sessionTokenResult =
                stsClient.getSessionToken(getSessionTokenRequest);
        Credentials sessionCredentials = sessionTokenResult.getCredentials();

        // Create basic session credentials using the generated session token
        BasicSessionCredentials basicSessionCredentials =
                new BasicSessionCredentials(sessionCredentials.getAccessKeyId(),
                        sessionCredentials.getSecretAccessKey(),
                        sessionCredentials.getSessionToken());

        // Create a new S3 client using the basic session credentials of the service instance
        AmazonS3 testAmazonClient = new AmazonS3Client(basicSessionCredentials);

        // Create a new object in the service instance's S3 bucket
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("text/plain");
        testAmazonClient.putObject(new PutObjectRequest(applicationId, "test", new ByteArrayInputStream("test".getBytes()), objectMetadata));
    }

    /**
     * Create a new IAM user for a service instance using its unique id
     *
     * @param applicationId is the service instance's unique id from the broker catalog
     * @return a new {@link S3User} containing the credential details for the new service instance's IAM user
     */
    public S3User createUserResult(String applicationId) {

        S3User user = new S3User(applicationId);

        // Create a new user for the service instance
        user.setCreateUserResult(identityManagement.createUser(new CreateUserRequest(applicationId)));

        log.info("createUserResult {}", user.getCreateUserResult());

        // Create access key for new user
        CreateAccessKeyResult createAccessKeyResult =
                identityManagement.createAccessKey(new CreateAccessKeyRequest(applicationId)
                        .withUserName(user.getCreateUserResult().getUser().getUserName()));

        log.info("createAccessKeyResult {}", createAccessKeyResult);

        // Get access key and secret for new user
        user.setAccessKeyId(createAccessKeyResult.getAccessKey().getAccessKeyId());
        user.setAccessKeySecret(createAccessKeyResult.getAccessKey().getSecretAccessKey());

        // Create the bucket for the service instance
        amazonS3.createBucket(new CreateBucketRequest(applicationId));

        // Get or create the manage bucket policy for the new user
        String manageBucketArn = getManageBucketPolicyArn();

        // Attach the manage bucket policy to the new user
        identityManagement.attachUserPolicy(new AttachUserPolicyRequest()
                .withUserName(user.getCreateUserResult().getUser().getUserName())
                .withPolicyArn(manageBucketArn));

        return user;
    }

}
