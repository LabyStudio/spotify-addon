package de.labystudio.desktopmodules.spotify.api.lyrics.reader;

import java.util.ArrayList;
import java.util.List;

/**
 * The lyrics of a song, containing all voice lines with a time offset
 *
 * @author LabyStudio
 */
public class Lyrics {

    /**
     * All voice lines of the song with an offset in milliseconds
     */
    private final List<VoiceLine> voiceLines = new ArrayList<>();

    /**
     * Get the voice line of the song at the given offset
     *
     * @param offset The offset in milliseconds
     * @param shift  Shift the voice line index to get the previous (-1) voice line or the next one (+1)
     * @return The voice line at given input
     */
    public VoiceLine getVoiceLineAt(long offset, int shift) {
        int size = this.voiceLines.size();

        // Iterate all voice lines
        for (int i = 0; i < size; i++) {
            VoiceLine voiceLine = this.voiceLines.get(i);

            // If the voice line is above the given offset
            if (voiceLine.getOffset() >= offset) {
                // Shift the index
                int shiftedIndex = i + shift;

                // Check if the index is out of range
                boolean outOfRange = shiftedIndex < 0 || shiftedIndex >= size;

                // Get the voice line at the given index
                return shift == 0 ? voiceLine : outOfRange ? shift == -1 ? voiceLine : null : this.voiceLines.get(shiftedIndex);
            }
        }

        // No voice line found with the given input
        return null;
    }

    /**
     * Get the voice line of the song at the given offset
     *
     * @param offset The offset in milliseconds
     * @return The voice line at given input
     */
    public VoiceLine getVoiceLineAt(long offset) {
        return getVoiceLineAt(offset, 0);
    }

    /**
     * Get all voice lines of the lyrics
     *
     * @return A list of all voice lines
     */
    public List<VoiceLine> getVoiceLines() {
        return voiceLines;
    }

}
