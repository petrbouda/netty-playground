package pbouda.websocket.client;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketHandshakeHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketHandshakeHandler.class);

    private final WebSocketClientHandshaker handshaker;
    private final int id;
    private ChannelPromise handshakeFuture;

    public WebSocketHandshakeHandler(WebSocketClientHandshaker handshaker, int id) {
        this.handshaker = handshaker;
        this.id = id;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        LOG.info(id + " - WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                LOG.info(id + " - WebSocket Client connected!");
                handshakeFuture.setSuccess();

                // Handshake is done, start processing WebSocket messages
                ctx.pipeline().remove(WebSocketHandshakeHandler.class);
            } catch (WebSocketHandshakeException e) {
                LOG.error(id + " - WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}