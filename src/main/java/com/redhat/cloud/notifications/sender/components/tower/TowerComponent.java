package com.redhat.cloud.notifications.sender.components.tower;

import org.apache.camel.Endpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

/**
 *
 */
public class TowerComponent extends DefaultComponent  {

    @Metadata(label = "security", secret = true)
    String user;
    @Metadata(label = "security", secret = true)
    String password;
    @Metadata(required = true)
    String template;

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        System.out.println("re" + remaining);

        TowerEndpoint towerEndpoint = new TowerEndpoint(uri, remaining, parameters);
        return towerEndpoint;

    }

}
