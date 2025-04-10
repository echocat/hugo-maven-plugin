package org.echocat.maven.plugins.hugo.utils;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

public interface Urls {

    @Nonnull
    static URL parse(@Nonnull String plain) throws IllegalArgumentException {
        try {
            return new URL(plain);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Nonnull
    static InputStream startDownloadOf(@Nonnull URL url) throws UncheckedIOException {
        try {
            boolean success = false;
            final InputStream urlIs = url.openStream();
            try {
                final InputStream resultIs = new BufferedInputStream(urlIs);
                success = true;
                return resultIs;
            } finally {
                if (!success) {
                    urlIs.close();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(format("Cannot download '%s'", url), e);
        }
    }

    @Nonnull
    static String readUrlFullyToString(@Nonnull URL url) throws UncheckedIOException {
        try (final InputStream is = startDownloadOf(url);
             final Reader r = new InputStreamReader(is, UTF_8);
             final Reader br = new BufferedReader(r);
             final CharArrayWriter buf = new CharArrayWriter()) {
            IOUtils.copy(br, buf);
            return buf.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(format("Cannot read content of '%s'", url), e);
        }
    }

    @Nonnull
    static JSONObject readUrlFullyToJsonObject(@Nonnull URL url) throws JSONException {
        try {
            return new JSONObject(readUrlFullyToString(url));
        } catch (JSONException e) {
            throw new JSONException(format("Cannot parse JSON of '%s'", url), e);
        }
    }

}
