package sample.echo;

import java.net.InetSocketAddress;

import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ClientBuilder;
import com.twitter.finagle.example.echo.StringCodec;
import com.twitter.util.FutureEventListener;

public class EchoClient {

    public static void main(String[] args) {
        final Service<String, String> client = ClientBuilder
                .safeBuild(ClientBuilder.get().codec(new StringCodec())
                        .hosts(new InetSocketAddress(8944))
                        .hostConnectionLimit(100));

        System.out.println("apply " + client);

        client.apply("hello world!\n").addEventListener(
                new FutureEventListener<String>() {
                    @Override
                    public void onSuccess(String response) {
                        System.out.println("received respnse: " + response);
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                        cause.printStackTrace();
                        System.out.println("failed with cause:" + cause);
                    }
                });

        client.release();
    }

}
