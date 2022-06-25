package de.labystudio.desktopmodules.spotify;

import de.labystudio.desktopmodules.core.addon.Addon;
import de.labystudio.desktopmodules.spotify.modules.LyricsModule;
import de.labystudio.desktopmodules.spotify.modules.SpotifyModule;
import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyAPIFactory;
import de.labystudio.spotifyapi.SpotifyListenerAdapter;

/**
 * Spotify addon
 *
 * @author LabyStudio
 */
public class SpotifyAddon extends Addon {

    private SpotifyAPI spotifyAPI;
    private String lastError = null;
    private boolean initialized = false;

    @Override
    public void onInitialize() throws Exception {
        this.spotifyAPI = SpotifyAPIFactory.create();
        this.spotifyAPI.registerListener(new SpotifyListenerAdapter() {
            @Override
            public void onConnect() {
                lastError = null;
            }

            @Override
            public void onDisconnect(Exception exception) {
                lastError = exception.getMessage();
            }
        });

        registerModule(SpotifyModule.class);
        registerModule(LyricsModule.class);

        // Initialize api after modules are registered
        if (this.hasActiveModules()) {
            this.spotifyAPI.initialize();
        }
        this.initialized = true;
    }

    @Override
    public void onEnable() {
        // Don't initialize api before modules are registered
        if (this.initialized) {
            this.spotifyAPI.initialize();
        }
    }

    @Override
    public void onDisable() {
        this.spotifyAPI.stop();
    }

    public SpotifyAPI getSpotifyAPI() {
        return this.spotifyAPI;
    }

    public String getLastError() {
        return this.lastError;
    }
}
