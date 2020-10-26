package com.nr.async7;

import com.nr.ChannelRoute;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CountDownLatch;

import static com.nr.async7.DoneSignal.forHandler;

public class ContextExecutor {

    private final ClientContext ctx;

    public ContextExecutor(ClientContext ctx) {
        this.ctx = ctx;
    }

    public void execute(CountDownLatch latch) throws IOException {
        var channel = AsynchronousSocketChannel.open();
        var addr = new InetSocketAddress(ctx.getRoute().getHost(), 80);
        var channelRoute = new ChannelRoute(channel, ctx.getRoute());
        var wrap = new CompletionChannelRoute(channelRoute, latch);
        channel.connect(addr, wrap, forHandler((result, attachment) -> send(attachment)));
    }

    private void send(CompletionChannelRoute channelRoute){
        System.out.println("Sending request to " + channelRoute.getHost() + channelRoute.getPath());
        var bb = ByteBuffer.wrap(ctx.getRequestString().getBytes());
        channelRoute.write(bb, channelRoute, forHandler((result, attachment) -> readReply(attachment)));
    }

    private void readReply(CompletionChannelRoute channelRoute){
        var buff = ByteBuffer.allocate(5 * 1024 * 1024);
        channelRoute.read(buff, channelRoute, forHandler((result, attachment) -> {
            byte[] bodyBuff = new byte[buff.position() + 1];
            buff.position(0);     // gotta reset the stateful buffer source!
            buff.get(bodyBuff, 0, bodyBuff.length);
            var body = new String(bodyBuff);
            ctx.getReceiveHandler().accept(body);
            channelRoute.countDown();
        }));
    }
}
