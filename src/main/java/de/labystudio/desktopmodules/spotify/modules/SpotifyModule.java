package de.labystudio.desktopmodules.spotify.modules;

import com.google.gson.JsonObject;
import de.labystudio.desktopmodules.core.loader.TextureLoader;
import de.labystudio.desktopmodules.core.module.Module;
import de.labystudio.desktopmodules.core.renderer.IRenderContext;
import de.labystudio.desktopmodules.core.renderer.font.Font;
import de.labystudio.desktopmodules.core.renderer.font.FontStyle;
import de.labystudio.desktopmodules.core.renderer.font.StringAlignment;
import de.labystudio.desktopmodules.core.renderer.font.StringEffect;
import de.labystudio.desktopmodules.spotify.SpotifyAddon;
import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyListenerAdapter;
import de.labystudio.spotifyapi.model.MediaKey;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.open.OpenSpotifyAPI;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Spotify module to display track, cover, progress..
 *
 * @author LabyStudio
 */
public class SpotifyModule extends Module<SpotifyAddon> {

    private static final Font FONT_TITLE = new Font("Dialog", FontStyle.PLAIN, 16);
    private static final Font FONT_TIME = new Font("Dialog", FontStyle.PLAIN, 10);

    private static final Color COLOR_BACKGROUND = new Color(40, 40, 40, 155);
    private static final Color COLOR_PROGRESS_BACKGROUND = new Color(20, 20, 20, 155);
    private static final Color COLOR_PROGRESS = new Color(30, 215, 96, 255);

    private final OpenSpotifyAPI openSpotifyAPI = new OpenSpotifyAPI();
    private BufferedImage coverImage;

    private BufferedImage textureSpotify;
    private BufferedImage textureControlPlay;
    private BufferedImage textureControlPause;
    private BufferedImage textureControlNext;
    private BufferedImage textureControlPrevious;

    public SpotifyModule() {
        super(300, 80);
    }

    @Override
    public void onInitialize(SpotifyAddon addon, JsonObject config) {
        super.onInitialize(addon, config);

        addon.getSpotifyAPI().registerListener(new SpotifyListenerAdapter() {
            @Override
            public void onTrackChanged(Track track) {
                coverImage = null;
                openSpotifyAPI.requestImageAsync(track, image -> coverImage = image);
            }
        });
    }

    @Override
    public void loadTextures(TextureLoader textureLoader) {
        String spotifyPath = "textures/spotify/";

        this.textureSpotify = textureLoader.load(spotifyPath + "spotify.png");
        this.textureControlPlay = textureLoader.load(spotifyPath + "control_play.png");
        this.textureControlPause = textureLoader.load(spotifyPath + "control_pause.png");
        this.textureControlNext = textureLoader.load(spotifyPath + "control_next.png");
        this.textureControlPrevious = textureLoader.load(spotifyPath + "control_previous.png");
    }

