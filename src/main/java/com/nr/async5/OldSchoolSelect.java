package com.nr.async5;

import com.nr.Route;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Single threaded select -- way back to POSIX select()
 * Selector since Java 1.4, released in 2002.
 * This uses NIO via Selector and SocketChannel and ByteBuffer.
 */
public class OldSchoolSelect {

    public static void main(String[] args) throws Exception {

        var routes = List.of(
                new Route("example.com", "/"),
                new Route("example.com", "/foo"),
                new Route("noisybox.net", "/"),
                new Route("noisybox.net", "/404")
            );
        var selector = Selector.open(); // make a new selector

        startConnects(routes, selector);

        var responses = new HashMap<Route,String>(routes.size());
        var responseBuffers = new HashMap<Route,ByteBuffer>();
        routes.forEach(route -> responseBuffers.put(route, ByteBuffer.allocate(5 * 1024 * 1024)));

        // Loop unless we've seen all of our responses...
        while(responses.size() < routes.size()) {
            int result = selector.select(); // BLOCKS until at least 1 channel is ready for a wanted operation
            System.out.println("There are " + result + " channels ready to operate");
            var selectionKeys = selector.selectedKeys();

            // restricted to use iterator!  YIKES.
            Iterator<SelectionKey> iter = selectionKeys.iterator();
            while(iter.hasNext()) {
                var key = iter.next();
                var channelRoute = (ChannelRoute) key.attachment();  // Nice cast!  Ouch.
                if (key.isConnectable()) {
                    System.out.println("Finishing connect for " + channelRoute.getHost() + channelRoute.getPath());
                    channelRoute.channel.finishConnect(); // What a great API huh
                    key.interestOps(SelectionKey.OP_WRITE);
                } else if (key.isReadable()) {
                    System.out.println("Reading from " + channelRoute.getHost() + channelRoute.getPath());
                    var bb = responseBuffers.get(channelRoute.route);
                    if (doRead(channelRoute, bb)) {
                        byte[] buff = new byte[bb.position() + 1];
                        bb.position(0);     // gotta reset the stateful buffer source!
                        bb.get(buff, 0, buff.length);
                        responses.put(channelRoute.route, new String(buff));
                        key.cancel(); //done reading, no longer care about this key
                    }
                } else if (key.isWritable() && !responses.containsKey(channelRoute.route)) {
                    System.out.println("Writing to " + channelRoute.getHost() + channelRoute.getPath());
                    doRequest(channelRoute);
                    key.interestOps(SelectionKey.OP_READ);
                }
                iter.remove();
            }
            selectionKeys.clear();  // MUST do this to help reset the selector state
        }
        responses.forEach((route,body) -> {
            System.out.println("Got " + body.length() + " bytes in response from " + route.getHost() + route.getPath());
//            System.out.println(body);
        });
    }

    private static void startConnects(List<Route> routes, Selector selector) {
        // Connect each socket via channel and register with selector
        routes.forEach(route -> {
            try {
                var channel = SocketChannel.open();
                channel.configureBlocking(false);       // This is essential!
                channel.connect(new InetSocketAddress(route.getHost(), 80));
                var selectionKey = channel.register(selector, SelectionKey.OP_CONNECT);
                selectionKey.attach(new ChannelRoute(channel, route));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static void doRequest(ChannelRoute channelRoute) throws Exception {
        var requestString = "GET " + channelRoute.getPath() + " HTTP/1.1\n" +
                "Host: " + channelRoute.getHost() + "\n" +
                "User-Agent: test\n\n";
        var bb = ByteBuffer.wrap(requestString.getBytes());
        while(bb.hasRemaining()) {
            channelRoute.channel.write(bb);
        }
    }

    // Returns true if we didn't read a full buffer's worth of data, meaning that there is more to come...
    private static boolean doRead(ChannelRoute channelRoute, ByteBuffer bb) throws Exception {
        int available = bb.remaining();
        int read = channelRoute.channel.read(bb);
        return read == -1 || (read < available);
    }

    static class ChannelRoute {
        private final SocketChannel channel;
        private final Route route;

        ChannelRoute(SocketChannel channel, Route route) {
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
