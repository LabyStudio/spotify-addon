package de.labystudio.desktopmodules.spotify;

import de.labystudio.desktopmodules.core.addon.Addon;
import de.labystudio.desktopmodules.core.module.Module;
import de.labystudio.desktopmodules.spotify.api.SpotifyAPI;
import de.labystudio.desktopmodules.spotify.api.Track;
import de.labystudio.desktopmodules.spotify.api.lyrics.LyricsProvider;
import de.labystudio.desktopmodules.spotify.api.lyrics.reader.Lyrics;
import de.labystudio.desktopmodules.spotify.modules.LyricsModule;
import de.labystudio.desktopmodules.spotify.modules.SpotifyModule;

import java.util.function.Consumer;

/**
 * Spotify addon
 *
 * @author LabyStudio
 */
public class SpotifyAddon extends Addon implements Consumer<Track> {

    private LyricsProvider lyricsProvider;
    private SpotifyAPI spotifyAPI;

    private Lyrics lyrics = new Lyrics();

    @Override
    public void onEnable() throws Exception {
        this.lyricsProvider = new LyricsProvider();
        this.spotifyAPI = new SpotifyAPI(getConfigDirectory());

        // Register track change listener
        this.spotifyAPI.addTrackChangeListener(this);

        registerModule(SpotifyModule.class);
        registerModule(LyricsModule.class);
    }

    @Override
    public void onDisable() {
        this.spotifyAPI.forceDisconnect();
    }

    @Override
    public void accept(Track track) {
        this.lyricsProvider.requestAsync(track, lyrics -> {
            this.lyrics = lyrics;
        });
    }

    @Override
    public void onModuleVisibilityChanged(Module module, boolean enabled) {
        this.spotifyAPI.updateConnectionState(hasActiveModules());
    }

    public SpotifyAPI getSpotifyAPI() {
        return spotifyAPI;
    }

    public Lyrics getLyrics() {
        return lyrics;
    }
}
