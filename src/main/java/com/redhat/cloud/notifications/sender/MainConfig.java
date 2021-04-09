package com.redhat.cloud.notifications.sender;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import java.io.IOException;

/**
 *
 */
@ApplicationScoped
public class MainConfig extends RouteBuilder {

    public void configure () {

        Processor myTansformer = new ResultTransformer();

        // If the webhook sender fails, we mark the route as handled
        // and forward to the error handler
        // Setting handled to true ends the processing chain below
        onException(IOException.class)
            .to("direct:error")
            .handled(true);


        // The error handler. We set the outcome to fail and then send to kafka
        from("direct:error")
                .setBody(constant("Fail"))
                .process(myTansformer)
                .marshal().json()
                .log("Fail with ${body} and ${header.cid}")
                .to("kafka:notif-return")
        ;

        from("direct:webhook")
            .to("qute:notifications/webhook")
            .toD("vertx-http:${header.targetUrl}")
            ;

        from("direct:slack")
            .to("qute:notifications/slack.txt")
            .toD("slack:#heiko-test?webhookUrl=${header.targetUrl}")
            ;


        /*
         * Main processing entry point, receiving data from Kafka
         */

        from("kafka:notifs")
            .log("Message received from Kafka : ${body}")

            .setHeader("timeIn", simpleF("%d",System.currentTimeMillis()))
            .setHeader("targetUrl",jsonpath("$.meta.url"))
            .setHeader("type",jsonpath("$.meta.type"))
            .setHeader("cid", jsonpath("$.meta.historyId"))
            .log("  with ID ${cid}" )

            .errorHandler(
                    deadLetterChannel("direct:error"))
            // translate the json formatted string body into a Java class
            .unmarshal().json()
            .choice()
                .when().simple("${header.type}== 'webhook'")
                    .to("direct:webhook")
                .when().simple("${header.type}== 'slack'")
                    .to("direct:slack")
                .otherwise()
                    .log(LoggingLevel.ERROR, "Unsupported type: ${header.type}")
                   // TODO flag as failure
            .end()
                // Processing is done, now look at the output
            .setBody(constant("Success"))
            .process(myTansformer)
                .marshal().json()
                .log("Success with ${body} and ${header.cid}")
            .to("kafka:notif-return")
        ;

    }
}
