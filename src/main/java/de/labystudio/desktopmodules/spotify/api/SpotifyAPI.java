package de.labystudio.desktopmodules.spotify.api;

import de.labystudio.desktopmodules.spotify.api.connector.WinSpotifyConnector;
import de.labystudio.desktopmodules.spotify.api.protocol.PacketHandler;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.DataPacket;
import de.labystudio.desktopmodules.spotify.api.rest.OpenSpotifyAPI;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Spotify API
 *
 * @author LabyStudio
 */
public class SpotifyAPI implements PacketHandler {

    /**
     * Local Windows Spotify connector to get the live data
     */
    private final WinSpotifyConnector spotifyConnector;

    /**
     * Single executor for the connector to prevent two instances of the spotify api
     */
    private final ExecutorService connectExecutor = Executors.newSingleThreadExecutor();

    /**
     * Open spotify api
     */
    private final OpenSpotifyAPI openSpotifyApi = new OpenSpotifyAPI();

    /**
     * The current running track
     */
    private Track track;

    /**
     * Current playing state of Spotify
     */
    private boolean playing;

    /**
     * The progress of the current track (Updated on each data change)
     */
    private int progress;

    /**
     * The last time of the progress change
     */
    private long timeLastProgressChanged;

    public SpotifyAPI(File directory) {
        this.spotifyConnector = new WinSpotifyConnector(directory, this);
    }

    @Override
    public void handleDataPacket(DataPacket packet) {
        String id = packet.getTrackId();
        String name = packet.getTrackName();
        String artist = packet.getTrackArtist();
        int length = packet.getTrackLength();

        // Update track and playing state
        if ((this.playing = packet.isPlaying()) && (this.track == null || this.track.hasChanged(packet))) {
            // Create new track
            this.track = new Track(id, name, artist, length);

            // Progress value updates on track change
            this.timeLastProgressChanged = System.currentTimeMillis();

            // Update cover image
            this.openSpotifyApi.requestImageAsync(this.track, image -> {
                this.track.updateCover(image);
            });
        }

        // Store the time when the progress value changed
        if (!this.playing) {
            this.timeLastProgressChanged = System.currentTimeMillis();
        }

        // Update track progress
        this.progress = packet.getProgress();
    }

    /**
     * Update the connection depending if required or not
     *
     * @param connectionRequired Connection required because it's in use
     */
    public void updateConnectionState(boolean connectionRequired) {
        this.connectExecutor.execute(() -> {
            try {
                // Connect if required and disconnect if not required
                if (connectionRequired && !this.spotifyConnector.isConnected()) {
                    this.spotifyConnector.prepareAndConnect();
                } else if (!connectionRequired && this.spotifyConnector.isConnected()) {
                    this.spotifyConnector.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Force the Spotify connector to shutdown.
     */
    public void forceDisconnect() {
        this.spotifyConnector.disconnect();
    }

    /**
     * Get the prediction of the current progress
     *
     * @return Calculated playing progress in ms
     */
    public long getProgress() {
        // No song playing
        if (this.track == null) {
            return 0;
        }

        // Calculate progress
        long demonTime = this.progress + (this.playing ? System.currentTimeMillis() - this.timeLastProgressChanged : 0);
        return Math.min(demonTime, this.track.getLength());
    }

    /**
     * The current playing state of Spotify
     *
     * @return A song is playing right now
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Get the current running Spotify track
     *
     * @return The running track
     */
    public Track getTrack() {
        return track;
    }
}
