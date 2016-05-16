package com.sample.apns.client;

import com.relayrides.pushy.apns.DeliveryPriority;
import com.sample.apns.client.service.MessageSender;
import com.sample.apns.client.service.PayloadService;
import com.sample.apns.common.Configuration;
import org.apache.camel.util.IOHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CmdClient {
    private static final Logger LOG = LoggerFactory.getLogger(CmdClient.class);

    private static final AbstractApplicationContext context = new ClassPathXmlApplicationContext("camel-client.xml");
    private static final Configuration configuration = context.getBean("configuration", Configuration.class);
    private static final PayloadService payloadService = context.getBean("payloadService", PayloadService.class);
    private static final MessageSender messageSender = context.getBean("messageSender", MessageSender.class);

    private CmdClient() {
    }

    public static void main(String[] args) throws Exception {
        if (!configuration.serverPortInUse()) {
            LOG.warn("Use: 'mvn compile exec:java -Pserver' to start the Server.");
            throw new IllegalStateException(
                    "Client requires that the Server is already running! " +
                            "Server is NOT running on port: " + configuration.getServerPort());
        }

        try {
            new CmdClient().runCmdLoop();
        } finally {
            // close the application context
            IOHelper.close(context);
        }
    }

    private void runCmdLoop() throws Exception {
        System.out.println("Type 'quite' to quit");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String token = readLine("Token", scanner);
            if (quite(token)) {
                break;
            }

            String topic = readLine("Topic", scanner);
            if (quite(topic)) {
                break;
            }

            String inputInvalidationTime = readLine("Invalidation Time [" + configuration.getDateTimeFormat() + "]", scanner);
            if (quite(inputInvalidationTime)) {
                break;
            }
            String invalidationTime = tryParseInvalidationTime(inputInvalidationTime);

            String inputPriority = readLine("Priority " + Arrays.toString(DeliveryPriority.values()), scanner);
            if (quite(inputPriority)) {
                break;
            }
            String priority = tryParseDeliveryPriority(inputPriority);

            String payload = readLines("Payload", scanner);
            if (quite(payload)) {
                break;
            }

            String inputCompactPayload = readLine("Compact payload [yes / no = default]", scanner);
            if (quite(inputCompactPayload)) {
                break;
            }
            boolean compactPayload = tryParseUseCompactPayloadAnswer(inputCompactPayload);

            if (compactPayload) {
                payload = payloadService.compactJson(payload);
            }
            submitPushNotification(token, topic, payload, invalidationTime, priority);
        }
    }

    private boolean tryParseUseCompactPayloadAnswer(String compactPayloadStr) {
        boolean compactPayload = false;
        if (!StringUtils.isBlank(compactPayloadStr) &&
                compactPayloadStr.trim().toLowerCase().startsWith("yes")) {
            compactPayload = true;
        }
        return compactPayload;
    }

    private String tryParseDeliveryPriority(String priorityStr) {
        String priority = DeliveryPriority.IMMEDIATE.name();
        if (priorityStr != null) {
            try {
                priority = DeliveryPriority.valueOf(priorityStr.trim()).name();
            } catch (RuntimeException e) {
                LOG.warn("Can't parse priority string");
            }
        }
        return priority;
    }

    private String tryParseInvalidationTime(String invalidationTimeStr) {
        if (invalidationTimeStr != null) {
            String trimmedDateStr = invalidationTimeStr.trim();
            try {
                configuration.getDateTimeFormatter().parse(trimmedDateStr);
                return trimmedDateStr;
            } catch (Exception e) {
                LOG.warn("Can't parse invalidation time");
            }
        }

        return null;
    }

    private void submitPushNotification(String token, String topic, String payload,
                                        String invalidationTime, String priority) {
        Map<String, Object> message = new HashMap<>();
        message.put("token", token);
        message.put("topic", topic);
        message.put("payload", payload);
        message.put("invalidationTime", invalidationTime);
        message.put("priority", priority);

        messageSender.sendMessage(message);
    }

    private String readLine(String param, Scanner scanner) {
        System.out.printf("\nInsert %s: ", param);
        String line = scanner.nextLine().trim();
        if (StringUtils.isBlank(line)) {
            return null;
        }

        return line;
    }

    private String readLines(String param, Scanner scanner) {
        System.out.printf("\nInsert (stop reading type - EOF) %s:\n", param);

        StringBuilder sb = new StringBuilder();
        Scanner innerScanner = new Scanner(System.in);
        while (true) {
            String line = innerScanner.nextLine();
            if (eof(line)) {
                break;
            }

            sb.append(line);
            sb.append("\n");
        }

        return sb.toString();
    }

    private boolean quite(String text) {
        return text != null && text.trim().toLowerCase().startsWith("quite");
    }

    private boolean eof(String text) {
        return text != null && text.trim().toLowerCase().startsWith("eof");
    }
}
