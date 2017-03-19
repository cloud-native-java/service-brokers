package cnj.s3;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

@Service
public class S3Service {

 private final Log log = LogFactory.getLog(getClass());

 private final AmazonIdentityManagement id;

 private final AmazonS3 s3;

 public S3Service(AmazonIdentityManagementClient id, AmazonS3Client s3Client) {
  this.id = id;
  this.s3 = s3Client;
 }

 private String getManageBucketPolicyDocument() throws IOException {
  URL policyDocumentUrl = new ClassPathResource("manage-bucket-policy.json")
   .getURL();
  try (InputStream inputStream = policyDocumentUrl.openStream()) {
   return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
  }
 }

 public S3User createS3UserForBucket(String bucketName) {
  S3User user = new S3User(bucketName);
  user.setCreateUserResult(id.createUser(new CreateUserRequest(bucketName)));
  User createdUser = user.getCreateUserResult().getUser();
  CreateAccessKeyResult createAccessKeyResult = id
   .createAccessKey(new CreateAccessKeyRequest(bucketName)
    .withUserName(createdUser.getUserName()));
  AccessKey accessKey = createAccessKeyResult.getAccessKey();
  user.setAccessKeyId(accessKey.getAccessKeyId());
  user.setAccessKeySecret(accessKey.getSecretAccessKey());
  s3.createBucket(new CreateBucketRequest(bucketName));
  String manageBucketArn = getOrCreateManageBucketPolicyArn();
  id.attachUserPolicy(new AttachUserPolicyRequest().withUserName(
   createdUser.getUserName()).withPolicyArn(manageBucketArn));
  return user;
 }

 public boolean deleteBucket(String bucketName, String accessKeyId,
  String userName) {
  try {
   List<String> objectKeys = s3.listObjects(bucketName).getObjectSummaries()
    .stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
   if (objectKeys.size() > 0)
    s3.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(objectKeys
     .toArray(new String[objectKeys.size()])));
   s3.deleteBucket(bucketName);
   id.detachUserPolicy(new DetachUserPolicyRequest().withPolicyArn(
    getOrCreateManageBucketPolicyArn()).withUserName(userName));
   id.deleteAccessKey(new DeleteAccessKeyRequest(userName, accessKeyId));
   id.deleteUser(new DeleteUserRequest(userName));
  }
  catch (Exception ex) {
   log.error("Could not delete instance bucket {}", ex);
   return false;
  }
  return true;
 }

 private String getOrCreateManageBucketPolicyArn() {
  String manageBucketArn;
  try {
   Pattern p = Pattern.compile("(?<=::)([\\d]*)(?=:)");
   Matcher m = p.matcher(id.getUser().getUser().getArn());
   GetPolicyResult policyResult = id.getPolicy(new GetPolicyRequest()
    .withPolicyArn(String.format("arn:aws:iam::%s:policy/manage-bucket",
     m.find() ? m.group(1) : ":")));
   manageBucketArn = policyResult.getPolicy().getArn();
  }
  catch (NoSuchEntityException ex) {
   try {
    CreatePolicyResult createPolicyResult = id
     .createPolicy(new CreatePolicyRequest()
      .withPolicyDocument(getManageBucketPolicyDocument())
      .withPolicyName("manage-bucket")
      .withDescription(
       "Allows service instances to manage the content of an exclusive S3 bucket"));
    manageBucketArn = createPolicyResult.getPolicy().getArn();
   }
   catch (Exception exr) {
    String msg = String.format("arn:aws:iam::%s:policy/manage-bucket", id
     .getUser().getUser().getUserId());
    throw new RuntimeException(msg, exr);
   }
  }
  return manageBucketArn;
 }

}