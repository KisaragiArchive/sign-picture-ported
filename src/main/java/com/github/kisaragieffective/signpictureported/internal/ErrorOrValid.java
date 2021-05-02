package com.github.kisaragieffective.signpictureported.internal;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ErrorOrValid<E, V> {
    boolean isError();

    boolean isValid();

    Optional<? extends E> error();

    Optional<? extends V> value();

    static <E, D> Error<? extends E, ? extends D> error(E e) {
        return new Error<>(e);
    }

    static <D, V> Valid<? extends D, ? extends V> valid(V v) {
        return new Valid<>(v);
    }

    final class Error<E, D> implements ErrorOrValid<E, D> {
        private final E error;

        public Error(@NotNull E error) {
            this.error = error;
        }
        @Override
        public boolean isError() {
            return true;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public Optional<E> error() {
            return Optional.of(error);
        }

        @Override
        public Optional<D> value() {
            return Optional.empty();
        }
    }

    final class Valid<D, V> implements ErrorOrValid<D, V> {
        private final V value;

        public Valid(V value) {
            this.value = value;
        }

        @Override
        public boolean isError() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Optional<D> error() {
            return Optional.empty();
        }

        @Override
        public Optional<V> value() {
            return Optional.of(value);
        }
    }
}
