package pbouda.flow;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.CharsetUtil;
import pbouda.flow.client.Client;
import pbouda.flow.server.Server;

public class SingleMessage {

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
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        String message = LOREM.getParagraphs(1, 5);
        buffer.writeCharSequence(message, CharsetUtil.UTF_8);
        server.getConnectedChannel().write(buffer)
                .addListener(future -> {
                    if (future.isSuccess()) {
                        System.out.println("Message sent!");
                    } else {
                        future.cause().printStackTrace();
                    }
                });
    }
}
