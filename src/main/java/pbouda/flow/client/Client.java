package pbouda.flow.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.util.concurrent.Future;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Client implements AutoCloseable {

    private final EventLoopGroup group;

    public Client() {
        this.group = new EpollEventLoopGroup();
    }

    public Channel connect() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
                .group(new EpollEventLoopGroup())
                .channel(EpollSocketChannel.class)
                .handler(new SimpleChannelInboundHandler<ByteBuf>() {
                    private final List<byte[]> buffers = new ArrayList<>();
                    private int byteSize = 0;

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                        byte[] bytes = ByteBufUtil.getBytes(msg);
                        byteSize += bytes.length;
                        buffers.add(bytes);
                    }

                    @Override
                    public void channelReadComplete(ChannelHandlerContext ctx) {
                        int current = 0;
                        byte[] backingArray = new byte[byteSize];
                        for (byte[] buffer : buffers) {
                            System.arraycopy(buffer, 0, backingArray, current, buffer.length);
                            current += buffer.length;
                        }
                        buffers.clear();
                        System.out.println(new String(backingArray));

                        // Send Back
                        ByteBuf buffer = ByteBufAllocator.DEFAULT.heapBuffer();
                        buffer.writeCharSequence("Thank you, Mate!", Charset.defaultCharset());
                        ctx.writeAndFlush(buffer);
                    }
                });

        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync()
                .addListener(f -> System.out.println("Client connected"))
                .syncUninterruptibly();

        return channelFuture.channel();
    }

    @Override
    public void close() {
        Future<?> boss = group.shutdownGracefully();
        boss.syncUninterruptibly();
    }
}
