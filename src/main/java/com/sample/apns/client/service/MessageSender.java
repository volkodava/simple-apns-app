package com.sample.apns.client.service;

import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

public class MessageSender implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(MessageSender.class);

    private ProducerTemplate camelTemplate;

    public void sendMessage(Map<String, Object> message) {
        Validate.notEmpty(message, "Message map must not be null");

        LOG.info("Invoking service with:\n" + message);
        String response = (String) camelTemplate.sendBody("{{topic.address}}", ExchangePattern.InOut, message);
        LOG.info("... the result is: " + response);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Validate.notNull(camelTemplate, "Camel template must not be null");
    }

    public ProducerTemplate getCamelTemplate() {
        return camelTemplate;
    }

    public void setCamelTemplate(ProducerTemplate camelTemplate) {
        this.camelTemplate = camelTemplate;
    }
}
