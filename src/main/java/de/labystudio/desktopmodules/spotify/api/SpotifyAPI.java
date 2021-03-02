package de.labystudio.desktopmodules.spotify.api;

import de.labystudio.desktopmodules.spotify.api.connector.WinSpotifyConnector;
import de.labystudio.desktopmodules.spotify.api.protocol.ErrorType;
import de.labystudio.desktopmodules.spotify.api.protocol.PacketHandler;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.both.DataPacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.client.NextPacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.client.PlayPausePacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.client.PreviousPacket;
import de.labystudio.desktopmodules.spotify.api.rest.OpenSpotifyAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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
     * Called when the track changed
     */
    private final List<Consumer<Track>> trackChangeListeners = new ArrayList<>();

    /**
     * The last error type (Is null if there is no error)
     */
    private ErrorType lastErrorType;

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

            // Call change listeners
            for (Consumer<Track> listener : this.trackChangeListeners) {
                listener.accept(this.track);
            }

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
        this.lastErrorType = null;
    }

    @Override
    public void handleExecutableError(ErrorType type) {
        this.lastErrorType = type;
        this.track = null;
    }

    /**
     * Connect to the Spotify api
     */
    public void connect() {
        this.connectExecutor.execute(() -> {
            // Connect if not connected yet
            if (!this.spotifyConnector.isConnected()) {
                this.spotifyConnector.prepareAndConnect();
            }
        });
    }

    /**
     * Force the Spotify connector to shutdown.
     */
    public void disconnect() {
        this.connectExecutor.execute(() -> this.spotifyConnector.disconnect(false));
    }

    /**
     * Send media command to the spotify connector
     *
     * @param command Media command type
     */
    public void sendMediaCommand(EnumMediaCommand command) {
        switch (command) {
            case PREVIOUS:
                this.spotifyConnector.sendPacketAndFlush(new PreviousPacket());
                break;
            case PLAY_PAUSE:
                this.spotifyConnector.sendPacketAndFlush(new PlayPausePacket());
                break;
            case NEXT:
                this.spotifyConnector.sendPacketAndFlush(new NextPacket());
                break;
        }
    }

    /**
     * Register a track change listener
     *
     * @param listener The listener
     */
    public void addTrackChangeListener(Consumer<Track> listener) {
        this.trackChangeListeners.add(listener);
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
     * Is connected to the SpotifyAPI executable
     *
     * @return Connection alive
     */
    public boolean isConnected() {
        return this.spotifyConnector != null && this.spotifyConnector.isConnected();
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

    /**
     * The last error from the executable (Is null if there is no error)
     *
     * @return Error type
     */
    public ErrorType getLastErrorType() {
        return lastErrorType;
    }

    /**
     * Spotify media commands
     */
    public enum EnumMediaCommand {
        PREVIOUS, PLAY_PAUSE, NEXT
    }

}
