package io.spring.workshop.reactornetty.udp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.resources.LoopResources;
import reactor.netty.udp.UdpServer;

import static org.junit.Assert.assertNotNull;

/**
 * Learn how to create a UDP server and client
 *
 * @author Violeta Georgieva
 * @see <a href="http://next.projectreactor.io/docs/netty/snapshot/api/reactor/netty/udp/UdpServer.html">UdpServer javadoc</a>
 * @see <a href="http://next.projectreactor.io/docs/netty/snapshot/api/reactor/netty/udp/UdpClient.html">UdpClient javadoc</a>
 */
public class UdpEchoTests {

    @Test
    public void echoTest() {
        LoopResources loopResources = LoopResources.create("echo-test-udp");
        Connection server =
                UdpServer.create() // Prepares a UDP server for configuration.
                         .port(0)  // Configures the port number as zero, this will let the system pick up
                                   // an ephemeral port when binding the server.
                         .runOn(loopResources) // Configures the UDP server to run on a specific LoopResources.
                         .wiretap()            // Applies a wire logger configuration.
                         .handle((in, out) ->
                                 in.receiveObject() // Obtains the inbound Flux.
                                   .map(o -> { // Transforms the datagram.
                                       if (o instanceof DatagramPacket) {
                                           // Incoming DatagramPacket
                                           DatagramPacket p = (DatagramPacket) o;
                                           ByteBuf buf1 = Unpooled.copiedBuffer("Hello ", CharsetUtil.UTF_8);
                                           // Creates a new ByteBuf using the incoming DatagramPacket content.
                                           ByteBuf buf2 = Unpooled.copiedBuffer(buf1, p.content()
                                                                                       .retain());
                                           // Creates a new DatagramPacket with the ByteBuf and the sender
                                           // information from the incoming DatagramPacket.
                                           return new DatagramPacket(buf2, p.sender());
                                       }
                                       else {
                                           return Mono.error(new Exception("Unexpected type of the message: " + o));
                                       }
                                   })
                                   .flatMap(out::sendObject)
                                   .log("udp-server"))
                         .bind()   // Binds the UDP server and returns a Mono<Connection>.
                         .block(); // Blocks and waits the server to finish initialising.

        assertNotNull(server);

        server.disposeNow();       // Stops the server and releases the resources.
    }
}
