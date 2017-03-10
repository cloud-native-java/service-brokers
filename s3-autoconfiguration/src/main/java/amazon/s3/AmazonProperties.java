package amazon.s3;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@ConfigurationProperties(prefix = "amazon")
public class AmazonProperties {

 @NestedConfigurationProperty
 private Aws aws;

 @NestedConfigurationProperty
 private S3 s3;

 @Data
 public static class Aws {

  private String accessKeyId;

  private String accessKeySecret;
 }

 @Data
 public static class S3 {

  private String defaultBucket;

  // required minimum: 900 seconds.
  private int sessionDuration = 900;
 }
}
