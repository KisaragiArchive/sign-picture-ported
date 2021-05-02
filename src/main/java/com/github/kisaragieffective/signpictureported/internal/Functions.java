package com.github.kisaragieffective.signpictureported.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Functions {
    private Functions() {}

    public static <T, R> R switchBasedNullish(
            @Nullable T that,
            Function<@NotNull ? super T, ? extends R> ifNotNullMapper,
            Supplier<? extends R> ifNull
    ) {
        return that != null ? ifNotNullMapper.apply(that) : ifNull.get();
    }

    public static <A, B, C> Function<? super A, ? extends C> compose(
            Function<? super A, ? extends B> ab, Function<? super B, ? extends C> bc
    ) {
        return ab.andThen(bc);
    }

    public static <A, R> BiFunction<? super A, ? super Object, ? extends R> _1(
            Function<? super A, ? extends R> ar
    ) {
        return (a, o) -> ar.apply(a);
    }

    public static <A, B> B pipe(A a, Function<? super A, ? extends B> ab) {
        return ab.apply(a);
    }
}
