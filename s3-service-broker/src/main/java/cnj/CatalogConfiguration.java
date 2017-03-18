package cnj;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
class CatalogConfiguration {

	private final Log log = LogFactory.getLog(getClass());
	private final String spacePrefix;

	CatalogConfiguration(
			@Value("${vcap.application.space_id:${USER:}}") String spaceId) {
		this.spacePrefix = String.format("%s-", spaceId.chars().boxed()
				.filter(Character::isLetterOrDigit)
				.map(i -> Character.toString(Character.toChars(i)[0]))
				.collect(Collectors.joining("")));

		this.log.info("catalog prefix = " + this.spacePrefix);
	}

	@Bean
	Catalog catalog() {
		List<ServiceDefinition> definitions = Collections
				.singletonList(new ServiceDefinition(
						this.spacePrefix + "1489291412183",
						"s3-service-broker",
						"A simple AWS S3 service broker implementation",
						true,
						false,
						Collections.singletonList(new Plan(this.spacePrefix + "249722552510577", "basic",
								"Amazon S3 bucket with unlimited storage", getPlanMetadata(), true, true)),
						Arrays.asList("s3", "storage", "cache", "AWS"),
						getServiceDefinitionMetadata(), null, null));
		return new Catalog(definitions);
	}

	private Map<String, Object> getServiceDefinitionMetadata() {
		Map<String, Object> sdMetadata = new HashMap<>();
		sdMetadata.put("displayName", "s3-service-broker");
		sdMetadata.put("providerDisplayName", "s3-service-broker");
		sdMetadata.put("longDescription",
				"A backing service with unlimited Amazon S3 storage");
		sdMetadata.put("documentationUrl", "http://aws.amazon.com/s3");
		sdMetadata.put("supportUrl", "http://aws.amazon.com/s3");
		return sdMetadata;
	}

	private Map<String, Object> getPlanMetadata() {
		return Collections.singletonMap("costs", Collections.singletonMap("free", true));
	}
}
