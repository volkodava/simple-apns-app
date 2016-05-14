package com.sample.apns.server.service;

import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ClientNotConnectedException;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service(value = "apnsNotifier")
public class ApnsNotifier {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Value("${pkcs12.cert.file}")
    private Resource pkcs12Cert;
    @Value("${pkcs12.passwd.file}")
    private Resource pkcs12Passwd;
    @Value("${command.timeout_in_secs}")
    private long commandTimeout;
    private TimeUnit commandTimeoutUnit = TimeUnit.SECONDS;

    private ApnsClient<SimpleApnsPushNotification> apnsClient;

    public String push(Map<String, Object> params) throws Exception {
        String token = (String) params.get("token");
        String topic = (String) params.get("topic");
        String payload = (String) params.get("payload");

        StringBuilder replyMessage = new StringBuilder();
        replyMessage.append(String.format("Pushing:\ntoken:\t%s\ntopic:\t%s\npayload:\t%s", token, topic, payload));

        SimpleApnsPushNotification pushNotification =
                new SimpleApnsPushNotification(token, topic, payload);

        try {
            PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
                    apnsClient.sendNotification(pushNotification).get(commandTimeout, commandTimeoutUnit);

            if (pushNotificationResponse.isAccepted()) {
                replyMessage.append("\nEverything worked! The notification was successfully sent to and accepted by the gateway.");
            } else {
                if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
                    replyMessage.append("\nNotification was rejected by the APNs gateway " +
                            "because the destination token is no longer valid: " +
                            pushNotificationResponse.getRejectionReason());
                } else {
                    replyMessage.append("\nNotification rejected by the APNs gateway: " +
                            pushNotificationResponse.getRejectionReason());
                }
            }
        } catch (ExecutionException e) {
            // Something went wrong when trying to send the notification to the
            // APNs gateway. The notification never actually reached the gateway,
            // so we shouldn't consider this a permanent failure.
            log.error("Failed to send push notification.", e);
            replyMessage.append("\nFailed to send push notification: " + e.getCause());

            if (e.getCause() instanceof ClientNotConnectedException) {
                // If we failed to send the notification because the client isn't
                // connected, we can wait for an automatic reconnection attempt
                // to succeed before sending more notifications.
                apnsClient.getReconnectionFuture().await(commandTimeout, commandTimeoutUnit);
            }
        }

        return replyMessage.toString();
    }

    @PostConstruct
    public void initialize() throws Exception {
        Validate.notNull(pkcs12Cert, "Certificate file must not be null");
        Validate.isTrue(pkcs12Cert.exists(), "Certificate file must exists");
        Validate.isTrue(commandTimeout > 0, "Timeout must be any positive number");

        File certFile = getCertFile();
        String certPassword = getCertPassword();

        log.debug("Connecting to APNs gateway using certificate '{}' with password '{}'", certFile.getAbsolutePath(),
                certPassword);

        apnsClient = new ApnsClient<>(certFile, certPassword);
        // connect to the APNs gateway
        apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST).await(commandTimeout, commandTimeoutUnit);

        Validate.isTrue(apnsClient.isConnected(), "Can't connect to the APNs gateway. Check log file.");
    }

    @PreDestroy
    public void shutdown() throws Exception {
        if (apnsClient != null) {
            apnsClient.disconnect().await(commandTimeout, commandTimeoutUnit);
        }
    }

    private File getCertFile() throws IOException {
        return pkcs12Cert.getFile();
    }

    private String getCertPassword() throws IOException {
        String password;
        if (pkcs12Passwd != null && pkcs12Passwd.exists()) {
            // read password from file
            password = IOUtils.toString(pkcs12Passwd.getURI()).trim();
        } else {
            password = "";
        }

        return password;
    }
}
