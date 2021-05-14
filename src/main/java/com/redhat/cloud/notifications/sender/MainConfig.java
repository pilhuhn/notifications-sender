package com.redhat.cloud.notifications.sender;

import javax.enterprise.context.ApplicationScoped;

import com.redhat.cloud.notifications.sender.generated.Incident;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servicenow.ServiceNowConstants;

import java.io.IOException;

/**
 * The main class that does the work setting up the Camel routes.
 * Entry point for messages is blow 'from("kafka:notifs")
 * Upon success/failure a message is returned in the notif-return
 * topic.
 *
 * We need to register some classes for reflection here, so that
 * native compilation works.
 */
@RegisterForReflection(targets = {
    Exception.class,
    IOException.class
})
@ApplicationScoped
public class MainConfig extends RouteBuilder {

    public void configure () {

        Processor myTransformer = new ResultTransformer();
        Processor snowTransformer = new SnowTransformer();
        Processor snowResultTransformer = new SnowResultTransformer();

        // If the webhook sender fails, we mark the route as handled
        // and forward to the error handler
        // Setting handled to true ends the processing chain below
        onException(IOException.class)
            .to("direct:error")
            .handled(true);


        // The error handler. We set the outcome to fail and then send to kafka
        from("direct:error")
                .setBody(constant("Fail"))
                .process(myTransformer)
                .marshal().json()
                .log("Fail with ${body} and ${header.cid}")
                .to("kafka:notif-return")
        ;

        from("direct:webhook")
            .to("qute:notifications/webhook")
            .toD("vertx-http:${header.targetUrl}/${header.cid}")
                .setBody(constant("Success")) // TODO ?
            ;

        from("direct:slack")
            .to("qute:notifications/slack.txt")
            .toD("slack:#heiko-test?webhookUrl=${header.targetUrl}")
                .setBody(constant("Success")) // TODO ?
            ;

        // We create an Incident in ServiceNow
        from("direct:snow")
                .setHeader(ServiceNowConstants.REQUEST_MODEL).constant(Incident.class)
                .setHeader(ServiceNowConstants.ACTION, simple(ServiceNowConstants.ACTION_CREATE))
                .setHeader(ServiceNowConstants.RESOURCE, simple("table"))
                .setHeader(ServiceNowConstants.RESOURCE_TABLE, simple("incident"))
                .setHeader("CamelServiceNowTable", simple("incident"))
                .setHeader(ServiceNowConstants.MODEL).constant(Incident.class)

            .process(snowTransformer)
            .toD("servicenow:${header.targetUrl}" +
                    "?userName=${header.user}&password=${header.token}" +
                    "")
            .process(snowResultTransformer)
                ;


        from("direct:tower")
            .toD("tower:${header.targetUrl}?basicAuth=${header.basicAuth}");

        /*
         * Main processing entry point, receiving data from Kafka
         */
        from("kafka:notifs")
            .log("Message received via Kafka : ${body}")

            .setHeader("timeIn", simpleF("%d",System.currentTimeMillis()))
            .setHeader("targetUrl",jsonpath("$.meta.url"))
            .setHeader("type",jsonpath("$.meta.type"))
            .setHeader("cid", jsonpath("$.meta.historyId"))
            .setHeader("basicAuth", jsonpath("$.meta.basicAuth"))

            .errorHandler(
                    deadLetterChannel("direct:error"))
            // translate the json formatted string body into a Java class
            .unmarshal().json()
            .choice()
                .when().simple("${header.type}== 'webhook'")
                    .to("direct:webhook")
                .when().simple("${header.type}== 'slack'")
                    .to("direct:slack")
                .when().simple("${header.type}== 'snow'")
                    .to("direct:snow")
                .when().simple("${header.type}== 'ansible'")
                    .to("direct:tower")
                .otherwise()
                    .log(LoggingLevel.ERROR, "Unsupported type: ${header.type}")
                   // flag as failure
                    .to("direct:error")
            .end()
            // Processing is done, now look at the output
            .process(myTransformer)
                .marshal().json()
                .log("Success with ${body} and ${header.cid}")
            .to("kafka:notif-return")
        ;

    }
}
