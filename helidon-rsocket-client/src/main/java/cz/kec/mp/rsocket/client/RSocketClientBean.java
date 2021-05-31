package cz.kec.mp.rsocket.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import io.helidon.common.reactive.Multi;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.rsocket.Payload;

@ApplicationScoped
public class RSocketClientBean {

    @Produces
    @Named("rsocket-request-provider-1")
    private RSocketRequestProvider requestProvider = connection ->
            Multi.just("Give","me","the","stream!");

    @Incoming("rsocket-channel-1")
    public void receive(Payload payload) {
        System.out.println("RECEIVED: " + payload.getDataUtf8());
    }

}
