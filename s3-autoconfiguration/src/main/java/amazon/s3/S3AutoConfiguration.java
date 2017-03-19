package amazon.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactoryBean;
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
 @ConditionalOnMissingBean(AmazonS3Client.class)
 @ConditionalOnClass(AmazonS3Client.class)
 public AmazonS3Client amazonS3Client(AmazonProperties properties) { // <2>
  AmazonProperties.Aws aws = properties.getAws();
  // <3>
  MethodInterceptor interceptor = new RefreshableAmazonS3ClientMethodInterceptor(
   aws.getAccessKeyId(), aws.getAccessKeySecret(), properties.getS3()
    .getSessionDuration());
  // <4>
  ProxyFactoryBean pfb = new ProxyFactoryBean();
  pfb.setTargetClass(AmazonS3Client.class);
  pfb.setProxyTargetClass(true);
  pfb.addAdvice(interceptor);

  return AmazonS3Client.class.cast(pfb.getObject());
 }
}
