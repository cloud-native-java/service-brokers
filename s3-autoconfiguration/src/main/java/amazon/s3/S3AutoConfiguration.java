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

import java.util.Date;

@Configuration
@EnableConfigurationProperties(AmazonProperties.class)
public class S3AutoConfiguration {

	@Bean
	public AmazonProperties awsProperties() {
		return new AmazonProperties();
	}

	@Bean
	@ConditionalOnMissingBean(AmazonS3Client.class)
	@ConditionalOnClass(AmazonS3Client.class)
	public AmazonS3Client amazonS3Client(AmazonProperties aws) {

		RefreshingBasicSessionCredentialsMethodInterceptor interceptor =
				new RefreshingBasicSessionCredentialsMethodInterceptor(aws.getAws().getAccessKeyId(),
						aws.getAws().getAccessKeySecret(),
						aws.getS3().getSessionDuration());

		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setTargetClass(AmazonS3Client.class);
		proxyFactoryBean.setProxyTargetClass(true);
		proxyFactoryBean.addAdvice(interceptor);

		return AmazonS3Client.class.cast(proxyFactoryBean.getObject());
	}

	private static class RefreshingBasicSessionCredentialsMethodInterceptor
			implements MethodInterceptor {
		private final String accessKeyId, accessKeySecret;

		private final int duration;
		private Credentials sessionCredentials;

		private AmazonS3Client amazonS3Client;

		RefreshingBasicSessionCredentialsMethodInterceptor(String accessKeyId, String accessKeySecret, int d) {
			this.accessKeyId = accessKeyId;
			this.accessKeySecret = accessKeySecret;
			this.duration = d;
		}

		private final Object monitor = new Object();

		private AmazonS3Client refreshSessionCredentials(String accessKeyId, String accessKeySecret) {

			// synchronized because the three variables may be in inconsistent state.
			// BUT: this will happen once every 15 minutes, in the worst case.

			if (this.sessionCredentials == null || this.sessionCredentials.getExpiration().before(new Date())) {
				synchronized (this.monitor) {
					AWSSecurityTokenServiceClient stsClient = new AWSSecurityTokenServiceClient(
							new BasicAWSCredentials(accessKeyId, accessKeySecret));
					GetSessionTokenRequest getSessionTokenRequest = new GetSessionTokenRequest()
							.withDurationSeconds(this.duration);
					this.sessionCredentials = stsClient.getSessionToken(getSessionTokenRequest).getCredentials();
					BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
							sessionCredentials.getAccessKeyId(),
							sessionCredentials.getSecretAccessKey(),
							sessionCredentials.getSessionToken());
					this.amazonS3Client = new AmazonS3Client(
							basicSessionCredentials);
				}
			}
			return this.amazonS3Client;
		}

		@Override
		public Object invoke(MethodInvocation methodInvocation) throws Throwable {

			AmazonS3Client amazonS3Client = this.refreshSessionCredentials(
					this.accessKeyId, this.accessKeySecret);

			return methodInvocation.getMethod().invoke(amazonS3Client, methodInvocation.getArguments());
		}

	}


}
