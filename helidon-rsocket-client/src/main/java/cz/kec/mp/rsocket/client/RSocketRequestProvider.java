package cz.kec.mp.rsocket.client;

import java.util.concurrent.Flow;

import io.rsocket.Payload;

@FunctionalInterface
interface RSocketRequestProvider {
    Flow.Publisher<String> request(RSocketConnection connection);
    
    interface RSocketConnection{
        int port();
        String address();
        String route();
    }

    static RSocketConnection conn(String address, int port, String route){
        return new RSocketConnection() {
            @Override
            public int port() {
                return port;
            }

            @Override
            public String address() {
                return address;
            }

            @Override
            public String route() {
                return route;
            }
        };
    }
}
