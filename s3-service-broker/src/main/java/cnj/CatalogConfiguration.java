package cnj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
class CatalogConfiguration {

 private final String spacePrefix;

 @Autowired
 CatalogConfiguration( // <1>
  @Value("${vcap.application.space_id:${USER:}}") String spaceId) {
  this.spacePrefix = spaceId + '-';
 }

 @Bean
 Catalog catalog() {
  Plan basic = buildBasicPlan(); // <2>
  ServiceDefinition definition = buildS3ServiceDefinition(basic); // <3>
  return new Catalog(Collections.singletonList(definition));
 }

 private ServiceDefinition buildS3ServiceDefinition(Plan basic) {
  String description = "Provides AWS S3 access";
  String id = this.spacePrefix + "1489291412183";
  String name = "s3-service-broker";
  boolean bindable = true;
  List<Plan> plans = Collections.singletonList(basic);
  return new ServiceDefinition(id, name, description, bindable, plans);
 }

 private Plan buildBasicPlan() {
  String planName = "basic";
  String planDescription = "Amazon S3 bucket with unlimited storage";
  boolean free = true;
  boolean bindable = true;
  String planId = this.spacePrefix + "249722552510577";
  Map<String, Object> metadata = Collections.singletonMap("costs",
   Collections.singletonMap("free", true));
  return new Plan(planId, planName, planDescription, metadata, free, bindable);
 }
}
