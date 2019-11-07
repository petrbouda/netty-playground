package pbouda.watermarks;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public final class Server {

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .option(ChannelOption.SO_BACKLOG, 100)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     p.addLast(new SimpleChannelInboundHandler<>() {
                         @Override
                         protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                             System.out.println("start to write");

                             long bytesBeforeUnwritable = ctx.channel().bytesBeforeUnwritable();
                             int count = 0;
                             while (true) {
                                 ByteBuf oneByte = Unpooled.buffer(1);
                                 oneByte.writeByte(1);

                                 ctx.writeAndFlush(oneByte);
                                 count ++;
                                 if (ctx.channel().bytesBeforeUnwritable() != bytesBeforeUnwritable){
                                     bytesBeforeUnwritable = ctx.channel().bytesBeforeUnwritable();
                                     System.out.println(count + " : " + bytesBeforeUnwritable);
                                 }
                             }
                         }
                     });
                 }
             });

            ChannelFuture f = b.bind(8007).sync();
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}