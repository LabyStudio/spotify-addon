package de.labystudio.desktopmodules.spotify.api.protocol.packet.both;

import de.labystudio.desktopmodules.spotify.api.protocol.PacketHandler;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.SpotifyPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    public String getTrackId() {
        return trackId;
    }

    public int getProgress() {
        return progress;
    }

    public int getTrackLength() {
        return trackLength;
    }

    public boolean isPlaying() {
        return playing;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getTrackArtist() {
        return trackArtist;
    }
}
