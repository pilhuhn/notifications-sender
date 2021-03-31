package com.redhat.cloud.notifications.sender;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

/**
 *
 */
@ApplicationScoped
public class MainConfig extends RouteBuilder {

    public void configure () {

        from("kafka:notifs")
            .log("Message received from Kafka : ${body}")
            .log("    on the topic ${headers[kafka.TOPIC]}")
            .log("    on the partition ${headers[kafka.PARTITION]}")
            .log("    with the offset ${headers[kafka.OFFSET]}")
            .log("    with the key ${headers[kafka.KEY]}")

            .setHeader("targetUrl",jsonpath("$.meta.url"))
            .setHeader("type",jsonpath("$.meta.type"))
            // translate the json formatted string body into a Java class
            .unmarshal().json()
            .choice()
                .when().simple("${header.type}== 'webhook'")
                    .to("qute:notifications/webhook")
                    .toD("vertx-http:" + "${header.targetUrl}")
                .when().simple("${header.type}== 'slack'")
                    .to("qute:notifications/slack.txt")
                    .toD("slack:#heiko-test?webhookUrl=${header.targetUrl}")
                .otherwise()
                    .log(LoggingLevel.ERROR, "Unsupported type: " + simple("${header.type}"))
                   // TODO flag as failure
            .end()
                // Processing is done, now look at the output
            .onCompletion()
                .to("kafka:notif-return")

        ;

    }
}
