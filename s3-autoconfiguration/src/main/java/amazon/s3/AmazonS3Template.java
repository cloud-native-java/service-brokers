package amazon.s3;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;

import java.io.File;
import java.util.Date;

public class AmazonS3Template {

 private final String defaultBucket;

 private final String accessKeyId;

 private final String accessKeySecret;

 private final Object monitor = new Object();

 private volatile Credentials sessionCredentials;

 public AmazonS3Template(String defaultBucket, String accessKeyId,
  String accessKeySecret) {
  this.defaultBucket = defaultBucket;
  this.accessKeyId = accessKeyId;
  this.accessKeySecret = accessKeySecret;
 }

 public PutObjectResult save(String key, File file) {
  return getAmazonS3Client().putObject(
   new PutObjectRequest(defaultBucket, key, file));
 }

 public S3Object get(String key) {
  return getAmazonS3Client().getObject(defaultBucket, key);
 }

 private AmazonS3 getAmazonS3Client() {
  return new AmazonS3Client(getBasicSessionCredentials());
 }

 private BasicSessionCredentials getBasicSessionCredentials() {
  synchronized (this.monitor) {
   if (sessionCredentials == null
    || sessionCredentials.getExpiration().before(new Date())) {
    sessionCredentials = getSessionCredentials();
   }
  }

  return new BasicSessionCredentials(sessionCredentials.getAccessKeyId(),
   sessionCredentials.getSecretAccessKey(),
   sessionCredentials.getSessionToken());
 }

 /**
  * Creates a new session credential
  * that is valid for 12 hours
  */
 private Credentials getSessionCredentials() {
  // Create a new session with the user
  // credentials for the service
  // instance
  AWSSecurityTokenServiceClient stsClient = new AWSSecurityTokenServiceClient(
   new BasicAWSCredentials(accessKeyId, accessKeySecret));

  // Start a new session for managing a
  // service instance's bucket
  GetSessionTokenRequest getSessionTokenRequest = new GetSessionTokenRequest()
   .withDurationSeconds(43200);

  // Get the session token for the
  // service instance's bucket
  sessionCredentials = stsClient.getSessionToken(getSessionTokenRequest)
   .getCredentials();

  return sessionCredentials;
 }

 public ObjectListing listObjects(ListObjectsRequest listObjectsRequest) {
  return getAmazonS3Client().listObjects(listObjectsRequest);
 }

 public void putObject(PutObjectRequest putObjectRequest) {
  getAmazonS3Client().putObject(putObjectRequest);
 }
}
