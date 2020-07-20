package pbouda.websocket.client;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketClientHandler.class);

    private final int id;

    public WebSocketClientHandler(int id) {
        this.id = id;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOG.info(id + " - WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
        LOG.info(id + " - WebSocket Client received message: " + textFrame.text() + " | " + ctx.channel().remoteAddress());
    }
}