package com.sample.apns.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.util.IOHelper;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Client that uses the {@link ProducerTemplate} to easily exchange messages
 * with the Server.
 * <p/>
 * Requires that the JMS broker is running, as well as Server
 */
public final class Client {

    static final Logger log = LoggerFactory.getLogger(Client.class);
    static final int MAX_PAYLOAD_SIZE = 4096;

    static final AbstractApplicationContext context = new ClassPathXmlApplicationContext("camel-client.xml");
    static final ObjectMapper mapper = context.getBean("jacksonObjectMapper", ObjectMapper.class);

    private Client() {
    }

    public static void main(final String[] args) throws Exception {
        log.info("Notice this client requires that the Server is already running!");
        log.info("Use: 'mvn compile exec:java -Pserver' to start the Server.");

        Map<String, Object> message = new HashMap<>();
        // hexadecimal bytes of the device token for the target device (device id)
        message.put("token", "00fc13adff785122b4ad28809a3420982341241421348097878e577c991de8f0");
        // application bundle id (topic)
        message.put("topic", "com.yourcompany.yourexampleapp");
        // json payload of notification
        message.put("payload", compactJson("{ \"aps\" : { \"alert\" : \"Hello\" } }"));

        new Client().sendMessage(message);
    }

    private void sendMessage(Map<String, Object> message) throws Exception {
        // get the camel template for spring template style sending of messages (= producer)
        ProducerTemplate camelTemplate = context.getBean("camelTemplate", ProducerTemplate.class);

        log.info("Invoking service with:\n" + message);
        String response = (String) camelTemplate.sendBody("{{topic.address}}", ExchangePattern.InOut, message);
        log.info("... the result is: " + response);

        // close the application context
        IOHelper.close(context);
    }

    private static String compactJson(String payload) throws IOException {
        Object parsedObject = mapper.readValue(payload, Object.class);
        String result = mapper.writeValueAsString(parsedObject);

        Validate.isTrue(result.length() <= MAX_PAYLOAD_SIZE, "Payload size must be less or equal to " + MAX_PAYLOAD_SIZE + " bytes.");

        return result;
    }
}
