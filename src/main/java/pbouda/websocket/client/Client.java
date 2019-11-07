package pbouda.websocket.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.util.concurrent.Future;

import java.net.URI;
import java.net.URISyntaxException;

public class Client implements AutoCloseable {

    private final EventLoopGroup group;
    private final int id;

    private static final URI URI;

    static {
        try {
            URI = new URI("ws://127.0.0.1:8080/ws");
        } catch (URISyntaxException e) {
            throw new RuntimeException("Cannot parse WS Address", e);
        }
    }

    public Client() {
        this(new NioEventLoopGroup(), 1);
//        this(new EpollEventLoopGroup(), 1);
    }

    public Client(EventLoopGroup group, int id) {
        this.group = group;
        this.id = id;
    }

    public Channel connect() throws InterruptedException {
        var handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                URI, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());

        var wsHandshakeHandler = new WebSocketClientHandler(handshaker, id);

        Bootstrap bootstrap = new Bootstrap()
                .group(group)
//                .channel(EpollSocketChannel.class)
                .channel(NioSocketChannel.class)
                .handler(new CustomClientInitializer(wsHandshakeHandler));

        ChannelFuture channelFuture = bootstrap.connect(URI.getHost(), URI.getPort()).sync()
                .addListener(f -> System.out.println("Client connected: ID: " + id))
                .syncUninterruptibly();

        wsHandshakeHandler.handshakeFuture().syncUninterruptibly();

        return channelFuture.channel();
    }

    @Override
    public void close() {
        Future<?> boss = group.shutdownGracefully();
        boss.syncUninterruptibly();
    }

    private static class CustomClientInitializer extends ChannelInitializer<SocketChannel> {

        private final ChannelHandler handler;

        private CustomClientInitializer(ChannelHandler handler) {
            this.handler = handler;
        }

        @Override
        protected void initChannel(SocketChannel channel) {
            channel.pipeline()
                    .addLast(new HttpClientCodec())
                    .addLast(new HttpObjectAggregator(8192))
                    .addLast(WebSocketClientCompressionHandler.INSTANCE)
                    .addLast(handler);
        }
    }
}
