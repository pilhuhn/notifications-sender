package com.redhat.cloud.notifications.sender;

import com.redhat.cloud.notifications.sender.generated.Incident;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

/**
 *
 */
public class SnowResultTransformer implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();

        Incident incident = in.getBody(Incident.class);

        // Return the incident id back to the caller
        // Should probably be in a better place ...
        in.setHeader("targetUrl", incident.getNumber());
        in.setBody("Success " + incident.getNumber());
    }
}
