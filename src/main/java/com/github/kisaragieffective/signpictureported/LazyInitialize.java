package com.github.kisaragieffective.signpictureported;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

public class LazyInitialize<T> implements Closeable {
    public final Supplier<? extends T> value;
    private boolean needsInit = true;
    // late-bind
    private T lateBindCache = null;
    public LazyInitialize(Supplier<? extends T> value) {
        this.value = value;
    }

    public T getValue() {
        if (needsInit) {
            lateBindCache = value.get();
            needsInit = false;
        }
        return lateBindCache;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LazyInitialize<?> box = (LazyInitialize<?>) o;
        return Objects.equals(lateBindCache, box.lateBindCache);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lateBindCache);
    }

    @Override
    public void close() throws IOException {
        if (lateBindCache instanceof Closeable) {
            ((Closeable) lateBindCache).close();
            needsInit = true;
            lateBindCache = null;
        }
    }
}
