package pbouda.websocket.client;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseHandler extends ChannelDuplexHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) {
        LOG.info("Incoming: " + obj.getClass());
        ctx.fireChannelRead(((TextWebSocketFrame) obj).retain().retain().retain());
    }

    public void write(ChannelHandlerContext context, Object obj, ChannelPromise promise) {
        context.writeAndFlush(obj, promise);
    }
}