package com.nr.async7;

public interface Completer<V,A> {
    void onComplete(V result, A attachment);
}
