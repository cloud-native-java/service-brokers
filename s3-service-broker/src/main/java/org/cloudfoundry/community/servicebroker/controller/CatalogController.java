package org.cloudfoundry.community.servicebroker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cloudfoundry.community.servicebroker.catalog.Catalog;
import org.cloudfoundry.community.servicebroker.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * See: Source: http://docs.cloudfoundry.com/docs/running/architecture/services/writing-service.html
 *
 * @author sgreenberg@gopivotal.com
 */
@RestController
public class CatalogController extends BaseController {

    public static final String BASE_PATH = "/v2/catalog";

    private static final Logger logger = LoggerFactory.getLogger(CatalogController.class);

    private CatalogService service;
    private ObjectMapper objectMapper;

    @Autowired
    public CatalogController(CatalogService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @ResponseBody
    @RequestMapping(value = BASE_PATH, method = RequestMethod.GET)
    public Catalog getCatalog() {
        logger.debug("GET: " + BASE_PATH + ", getCatalog()");
        return service.getCatalog();
    }

    @ResponseBody
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<Map> getIndex() throws IOException {
        return new ResponseEntity<Map>(objectMapper.readValue("{ \"status\": \"UP\" }", HashMap.class), HttpStatus.OK);
    }

}
