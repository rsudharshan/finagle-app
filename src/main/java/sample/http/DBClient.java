/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sample.http;
import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.http.Http;
import com.twitter.util.Future;
import com.twitter.util.FuturePool;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import scala.Function0;
/**
 *
 * @author Sudharshan
 */
public class DBClient extends com.twitter.util.Function0<String> {
  

    @Override
    public String apply() {
        try {
            Thread.sleep(1000);
      
        } catch (InterruptedException ex) {
            Logger.getLogger(DBClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Hello";
              
    }


}
