package de.labystudio.desktopmodules.spotify.api.lyrics.source;

import com.google.gson.Gson;
import de.labystudio.desktopmodules.spotify.api.lyrics.reader.Lyrics;
import de.labystudio.spotifyapi.model.Track;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Lyrics database source object
 *
 * @author LabyStudio
 */
public abstract class LyricsSource {

    protected final Gson GSON = new Gson();

    /**
     * Get the lyrics from the given track
     *
     * @param track The Spotify track
     * @return The lyrics (Can be null if there is no result)
     * @throws Exception Request exception
     */
    public abstract Lyrics get(Track track) throws Exception;

    /**
     * API referer value
     *
     * @return The referer value
     */
    protected abstract String getReferer();

    /**
     * API User-Agent value
     *
     * @return The User-Agent value
     */
    protected abstract String getUserAgent();

    /**
     * Send a request to the given remote URL
     *
     * @param urlFormat URL to request containing variables (like %s)
     * @param query     Query to fill the variables with
     * @param payload   Payload in bytes (Can be null to send it without a payload)
     * @return The response as a string
     * @throws Exception Request exception
     */
    protected String request(String urlFormat, String query, byte[] payload) throws Exception {
        // Create URL
        String url = String.format(urlFormat, URLEncoder.encode(query, "UTF-8"));

        // Open connection
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        httpURLConnection.setRequestProperty("User-Agent", getUserAgent());
        httpURLConnection.setRequestProperty("Referer", getReferer());
        httpURLConnection.setRequestMethod("POST");

        // Add payload
        if (payload != null) {
            httpURLConnection.setDoOutput(true);
            OutputStream out = httpURLConnection.getOutputStream();
            out.write(payload);
        }

        // Check response code
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode / 100 != 2) {
            throw new Exception("Response code: " + responseCode);
        }

        // Get input stream
        InputStream in = httpURLConnection.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Read bytes
        byte[] data = new byte[1024];
        int count;
        while ((count = in.read(data, 0, 1024)) != -1) {
            baos.write(data, 0, count);
        }

        // To string
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }
}
