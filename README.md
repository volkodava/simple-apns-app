# simple-apns-app

Simple application which sends Apple Push Notification over JMS using Apache Camel, Spring IoC and Pushy.

# About

Application aims to decouple APNs related code from the client code to simplify application testing
since if a connection to APNs gateway will be opened and closed repeatedly,
APNs will treat it as a denial of service attack and block connections for a period of time.
This temporary block usually expire if no connection attempts are made for about one hour.
More details you may find in the following topics:
- [Tutorial on Spring Remoting with JMS](http://camel.apache.org/tutorial-jmsremoting.html)
- [Local and Remote Notification Programming Guide](https://developer.apple.com/library/mac/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/APNsProviderAPI.html#//apple_ref/doc/uid/TP40008194-CH101-SW6)
- [Troubleshooting Push Notifications](https://developer.apple.com/library/ios/technotes/tn2265/_index.html)

This project is just meant to be a demonstration, therefore it is neither well documented nor well tested. Use it to learn about the technologies used, but do not use it for productive applications.

Any feedback is welcome, and I will incorporate useful pull requests.

# Technologies

- [Spring Framework](https://projects.spring.io/spring-framework/) as dependency injection container
- [Apache Camel](http://camel.apache.org/) as integration framework based on known Enterprise Integration Patterns
- [Pushy](http://relayrides.github.io/pushy/) as library for sending APNs (iOS/OS X) push notifications

# Running

## Run the Server

The Server is started using the `org.apache.camel.spring.Main` class that can start application out-of-the-box. The Server can be started with:
> ```mvn compile exec:java -Pserver```

## Run the Clients

The Clients is started using their main class respectively.
> ```mvn compile exec:java -Pclient```
