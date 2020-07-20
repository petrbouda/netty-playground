package pbouda.flow.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

public class SecondInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("REGISTERED " + getClass().getTypeName());
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("UNREGISTERED " + getClass().getTypeName());
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("ACTIVE " + getClass().getTypeName());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("INACTIVE " + getClass().getTypeName());
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println();

        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeCharSequence("You're Welcome", CharsetUtil.UTF_8);

        ctx.writeAndFlush(buffer);
        ctx.fireChannelRead(msg.retain());
    }
}
