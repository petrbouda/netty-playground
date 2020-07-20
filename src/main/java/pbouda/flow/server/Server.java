package pbouda.flow.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Future;
import pbouda.flow.handler.*;

import java.lang.management.ManagementFactory;

public class Server implements AutoCloseable {

    private static final int PORT = 8080;

    private final ServerBootstrap bootstrap;
    private final EventLoopGroup bossEventLoopGroup;
    private final EventLoopGroup workerEventLoopGroup;
    private Channel connectedChannel;

    public Server() {
        this.bossEventLoopGroup = new EpollEventLoopGroup(1);
        this.workerEventLoopGroup = new EpollEventLoopGroup(1);

        this.bootstrap = new ServerBootstrap()
                .channel(EpollServerSocketChannel.class)
                .group(bossEventLoopGroup, workerEventLoopGroup)
                .localAddress(8080)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        connectedChannel = channel;

                        ChannelPipeline pipeline = channel.pipeline();

                        pipeline.addLast(new FirstInboundHandler());
                        pipeline.addLast(new FirstOutboundHandler());

                        // Switched to see write in Inbound
                        pipeline.addLast(new SecondOutboundHandler());
                        pipeline.addLast(new SecondInboundHandler());

                        pipeline.addLast(new ThirdInboundHandler());
                        pipeline.addLast(new ThirdOutboundHandler());
                    }
                });
    }

    public Channel start() {
        ChannelFuture serverBindFuture = bootstrap.bind();
        serverBindFuture.addListener(f ->
                System.out.printf("PID %s - Broadcaster started on port '%s'\n", ManagementFactory.getRuntimeMXBean().getPid(), PORT));

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

    public Channel getConnectedChannel() {
        return connectedChannel;
    }
}
