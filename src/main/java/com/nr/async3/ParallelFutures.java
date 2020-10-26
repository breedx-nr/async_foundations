package com.nr.async3;

import com.nr.Route;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Just like the previous example, but uses Future to return the results
 * to the calling thread.
 */
public class ParallelFutures {

    public static void main(String[] args) throws Exception {

        var routes = List.of(
                new Route("example.com", "/"),
                new Route("example.com", "/foo"),
                new Route("noisybox.net", "/")
        );

        var pool = Executors.newFixedThreadPool(3);
        var futures = new ArrayList<Future<String>>();

        for (Route route : routes) {
            futures.add(pool.submit(() -> request(route.getHost(), route.getPath())));
        }

        futures.forEach(future -> {
            try {
                System.out.println(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }

    private static String request(String host, String path) {
        try {
            var sock = new Socket(host, 80);
            var out = doRequest(sock, host, path);
            var response = readResponse(sock);
            out.close();
            sock.close();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static OutputStream doRequest(Socket sock, String host, String path) throws Exception {
        var out = sock.getOutputStream();
        var requestString = "GET " + path +  " HTTP/1.1\n" +
                "Host: " + host + "\n" +
                "User-Agent: test\n\n";
        out.write(requestString.getBytes());
        out.flush();
        return out;
    }

    private static String readResponse(Socket sock) throws IOException {
        var in = sock.getInputStream();
        var isr = new InputStreamReader(in);
        var response = "";
        char[] buff = new char[1024];
        int rc;
        while( (rc = isr.read(buff)) != -1){
            response = response + new String(buff, 0, rc);
            if(rc < 1024) break;
        }
        return response;
    }
}
