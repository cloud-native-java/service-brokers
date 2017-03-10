package amazon.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.io.File;

public class RefreshingS3AutoConfigurationTest {

 private Log log = LogFactory.getLog(getClass());

 private String bucket = null; // u have
                               // to
                               // configure
                               // this
                               // by
                               // extracting
                               // the
                               // bucket
                               // from
                               // the
                               // service
                               // broker

 @Configuration
 public static class Empty {
 }

 @Test
 public void s3ClientConfiguration() throws Throwable {

  AnnotationConfigApplicationContext applicationContext = load(Empty.class,
   "amazon.aws.accessKeyId=" + System.getenv("AWS_ACCESS_KEY_ID"),
   "amazon.aws.accessKeySecret=" + System.getenv("AWS_SECRET_ACCESS_KEY"),
   "amazon.s3.sessionDuration=900");

  AmazonS3Client amazonS3Client = applicationContext
   .getBean(AmazonS3Client.class);

  if (null != this.bucket) {
   testConnectivity(amazonS3Client);
  }
 }

 private void testConnectivity(AmazonS3Client amazonS3Client) {
  File content = new File("REPLACE-WITH-A-FILE");
  PutObjectResult objectResult = amazonS3Client.putObject(this.bucket,
   "my-file", content);
  log.info("wrote content to the bucket. " + objectResult.getETag());
  log.info("time to read from the bucket. ");
  S3Object s3Object = amazonS3Client.getObject(new GetObjectRequest(
   this.bucket, "my-file"));
  Assert.assertNotNull(s3Object);
  Assert.assertNotNull(s3Object.getKey());
 }

 private static AnnotationConfigApplicationContext load(Class<?> config,
  String... environment) {
  AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
  EnvironmentTestUtils.addEnvironment(applicationContext, environment);
  applicationContext.register(config);
  applicationContext.register(S3AutoConfiguration.class);
  applicationContext.refresh();
  return applicationContext;
 }
}
