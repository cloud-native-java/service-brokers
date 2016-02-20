package org.cloudfoundry.community.servicebroker.model;


import org.cloudfoundry.community.servicebroker.config.BrokerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ServiceMetaData {
    public static Map<String, Object> META_DATA;

    @Autowired
    public ServiceMetaData(BrokerConfig brokerConfig) {
        ServiceMetaData.META_DATA = brokerConfig.getServiceDefinitionMetaData();
    }
}
