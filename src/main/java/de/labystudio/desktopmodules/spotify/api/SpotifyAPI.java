package de.labystudio.desktopmodules.spotify.api;

import de.labystudio.desktopmodules.spotify.api.connector.WinSpotifyConnector;
import de.labystudio.desktopmodules.spotify.api.protocol.PacketHandler;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.DataPacket;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Spotify API
 *
 * @author LabyStudio
 */
public class SpotifyAPI implements PacketHandler {

    private final WinSpotifyConnector spotifyConnector;
    private final ExecutorService connectExecutor = Executors.newSingleThreadExecutor();

    private Track track;

    private boolean playing;
    private int progress;

    private long timeLastPaused;

    public SpotifyAPI(File directory) {
        this.spotifyConnector = new WinSpotifyConnector(directory, this);
    }

    @Override
    public void handleDataPacket(DataPacket packet) {
        String name = packet.getTrackName();
        String artist = packet.getTrackArtist();
        int length = packet.getTrackLength();

        // Update track and playing state
        if ((this.playing = packet.isPlaying()) && (this.track == null || this.track.hasChanged(packet))) {
            this.track = new Track(name, artist, length);
            this.timeLastPaused = System.currentTimeMillis();
        }

        // Store paused time
        if (!this.playing) {
            this.timeLastPaused = System.currentTimeMillis();
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

    public long getProgress() {
        // No song playing
        if (this.track == null) {
            return 0;
        }

        // Calculate progress
        long demonTime = this.progress + (this.playing ? System.currentTimeMillis() - this.timeLastPaused : 0);
        return Math.min(demonTime, this.track.getLength());
    }

    public WinSpotifyConnector getSpotifyConnector() {
        return spotifyConnector;
    }

    public boolean isPlaying() {
        return playing;
    }

    public Track getTrack() {
        return track;
    }
}
