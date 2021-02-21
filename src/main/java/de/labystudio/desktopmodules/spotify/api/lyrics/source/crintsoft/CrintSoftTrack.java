package de.labystudio.desktopmodules.spotify.api.lyrics.source.crintsoft;

/**
 * The result entry of the crintsoft database search query
 * Each entry represents an available lyrics from this server
 *
 * @author LabyStudio
 */
public class CrintSoftTrack {

    /**
     * The name of the track
     */
    private String name;

    /**
     * The artist of the track
     */
    private String artist;

    /**
     * The url to the lyrics file
     */
    private String url;

    /**
     * Create track with url
     *
     * @param url Url to the lyrics file on this server
     */
    public CrintSoftTrack(String url) {
        this.url = url;
    }

    /**
     * Get the name of the track
     *
     * @return Name of the track
     */
    public String getName() {
        return name;
    }

    /**
     * Get the artist of the track
     *
     * @return Artist of the track
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Get the url of the lyrics file
     *
     * @return Url to the lyrics file on this server
     */
    public String getUrl() {
        return url;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setArtist(String artist) {
        this.artist = artist;
    }

    protected void setUrl(String url) {
        this.url = url;
    }
}
