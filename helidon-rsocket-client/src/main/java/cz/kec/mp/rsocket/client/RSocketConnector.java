package cz.kec.mp.rsocket.client;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.enterprise.inject.spi.CDI;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.IncomingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.FlowAdapters;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketClient;
import io.rsocket.metadata.CompositeMetadataCodec;
import io.rsocket.metadata.RoutingMetadata;
import io.rsocket.metadata.TaggingMetadataCodec;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;

import io.helidon.common.reactive.Multi;

@ApplicationScoped
@Connector("rsocket-connector")
public class RSocketConnector implements IncomingConnectorFactory {

    @Override
    public PublisherBuilder<? extends Message<?>> getPublisherBuilder(Config config) {
        String providerBeanName = config.getValue("request-provider", String.class);
        String route = config.getValue("route", String.class);
        String address = config.getValue("address", String.class);
        Integer port = config.getValue("port", Integer.class);
        RSocketType type = config.getOptionalValue("type", RSocketType.class).orElse(RSocketType.REQUEST_CHANNEL);
        Instance<RSocketRequestProvider> reqProviderBeanInstance = CDI.current().select(RSocketRequestProvider.class, NamedLiteral.of(providerBeanName));
        if (reqProviderBeanInstance.isAmbiguous()) {
            throw new RuntimeException("Request provider named bean " + providerBeanName + " is ambiguous!");
        }
        if (reqProviderBeanInstance.isUnsatisfied()) {
            throw new RuntimeException("Request provider named bean " + providerBeanName + " not found!");
        }
        RSocketRequestProvider requestProvider = reqProviderBeanInstance.stream().findFirst().get();
        RSocket rSocket = io.rsocket.core.RSocketConnector.create()
                .metadataMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString())
                .connect(TcpClientTransport.create(address, port))
                .block();

        Supplier<CompositeByteBuf> metadataSupplier = () -> {
            CompositeByteBuf metadata = ByteBufAllocator.DEFAULT.compositeBuffer();
            RoutingMetadata routingMetadata = TaggingMetadataCodec.createRoutingMetadata(ByteBufAllocator.DEFAULT, List.of(route));
            CompositeMetadataCodec.encodeAndAddMetadata(metadata,
                    ByteBufAllocator.DEFAULT,
                    WellKnownMimeType.MESSAGE_RSOCKET_ROUTING,
                    routingMetadata.getContent());
            return metadata;
        };
        
        Flow.Publisher<Payload> requestPub = Multi.create(requestProvider.request(RSocketRequestProvider.conn(address, port, route)))
                .map(s -> ByteBufAllocator.DEFAULT.compositeBuffer().writeBytes(s.getBytes(StandardCharsets.UTF_8)))
                .map(s -> DefaultPayload.create(s, metadataSupplier.get()));

        switch (type) {
            case REQUEST_STREAM:
                return ReactiveStreams.fromPublisher(FlowAdapters.toPublisher(requestPub))
                        .limit(1)
                        .flatMap(p -> ReactiveStreams.fromPublisher(rSocket.requestStream(p)))
                        // custom message with metadata will be better
                        .map(Message::of);
            case REQUEST_CHANNEL:
                return ReactiveStreams.fromPublisher(
                                rSocket.requestChannel(ReactiveStreams.fromPublisher(FlowAdapters.toPublisher(requestPub)).buildRs())
                        )
                        // custom message with metadata will be better
                        .map(Message::of);
            case REQUEST_RESPONSE:
                return ReactiveStreams.fromPublisher(FlowAdapters.toPublisher(requestPub))
                        .limit(1)
                        .flatMap(p -> ReactiveStreams.fromPublisher(rSocket.requestResponse(p)))
                        // custom message with metadata will be better
                        .map(Message::of);
            default:
                throw new IllegalStateException("Shouldn't get here!");
        }
    }
}
