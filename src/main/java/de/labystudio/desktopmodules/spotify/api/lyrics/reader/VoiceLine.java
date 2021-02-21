package de.labystudio.desktopmodules.spotify.api.lyrics.reader;

/**
 * A voice line containing the lyrics of a song at a given offset in milliseconds
 *
 * @author LabyStudio
 */
public class VoiceLine {

    private final long offset;
    private final String content;

    public VoiceLine(long offset, String content) {
        this.offset = offset;
        this.content = content;
    }

    /**
     * The lyrics line
     *
     * @return The actual lyrics text
     */
    public String getContent() {
        return content;
    }

    /**
     * The offset in milliseconds of this line
     *
     * @return Offset in milliseconds
     */
    public long getOffset() {
        return offset;
    }

}
