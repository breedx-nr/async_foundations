package com.nr;

public class Route {

    private final String host;
    private final String path;

    public Route(String host, String path) {
        this.host = host;
        this.path = path;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }
}
