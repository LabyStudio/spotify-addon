package de.labystudio.desktopmodules.spotify.api.lyrics.reader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to read lyric files
 *
 * @author LabyStudio
 */
public class LyricsReader {

    private static final Pattern TIME_PATTERN = Pattern.compile("[\\[]([0-9]{2}):([0-9]{2}).([0-9]{2})[\\]]");
    private static final Comparator<VoiceLine> OFFSET_COMPARATOR = Comparator.comparingLong(VoiceLine::getOffset);

    private final Scanner scanner;

    /**
     * Create a lyrics reader from an input stream
     *
     * @param inputStream Lyrics text input stream
     */
    public LyricsReader(InputStream inputStream) {
        this.scanner = new Scanner(inputStream, "UTF-8");
    }

    /**
     * Create a lyrics reader from a string
     *
     * @param lyricsText Lyrics text
     */
    public LyricsReader(String lyricsText) {
        this.scanner = new Scanner(lyricsText);
    }

    /**
     * Read the lyrics from the input source.
     * It will automatically close the input stream
     *
     * @return The lyrics object. Voice lines are empty if the reading fails.
     */
    public Lyrics readLyrics() {
        Lyrics lyric = new Lyrics();
        List<VoiceLine> voiceLines = lyric.getVoiceLines();

        while (this.scanner.hasNextLine()) {
            String dataLine = this.scanner.nextLine();

            if (dataLine.contains("[") && dataLine.contains("]")) {
                // Find timecodes
                Matcher matcher = TIME_PATTERN.matcher(dataLine);

                int timeCodeLength = 0;
                List<Long> offsets = new ArrayList<Long>();

                while (matcher.find()) {
                    // Get timecode
                    long minutes = Integer.parseInt(matcher.group(1));
                    long seconds = Integer.parseInt(matcher.group(2));
                    long milliseconds = Integer.parseInt(matcher.group(3)) * 10L;

                    // Convert timecode to timestamp
                    long offset = milliseconds + seconds * 1000 + minutes * 1000 * 60;

                    // Add timecode string length
                    timeCodeLength += matcher.group(0).length();

                    offsets.add(offset);
                }

                // Remove timecode of data line
                String content = dataLine.substring(timeCodeLength);
                for (Long offset : offsets) {
                    // Create voiceline
                    VoiceLine voiceLine = new VoiceLine(offset, content);
                    voiceLines.add(voiceLine);
                }
            }
        }

        // Close scanner
        this.scanner.close();

        // Wrong format?
        if (voiceLines.isEmpty()) {
            return lyric;
        }

        // Sort offsets
        voiceLines.sort(OFFSET_COMPARATOR);

        // Workaround for start and end animation bug
        voiceLines.add(0, new VoiceLine(voiceLines.get(0).getOffset() / 2, ""));
        voiceLines.add(0, new VoiceLine(0, ""));
        voiceLines.add(new VoiceLine(voiceLines.get(voiceLines.size() - 1).getOffset() + 1000, ""));

        return lyric;
    }
}
