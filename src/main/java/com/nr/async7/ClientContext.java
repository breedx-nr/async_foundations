package com.nr.async7;

import com.nr.Route;

import java.util.function.Consumer;

class ClientContext {

    private final Route route;
    private String requestString;
    private Consumer<String> receiveHandler;

    public ClientContext(Route route) {
        this.route = route;
    }

    public ClientContext thenSendAsync(String requestString) {
        this.requestString = requestString;
        return this;
    }

    public void thenReceiveFullyAsync(Consumer<String> handler) {
        this.receiveHandler = handler;
    }

    public Route getRoute() {
        return route;
    }

    public String getRequestString() {
        return requestString;
    }

    public Consumer<String> getReceiveHandler() {
        return receiveHandler;
    }
}
