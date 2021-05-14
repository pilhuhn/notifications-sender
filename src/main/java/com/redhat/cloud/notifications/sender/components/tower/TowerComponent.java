package com.redhat.cloud.notifications.sender.components.tower;

import org.apache.camel.Endpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

/**
 *
 */
public class TowerComponent extends DefaultComponent  {

    @Metadata(label = "security", secret = true, description = "Base64 encoded user:pass for http basic auth")
    String basicAuth;
    @Metadata(required = false, description = "The number of the template to run. Normally comes via meta->extras->template")
    String template;

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        System.out.println("re" + remaining);

        TowerEndpoint towerEndpoint = new TowerEndpoint(uri, remaining, parameters);
        return towerEndpoint;

    }

}
