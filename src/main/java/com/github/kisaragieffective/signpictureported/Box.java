package com.github.kisaragieffective.signpictureported;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

public class Box<T> implements Closeable {
    public final Supplier<? extends T> value;
    private boolean needsInit = true;
    // late-bind
    private T cache = null;
    public Box(Supplier<? extends T> value) {
        this.value = value;
    }

    public T getValue() {
        if (needsInit) {
            cache = value.get();
            needsInit = false;
        }
        return cache;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Box<?> box = (Box<?>) o;
        return Objects.equals(cache, box.cache);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cache);
    }

    @Override
    public void close() throws IOException {
        if (cache instanceof Closeable) {
            ((Closeable) cache).close();
            needsInit = true;
            cache = null;
        }
    }
}
