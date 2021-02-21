package de.labystudio.desktopmodules.spotify.api.protocol.packet.both;

import de.labystudio.desktopmodules.spotify.api.protocol.PacketHandler;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.SpotifyPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The main data packet. All information transferred from Spotify are in this packet
 *
 * @author LabyStudio
 */
public class DataPacket extends SpotifyPacket {

    private String trackId;
    private int progress;
    private int trackLength;
    private boolean playing;
    private String trackName;
    private String trackArtist;

    public DataPacket() {
    }

    @Override
    public void write(DataOutputStream outputStream) {
        // No data
    }

    @Override
    public void read(DataInputStream inputStream) throws IOException {
        this.trackId = readString(inputStream);
        this.progress = readInt(inputStream);
        this.trackLength = readInt(inputStream);
        this.playing = inputStream.readBoolean();
        this.trackName = readString(inputStream);
        this.trackArtist = readString(inputStream);
    }

    @Override
    public void handlePacket(PacketHandler packetHandler) {
        packetHandler.handleDataPacket(this);
    }

    /**
     * The track id of the currently playing track. This id can be used to make a request to OpenSpotify about this track
     *
     * @return The track id as string
     */
    public String getTrackId() {
        return trackId;
    }

    /**
     * The playing progress in ms of the track. The maximum is the track length.
     *
     * @return Track progress
     */
    public int getProgress() {
        return progress;
    }

    /**
     * The track length in ms
     *
     * @return Track length in milliseconds
     */
    public int getTrackLength() {
        return trackLength;
    }

    /**
     * The current playing state of Spotify
     *
     * @return A song is playing
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * The name of the track (Is unknown when paused)
     *
     * @return Track name
     */
    public String getTrackName() {
        return trackName;
    }

    /**
     * The artist of the track (Is unknown when paused)
     *
     * @return Track artist
     */
    public String getTrackArtist() {
        return trackArtist;
    }
}
