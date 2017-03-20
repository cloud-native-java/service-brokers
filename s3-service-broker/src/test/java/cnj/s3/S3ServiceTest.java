package cnj.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class S3ServiceTest {

 @Autowired
 private AmazonS3 s3;

 @Autowired
 private S3Service s3Service;

 @Test
 public void testS3Service() throws Throwable {

  String serviceInstanceId = System.currentTimeMillis() + "";
  S3User user = s3Service.createS3UserAndBucket(serviceInstanceId);
  System.out.println("map: " + user.toString());

  // push
  File file = new File(System.getProperty("user.home"), "/Desktop/img.png");
  FileSystemResource fsr = new FileSystemResource(file);

  ObjectMetadata objectMetadata = new ObjectMetadata();
  objectMetadata.setContentType("image/png");

  String userName = user.getUsername();
  PutObjectRequest request = new PutObjectRequest(userName, file.getName(),
   fsr.getInputStream(), objectMetadata)
   .withCannedAcl(CannedAccessControlList.PublicRead);

  PutObjectResult objectResult = s3.putObject(request);

  log("" + objectResult);

  // enumerate
  ObjectListing listing = s3.listObjects(new ListObjectsRequest()
   .withBucketName(serviceInstanceId));
  List<S3ObjectSummary> collect = listing.getObjectSummaries().stream()
   .collect(Collectors.toList());
  collect.forEach(System.out::println);
 }

 private void log(Object x) {
  System.out.println(x);
 }
}