package pbouda.websocket;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import pbouda.websocket.client.Client;
import pbouda.websocket.server.Server;

public class Start {

    private static final Lorem LOREM = LoremIpsum.getInstance();

    public static void main(String[] args) throws InterruptedException {
        // ------------------
        //  Websocket Server
        // ------------------
        var server = new Server();
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));

        server.start()
                .closeFuture()
                .addListener(f -> System.out.println("Websocket Server closed"));

        // -----------------
        // Websocket Client
        // -----------------
        var client = new Client();
        Runtime.getRuntime().addShutdownHook(new Thread(client::close));

        client.connect()
                .closeFuture()
                .addListener(v -> System.out.println("Websocket Client closed"));

        // -----------------
        // Sending Message
        // -----------------
        ChannelGroup channelGroup = server.getChannelGroup();


        while (true) {
            Thread.sleep(500);

            ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();

            String message = LOREM.getParagraphs(1, 5);
            buffer.writeCharSequence(message, CharsetUtil.UTF_8);

            channelGroup.write(new TextWebSocketFrame(buffer))
                    .addListener(future -> {
                        if (future.isSuccess()) {
//                            System.out.println("Message sent!");
                        } else {
                            future.cause().printStackTrace();
                        }
                    });
        }
        // Stop and don't kill the clients.
        // Thread.currentThread().join();
    }

}
