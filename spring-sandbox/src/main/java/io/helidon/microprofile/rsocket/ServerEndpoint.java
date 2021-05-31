package io.helidon.microprofile.rsocket;

import javax.websocket.Decoder;
import javax.websocket.Encoder;

public @interface ServerEndpoint {
    int value();
    public Class<? extends Decoder>[] decoders() default {};
    public Class<? extends Encoder>[] encoders() default {};
}
