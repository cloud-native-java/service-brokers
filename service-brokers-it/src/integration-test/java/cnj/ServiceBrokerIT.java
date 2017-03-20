package cnj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationManifest;
import org.cloudfoundry.operations.serviceadmin.DeleteServiceBrokerRequest;
import org.cloudfoundry.operations.serviceadmin.ServiceBroker;
import org.cloudfoundry.operations.services.ListServiceOfferingsRequest;
import org.cloudfoundry.operations.services.ServicePlan;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ServiceBrokerIT.Config.class)
public class ServiceBrokerIT {

 @SpringBootApplication
 public static class Config {
 }

 private final Log log = LogFactory.getLog(getClass());

 private final String serviceBrokerRootName = "s3-service-broker";

 private final String serviceBrokerName = "amazon-s3";

 private final RestTemplate restTemplate = new RestTemplateBuilder().build();

 private File sampleApplicationDirectory, serviceBrokerApplicationDirectory,
  serviceBrokerApplicationManifest, sampleApplicationManifest;

 @Autowired
 private CloudFoundryOperations cloudFoundryOperations;

 @Autowired
 private CloudFoundryService cf;

 private ClassPathResource jpgResource;

 @Before
 public void setUp() throws Throwable {
  log.info("setUP()!");
  this.jpgResource = new ClassPathResource("/cnj.jpg");
  Assert.assertTrue("the image we will upload later should exist.",
   this.jpgResource.exists());
  File root = new File(".");
  this.serviceBrokerApplicationDirectory = new File(root,
   "../s3-service-broker");
  this.sampleApplicationDirectory = new File(root, "../s3-sample");
  this.serviceBrokerApplicationManifest = new File(
   this.serviceBrokerApplicationDirectory, "manifest.yml");
  this.sampleApplicationManifest = new File(this.sampleApplicationDirectory,
   "manifest.yml");
  this.reset();
 }

 @After
 public void tearDown() throws Throwable {

  String teardownEnvVarName = "TEARDOWN_SERVICE_BROKER";
  String env = System.getenv(teardownEnvVarName);
  if (env != null && env.equalsIgnoreCase("false")) {
   log.info(teardownEnvVarName + " is set to false, so not "
    + "tearing down the service broker integration test");
   return;
  }

  log.info("tearDown");
  this.reset();

 }

 @Test
 public void testDeployingServiceBroker() throws Throwable {
  this.deploy();
  String urlForSampleApp = this.cf.urlForApplication("s3-sample-app");
  String fileName = UUID.randomUUID().toString();
  String s3RootUrl = urlForSampleApp + "/s3/resources";
  MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
  parts.put(HttpHeaders.CONTENT_TYPE, Collections.singletonList("image/jpeg"));
  parts.put("file", Collections.singletonList(this.jpgResource));
  RequestEntity<MultiValueMap<String, Object>> requestEntity = RequestEntity
   .post(URI.create(s3RootUrl)).body(parts);
  ResponseEntity<String> post = this.restTemplate.exchange(s3RootUrl + "/"
   + fileName, HttpMethod.POST, requestEntity, String.class);
  Assert.assertEquals("the file " + fileName + " should have been uploaded",
   201, post.getStatusCode().value());
  ResponseEntity<JsonNode> responseEntity = this.restTemplate.exchange(
   s3RootUrl, HttpMethod.GET, null, JsonNode.class);
  Assert.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
  JsonNode body = responseEntity.getBody();
  AtomicReference<String> uriAR = new AtomicReference<>();
  ArrayNode.class
   .cast(body)
   .forEach(
    n -> {
     JsonNode jsonNode = n.get("links");
     ArrayNode.class
      .cast(jsonNode)
      .forEach(
       linkNode -> {
        String href = linkNode.get("href").asText();
        if (href.contains(fileName)) {
         uriAR.set(href);
         log
          .info("the file we uploaded ("
           + fileName
           + ") has been confirmed discovered in the service using the service broker.");
        }
       });
    });
  Assert.assertNotNull(
   "we should be able to find a matching URI in the responses for file "
    + fileName + "!", uriAR.get());
 }

 private void deploy() {

  String serviceBrokerApplicationName = this.deployServiceBrokerApplication();
  this.log.info("the service broker's name is " + serviceBrokerApplicationName);
  this.configureServiceBrokerForApplication(serviceBrokerApplicationName);
  String sampleApplicationName = this.deploySampleApp();
  this.log.info("deployed the sample application, " + sampleApplicationName);
 }

