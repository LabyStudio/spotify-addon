package de.labystudio.desktopmodules.spotify.api.rest;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import de.labystudio.desktopmodules.spotify.api.Track;
import de.labystudio.desktopmodules.spotify.api.rest.model.AccessTokenResponse;
import de.labystudio.desktopmodules.spotify.api.rest.model.track.OpenTrack;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * OpenSpotify REST API
 *
 * @author LabyStudio
 */
public class OpenSpotifyAPI {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/71.0.3578.98";

    private static final String URL_API_GEN_ACCESS_TOKEN = "https://open.spotify.com/get_access_token?reason=transport&productType=web_player";
    private static final String URL_API_TRACKS = "https://api.spotify.com/v1/tracks/%s";

    private final Executor executor = Executors.newSingleThreadExecutor();

    private AccessTokenResponse accessTokenResponse;

    public OpenSpotifyAPI() {
        generateAccessTokenAsync(accessTokenResponse -> this.accessTokenResponse = accessTokenResponse);
    }

    /**
     * Generate an access token asynchronously for the open spotify api
     */
    private void generateAccessTokenAsync(Consumer<AccessTokenResponse> callback) {
        this.executor.execute(() -> {
            try {
                // Generate access token
                callback.accept(generateAccessToken());
            } catch (Exception error) {
                error.printStackTrace();
            }
        });
    }

    /**
     * Generate an access token for the open spotify api
     */
    private AccessTokenResponse generateAccessToken() throws IOException {
        // Open connection
        HttpsURLConnection connection = (HttpsURLConnection) new URL(URL_API_GEN_ACCESS_TOKEN).openConnection();
        connection.addRequestProperty("User-Agent", USER_AGENT);
        connection.addRequestProperty("referer", "https://open.spotify.com/");
        connection.addRequestProperty("app-platform", "WebPlayer");

        // Read response
        JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
        return new Gson().fromJson(reader, AccessTokenResponse.class);
    }

    /**
     * Request the cover image of the given track asynchronously
     *
     * @param track    The track to lookup
     * @param callback Response with the buffered image track. It won't be called on an error.
     */
    public void requestImageAsync(Track track, Consumer<BufferedImage> callback) {
        this.executor.execute(() -> {
            try {
                BufferedImage image = requestImage(track, true);
                if (image != null) {
                    callback.accept(image);
                }
            } catch (Exception error) {
                error.printStackTrace();
            }
        });
    }

    /**
     * Request the cover image of the given track
     *
     * @param track                     The track to lookup
     * @param canGenerateNewAccessToken It will try again once if it fails
     * @return Buffered image track. Is null if it fails
     * @throws Exception Exception can occur during the request
     */
    private BufferedImage requestImage(Track track, boolean canGenerateNewAccessToken) throws Exception {
        // Create REST API url
        String url = String.format(URL_API_TRACKS, track.getId());

        // Connect
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", USER_AGENT);
        connection.addRequestProperty("referer", "https://open.spotify.com/");
        connection.addRequestProperty("app-platform", "WebPlayer");
        connection.addRequestProperty("origin", "https://open.spotify.com");
        connection.addRequestProperty("authorization", "Bearer " + this.accessTokenResponse.accessToken);

        // Access token outdated
        if (connection.getResponseCode() / 100 != 2) {
            // Prevent infinite loop
            if (canGenerateNewAccessToken) {
                // Generate new access token
                this.accessTokenResponse = generateAccessToken();

                // Try again
                return requestImage(track, false);
            } else {
                // Request failed twice
                return null;
            }
        }

        // Read response
        JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
        OpenTrack openTrack = new Gson().fromJson(reader, OpenTrack.class);

        // Get largest image url
        String imageUrl = openTrack.album.images.get(0).url;

        // Download cover image
        if (imageUrl != null) {
            return ImageIO.read(new URL(imageUrl));
        }

        return null;
    }
}
