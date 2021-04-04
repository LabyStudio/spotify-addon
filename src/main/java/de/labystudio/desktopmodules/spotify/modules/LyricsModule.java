package de.labystudio.desktopmodules.spotify.modules;

import com.google.gson.JsonObject;
import de.labystudio.desktopmodules.core.addon.Addon;
import de.labystudio.desktopmodules.core.loader.TextureLoader;
import de.labystudio.desktopmodules.core.module.Module;
import de.labystudio.desktopmodules.core.renderer.IRenderContext;
import de.labystudio.desktopmodules.core.renderer.font.Font;
import de.labystudio.desktopmodules.core.renderer.font.FontStyle;
import de.labystudio.desktopmodules.core.renderer.font.StringAlignment;
import de.labystudio.desktopmodules.core.renderer.font.StringEffect;
import de.labystudio.desktopmodules.spotify.SpotifyAddon;
import de.labystudio.desktopmodules.spotify.api.SpotifyAPI;
import de.labystudio.desktopmodules.spotify.api.Track;
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

    /**
     * If the lyrics is not synchronized with the actual song
     * then it is possible to shift the voice lines with this variable
     */
    private static final int VOICE_LINE_SHIFT = -2;

    private BufferedImage textureSpotify;

    private boolean smoothAnimation;

    private final VoiceLine[] voiceLineStack = new VoiceLine[3];
    private long lastVoiceLineChanged;

    private long customOffsetShift = 0;

    public LyricsModule() {
        super(550, 25);
    }

    @Override
    public void onInitialize(SpotifyAddon addon, JsonObject config) {
        super.onInitialize(addon, config);

        // Reset custom offset shit
        addon.getSpotifyAPI().addTrackChangeListener(track -> this.customOffsetShift = 0);
    }

    @Override
    public void onLoadConfig(JsonObject config) {
        super.onLoadConfig(config);

        this.smoothAnimation = Addon.getConfigValue(config, "smooth_animation", true);
    }

    @Override
    public void onTick() {
        Lyrics lyrics = this.addon.getLyrics();

        // Only if the lyrics has voice lines
        if (lyrics != null && lyrics.hasLines()) {
            long progress = this.addon.getSpotifyAPI().getProgress() + this.customOffsetShift;

            // Indicator if the next voice line should be displayed
            boolean voiceLineChanged = false;

            // Iterate the voice line stack and check if the offset changed
            for (int i = 0; i < this.voiceLineStack.length; i++) {
                // Get the voice line at given offset and shift
                VoiceLine voiceLine = lyrics.getVoiceLineAt(progress, VOICE_LINE_SHIFT + i);

                // We just have to compare the first one because it is always the same result for each line
                if (i == 0) {

                    // Compare the voice line in the stack with the one from the current iteration
                    if (compare(this.voiceLineStack[i], voiceLine)) {

                        // No changes, we don't have to continue here
                        break;
                    }

                    // The voice lines have changed, we need an animation for that!
                    voiceLineChanged = true;
                }

                // Update voice lines in stack
                this.voiceLineStack[i] = voiceLine;
            }

            // Mark the offset when the voice line changed in the stack
            if (voiceLineChanged) {
                this.lastVoiceLineChanged = progress;
            }
        }
    }

    @Override
    public void onRender(IRenderContext context, int width, int height) {
        context.drawImage(this.textureSpotify, this.rightBound ? this.width - this.height : 0, 0, height, height);

        Lyrics lyrics = this.addon.getLyrics();
        SpotifyAPI api = this.addon.getSpotifyAPI();

        // No voice lines in the stack
        if (lyrics == null || !lyrics.hasLines() || this.voiceLineStack[0] == null) {
            Track track = api.getTrack();

            // Draw track name
            context.drawString(track == null ? "Spotify" : track.getName(),
                    this.rightBound ? this.width - this.height - 5 : this.height + 5, (float) 10,
                    StringAlignment.from(this.rightBound), StringEffect.NONE, Color.WHITE, FONT);

            // Draw artist name
            context.drawString(track == null ? "No song playing" : track.getArtist(),
                    this.rightBound ? this.width - this.height - 5 : this.height + 5, (float) 22,
                    StringAlignment.from(this.rightBound), StringEffect.NONE, Color.WHITE, FONT);
            return;
        }

        // Get offset of the current playing track
        long progress = api.getProgress() + this.customOffsetShift;

        // Get the progress of the current playing animation
        double animationProgress = (progress - this.lastVoiceLineChanged) / 50d;

        // The progress in a range from 0 to 12
        double animationProgressInRange = Math.min(animationProgress, 12);

        // Make it smooth
        if (this.smoothAnimation) {
            double sigmoidInput = animationProgress / 2.0;
            animationProgressInRange = (1 - Math.exp(-sigmoidInput)) / (1 + Math.exp(-sigmoidInput)) * 12;
        }

        // The animated y position of the voice lines
        double y = 10 - animationProgressInRange;

        // Iterate all voice lines in the stack
        for (VoiceLine voiceLine : this.voiceLineStack) {

            // Draw the next voice line in the stack
            if (voiceLine != null) {
                context.drawString(voiceLine.getContent(), this.rightBound ? this.width - this.height - 5 : this.height + 5, (float) y,
                        StringAlignment.from(this.rightBound), StringEffect.SHADOW, Color.WHITE, FONT);
            }

            y += 12;
        }
    }

    @Override
    public void onMouseScroll(int x, int y, int velocity) {
        this.customOffsetShift += velocity * 1000L;
    }

    @Override
    public void onMousePressed(int x, int y, int mouseButton) {
        super.onMousePressed(x, y, mouseButton);

        // Reset shift
        if (mouseButton == 3) {
            this.customOffsetShift = 0;
        }
    }

    @Override
    public void loadTextures(TextureLoader textureLoader) {
        this.textureSpotify = textureLoader.load("textures/spotify/lyrics/spotify.png");
    }

    @Override
    protected String getIconPath() {
        return "textures/spotify/lyrics/lyrics.png";
    }

    @Override
    public String getDisplayName() {
        return "Lyrics";
    }

    /**
     * Compare the offset of two voice lines
     *
     * @param first  First voice line to compare (Can be null)
     * @param second Second voice line to compare (Can be null)
     * @return Matching offset
     */
    private boolean compare(VoiceLine first, VoiceLine second) {
        return first == null ? second == null : second != null && first.getOffset() == second.getOffset();
    }

}
