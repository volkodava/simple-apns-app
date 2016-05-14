package com.sample.apns.server;

import org.apache.camel.builder.RouteBuilder;

/**
 * This class defines the routes on the Server.
 */
public class Server extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // subscribed to the topic with the name, resolved from properties - {{topic.address}}
        // deliver messages to the logger and spring bean
        from("{{topic.address}}").
                to("log:com.sample.apns.server?level=INFO").
                to("bean:apnsNotifier?method=push");
    }
}
