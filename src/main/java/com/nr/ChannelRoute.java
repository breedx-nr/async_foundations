package com.nr;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ChannelRoute {
    private final AsynchronousSocketChannel channel;
    private final Route route;

    public ChannelRoute(AsynchronousSocketChannel channel, Route route) {
        this.channel = channel;
        this.route = route;
    }

    public String getHost() {
        return route.getHost();
    }

    public String getPath() {
        return route.getPath();
    }

    public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
        channel.read(dst, attachment, handler);
    }

    public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
        channel.write(src, attachment, handler);
    }

    public AsynchronousSocketChannel getChannel() {
        return channel;
    }
}
