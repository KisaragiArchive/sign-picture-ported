package com.github.kisaragieffective.signpictureported.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class HttpReqBuilder {
    private final URL url;

    public HttpReqBuilder(URL url) {
        this.url = url;
    }

    private final Map<HeaderKey<?>, Object> headers = new HashMap<>();

    public <T> HttpReqBuilder header(HeaderKey<T> headerKey, T value) {

    }

    public <T> Optional<T> send(Function<InputStream, T> reader) {
        try {
            final HttpURLConnection con = ((HttpURLConnection) url.openConnection());
            con.setInstanceFollowRedirects(true);
            headers.forEach((k, v) -> {
                con.setRequestProperty(k.name(), v.toString());
            });
            con.connect();

            return Optional.of(reader.apply(con.getInputStream()));
        } catch (IOException ioe) {
            return Optional.empty();
        }
    }
}
