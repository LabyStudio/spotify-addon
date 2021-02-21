package de.labystudio.desktopmodules.spotify;

import de.labystudio.desktopmodules.core.addon.Addon;
import de.labystudio.desktopmodules.spotify.api.SpotifyAPI;
import de.labystudio.desktopmodules.spotify.api.lyrics.LyricsProvider;
import de.labystudio.desktopmodules.spotify.api.lyrics.reader.Lyrics;
import de.labystudio.desktopmodules.spotify.modules.LyricsModule;
import de.labystudio.desktopmodules.spotify.modules.SpotifyModule;

/**
 * Spotify addon
 *
 * @author LabyStudio
 */
public class SpotifyAddon extends Addon {

    private LyricsProvider lyricsProvider;
    private SpotifyAPI spotifyAPI;

    private Lyrics lyrics;

    @Override
    public void onInitialize() throws Exception {
        this.lyricsProvider = new LyricsProvider();
        this.spotifyAPI = new SpotifyAPI(getConfigDirectory());

        // Register track change listener
        this.spotifyAPI.addTrackChangeListener(track ->
                this.lyricsProvider.requestAsync(track, lyrics -> this.lyrics = lyrics));

        registerModule(SpotifyModule.class);
        registerModule(LyricsModule.class);
    }

    @Override
    public void onEnable() {
        this.spotifyAPI.connect();
    }

    @Override
    public void onDisable() {
        this.spotifyAPI.disconnect();
    }

    public SpotifyAPI getSpotifyAPI() {
        return spotifyAPI;
    }

    public Lyrics getLyrics() {
        return lyrics;
    }
}
