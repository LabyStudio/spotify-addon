package de.labystudio.desktopmodules.spotify.api;

import de.labystudio.desktopmodules.spotify.api.protocol.packet.both.DataPacket;

import java.awt.image.BufferedImage;

public class Track {

    private String id;
    private String name;
    private String artist;

    private int length;

    private BufferedImage cover;

    public Track(String id, String name, String artist, int length) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.length = length;
    }

    public void updateCover(BufferedImage image) {
        this.cover = image;
    }

    public boolean hasChanged(DataPacket packet) {
        return !this.name.equals(packet.getTrackName()) || !this.artist.equals(packet.getTrackArtist()) || this.length != packet.getTrackLength();
    }

    public String getId() {
        return id;
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

    public BufferedImage getCover() {
        return cover;
    }
}
