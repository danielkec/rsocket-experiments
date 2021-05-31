# Helidon RSocket client 
With Reactive Messaging

Basic idea is providing request value(stream or not) over supplying bean:
```java
    @Produces
    @Named("rsocket-request-provider-1")
    private RSocketRequestProvider requestProvider = connection ->
            Multi.just("Give","me","the","stream!");

    @Incoming("rsocket-channel-1")
    public void receive(Payload payload) {
        System.out.println("RECEIVED: " + payload.getDataUtf8());
    }
```
Connector can create RSocket connection based on configuration and supplied request value.

```yaml
mp.messaging:
  incoming:
    rsocket-channel-1:
      connector: rsocket-connector
      request-provider: rsocket-request-provider-1
      address: localhost
      port: 7979
      route: request-stream
      type: REQUEST_CHANNEL
```

Running client example against Spring RSocket example:
```shell
# Start server
cd spring-rsocket-server
mvn clean install && java -jar ./target/spring-rsocket-server-0.0.1-SNAPSHOT.jar
```

```shell
# Start client
cd helidon-rsocket-client
mvn clean install && java -jar ./target/helidon-rsocket-client.jar
```