package com.redhat.cloud.notifications.sender;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.qute.QuteEndpoint;
import org.apache.camel.component.slack.SlackComponent;
import org.apache.camel.impl.DefaultCamelContext;

/**
 *
 */
@ApplicationScoped
public class MainConfig extends RouteBuilder {

    public void configure () {

        from("kafka:notifs?brokers=localhost:9092")
            .log("Message received from Kafka : ${body}")
            .log("    on the topic ${headers[kafka.TOPIC]}")
            .log("    on the partition ${headers[kafka.PARTITION]}")
            .log("    with the offset ${headers[kafka.OFFSET]}")
            .log("    with the key ${headers[kafka.KEY]}")

            //.dynamicRouter(method(Router.class,"route"))


            .setHeader("targetUrl",jsonpath("$.meta.url"))
            .setHeader("type",jsonpath("$.meta.type"))
            .setHeader(Exchange.HTTP_URI, jsonpath("$.meta.url"))
            .choice()
                .when().jsonpath("$.meta[?(@.type=='webhook')]")
                .setHeader(Exchange.HTTP_URI, jsonpath("$.meta.url"))
                    .to("qute:notifications/webhook")
                    .to("ahc:" + header("targetUrl"))   // header() is not eval'd, but turned to string
                .when().jsonpath("$.meta[?(@.type=='slack')]" )
                    .to("qute:notifications/slack.txt")
                    .to("slack:#heiko-test?webHookUrl=" + jsonpath("$.meta.url")) // concat fails
            .end()
            .log("  _>  ${body}")

        ;


//        from ("qute:/notifications/slack.txt")
//                .log("now qute slack");

        from("kafka:bla?brokers=localhost:9092").to("qute:notifications/slack.txt")
                .log("++++++++++ Template eval +++++++")
                .log("${body}")
                .log("++++++++++---------------+++++++")
        ;
    }
}
