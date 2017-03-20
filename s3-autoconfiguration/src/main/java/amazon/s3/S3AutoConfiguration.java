package amazon.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.STSSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AmazonProperties.class)
public class S3AutoConfiguration {

 // <1>
 @Bean
 @ConditionalOnMissingBean(AmazonS3.class)
 @ConditionalOnClass(AmazonS3.class)
 public AmazonS3 amazonS3(AmazonProperties awsProps) {

  String rootAwsAccessKeyId = awsProps.getAws().getAccessKeyId();
  String rootAwsAccessKeySecret = awsProps.getAws().getAccessKeySecret();

  AWSCredentials credentials = new BasicAWSCredentials(rootAwsAccessKeyId,
   rootAwsAccessKeySecret);
  AWSSecurityTokenService stsClient = new AWSSecurityTokenServiceClient(
   credentials);
  AWSCredentialsProvider credentialsProvider = new STSSessionCredentialsProvider(
   stsClient);
  return AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1)
   .withCredentials(credentialsProvider).build();
 }
}
