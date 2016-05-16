package com.sample.apns.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class PayloadService implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(PayloadService.class);
    private static final int MAX_PAYLOAD_SIZE = 4096;

    private ObjectMapper mapper;

    public String compactJson(String payload) {
        try {
            Object parsedObject = mapper.readValue(payload, Object.class);
            String result = mapper.writeValueAsString(parsedObject);

            if (result.length() <= MAX_PAYLOAD_SIZE) {
                LOG.warn("Payload size must be less or equal to {} bytes.", MAX_PAYLOAD_SIZE);
            }

            return result;
        } catch (Exception e) {
            LOG.warn("Can't compact provided JSON payload");
        }

        return payload;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Validate.notNull(mapper, "Mapper must not be null");
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }
}
