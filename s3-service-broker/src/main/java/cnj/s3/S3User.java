package cnj.s3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class S3User {

 private String applicationId;

 // private CreateUserResult
 // createUserResult;

 private String accessKeyId, username, accessKeySecret;

 public S3User(String applicationId) {
  this.applicationId = applicationId;
 }

}