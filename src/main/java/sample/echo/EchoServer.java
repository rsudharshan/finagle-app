package sample.echo;

import java.net.InetSocketAddress;

import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.example.echo.StringCodec;
import com.twitter.util.Future;

public class EchoServer {

    public static void main(String[] args) {

        Service<String, String> service = new Service<String, String>() {
            public Future<String> apply(String request) {
                System.out.println("received: " + request);
                return Future.value(request);
            }
        };

        System.out.println("start " + service);

        ServerBuilder.safeBuild(service,
                ServerBuilder.get().codec(new StringCodec()).name("EchoServer")
                        .bindTo(new InetSocketAddress(8944)));
    }

}
