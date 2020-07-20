package pbouda.websocket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ImmediateEventExecutor;

import java.lang.management.ManagementFactory;

public class Server implements AutoCloseable {

    private static final String WS_PATH = "/ws";
    private static final int PORT = 8080;

    private final ServerBootstrap bootstrap;
    private final ChannelGroup channelGroup;
    private final EventLoopGroup bossEventLoopGroup;
    private final EventLoopGroup workerEventLoopGroup;

    public static void main(String[] args) {
        var server = new Server();
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));

        server.start()
                .closeFuture()
                .addListener(f -> System.out.println("Websocket Server closed"));
    }

    public Server() {
        this.channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
         this.bossEventLoopGroup = new EpollEventLoopGroup(1);
         this.workerEventLoopGroup = new EpollEventLoopGroup();

        this.bootstrap = new ServerBootstrap()
                .channel(EpollServerSocketChannel.class)
                .group(bossEventLoopGroup, workerEventLoopGroup)
                .localAddress(PORT)
                // .handler(new LoggingHandler(LogLevel.INFO))
                // .childOption(ChannelOption.SO_SNDBUF, 1024 * 1024)
                // .childOption(ChannelOption.SO_RCVBUF, 32 * 1024)
                .childHandler(new RouterChannelInitializer(channelGroup));

        /*
         * The maximum queue length for incoming connection indications
         * (a request to connect) is set to the backlog parameter. If
         * a connection indication arrives when the queue is full,
         * the connection is refused.
         */
        // bootstrap.option(ChannelOption.SO_BACKLOG, 100);
        // bootstrap.handler(new LoggingHandler(LogLevel.INFO));


        // Receive and Send Buffer - always be able to fill in an entire entity.
        // bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT);
    }

    public Channel start() {
        ChannelFuture serverBindFuture = bootstrap.bind();
        serverBindFuture.addListener(f -> {
            System.out.printf("PID %s - Broadcaster started on port '%s' and path '%s'",
                    ManagementFactory.getRuntimeMXBean().getPid(), PORT, WS_PATH);
        });

        // Wait for the binding is completed
        serverBindFuture.syncUninterruptibly();
        return serverBindFuture.channel();
    }

    @Override
    public void close() {
        Future<?> boss = bossEventLoopGroup.shutdownGracefully();
        Future<?> workers = workerEventLoopGroup.shutdownGracefully();
        boss.syncUninterruptibly();
        workers.syncUninterruptibly();
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }
}
