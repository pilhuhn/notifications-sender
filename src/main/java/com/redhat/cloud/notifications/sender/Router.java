package com.redhat.cloud.notifications.sender;

import io.vertx.core.json.Json;
import org.apache.camel.DynamicRouter;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangeProperties;

import java.util.Map;

/**
 *
 */
public class Router {

    // This is currently not used.

    @DynamicRouter
    public String route(String body, @ExchangeProperties Map<String,Object> properties) {

        int invoked = 0;
        Object current = properties.get("invoked");
        if (current!=null) {
            invoked = Integer.valueOf(current.toString());
        }
        invoked++;
        properties.put("invoked",invoked);

        Object o = properties.get(Exchange.SLIP_ENDPOINT);
        System.out.println("Previous endpoint " + o);

        if (invoked==2) {
            System.out.println("+++ already seen ++");
            return null;
        }


//        System.out.println(body);
        Map jo = Json.decodeValue(body, Map.class);
        Map<String,String> meta = (Map<String, String>) jo.get("meta");

        String url = meta.get("url");
        switch (meta.get("type")) {
            case "webhook":
                return "ahc:" + url;
            case "slack":
                if (invoked==1) {
                    return "qute:notifications/slack.txt";
                }
                return "slack:#heiko-test?webhookUrl=" + url;

            // Fallback
            default:
                return null;
        }
    }
}
