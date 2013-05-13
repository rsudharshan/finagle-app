import com.twitter.finagle.Service
import com.twitter.finagle.builder._
import com.twitter.finagle.http._
import com.twitter.util.Future
import java.net.InetSocketAddress
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.util.CharsetUtil.UTF_8
object EchoServer extends App
 {
    val service=new Service[Request,Response]{
    def apply(request:Request)={
    val response=Response()
    response.setContentType(MediaType.Html, UTF_8.name)
    println("params = " + request.params)
    val responseContent= "Hello " + request.getParam("name") 
    // we can use request.getIntParam() like that 
    response.setContent(ChannelBuffers.copiedBuffer(responseContent, UTF_8))
    Future.value(response)
    }
 }

 val echoServer: Server = ServerBuilder()
      .codec(RichHttp[Request](Http(_compressionLevel = 6)))
      .bindTo(new InetSocketAddress(8084))
      .name("EchoServer")
      .build(service)
 }