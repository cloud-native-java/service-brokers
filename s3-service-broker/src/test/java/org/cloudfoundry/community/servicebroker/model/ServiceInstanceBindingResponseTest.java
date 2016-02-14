package org.cloudfoundry.community.servicebroker.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.cloudfoundry.community.servicebroker.catalog.ServiceInstanceBinding;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceInstanceBindingResponseTest {

    private ServiceInstanceBindingResponse serviceInstanceBindingResponse;
    private ServiceInstanceBinding serviceInstanceBinding;
    private ObjectWriter objectWriter;

    @Before
    public void before() throws Throwable {
        ObjectMapper objectMapper = new ObjectMapper();
        this.serviceInstanceBinding = Mockito.mock(ServiceInstanceBinding.class);
        this.serviceInstanceBindingResponse = new ServiceInstanceBindingResponse(serviceInstanceBinding);
        this.objectWriter = objectMapper.writerFor(ServiceInstanceBindingResponse.class);
    }

    @Test
    public void testGetSyslogDrainUrlWithoutNull() throws Exception {
        String syslogUrl = "syslog://some-log.com";
        when(this.serviceInstanceBinding.getSyslogDrainUrl()).thenReturn(syslogUrl);
        String result = objectWriter.writeValueAsString(this.serviceInstanceBindingResponse);
        assertTrue(result.contains("\"syslog_drain_url\":\"" + syslogUrl + "\""));
        verify(this.serviceInstanceBinding).getSyslogDrainUrl();
    }

    @Test
    public void testGetSyslogDrainUrlWithNull() throws Exception {
        when(this.serviceInstanceBinding.getSyslogDrainUrl()).thenReturn(null);
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().serializationInclusion(JsonInclude.Include.NON_NULL).build();
        String result = objectMapper.writeValueAsString(this.serviceInstanceBindingResponse);
        assertEquals(result, "{\"credentials\":{}}");
        verify(this.serviceInstanceBinding).getSyslogDrainUrl();
    }
}