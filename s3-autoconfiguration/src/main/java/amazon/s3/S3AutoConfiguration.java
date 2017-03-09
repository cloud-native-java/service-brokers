package amazon.s3;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(AmazonS3Template.class)
@EnableConfigurationProperties(AmazonProperties.class)
public class S3AutoConfiguration {

 @Bean
 AmazonS3Template amazonS3Template(AmazonProperties amazonProperties) {
  return new AmazonS3Template(amazonProperties.getS3().getDefaultBucket(),
    amazonProperties.getAws().getAccessKeyId(), amazonProperties.getAws()
      .getAccessKeySecret());
 }
}
