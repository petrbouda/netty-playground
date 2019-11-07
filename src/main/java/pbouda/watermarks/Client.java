package pbouda.watermarks;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

public final class Client {

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_NODELAY, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     p.addLast(new SimpleChannelInboundHandler<>() {
                         @Override
                         public void channelActive(ChannelHandlerContext ctx) throws Exception {
                             super.channelActive(ctx);

                             // trigger server write
                             ByteBuf trigger = Unpooled.buffer(1);
                             trigger.writeByte(1);
                             ctx.writeAndFlush(trigger);
                         }

                         @Override
                         protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                             System.out.println("hold on read");
                             Thread.sleep(TimeUnit.DAYS.toMillis(1));
                         }
                     });
                 }
             });
            ChannelFuture f = b.connect("127.0.0.1", 8007).sync();
            f.channel().closeFuture().sync();
        } finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }
    }
}