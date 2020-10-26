package com.nr.async7;

import com.nr.Route;

import java.util.List;

/**
 * Let's pretend we're building a single-threaded client framework based on the principles
 * that we have established so far.
 */
public class ClientFramework {

    public static void main(String[] args) {

        var routes = List.of(
                new Route("example.com", "/"),
                new Route("example.com", "/foo"),
                new Route("noisybox.net", "/"),
                new Route("noisybox.net", "/404")
        );

        var client = AsyncClient.create();
        routes.forEach(route -> {
            var requestString = "GET " + route.getPath() + " HTTP/1.1\n" +
                    "Host: " + route.getHost() + "\n" +
                    "User-Agent: test\n\n";

            client.connectAsync(route)
                .thenSendAsync(requestString)
                .thenReceiveFullyAsync( body -> {
                    System.out.println("Got " + body.length() + " bytes in response from " + route.getHost() + route.getPath());
//                    System.out.println(body);
                });
        });
        client.runSync();
    }
}
