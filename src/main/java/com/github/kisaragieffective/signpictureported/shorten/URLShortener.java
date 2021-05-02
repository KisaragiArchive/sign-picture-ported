package com.github.kisaragieffective.signpictureported.shorten;

import java.net.URL;

@SuppressWarnings("unused")
public interface URLShortener {
    /**
     *
     * post != original
     * @param original for shorten
     * @return the shorten URL
     */
    URL post(URL original);
}
