package com.github.kisaragieffective.signpictureported;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return a -> bc.apply(ab.apply(a));
    }
}
