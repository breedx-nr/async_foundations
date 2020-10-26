package com.nr.async7;

import java.nio.channels.CompletionHandler;

public class DoneSignal {

    public static <V,A> CompletionHandler<V,A> forHandler(Completer<V,A> delegate){
        return new CompletionHandler<V, A>() {
            @Override
            public void completed(V result, A attachment) {
                delegate.onComplete(result, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment) {
                exc.printStackTrace();
                // because why not!  FIXME
            }
        };
    }
}
