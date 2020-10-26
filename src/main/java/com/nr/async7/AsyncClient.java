package com.nr.async7;

import com.nr.Route;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

public class AsyncClient {

    private final Collection<ClientContext> contexts = new HashSet<>();

    public static AsyncClient create() {
        return new AsyncClient();
    }

    public ClientContext connectAsync(Route route) {
        var context = new ClientContext(route);
        contexts.add(context);
        return context;
    }

    public void runSync() {
        var latch = new CountDownLatch(contexts.size());
        contexts.forEach(ctx -> {
            try {
                var runtime = new ContextExecutor(ctx);
                runtime.execute(latch);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
