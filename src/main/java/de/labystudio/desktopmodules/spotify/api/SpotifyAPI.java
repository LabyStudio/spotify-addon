package de.labystudio.desktopmodules.spotify.api;

import java.io.File;

/**
 * Spotify API
 *
 * @author LabyStudio
 */
public class SpotifyAPI {

    private final WinSpotifyConnector spotifyConnector;

    public SpotifyAPI(File directory) {
        this.spotifyConnector = new WinSpotifyConnector(directory);
    }

    /**
     * Update the connection depending if required or not
     *
     * @param connectionRequired Connection required because it's in use
     */
    public void updateConnectionState(boolean connectionRequired) {
        try {
            // Connect if required and disconnect if not required
            if (connectionRequired && !this.spotifyConnector.isConnected()) {
                this.spotifyConnector.prepareAndConnectAsync();
            } else if (!connectionRequired && this.spotifyConnector.isConnected()) {
                this.spotifyConnector.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WinSpotifyConnector getSpotifyConnector() {
        return spotifyConnector;
    }
}
