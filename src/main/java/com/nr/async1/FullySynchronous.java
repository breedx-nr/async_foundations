package com.nr.async1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * What does it take to do 3 requests?
 */
public class FullySynchronous {

    public static void main(String[] args) throws Exception {
        var sock = new Socket("example.com", 80);
        System.out.println(sock.isConnected());
        var out = doRequest(sock);
        var response = readResponse(sock);
        out.close();
        sock.close();
        System.out.println(response);

    }

    private static OutputStream doRequest(Socket sock) throws Exception {
        var out = sock.getOutputStream();
        out.write("GET / HTTP/1.1\nHost: example.com\nUser-Agent: test\n\n".getBytes());
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
