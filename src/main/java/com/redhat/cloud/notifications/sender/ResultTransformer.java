package com.redhat.cloud.notifications.sender;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ResultTransformer implements Processor {

    public void process(Exchange exchange) throws Exception {

        Message in = exchange.getIn();
        String oldBody = in.getBody(String.class);

        String cid = in.getHeader("cid", String.class);

        Map<String,String> out = new HashMap<>();
        out.put("outcome", oldBody);
        out.put("historyId", cid);

        in.setBody(out);
    }


}
