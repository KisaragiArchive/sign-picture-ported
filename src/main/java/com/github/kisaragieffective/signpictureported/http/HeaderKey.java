package com.github.kisaragieffective.signpictureported.http;

public class HeaderKey<T> {
    private HeaderKey() {}

    String name();

    static <T> HeaderKey<T> of(String name) {
        return new HeaderKey<>() {
            @Override
            String name() {
                return name;
            }
        };
    }

    HeaderKey<Integer> ContentLength = HeaderKey.of("Content-Length");
    HeaderKey<String> ContentType = HeaderKey.of("Content-Type");

}
