package pbouda.websocket.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class RouterChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ChannelGroup channelGroup;

    RouterChannelInitializer(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws SSLException, NoSuchAlgorithmException, CertificateException {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new LoggingHandler(LogLevel.TRACE));
        pipeline.addLast(new HttpServerCodec());

        // For Streaming of Continuation frames
        // https://tools.ietf.org/html/rfc6455#section-5.4
        // A single WebSocket frame, per RFC-6455 base framing, has a maximum size limit of 2^63 bytes
        // (9,223,372,036,854,775,807 bytes ~= 9.22 exabytes)
        // pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));

        // pipeline.addLast(new IdleStateHandler(60, 30, 0));
        // children: ReadTimeoutHandler & WriteTimeoutHandler

        // Sec-WebSocket-Extensions: permessage-deflate
        // pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true));
        pipeline.addLast(new WebsocketEventHandler(channelGroup));

        pipeline.addLast(new SlowConsumerDisconnectHandler());
    }
}
