package de.labystudio.desktopmodules.spotify.api.connector;

import de.labystudio.desktopmodules.spotify.api.protocol.CommandPacket;
import de.labystudio.desktopmodules.spotify.api.protocol.PacketHandler;
import de.labystudio.desktopmodules.spotify.api.protocol.PacketRegistry;
import de.labystudio.desktopmodules.spotify.api.protocol.SpotifyPacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.DataPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Windows Spotify connector
 * Downloads the SpotifyAPI for Windows and connects to it
 *
 * @author LabyStudio
 */
public class WinSpotifyConnector {

    private static final long UPDATE_INTERVAL_SECONDS = 1;
    private static final int SOCKET_TIMEOUT_MS = 5000;
    private static final SocketAddress ADDRESS_SOCKET_API = new InetSocketAddress("localhost", 32018);

    private final File fileSpotifyApi;
    private final PacketHandler packetHandler;

    private final Queue<SpotifyPacket> packetQueue = new LinkedList<>();

    private Process process;
    private ScheduledFuture<?> task;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    /**
     * Create the spotify connector
     *
     * @param directory The directory to place the executable
     */
    public WinSpotifyConnector(File directory, PacketHandler packetHandler) {
        this.fileSpotifyApi = new File(directory, "SpotifyAPI.exe");
        this.packetHandler = packetHandler;

        // Attach shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::disconnect));
    }

    /**
     * Prepare the spotify api executable and connect to it
     */
    public void prepareAndConnect() {
        try {
            // Download latest executable
            new WinSpotifyExecutableProvider(this.fileSpotifyApi).provideLatestVersion();

            // Connect if executable is available
            if (this.fileSpotifyApi.exists()) {
                connect();
            }
        } catch (Exception e) {
            e.printStackTrace();

            // Disconnect
            disconnect();
        }
    }

    /**
     * Connect to the executable
     *
     * @throws IOException Connect exception
     */
    public void connect() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        String[] arguments = {this.fileSpotifyApi.getAbsolutePath()};

        // Execute api executable
        this.process = runtime.exec(arguments);

        // Connect to api process
        this.socket = new Socket();
        this.socket.connect(ADDRESS_SOCKET_API, SOCKET_TIMEOUT_MS);
        this.socket.setSoTimeout(SOCKET_TIMEOUT_MS);

        // Get IO stream
        if (this.socket.isConnected()) {
            this.inputStream = new DataInputStream(this.socket.getInputStream());
            this.outputStream = new DataOutputStream(this.socket.getOutputStream());
        }

        // Run repeating task
        if (this.task == null || this.task.isDone() || this.task.isCancelled()) {
            this.task = Executors.newSingleThreadScheduledExecutor()
                    .scheduleAtFixedRate(this::onUpdate, 0, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * Keep the connection alive and request the latest spotify state
     */
    private void onUpdate() {
        // Request Spotify data each update
        sendPacket(new DataPacket());

        try {
            // Reconnect if socket is closed
            if (!isConnected()) {
                connect();
            }

            // Send next packet in queue
            SpotifyPacket packet = this.packetQueue.poll();
            if (packet != null) {
                Byte id = PacketRegistry.getIdOf(packet.getClass());

                // Write packet to socket
                if (id != null) {
                    this.outputStream.writeByte(id);
                    packet.write(this.outputStream);
                    this.outputStream.flush();

                    // Wait before sending the next command
                    Thread.sleep(200L);

                    // Command packets have no response
                    if (!(packet instanceof CommandPacket)) {
                        // Read packet type
                        int packetId = readInt(this.inputStream);
                        SpotifyPacket receivedPacket = PacketRegistry.createById((byte) packetId);

                        // Read and handle packet data
                        if (receivedPacket != null) {
                            receivedPacket.read(this.inputStream);
                            receivedPacket.handlePacket(this.packetHandler);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            // Ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a spotify packet to the send queue
     *
     * @param packet Spotify packet to send
     */
    public void sendPacket(SpotifyPacket packet) {
        this.packetQueue.add(packet);
    }

    /**
     * Disconnect from the executable
     */
    public void disconnect() {
        // Stop task
        if (this.task != null && !this.task.isCancelled() && !this.task.isDone()) {
            this.task.cancel(true);
        }

        // Close socket connection
        if (this.socket != null && !this.socket.isClosed()) {
            try {
                this.socket.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        // Close executable
        if (this.process != null && this.process.isAlive()) {
            this.process.destroyForcibly();
        }
    }

    /**
     * Is socket connection alive
     *
     * @return Socket connection alive state
     */
    public boolean isConnected() {
        return this.socket != null && this.socket.isConnected() && !this.socket.isClosed();
    }

    /**
     * Read integer from input stream
     *
     * @param inputStream Input stream
     * @return The integer
     * @throws Exception Read exception
     */
    private int readInt(DataInputStream inputStream) throws IOException {
        byte[] lenBytes = new byte[4];
        inputStream.read(lenBytes, 0, 4);
        return (lenBytes[3] & 0xFF) << 24 | (lenBytes[2] & 0xFF) << 16 | (lenBytes[1] & 0xFF) << 8
                | (lenBytes[0] & 0xFF);
    }
}
