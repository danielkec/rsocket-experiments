package com.example.spring.sandbox;

import java.util.concurrent.Flow;

import io.helidon.common.reactive.Multi;
import io.helidon.common.reactive.Single;
import io.helidon.microprofile.rsocket.OnMessage;
import io.helidon.microprofile.rsocket.ServerEndpoint;

@ServerEndpoint(7878)
public class RSocketEndpoint {

    @OnMessage("request-response")
    public Flow.Publisher<String> requestResponse(String message) {
        return Single.just("Got message" + message);
    }

    @OnMessage("fire-and-forget")
    public Flow.Publisher<String> fireAndForget(String message) {
        return Multi.empty();
    }

    @OnMessage("request-stream")
    public Flow.Publisher<String> requestStream(String message) {
        return Multi.just("Got", "message", message);
    }

    @OnMessage("channel")
    public Flow.Publisher<String> requestStream(Flow.Publisher<String> incoming) {
        return Multi.create(incoming).map(s -> "Got message " + s);
    }
}
