package com.github.kisaragieffective.signpictureported.internal;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

import static com.github.kisaragieffective.signpictureported.internal.InternalSpecialUtility.never;

public final class WrapExceptions {
    public static <A> Supplier<A> wrapExceptionToUnchecked(ThrowableSupplier<A, ?> sa) {
        return () -> {
            try {
                return sa.get();
            } catch (Throwable e) {
                handleThrowable(e);
                return never();
            }
        };
    }

    public static Runnable wrapExceptionToUnchecked(ThrowableRunnable<?> tr) {
        return () -> {
            try {
                tr.run();
            } catch (Throwable e) {
                handleThrowable(e);
            }
        };
    }

    @Contract("_->fail")
    private static void handleThrowable(Throwable e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else if (e instanceof Error) {
            throw (Error) e;
        } else if (e instanceof IOException) {
            throw new UncheckedIOException((IOException) e);
        } else {
            throw new RuntimeException(e);
        }
    }
}
