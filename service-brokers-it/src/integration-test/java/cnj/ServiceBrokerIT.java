package cnj;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationManifest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import sun.plugin.dom.exception.InvalidStateException;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ServiceBrokerIT.Config.class)
public class ServiceBrokerIT {

	@SpringBootApplication
	public static class Config {
	}

	private final Log log = LogFactory.getLog(getClass());
	private final String serviceBrokerRootName = "s3-service-broker";
	private final String serviceBrokerName = "amazon-s3";
	private File serviceBrokerApplicationDirectory;

	@Autowired
	private CloudFoundryService cloudFoundryService;

	@Autowired
	private CloudFoundryOperations cloudFoundryOperations;

	@Before
	public void before() throws Throwable {
		File root = new File(".");
		this.serviceBrokerApplicationDirectory = new File(root, "../s3-service-broker");
	}

	private String deployServiceBrokerApplication() {
		String serviceBrokerAppDbName = this.serviceBrokerRootName + "-db";
		this.cloudFoundryService.createServiceIfMissing("cleardb", "spark", serviceBrokerAppDbName);
		File manifest = new File(this.serviceBrokerApplicationDirectory, "manifest.yml");
		Map<File, ApplicationManifest> manifestMap = this.cloudFoundryService.applicationManifestFrom(manifest);
		Optional<String> optional = manifestMap.values().stream().findFirst().map(ApplicationManifest::getName);
		Map<String, String> env = new HashMap<>();
		Arrays.asList("AWS_ACCESS_KEY_ID,AWS_SECRET_ACCESS_KEY".split(",")).forEach(x -> env.put(x, System.getenv(x)));
		manifestMap.forEach((f, am) -> this.cloudFoundryService.pushApplicationUsingManifest(f, am, env, true));
		return optional.orElseThrow(() -> new InvalidStateException("the application must have a name!"));
	}

	private void configureServiceBrokerForApplication(String appName) {
		log.info("configuring service broker for the application " + appName);
		String urlForApplication = this.cloudFoundryService.urlForApplication(appName);
		cloudFoundryService.createServiceBroker(this.serviceBrokerName,
				urlForApplication, "admin", "admin", true);
	}

	@Test
	public void testDeployingServiceBroker() throws Throwable {
		String serviceBrokerApplicationName = this.deployServiceBrokerApplication();
		this.configureServiceBrokerForApplication(serviceBrokerApplicationName);
	}

	/*

 @Before
 public void before() throws Throwable {

  File root = new File(".");
  File configClientManifest = new File(root, "../configuration-client/manifest.yml");
  File configServiceManifest = new File(root, "../configuration-service/manifest.yml");

  String rmqService = "rabbitmq-bus";
  this.service.createServiceIfMissing("cloudamqp", "lemur", rmqService);
  this.service.pushApplicationAndCreateUserDefinedServiceUsingManifest(configServiceManifest);
  this.service.pushApplicationUsingManifest(configClientManifest);
 }

 @Test
 public void clientIsConnectedToService() throws Exception {

  String configClientUrl = this.service.urlForApplication("configuration-client");
  log.info("the application is running at " + configClientUrl);
  String url = configClientUrl + "/project-name";
  log.info("url: " + url);
  ResponseEntity<String> entity = this.restTemplate.getForEntity(url,
    String.class);
  assertEquals(entity.getStatusCode(), HttpStatus.OK);
  assertTrue(entity.getBody().contains("Spring Cloud"));
 }*/
}
