package amazon.s3;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

@Configuration
@EnableConfigurationProperties(AmazonProperties.class)
public class S3AutoConfiguration {

 @Bean
 @ConditionalOnMissingBean(AmazonS3Client.class)
 @ConditionalOnClass(AmazonS3Client.class)
 public AmazonS3Client amazonS3Client(AmazonProperties aws) {

  RefreshableAmazonS3ClientMethodInterceptor interceptor = new RefreshableAmazonS3ClientMethodInterceptor(
   aws.getAws().getAccessKeyId(), aws.getAws().getAccessKeySecret(), aws
    .getS3().getSessionDuration());

  ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
  proxyFactoryBean.setTargetClass(AmazonS3Client.class);
  proxyFactoryBean.setProxyTargetClass(true);
  proxyFactoryBean.addAdvice(interceptor);

  return AmazonS3Client.class.cast(proxyFactoryBean.getObject());
 }

 private static class RefreshableAmazonS3ClientMethodInterceptor implements
  MethodInterceptor {

  private final String accessKeyId, accessKeySecret;

  private final int duration;

  private Credentials sessionCredentials;

  RefreshableAmazonS3ClientMethodInterceptor(String accessKeyId,
   String accessKeySecret, int duration) {
   this.accessKeyId = accessKeyId;
   this.accessKeySecret = accessKeySecret;
   this.duration = duration;
  }

  private AmazonS3Client refreshSessionCredentials(String accessKeyId,
   String accessKeySecret) {
   AWSSecurityTokenServiceClient client = new AWSSecurityTokenServiceClient(
    new BasicAWSCredentials(accessKeyId, accessKeySecret));
   GetSessionTokenRequest request = new GetSessionTokenRequest()
    .withDurationSeconds(this.duration);
   this.sessionCredentials = client.getSessionToken(request).getCredentials();
   BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
    sessionCredentials.getAccessKeyId(),
    sessionCredentials.getSecretAccessKey(),
    sessionCredentials.getSessionToken());
   return new AmazonS3Client(basicSessionCredentials);
  }

  @Override
  public Object invoke(MethodInvocation methodInvocation) throws Throwable {
   Method method = methodInvocation.getMethod();
   return method.invoke(client(), methodInvocation.getArguments());
  }

  private AmazonS3Client client() {
   return this
    .refreshSessionCredentials(this.accessKeyId, this.accessKeySecret);
  }
 }
}
