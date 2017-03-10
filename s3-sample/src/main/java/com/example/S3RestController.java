package com.example;

import amazon.s3.AmazonProperties;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/s3")
class S3RestController {

 private Log log = LogFactory.getLog(getClass());

 private final AmazonS3Client amazonS3Client;

 private final String defaultBucket;

 @Autowired
 public S3RestController(AmazonProperties amazonProperties,
  AmazonS3Client amazonS3Client) {
  this.amazonS3Client = amazonS3Client;
  this.defaultBucket = amazonProperties.getS3().getDefaultBucket();
  log.debug("defaultBucket = " + this.defaultBucket);
 }

 @GetMapping("/resources")
 List<Resource<S3ObjectSummary>> list() {

  ListObjectsRequest request = new ListObjectsRequest()
   .withBucketName(this.defaultBucket);

  ObjectListing listing = this.amazonS3Client.listObjects(request);

  return listing.getObjectSummaries().stream().map(this::from)
   .collect(Collectors.toList());
 }

 @PostMapping("/resources/{name}")
 ResponseEntity<?> upload(@PathVariable String name,
  @RequestParam MultipartFile file) throws Throwable {

  if (!file.isEmpty()) {
   ObjectMetadata objectMetadata = new ObjectMetadata();
   objectMetadata.setContentType(file.getContentType());
   PutObjectRequest request = new PutObjectRequest(this.defaultBucket, name,
    file.getInputStream(), objectMetadata)
    .withCannedAcl(CannedAccessControlList.PublicRead);
   PutObjectResult objectResult = this.amazonS3Client.putObject(request);
   URI location = URI.create(urlFor(this.defaultBucket, name));
   String str = String
    .format("uploaded %s at %s to %s", objectResult.getContentMd5(), Instant
     .now().toString(), location.toString());
   log.debug(str);
   return ResponseEntity.created(location).build();
  }
  return ResponseEntity.badRequest().build();
 }

 private String urlFor(String bucket, String file) {
  return String.format("https://s3.amazonaws.com/%s/%s", bucket, file);
 }

 private Resource<S3ObjectSummary> from(S3ObjectSummary a) {
  Link link = new Link(this.urlFor(a.getBucketName(), a.getKey()))
   .withRel("location");

  return new Resource<>(a, link);
 }
}
