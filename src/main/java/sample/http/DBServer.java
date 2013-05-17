package sample.http;
import com.twitter.concurrent.ChannelSource;
import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.http.Http;
import com.twitter.finagle.http.Request;
import com.twitter.util.Duration;
import com.twitter.util.ExecutorServiceFuturePool;
import com.twitter.util.Function0;
import com.twitter.util.Future;
import com.twitter.util.FutureEventListener;
import com.twitter.util.JavaTimer;
import com.twitter.util.Timer;
import java.net.InetSocketAddress;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
import com.mysql.jdbc.Driver;
/**
 *
 * @author Sudharshan
 */
public class DBServer {

    public static class DBHandler extends com.twitter.util.Function0<String> {

     
        private String username;
        private String password;
        private String url;
        private String query;

        public DBHandler(String username, String password, String url,String query) {
            this.username = username;
            this.password = password;
            this.url = url;
            this.query = query;
            
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
                Class.forName("com.mysql.jdbc.Driver");
                java.sql.Connection con = DriverManager.getConnection(url, username, password);
                System.out.println("Connected "+ url);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    result += rs.getString("name");
                    
                }
                System.out.println("read from "+ url);
                System.out.println(result);
                rs.close();
                con.close();

            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DBServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(DBServer.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            } 
            return result;

        }
    }//end of DBHandler class 

    public List<String> dbquery(String username, String password, String url,String query) {
        ExecutorService es = Executors.newFixedThreadPool(4); // Number of threads to devote to blocking requests
        ExecutorServiceFuturePool esfp = new ExecutorServiceFuturePool(es); // Pool to process blockng requests so server thread doesn'
        List<String> ips=new ArrayList<String>();
        Timer timer=new JavaTimer();
        ips.add("192.168.122.193");
        ips.add("192.168.122.194");
        ips.add("192.168.122.195");
        
        List<Future<String>> results=new ArrayList<Future<String>>();
        for(String ip:ips)
        {
        Future<String> result = esfp.apply(new DBHandler(username, password, "jdbc:mysql://"+ip+":3306/annotation",query));
        results.add(result);
        }
        final List<String> resultSum=new ArrayList<String>();
        Future.collect(results).within(Duration.apply(3, TimeUnit.SECONDS), timer).addEventListener(new FutureEventListener<List<String>>() {
                    @Override
                    public void onFailure(Throwable cause) {
                        System.out.println("failed with cause: " + cause);
                        cause.printStackTrace();
                    }
                    @Override
                    public void onSuccess(List<String> t) {
                       System.out.println("result came "+t);
                       resultSum.addAll(t);
                    }
                });
       
        
//        for(Future<String> res:results)
//        {
//            res.within(Duration.apply(1, TimeUnit.SECONDS), timer).addEventListener(new FutureEventListener<String>() {
//                    @Override
//                    public void onFailure(Throwable cause) {
//                        System.out.println("failed with cause: " + cause);
//                    }
//                    @Override
//                    public void onSuccess(String t) {
//                       System.out.println("result came "+t);
//                    }
//                });
//        }
        //return result;
        return resultSum;
    }
    public static void main(String[] ar) {
        Service<HttpRequest, HttpResponse> service;
        service = new Service<HttpRequest, HttpResponse>() {
            @Override
            public Future<HttpResponse> apply(HttpRequest request) {
                System.out.println("Request came");
                String name = Request.apply(request).getParam("name");
                final HttpResponse res = new DefaultHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                res.setHeader(HttpHeaders.Names.CONTENT_TYPE,
                        "text/plain; charset=UTF-8");
                DBServer dbs = new DBServer();
                List<String> result = dbs.dbquery("myadmn", "XeOh88SK", "jdbc:mysql://localhost:3306/test","select * from aspects;");
                
//                result.addEventListener(new FutureEventListener<String>() {
//                    @Override
//                    public void onFailure(Throwable cause) {
//                        System.out.println("failed with cause: " + cause);
//                    }
//                    @Override
//                    public void onSuccess(String t) {
//                       System.out.println("result came "+t);
//                    }
//                });
//                
                res.setContent(ChannelBuffers.copiedBuffer("Hello "+name, CharsetUtil.UTF_8));
                return Future.<HttpResponse>value(res);
            }
        };

        ServerBuilder.safeBuild(service,
                ServerBuilder.get().codec(Http.get()).name("DBServer")
                .bindTo(new InetSocketAddress("localhost", 10000)));

        System.out.println("Server is running @ 10000");

    }
}
