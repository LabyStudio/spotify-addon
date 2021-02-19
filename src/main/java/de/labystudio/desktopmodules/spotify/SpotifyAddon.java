package de.labystudio.desktopmodules.spotify;

import de.labystudio.desktopmodules.core.addon.Addon;
import de.labystudio.desktopmodules.spotify.modules.SpotifyModule;

public class SpotifyAddon extends Addon {

    @Override
    public void onEnable() throws Exception {
        System.out.println("Spotify addon enabled!");

        registerModule(SpotifyModule.class);
    }

    @Override
    public void onDisable() {
        System.out.println("Spotify addon disabled!");
    }
}
