package com.nr.async1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * What does it take to do 3 requests synchronously, back to back?
 */
public class FullySynchronous {

    public static void main(String[] args) throws Exception {
        var response = request("example.com", "/");
        System.out.println(response);
        response = request("example.com", "/foo");
        System.out.println(response);
        response = request("noisybox.net", "/");
        System.out.println(response);
    }

    private static String request(String host, String path) throws Exception {
        var sock = new Socket(host, 80);
        System.out.println(sock.isConnected());
        var out = doRequest(sock, host, path);
        var response = readResponse(sock);
        out.close();
        sock.close();
        return response;
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