    @Override
    public void onRender(IRenderContext context, int width, int height, int mouseX, int mouseY) {
        SpotifyAPI api = this.addon.getSpotifyAPI();
        Track track = api.isConnected() ? api.getTrack() : null;
        boolean extendedModule = isMouseOver();

        // Draw spotify image
        BufferedImage trackCover = track == null || this.coverImage == null ? this.textureSpotify : this.coverImage;
        context.drawImage(trackCover, this.rightBound ? width - height : 0, 0, height, height);

        // Draw background
        if (extendedModule) {
            context.drawRectWH(this.rightBound ? 0 : height, 0, this.rightBound ? width - height : width, height, COLOR_BACKGROUND);
        }

        // Draw track title and artist
        {
            double y = 24;
            double x = this.rightBound ? width - height - 5 : height + 5;

            String title = track == null ? "Spotify" : track.getName().length() > 34 ? track.getName().substring(0, 34) : track.getName();
            String subTitle = track == null ? "No song playing" : track.getArtist().length() > 34 ? track.getArtist().substring(0, 34) : track.getArtist();

            // Set error message if present
            if (api.isInitialized()) {
                if (api.isConnected()) {
                    if (this.addon.getLastError() != null) {
                        subTitle = this.addon.getLastError();
                    }
                } else {
                    subTitle = "Connecting to Spotify...";
                }
            } else {
                subTitle = "Initializing...";
            }

            // Draw text
            StringEffect stringEffect = extendedModule ? StringEffect.NONE : StringEffect.SHADOW;
            context.drawString(title, x, y, StringAlignment.from(this.rightBound), stringEffect, Color.WHITE, FONT_TITLE);
            context.drawString(subTitle, x, y * 2, StringAlignment.from(this.rightBound), stringEffect, Color.WHITE, FONT_TITLE);
        }

        // Change module rendering depending on mouse hover state
        if (extendedModule) {
            // Draw controls
            if (track != null) {
                double x = this.rightBound ? 20 : width - 45;
                double y = 25;
                double gradientWidth = 120;

                // Cover title and artist behind the control buttons
                if (this.rightBound) {
                    context.drawGradientRect(0, 0, gradientWidth, height,
                            COLOR_BACKGROUND, gradientWidth / 2D, 0,
                            new Color(0, 0, 0, 0), gradientWidth, 0);
                } else {
                    context.drawGradientRect(width - gradientWidth, 0, width, height,
                            new Color(0, 0, 0, 0), width - gradientWidth, 0,
                            COLOR_BACKGROUND, width - gradientWidth / 2D, 0);
                }

                // Draw previous, play pause and next
                context.drawImage(this.textureControlPrevious, x - 11, y);
                context.drawImage(api.isPlaying() ? this.textureControlPause : this.textureControlPlay, x, y);
                context.drawImage(this.textureControlNext, x + 28, y);
            }

            // Draw progress bar
            if (track != null && api.hasPosition()) {
                double paddingX = context.getStringWidth("00:00", FONT_TIME) + 5;

                double x = this.rightBound ? paddingX : height + paddingX;
                double y = height - 5;

                double progressWidth = this.width - paddingX - x - (this.rightBound ? height : 0);
                double progressHeight = FONT_TIME.getSize() / 2F;

                // Background
                context.drawRectWH(x, y - progressHeight, progressWidth, progressHeight, COLOR_PROGRESS_BACKGROUND);

                // Green bar
                context.drawRectWH(x, y - progressHeight, (progressWidth / (double) track.getLength() * api.getPosition()), progressHeight, COLOR_PROGRESS);

                // Elapsed time
                String elapsedTime = formatTime(api.getPosition());
                context.drawString(elapsedTime, x - 3, y, StringAlignment.RIGHT, StringEffect.NONE, Color.WHITE, FONT_TIME);

                // Remaining time
                String remainingTime = formatTime(track.getLength() - api.getPosition());
                context.drawString(remainingTime, x + progressWidth + 3, y, StringAlignment.LEFT, StringEffect.NONE, Color.WHITE, FONT_TIME);
            }
        } else {
            double x = this.rightBound ? width - height : 0;

            // Draw progress on cover
            if (track != null && api.hasPosition()) {
                context.drawRectWH(x, height - 3, height, 3, COLOR_PROGRESS_BACKGROUND);
                context.drawRectWH(x, height - 3, (this.height / (double) track.getLength() * api.getPosition()), 3, COLOR_PROGRESS);
            }
        }
    }

    @Override
    public void onMousePressed(int x, int y, int mouseButton) {
        int controlsX = this.rightBound ? 20 : this.width - 45;
        int controlsY = 25;

        SpotifyAPI api = this.addon.getSpotifyAPI();

        if (y > controlsY && y < controlsY + 24) {
            // Previous
            if (x > controlsX - 11 && x < controlsX) {
                api.pressMediaKey(MediaKey.PREV);
                return;
            }

            // Play/pause
            if (x > controlsX && x < controlsX + 17) {
                api.pressMediaKey(MediaKey.PLAY_PAUSE);
                return;
            }

            // Next
            if (x > controlsX + 28 && x < controlsX + 28 + 11) {
                api.pressMediaKey(MediaKey.NEXT);
                return;
            }
        }

        // Call super method to make module moveable
        super.onMousePressed(x, y, mouseButton);
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

    private String formatTime(long duration) {
        return String.format("%02d:%02d", duration / 1000 / 60, duration / 1000 % 60);
    }
}
