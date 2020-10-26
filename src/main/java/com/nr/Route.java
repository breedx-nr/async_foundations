package com.nr;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(host, route.host) &&
                Objects.equals(path, route.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, path);
    }
}
