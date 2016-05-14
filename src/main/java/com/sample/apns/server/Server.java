package com.sample.apns.server;

import org.apache.camel.builder.RouteBuilder;

/**
 * This class defines the routes on the Server.
 */
public class Server extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // the same as above but expressed as a URI configuration
        from("{{topic.address}}").
                to("log:com.sample.apns.server?level=INFO").
                to("bean:apnsNotifier?method=push");
    }
}
