package com.github.kisaragieffective.signpictureported.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class URLParseResult implements IParseResult {
    public static Optional<URLParseResult> from(String url) {
        try {
            return URLParseResult.from(new URL(url));
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }

    private static HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public static Optional<URLParseResult> from(URL url) {
        final URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            return Optional.empty();
        }

        final HttpRequest httpReq = HttpRequest.newBuilder(uri)
                .GET()
                .build();

        httpClient.sendAsync(httpReq, HttpResponse.BodyHandlers.ofInputStream())
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public DisplayConfigurationParseResult getDisplayConfiguration() {
        return null;
    }
}
