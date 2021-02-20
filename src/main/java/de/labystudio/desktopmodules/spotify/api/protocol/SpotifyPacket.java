package de.labystudio.desktopmodules.spotify.api.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Spotify packet
 *
 * @author LabyStudio
 */
public abstract class SpotifyPacket {

    /**
     * Write to spotify api
     *
     * @param outputStream Output stream
     * @throws IOException Write exception
     */
    public abstract void write(DataOutputStream outputStream) throws IOException;

    /**
     * Read from spotify api
     *
     * @param inputStream Input stream
     * @throws IOException Read exception
     */
    public abstract void read(DataInputStream inputStream) throws IOException;

    /**
     * Handle the packet
     *
     * @param packetHandler The callback interface
     */
    public abstract void handlePacket(PacketHandler packetHandler);

    /**
     * Read string from input stream
     *
     * @param inputStream Input stream
     * @return The string
     * @throws IOException Read exception
     */
    protected String readString(DataInputStream inputStream) throws IOException {
        int length = readInt(inputStream);
        byte[] receivedBytes = new byte[length];

        // Read string
        inputStream.read(receivedBytes, 0, length);

        return new String(receivedBytes, 0, length, StandardCharsets.UTF_8);
    }

    /**
     * Read integer from input stream
     *
     * @param inputStream Input stream
     * @return The integer
     * @throws Exception Read exception
     */
    protected int readInt(DataInputStream inputStream) throws IOException {
        byte[] lenBytes = new byte[4];
        inputStream.read(lenBytes, 0, 4);
        return (lenBytes[3] & 0xFF) << 24 | (lenBytes[2] & 0xFF) << 16 | (lenBytes[1] & 0xFF) << 8
                | (lenBytes[0] & 0xFF);
    }

}
