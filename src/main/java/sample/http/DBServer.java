/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sample.http;
import com.twitter.concurrent.ChannelSource;
import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.http.Http;
import com.twitter.finagle.http.Request;
import com.twitter.util.ExecutorServiceFuturePool;
import com.twitter.util.Function0;
import com.twitter.util.Future;
import com.twitter.util.FutureEventListener;
import java.net.InetSocketAddress;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
/**
 *
 * @author Sudharshan
 */
public class DBServer {

    public static class DBHandler extends com.twitter.util.Function0<String> {

        private String username;
        private String password;
        private String url;

        public DBHandler(String username, String password, String url) {
            this.username = username;
            this.password = password;
            this.url = url;
        }

        @Override
        public String apply() {
            String result = "default";
            /* Waiting simulation code ***
             long delayUntil = System.currentTimeMillis() + (username.length() * 1000);
            System.out.println("Waiting for "+username.length());

      long acc = 0;
      while(System.currentTimeMillis() < delayUntil) {
        // Let's bind and gag the CPU
        for(int i = 0; i < 1000; i++) {
            for(int j = 0; j < 1000; j++) {
                acc += username.length() + j + i;
            }
        }
      }
      System.out.println("Finished for "); */
            try {
                java.sql.Connection con = DriverManager.getConnection(url, username, password);
                System.out.println("Connected");
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("select * from users;");
                while (rs.next()) {
                    result = rs.getString("username");
                }
                rs.close();
                con.close();

            } catch (SQLException ex) {
                Logger.getLogger(DBServer.class.getName()).log(Level.SEVERE, null, ex);
            } 
            return result;

        }
    }//end of DBHandler class 

    public Future<String> dbquery(String username, String password, String url) {
        ExecutorService es = Executors.newFixedThreadPool(4); // Number of threads to devote to blocking requests
        ExecutorServiceFuturePool esfp = new ExecutorServiceFuturePool(es); // Pool to process blockng requests so server thread doesn'
        Function0<String> blockingWork = new DBHandler(username, password, url);
        Future<String> result = esfp.apply(blockingWork);
        return result;
    }
    public static void main(String[] ar) {
        Service<HttpRequest, HttpResponse> service;
        service = new Service<HttpRequest, HttpResponse>() {
            @Override
            public Future<HttpResponse> apply(HttpRequest request) {
                String name = Request.apply(request).getParam("name");
                final HttpResponse res = new DefaultHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                res.setHeader(HttpHeaders.Names.CONTENT_TYPE,
                        "text/plain; charset=UTF-8");
                DBServer dbs = new DBServer();
                Future<String> result = dbs.dbquery("root", "sudhar", "jdbc:mysql://localhost:3306/test");
                result.addEventListener(new FutureEventListener<String>() {
                    @Override
                    public void onFailure(Throwable cause) {
                        System.out.println("failed with cause: " + cause);
                    }
                    @Override
                    public void onSuccess(String t) {
                       System.out.println("result came "+t);
                    }
                });
                
                res.setContent(ChannelBuffers.copiedBuffer("Hello "+name, CharsetUtil.UTF_8));
                return Future.<HttpResponse>value(res);
            }
        };

        ServerBuilder.safeBuild(service,
                ServerBuilder.get().codec(Http.get()).name("HttpServer")
                .bindTo(new InetSocketAddress("localhost", 10000)));

        System.out.println("Server is running @ 10000");

    }
}