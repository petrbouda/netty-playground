package pbouda.websocket.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfrastructureHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger LOG = LoggerFactory.getLogger(InfrastructureHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof PongWebSocketFrame) {
            LOG.info("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            LOG.info("WebSocket Client received closing");
            ctx.channel().close();
        } else {
            ctx.fireChannelRead(frame.retain());
        }
    }
}
