package com.nr.async2;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * What if we want to do them at the same time?
 * We can leverage the power of threads!  Wowwy wow!
 * This is a _parallel_ operation, fire and forget.
 * All of the response handling happens on the thread.
 */
public class ParallelThreads {

    public static void main(String[] args) throws Exception {

        var pool = Executors.newFixedThreadPool(3);
        pool.submit(() -> {
            var response = request("example.com", "/");
            System.out.println(response);
        });
        pool.submit(() -> {
            var response = request("example.com", "/foo");
            System.out.println(response);
        });
        pool.submit(() -> {
            var response = request("noisybox.net", "/");
            System.out.println(response);
        });
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
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
