package com.sample.apns.common;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Configuration {
    private static final int MIN_PORT_NUMBER = 0;
    private static final int MAX_PORT_NUMBER = 65535;

    private int serverPort;
    private String topicAddress;
    private String dateTimeFormat;

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getTopicAddress() {
        return topicAddress;
    }

    public void setTopicAddress(String topicAddress) {
        this.topicAddress = topicAddress;
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public SimpleDateFormat getDateTimeFormatter() {
        return new SimpleDateFormat(dateTimeFormat);
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }


    public String convertDateToString(Date date) {
        if (date == null) {
            return null;
        }

        return getDateTimeFormatter().format(date);
    }

    public Date convertStringToDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }

        try {
            return getDateTimeFormatter().parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Date has invalid format: " + dateStr);
        }
    }

    public boolean serverPortInUse() {
        if (serverPort < MIN_PORT_NUMBER || serverPort > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid port number: " + serverPort);
        }

        try {
            new Socket(InetAddress.getLoopbackAddress(), serverPort).close();
            return true;
        } catch (IOException ex) {
        }

        return false;
    }
}
