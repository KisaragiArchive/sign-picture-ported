package com.github.kisaragieffective.signpictureported.internal;

public interface ThrowableRunnable<X extends Throwable> {
    void run() throws X;
}
