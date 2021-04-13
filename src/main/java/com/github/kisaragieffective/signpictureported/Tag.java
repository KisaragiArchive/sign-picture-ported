package com.github.kisaragieffective.signpictureported;

public final class Tag<T> {
    private Tag() {}
    public static <T> Tag<T> of() {
        return new Tag<>();
    }

    T tryCast(Object o) {
        return (T) o;
    }

    public boolean isAcceptable(Object o) {
        try {
            tryCast(o);
            return true;
        } catch (ClassCastException ignored) {
            return false;
        }
    }
}
