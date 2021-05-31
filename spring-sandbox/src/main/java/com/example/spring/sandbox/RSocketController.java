package com.example.spring.sandbox;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * spring.rsocket.server.port=7979
 */
@Controller
public class RSocketController {

    @MessageMapping("request-response")
    public Mono<String> requestResponse(String message) {
        return Mono.just("Got message " + message);
    }

    @MessageMapping("fire-and-forget")
    public Mono<Void> fireAndForget(String message) {
        return Mono.empty();
    }

    @MessageMapping("request-stream")
    public Flux<String> requestStream(String message) {
        return Flux.just(message).map(s -> "Got message " + s);
    }

    @MessageMapping("channel")
    public Flux<String> requestStream(Flux<String> incoming) {
        return Flux.from(incoming).map(s -> "Got message " + s);
    }

}
