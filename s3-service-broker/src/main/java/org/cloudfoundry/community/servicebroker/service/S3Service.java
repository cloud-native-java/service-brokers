package org.cloudfoundry.community.servicebroker.service;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.S3User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    public S3Service(@Value("${aws.access-key-id}") String awsAccessKeyId,
                     @Value("${aws.secret-access-key}") String awsSecretAccessKey) {
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
    public S3User createBucket(String applicationId) throws ServiceBrokerException {
        S3User user;

        try {
            user = createUserResult(applicationId);
        } catch (Exception ex) {
            log.error("Error creating IAM user {}", ex);
            throw new ServiceBrokerException("Error creating IAM user {}", ex);
        }

        return user;
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
    private String getManageBucketPolicyArn() throws ServiceBrokerException {

        String manageBucketArn;

        try {
            // Get the IAM account identifier from the user's resource name
            Pattern p = Pattern.compile("(?<=::)([\\d]*)(?=:)");
            Matcher m = p.matcher(identityManagement.getUser().getUser().getArn());

            // Use the account id to get the manage bucket policy
            GetPolicyResult policyResult = identityManagement
                    .getPolicy(new GetPolicyRequest().withPolicyArn(String.format("arn:aws:iam::%s:policy/manage-bucket",
                            m.find() ? m.group(1) : ":")));

            // Retrieve the manage bucket policy's ARN
            manageBucketArn = policyResult.getPolicy().getArn();
        } catch (NoSuchEntityException ex) {
            try {
                // If the manage bucket policy does not exist, create one
                CreatePolicyResult createPolicyResult = identityManagement
                        .createPolicy(new CreatePolicyRequest()
                                .withPolicyDocument(getManageBucketPolicyDocument())
                                .withPolicyName("manage-bucket")
                                .withDescription("Allows service instances to manage the content of an exclusive S3 bucket"));
                manageBucketArn = createPolicyResult.getPolicy().getArn();
            } catch (Exception exr) {
                // The manage bucket policy could not be created
                log.error(String.format("arn:aws:iam::%s:policy/manage-bucket",
                        identityManagement.getUser().getUser().getUserId()), exr);
                throw new ServiceBrokerException(exr);
            }
        }
        return manageBucketArn;
    }

    /**
     * Create a new IAM user for a service instance using its unique id
     *
     * @param applicationId is the service instance's unique id from the broker catalog
     * @return a new {@link S3User} containing the credential details for the new service instance's IAM user
     */
    public S3User createUserResult(String applicationId) throws ServiceBrokerException {

        S3User user = new S3User(applicationId);

        // Create a new user for the service instance
        user.setCreateUserResult(identityManagement.createUser(new CreateUserRequest(applicationId)));

        // Create access key for new user
        CreateAccessKeyResult createAccessKeyResult =
                identityManagement.createAccessKey(new CreateAccessKeyRequest(applicationId)
                        .withUserName(user.getCreateUserResult().getUser().getUserName()));

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
