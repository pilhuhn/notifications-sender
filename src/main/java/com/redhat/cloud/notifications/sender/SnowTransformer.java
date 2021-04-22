package com.redhat.cloud.notifications.sender;

import com.redhat.cloud.notifications.sender.generated.Incident;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import java.util.Map;

/**
 * Class to transform the generic input data into a
 * ServiceNow Incident.
 *
 * The Incident class is generated via the maven plugin
 * by talking to the snow instance.
 */
public class SnowTransformer implements Processor {

    public void process(Exchange exchange) throws Exception {

        Message in = exchange.getIn();
        Map<String,Object> body = (Map<String, Object>) in.getBody();
        Map<String,Object> payload = (Map<String, Object>) body.get("payload");
        Map<String,String> meta = (Map<String, String>) body.get("meta");

        String cid = in.getHeader("cid", String.class);

        String token = meta.get("X-Insight-Token");
        in.setHeader("token", token);
        Incident out = new Incident();
        out.setActive(true);
        out.setCategory("test");
        out.setDescription("This is a test");
        out.setShortDescription("Test from camel with id " + cid);
        String comments = createDescription((Map<String, String>) payload.get("payload"));
        out.setComments(comments);


        in.setBody(out);
    }

    private String createDescription(Map<String, String> payload) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,String> entry : payload.entrySet()) {
            sb.append("Key ").append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
