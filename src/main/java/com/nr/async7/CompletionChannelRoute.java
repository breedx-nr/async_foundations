package com.nr.async7;

import com.nr.ChannelRoute;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

public class CompletionChannelRoute {
    private final ChannelRoute delegate;
    private final CountDownLatch latch;

    public CompletionChannelRoute(ChannelRoute delegate, CountDownLatch latch) {
        this.delegate = delegate;
        this.latch = latch;
    }

    public String getHost() {
        return delegate.getHost();
    }

    public String getPath() {
        return delegate.getPath();
    }

    public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
        delegate.read(dst, attachment, handler);
    }

    public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
        delegate.write(src, attachment, handler);
    }

    public AsynchronousSocketChannel getChannel() {
        return delegate.getChannel();
    }

    public void countDown() {
        latch.countDown();
    }
}
