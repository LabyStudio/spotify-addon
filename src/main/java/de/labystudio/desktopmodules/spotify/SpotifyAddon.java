package de.labystudio.desktopmodules.spotify;

import de.labystudio.desktopmodules.core.addon.Addon;
import de.labystudio.desktopmodules.core.module.Module;
import de.labystudio.desktopmodules.spotify.api.SpotifyAPI;
import de.labystudio.desktopmodules.spotify.modules.SpotifyModule;

/**
 * Spotify addon
 *
 * @author LabyStudio
 */
public class SpotifyAddon extends Addon {

    private SpotifyAPI spotifyAPI;

    @Override
    public void onEnable() throws Exception {
        this.spotifyAPI = new SpotifyAPI(getConfigDirectory());

        registerModule(SpotifyModule.class);
    }

    @Override
    public void onDisable() {
        this.spotifyAPI.forceDisconnect();
    }

    @Override
    public void onModuleVisibilityChanged(Module module, boolean enabled) {
        this.spotifyAPI.updateConnectionState(hasActiveModules());
    }

    public SpotifyAPI getSpotifyAPI() {
        return spotifyAPI;
    }
}
