package com.nr.async6;

import com.nr.Route;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Single threaded but using NIO2.  So modern.
 * Shipped with Java 7 (released in 2011).
 */
public class Nio2Async {

    public static void main(String[] args) throws Exception {

        var routes = List.of(
                new Route("example.com", "/"),
                new Route("example.com", "/foo"),
                new Route("noisybox.net", "/"),
                new Route("noisybox.net", "/404")
            );

        var completionLatch = new CountDownLatch(routes.size());
        routes.forEach(route -> {
            try {
                var channel = AsynchronousSocketChannel.open();
                var addr = new InetSocketAddress(route.getHost(), 80);
                channel.connect(addr, new ChannelRoute(channel, route), new CompletionHandler<>() {
                    @Override
                    public void completed(Void result, ChannelRoute channelRoute) {
                        System.out.println("Connection established to " + channelRoute.getHost() + channelRoute.getPath());
                        doRequest(channelRoute, new CompletionHandler<>() {
                            @Override
                            public void completed(Integer result, ChannelRoute attachment) {
                                System.out.println("Request to " + channelRoute.getHost() + channelRoute.getPath() + " completed, reading response");
                                var buff = ByteBuffer.allocate(5 * 1024 * 1024);
                                attachment.channel.read(buff, channelRoute, new CompletionHandler<>() {
                                    @Override
                                    public void completed(Integer result, ChannelRoute channelRoute) {
                                        System.out.println("Response received from " + channelRoute.getHost() + channelRoute.getPath());
                                        byte[] bodyBuff = new byte[buff.position() + 1];
                                        buff.position(0);     // gotta reset the stateful buffer source!
                                        buff.get(bodyBuff, 0, bodyBuff.length);
                                        var body = new String(bodyBuff);
                                        System.out.println("Got " + body.length() + " bytes in response from " + route.getHost() + route.getPath());
//                                        System.out.println(body);
                                        completionLatch.countDown();
                                    }

                                    @Override
                                    public void failed(Throwable exc, ChannelRoute attachment) {
                                        exc.printStackTrace();
                                    }
                                });
                            }

                            @Override
                            public void failed(Throwable exc, ChannelRoute attachment) {
                                exc.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void failed(Throwable exc, ChannelRoute channelRoute) {
                        exc.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        completionLatch.await();
    }

    private static void doRequest(ChannelRoute channelRoute, CompletionHandler<Integer, ? super ChannelRoute> handler) {
        System.out.println("Sending request to " + channelRoute.getHost() + channelRoute.getPath());
        var requestString = "GET " + channelRoute.getPath() + " HTTP/1.1\n" +
                "Host: " + channelRoute.getHost() + "\n" +
                "User-Agent: test\n\n";
        var bb = ByteBuffer.wrap(requestString.getBytes());
        channelRoute.channel.write(bb, channelRoute, handler);
    }

    static class ChannelRoute {
        private final AsynchronousSocketChannel channel;
        private final Route route;

        ChannelRoute(AsynchronousSocketChannel channel, Route route) {
            this.channel = channel;
            this.route = route;
        }

        public String getHost() {
            return route.getHost();
        }

        public String getPath(){
            return route.getPath();
        }
    }
}
