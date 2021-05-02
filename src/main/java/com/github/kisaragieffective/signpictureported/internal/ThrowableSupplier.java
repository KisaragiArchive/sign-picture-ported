package com.github.kisaragieffective.signpictureported.internal;

public interface ThrowableSupplier<A, X extends Throwable> {
    A get() throws X;
}
