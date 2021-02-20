package de.labystudio.desktopmodules.spotify.modules;

import de.labystudio.desktopmodules.core.loader.TextureLoader;
import de.labystudio.desktopmodules.core.module.Module;
import de.labystudio.desktopmodules.core.renderer.IRenderContext;
import de.labystudio.desktopmodules.core.renderer.font.Font;
import de.labystudio.desktopmodules.core.renderer.font.FontStyle;
import de.labystudio.desktopmodules.core.renderer.font.StringAlignment;
import de.labystudio.desktopmodules.core.renderer.font.StringEffect;
import de.labystudio.desktopmodules.spotify.SpotifyAddon;
import de.labystudio.desktopmodules.spotify.api.SpotifyAPI;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.DataPacket;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Spotify module to display track, cover, progress..
 *
 * @author LabyStudio
 */
public class SpotifyModule extends Module<SpotifyAddon> {

    private static final Font FONT_TITLE = new Font("Dialog", FontStyle.PLAIN, 16);
    private static final Font FONT_TIME = new Font("Dialog", FontStyle.PLAIN, 10);

    private BufferedImage textureSpotify;
    private BufferedImage textureControlPlay;
    private BufferedImage textureControlPause;
    private BufferedImage textureControlNext;
    private BufferedImage textureControlPrevious;

    public SpotifyModule() {
        super(300, 80);
    }

    @Override
    public void loadTextures(TextureLoader textureLoader) {
        String spotifyPath = "textures/spotify/";

        this.textureSpotify = textureLoader.loadTexture(spotifyPath + "spotify.png");
        this.textureControlPlay = textureLoader.loadTexture(spotifyPath + "control_play.png");
        this.textureControlPause = textureLoader.loadTexture(spotifyPath + "control_pause.png");
        this.textureControlNext = textureLoader.loadTexture(spotifyPath + "control_next.png");
        this.textureControlPrevious = textureLoader.loadTexture(spotifyPath + "control_previous.png");
    }

    @Override
    public void onRender(IRenderContext context, int width, int height) {
        context.fillRect(0, 0, width - 1, height - 1, new Color(50, 50, 50, 130));

        // Draw spotify image
        context.drawImage(this.textureSpotify, this.rightBound ? width - height : 0, 0, height, height);

        SpotifyAPI api = this.addon.getSpotifyAPI();
        DataPacket dataPacket = api.getSpotifyConnector().getLastDataPacket();
        if (dataPacket != null) {
            int fontHeight = 24;
            int x = this.rightBound ? width - height - 5 : height + 5;

            // Draw track title
            context.setFont(FONT_TITLE);
            context.drawString(dataPacket.getTrackName(), x, fontHeight, StringAlignment.from(this.rightBound), StringEffect.NONE, Color.WHITE);
        }


    }

    @Override
    public void onTick() {

    }

    @Override
    protected String getIconPath() {
        return "textures/spotify/spotify.png";
    }

    @Override
    public String getDisplayName() {
        return "Spotify";
    }
}
