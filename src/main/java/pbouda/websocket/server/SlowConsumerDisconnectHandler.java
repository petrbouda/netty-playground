package pbouda.websocket.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class SlowConsumerDisconnectHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext context, Object obj, ChannelPromise promise) {
        System.out.println("OUTGOIIIIING");

//        if ((obj instanceof TextWebSocketFrame) || (obj instanceof BinaryWebSocketFrame)) {
        if (obj instanceof String) {
            context.executor().schedule(() -> {
                System.out.println("Heeey");
            }, 5, TimeUnit.SECONDS);

            if (context.channel().isWritable()) {
                context.writeAndFlush(obj);
                promise.setSuccess();
            } else {
                // Implementation of Timeout for Writability and closing the connection
                // context.executor().schedule(() -> context.channel().isWritable(), 5, TimeUnit.SECONDS);

                context.close().addListener(future -> {
                    SocketAddress target = context.channel().remoteAddress();
                    if (future.isSuccess()) {
                        System.out.println("Connection closed, became non-writable: " + target);
                    } else {
                        System.err.printf("Could not close a non-writable connection: " + target, future.cause());
                    }
                });
            }
        } else if (obj instanceof FullHttpResponse) {
            context.writeAndFlush(obj);
            promise.setSuccess();
        }
    }
}
