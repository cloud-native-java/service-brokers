package com.example;

import amazon.s3.AmazonProperties;
import amazon.s3.AmazonS3Template;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/s3")
class AmazonS3Controller {

 private final AmazonS3Template amazonS3Template;

 private final String bucketName;

 @Autowired
 AmazonS3Controller(AmazonS3Template template, AmazonProperties properties) {
  this.amazonS3Template = template;
  this.bucketName = properties.getS3().getDefaultBucket();
 }

 @ResponseBody
 @GetMapping("/resources")
 List<Resource<S3ObjectSummary>> list() {

  ListObjectsRequest request = new ListObjectsRequest()
   .withBucketName(this.bucketName);

  ObjectListing listing = this.amazonS3Template.listObjects(request);

  return listing.getObjectSummaries().stream().map(this::from)
   .collect(Collectors.toList());
 }

 @PostMapping("/resources")
 String upload(@RequestParam String name, @RequestParam MultipartFile file)
  throws Throwable {

  if (!file.isEmpty()) {
   ObjectMetadata objectMetadata = new ObjectMetadata();
   objectMetadata.setContentType(file.getContentType());

   PutObjectRequest request = new PutObjectRequest(this.bucketName, name,
    file.getInputStream(), objectMetadata)
    .withCannedAcl(CannedAccessControlList.PublicRead);

   this.amazonS3Template.putObject(request);
  }
  return "/";
 }

 private Resource<S3ObjectSummary> from(S3ObjectSummary a) {
  Link link = new Link(String.format("https://s3.amazonaws.com/%s/%s",
   a.getBucketName(), a.getKey())).withRel("url");
  return new Resource<>(a, link);
 }
}
