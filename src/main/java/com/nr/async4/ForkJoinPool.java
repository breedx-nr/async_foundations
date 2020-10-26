package com.nr.async4;

import com.nr.Route;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Similar to the previous, but uses the fork join pool via the jvm
 * and some stream style.
 */
public class ForkJoinPool {

    public static void main(String[] args) throws Exception {

        Stream.of(
                new Route("example.com", "/"),
                new Route("example.com", "/foo"),
                new Route("noisybox.net", "/")
            )
            .parallel() // fork join pool
            .map(route -> request(route.getHost(), route.getPath()))
            .forEach(System.out::println);
    }

    private static String request(String host, String path) {
        try {
            var sock = new Socket(host, 80);
            System.out.println(sock.isConnected());
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
        var requestString = "GET " + path + " HTTP/1.1\n" +
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
        while ((rc = isr.read(buff)) != -1) {
            response = response + new String(buff, 0, rc);
            if (rc < 1024) break;
        }
        return response;
    }
}
