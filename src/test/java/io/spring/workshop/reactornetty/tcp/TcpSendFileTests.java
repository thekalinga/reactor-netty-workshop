package io.spring.workshop.reactornetty.tcp;

import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;

/**
 * Learn how to create TCP server and client
 *
 * @author Violeta Georgieva
 * @see <a href="http://next.projectreactor.io/docs/netty/snapshot/api/reactor/netty/tcp/TcpServer.html">TcpServer javadoc</a>
 * @see <a href="http://next.projectreactor.io/docs/netty/snapshot/api/reactor/netty/tcp/TcpClient.html">TcpClient javadoc</a>
 */
public class TcpSendFileTests {

    @Test
    public void sendFileTest() {
        DisposableServer server =
                TcpServer.create()   // Prepares a TCP server for configuration.
                         .port(0)    // Configures the port number as zero, this will let the system pick up
                                     // an ephemeral port when binding the server.
                         .secure()   // Enables default SSL configuration.
                         .wiretap()  // Applies a wire logger configuration.
                         .handle((in, out) ->
                                 in.receive()
                                   .asString()
                                   .flatMap(s -> {
                                       try {
                                           Path file = Paths.get(getClass().getResource(s).toURI());
                                           return out.sendFile(file)
                                                     .then();
                                       } catch (URISyntaxException e) {
                                           return Mono.error(e);
                                       }
                                   })
                                   .log("tcp-server"))
                         .bindNow(); // Starts the server in a blocking fashion, and waits for it to finish initializing.

        assertNotNull(server);

        server.disposeNow(); // Stops the server and releases the resources.
    }
}
