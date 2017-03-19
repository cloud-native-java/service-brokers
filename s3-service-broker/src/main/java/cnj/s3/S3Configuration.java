package cnj.s3;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class S3Configuration {

 @Bean
 AmazonS3Client amazonS3Client(BasicAWSCredentials credentials) {
  return new AmazonS3Client(credentials);
 }

 @Bean
 AmazonIdentityManagementClient identityManagementClient(
  BasicAWSCredentials awsCredentials) {
  return new AmazonIdentityManagementClient(awsCredentials);
 }

 // <1>
 @Bean
 BasicAWSCredentials awsCredentials(
  @Value("${aws.access-key-id}") String awsAccessKeyId,
  @Value("${aws.secret-access-key}") String awsSecretAccessKey) {
  return new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
 }

 @Bean
 S3Service s3Service(AmazonIdentityManagementClient awsId, AmazonS3Client s3) {
  return new S3Service(awsId, s3);
 }
}
