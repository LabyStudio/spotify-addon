package de.labystudio.desktopmodules.spotify.api;

import de.labystudio.desktopmodules.spotify.api.protocol.packet.DataPacket;

public class Track {

    private String name;
    private String artist;

    private int length;

    public Track(String name, String artist, int length) {
        this.name = name;
        this.artist = artist;
        this.length = length;
    }

    public boolean hasChanged(DataPacket packet) {
        return !this.name.equals(packet.getTrackName()) || !this.artist.equals(packet.getTrackArtist()) || this.length != packet.getTrackLength();
    }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }
}
