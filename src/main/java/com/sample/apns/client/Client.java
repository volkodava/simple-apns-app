package com.sample.apns.client;

import com.relayrides.pushy.apns.DeliveryPriority;
import com.sample.apns.client.service.MessageSender;
import com.sample.apns.client.service.PayloadService;
import com.sample.apns.common.Configuration;
import org.apache.camel.util.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class Client {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private static final AbstractApplicationContext context = new ClassPathXmlApplicationContext("camel-client.xml");
    private static final Configuration configuration = context.getBean("configuration", Configuration.class);
    private static final PayloadService payloadService = context.getBean("payloadService", PayloadService.class);
    private static final MessageSender messageSender = context.getBean("messageSender", MessageSender.class);

    private Client() {
    }

    public static void main(final String[] args) throws Exception {
        if (!configuration.serverPortInUse()) {
            LOG.warn("Use: 'mvn compile exec:java -Pserver' to start the Server.");
            throw new IllegalStateException(
                    "Client requires that the Server is already running! " +
                            "Server is NOT running on port: " + configuration.getServerPort());
        }

        Map<String, Object> message = new HashMap<>();
        // hexadecimal bytes of the device token for the target device (device id)
        message.put("token", "00fc13adff785122b4ad28809a3420982341241421348097878e577c991de8f0");
        // application bundle id (topic)
        message.put("topic", "com.yourcompany.yourexampleapp");
        // json payload of notification
        message.put("payload", payloadService.compactJson("{ \"aps\" : { \"alert\" : \"Hello\" } }"));
        message.put("invalidationTime", configuration.convertDateToString(new Date()));
        message.put("priority", DeliveryPriority.IMMEDIATE.name());

        try {
            messageSender.sendMessage(message);
        } finally {
            // close the application context
            IOHelper.close(context);
        }
    }
}
