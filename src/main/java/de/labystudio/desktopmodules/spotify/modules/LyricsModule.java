package de.labystudio.desktopmodules.spotify.modules;

import de.labystudio.desktopmodules.core.loader.TextureLoader;
import de.labystudio.desktopmodules.core.module.Module;
import de.labystudio.desktopmodules.core.renderer.IRenderContext;
import de.labystudio.desktopmodules.core.renderer.font.Font;
import de.labystudio.desktopmodules.core.renderer.font.FontStyle;
import de.labystudio.desktopmodules.core.renderer.font.StringAlignment;
import de.labystudio.desktopmodules.core.renderer.font.StringEffect;
import de.labystudio.desktopmodules.spotify.SpotifyAddon;
import de.labystudio.desktopmodules.spotify.api.lyrics.reader.Lyrics;
import de.labystudio.desktopmodules.spotify.api.lyrics.reader.VoiceLine;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Module to display the lyrics of the current playing Spotify song
 *
 * @author LabyStudio
 */
public class LyricsModule extends Module<SpotifyAddon> {

    private static final Font FONT = new Font("Dialog", FontStyle.PLAIN, 12);

    private BufferedImage textureSpotify;

    private final VoiceLine[] voiceLineStack = new VoiceLine[3];
    private long lastVoiceLineChanged;

    public LyricsModule() {
        super(550, 25);
    }

    @Override
    public void loadTextures(TextureLoader textureLoader) {
        this.textureSpotify = textureLoader.loadTexture("textures/spotify/lyrics/spotify.png");
    }

    @Override
    public void onTick() {
        Lyrics lyrics = this.addon.getLyrics();
        long progress = this.addon.getSpotifyAPI().getProgress();

        boolean voiceLineChanged = false;
        for (int i = 0; i < this.voiceLineStack.length; i++) {
            VoiceLine voiceLine = lyrics.getVoiceLineAt(progress, -2 + i);

            if (i == 0) {
                if (!compare(this.voiceLineStack[i], voiceLine))
                    voiceLineChanged = true;
                else
                    break;
            }

            this.voiceLineStack[i] = voiceLine;
        }

        if (voiceLineChanged) {
            this.lastVoiceLineChanged = progress;
        }
    }

    @Override
    public void onRender(IRenderContext context, int width, int height) {
        context.drawImage(this.textureSpotify, this.rightBound ? this.width - this.height : 0, 0, height, height);

        long progress = this.addon.getSpotifyAPI().getProgress();

        double animationProgress = 0;
        if (this.voiceLineStack[0] != null) {
            animationProgress = (progress - this.lastVoiceLineChanged) / 50d;
        }

        double y = 10 - Math.min(animationProgress, 12);
        for (VoiceLine voiceLine : this.voiceLineStack) {
            if (voiceLine != null) {
                context.drawString(voiceLine.getContent(), this.rightBound ? this.width - this.height - 5 : this.height + 5, (float) y, StringAlignment.from(this.rightBound), StringEffect.NONE, Color.WHITE, FONT);
            }
            y += 12;
        }
    }

    @Override
    protected String getIconPath() {
        return "textures/spotify/lyrics/lyrics.png";
    }

    @Override
    public String getDisplayName() {
        return "Lyrics";
    }

    private boolean compare(VoiceLine first, VoiceLine second) {
        return first == null ? second == null : second != null && first.getOffset() == second.getOffset();
    }

}