 private void reset() throws Throwable {

  // delete the sample app
  String sampleAppName = this.applicationNameFromManifest(this.cf
   .applicationManifestFrom(this.sampleApplicationManifest));

  this.cf.destroyApplicationIfExists(sampleAppName);
  log.info("deleted " + sampleAppName);

  // delete the sample apps backing
  // service S3
  String s3Service = "s3-service";
  this.cf.destroyServiceIfExists(s3Service);
  log.info("deleted " + s3Service);

  // delete the service broker app
  log.info("attempting to delete " + this.serviceBrokerRootName);
  this.cf.destroyApplicationIfExists(this.serviceBrokerRootName);
  log.info("deleted application " + this.serviceBrokerRootName);

  // delete the service broker if it
  // exists
  ServiceBroker broker = this.cloudFoundryOperations.serviceAdmin().list()
   .filter(sb -> sb.getName().equals(this.serviceBrokerName)).blockFirst();

  if (null != broker) {
   log.info("attempting to delete service broker " + this.serviceBrokerName);
   this.cloudFoundryOperations
    .serviceAdmin()
    .delete(DeleteServiceBrokerRequest.builder().name(broker.getName()).build())
    .block();
   log.info("deleted service broker " + this.serviceBrokerName
    + " if it exists.");
  }

  // delete the service broker backing
  // service DB
  this.cf.destroyServiceIfExists(this.serviceBrokerRootName + "-db");
  log.info("deleted the service broker backing service "
   + this.serviceBrokerRootName + "-db");

  // clearing routes
  this.cf.destroyOrphanedRoutes();
  log.info("deleted orphaned routes");
 }

 private String deployServiceBrokerApplication() {
  String serviceBrokerAppDbName = this.serviceBrokerRootName + "-db";
  log.info("listing MySQL capable services.");
  this.cloudFoundryOperations
   .services()
   .listServiceOfferings(ListServiceOfferingsRequest.builder().build())
   .toStream()
   .filter(
    svc -> {
     String mySqlDbName = "mysql";
     return svc.getDescription().toLowerCase().contains(mySqlDbName)
      || svc.getLabel().toLowerCase().contains(mySqlDbName);
    })
   .map(
    svc -> Collections.singletonMap(svc,
     svc.getServicePlans().stream().filter(ServicePlan::getFree).findFirst()))
   .forEach(
    map -> map.forEach((so, key) -> key.ifPresent(sp -> {
     log.info("creating a service: " + so.getLabel() + ' ' + sp.getName() + ' '
      + serviceBrokerAppDbName);
     this.cf.createServiceIfMissing(so.getLabel(), sp.getName(),
      serviceBrokerAppDbName);
    })));

  Map<File, ApplicationManifest> manifestMap = this.cf
   .applicationManifestFrom(this.serviceBrokerApplicationManifest);

  Map<String, String> env = new HashMap<>();
  Arrays.asList("AWS_ACCESS_KEY_ID,AWS_SECRET_ACCESS_KEY".split(",")).forEach(
   x -> env.put(x, System.getenv(x)));
  manifestMap.forEach((f, am) -> this.cf.pushApplicationUsingManifest(f, am,
   env, true));
  return this.applicationNameFromManifest(manifestMap);
 }

 private void configureServiceBrokerForApplication(String appName) {
  log.info("configuring service broker for the application " + appName);
  String urlForApplication = this.cf.urlForApplication(appName);
  this.cf.createServiceBroker(this.serviceBrokerName, urlForApplication,
   "admin", "admin", true);
 }

 private String applicationNameFromManifest(
  Map<File, ApplicationManifest> manifestMap) {
  Optional<String> optional = manifestMap.values().stream().findFirst()
   .map(ApplicationManifest::getName);
  return optional.orElseThrow(() -> new RuntimeException(
   "the sample application must have a name!"));
 }

 private String deploySampleApp() {
  this.cf.createServiceIfMissing(this.serviceBrokerRootName, "basic",
   "s3-service");
  Map<File, ApplicationManifest> manifestMap = this.cf
   .applicationManifestFrom(sampleApplicationManifest);
  manifestMap.forEach((f, am) -> this.cf.pushApplicationUsingManifest(f, am,
   true));
  return this.applicationNameFromManifest(manifestMap);
 }
}
