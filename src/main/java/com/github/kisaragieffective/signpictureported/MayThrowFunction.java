package com.github.kisaragieffective.signpictureported;

public interface MayThrowFunction<A, R, X extends Throwable> {
    R apply(A a) throws X;
}
