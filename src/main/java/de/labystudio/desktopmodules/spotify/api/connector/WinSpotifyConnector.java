package de.labystudio.desktopmodules.spotify.api.connector;

import de.labystudio.desktopmodules.spotify.api.protocol.PacketHandler;
import de.labystudio.desktopmodules.spotify.api.protocol.PacketRegistry;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.CommandPacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.SpotifyPacket;
import de.labystudio.desktopmodules.spotify.api.protocol.packet.both.DataPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static de.labystudio.desktopmodules.spotify.api.protocol.PacketRegistry.RegistryChannel.CLIENT;
import static de.labystudio.desktopmodules.spotify.api.protocol.PacketRegistry.RegistryChannel.SERVER;

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

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> disconnect(false)));
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

            disconnect(true);
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
            this.task = this.executor.scheduleAtFixedRate(this::onUpdate, 0, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * Request the current spotify data and handle all packets in the queue
     */
    private void onUpdate() {
        // Request Spotify data each update
        sendPacket(new DataPacket());

        try {
            // Reconnect if socket is closed
            if (!isConnected()) {
                connect();
            }

            handlePacketQueue();
        } catch (Exception e) {
            e.printStackTrace();

            disconnect(true);
        }
    }

    /**
     * Send the next packet in the queue and handel the response
     *
     * @throws Exception Connection exception
     */
    private void handlePacketQueue() throws Exception {
        // Send next packet in queue
        SpotifyPacket packet = this.packetQueue.poll();
        if (packet != null) {
            Byte id = PacketRegistry.getIdOf(CLIENT, packet.getClass());

            // Write packet to socket
            if (id != null) {
                this.outputStream.writeByte(id);
                packet.write(this.outputStream);
                this.outputStream.flush();

                // Cancel if the connection is closed
                if (!isConnected()) {
                    return;
                }

                // Command packets have no response
                if (!(packet instanceof CommandPacket)) {
                    // Read packet type
                    int packetId = readInt(this.inputStream);
                    SpotifyPacket receivedPacket = PacketRegistry.createById(SERVER, (byte) packetId);

                    // Read and handle packet data
                    if (receivedPacket != null) {
                        receivedPacket.read(this.inputStream);
                        receivedPacket.handlePacket(this.packetHandler);
                    }
                }
            }
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
     * Add a spotify packet to the send queue and flush the entire queue right after
     *
     * @param packet Spotify packet to send and flush
     */
    public void sendPacketAndFlush(SpotifyPacket packet) {
        // Add packet to queue
        sendPacket(packet);

        // Async packet handling
        this.executor.execute(() -> {
            try {
                // Handle all packets in the queue
                while (!this.packetQueue.isEmpty()) {
                    // Flush
                    handlePacketQueue();
                }

                // Wait for the operating system to handle the media key
                Thread.sleep(200);

                // Update the spotify data again
                onUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Disconnect from the executable
     *
     * @param restartable The connector is able to reconnect after the disconnect
     */
    public void disconnect(boolean restartable) {
        // Stop task
        if (!restartable && this.task != null && !this.task.isCancelled() && !this.task.isDone()) {
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
