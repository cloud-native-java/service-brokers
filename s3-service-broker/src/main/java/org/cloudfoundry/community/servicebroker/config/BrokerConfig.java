package org.cloudfoundry.community.servicebroker.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cloudfoundry.community.servicebroker.catalog.Plan;
import org.cloudfoundry.community.servicebroker.catalog.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.repositories.ServiceDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(BrokerProperties.class)
public class BrokerConfig {

    @Autowired
    private BrokerProperties brokerProperties;

    @Bean
    CommandLineRunner commandLineRunner(ServiceDefinitionRepository serviceDefinitionRepository,
                                        Environment environment) {
        return args -> {
            // Initialize the service broker definition when running in cloud profile
            if (Arrays.asList(environment.getActiveProfiles()).contains("cloud")) {

                // Initialize a default service definition for the purpose of this example
                Plan plan = new Plan(brokerProperties.getBasicPlan().getId(), brokerProperties.getBasicPlan().getName(),
                        brokerProperties.getBasicPlan().getDescription());

                // Set plan to free
                plan.setFree(brokerProperties.getBasicPlan().getFree());

                // Create the default service definition describing the broker's purpose
                ServiceDefinition serviceDefinition =
                        new ServiceDefinition(brokerProperties.getDefinition().getId(),
                                brokerProperties.getDefinition().getName(),
                                brokerProperties.getDefinition().getDescription(),
                                brokerProperties.getDefinition().getBindable(),
                                Collections.singleton(plan));

                // Set meta data
                serviceDefinition.setMetadata(getServiceDefinitionMetaData());

                // Update plan
                serviceDefinitionRepository.save(serviceDefinition);
            }
        };
    }

    @Bean
    @Primary
    public ObjectMapper jacksonObjectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .defaultViewInclusion(false)
                .autoDetectFields(true)
                .indentOutput(true)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();
    }

    public Map<String, Object> getServiceDefinitionMetaData() {
        Map<String, Object> sdMetadata = new HashMap<>();

        sdMetadata.put("providerDisplayName", brokerProperties.getProviderDisplayName());
        sdMetadata.put("documentationUrl", brokerProperties.getDocumentationUrl());
        sdMetadata.put("supportUrl", brokerProperties.getSupportUrl());
        sdMetadata.put("displayName", brokerProperties.getDisplayName());
        sdMetadata.put("longDescription", brokerProperties.getLongDescription());
        sdMetadata.put("imageUrl", brokerProperties.getImageUrl());

        return sdMetadata;
    }
}
