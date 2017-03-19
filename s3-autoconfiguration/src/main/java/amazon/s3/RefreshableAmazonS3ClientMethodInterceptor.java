package amazon.s3;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

class RefreshableAmazonS3ClientMethodInterceptor implements MethodInterceptor {

 private final AtomicReference<S3ClientReference> clientReference = new AtomicReference<>();

 private final String accessKeyId, accessKeySecret;

 private final int duration;

 @AllArgsConstructor
 @Data
 private static class S3ClientReference {

  private final Credentials credentials;

  private final AmazonS3Client client;
 }

 @Override
 public Object invoke(MethodInvocation methodInvocation) throws Throwable {
  Method method = methodInvocation.getMethod();
  AmazonS3Client s3Client = this
   .refresh(this.accessKeyId, this.accessKeySecret);
  return method.invoke(s3Client, methodInvocation.getArguments());
 }

 RefreshableAmazonS3ClientMethodInterceptor(String accessKeyId,
  String accessKeySecret, int duration) {
  this.accessKeyId = accessKeyId;
  this.accessKeySecret = accessKeySecret;
  this.duration = duration;
 }

 private AmazonS3Client refresh(String accessKeyId, String accessKeySecret) {
  S3ClientReference reference = this.clientReference
   .updateAndGet(s3c -> {
    if (null == s3c || s3c.getCredentials().getExpiration().before(new Date())) {
     AWSSecurityTokenServiceClient client = new AWSSecurityTokenServiceClient(
      new BasicAWSCredentials(accessKeyId, accessKeySecret));
     GetSessionTokenRequest request = new GetSessionTokenRequest()
      .withDurationSeconds(duration);
     Credentials c = client.getSessionToken(request).getCredentials();
     BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
      c.getAccessKeyId(), c.getSecretAccessKey(), c.getSessionToken());
     AmazonS3Client amazonS3Client = new AmazonS3Client(basicSessionCredentials);
     return new S3ClientReference(c, amazonS3Client);
    }
    return s3c;
   });
  return reference.getClient();
 }
}
