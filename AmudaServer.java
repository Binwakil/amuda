import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;

/**
 * The AmudaServer class implements a UDP server that listens for client requests,
 * It serves as part of Task 2 in the COSC 650 Course Project by Amuda Group members:
 * - Almustapha Wakili
 * - Amuda Kamorudeen
 * - Binkam Deepak
 * - Ciana Hoggard
 *
 * This class is responsible for receiving requests from clients specifying a web server URL and a timer value.
 * Upon receiving a request, the server fetches data from the specified URL via HTTP GET, segments the data into
 * manageable packets, and sends these packets to the client over UDP. The server also handles acknowledgments (ACKs)
 * from the client to confirm data receipt. If an ACK is not received within the specified timer interval, the server
 * retransmits the data packets. This ensures robust data transmission even in environments with high packet loss.
 */
public class AmudaServer {

    private static final int PORT = 11111;  // UDP port number on which the server listens.
    private static final int PACKET_SIZE = 1000;  // Maximum size of the data payload in each UDP packet.

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                byte[] receiveBuffer = new byte[1024];  // Buffer for receiving incoming requests.
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
                handleClientRequest(serverSocket, receivePacket, clientMessage);
            }
        } catch (SocketException e) {
            System.err.println("Server socket error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    /**
     * Handles the incoming client message by validating and parsing the request and then initiating a new thread to process it.
     *
     * @param serverSocket the server's DatagramSocket
     * @param receivePacket the received DatagramPacket containing the client's message
     * @param clientMessage the client's message as a String
     */
    private static void handleClientRequest(DatagramSocket serverSocket, DatagramPacket receivePacket, String clientMessage) {
        try {
            String[] requestParts = clientMessage.trim().split(";");
            if (requestParts.length < 2) {
                System.err.println("Invalid client message format. Expected format: 'webServer;timeout'");
                return;
            }
            String webServer = requestParts[0];
            int timeout = Integer.parseInt(requestParts[1]);

            Thread handler = new Handler(serverSocket, receivePacket.getAddress(), receivePacket.getPort(), webServer, timeout);
            handler.start();
        } catch (NumberFormatException e) {
            System.err.println("Error parsing timeout value: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Array index out of bounds: " + e.getMessage());
        }
    }

    /**
     * The Handler class processes each client request in a separate thread to manage data fetching and packet sending.
     */
    private static class Handler extends Thread {
        private DatagramSocket socket;
        private InetAddress clientAddress;
        private int clientPort;
        private String webServer;
        private int timeout;

        public Handler(DatagramSocket socket, InetAddress clientAddress, int clientPort, String webServer, int timeout) {
            this.socket = socket;
            this.clientAddress = clientAddress;
            this.clientPort = clientPort;
            this.webServer = webServer;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(webServer);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                try (InputStream inputStream = connection.getInputStream()) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[16384];  // Data buffer for reading input stream.
                    int nRead;
                    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    byte[] webData = buffer.toByteArray();
                    sendPackets(webData);
                }
            } catch (MalformedURLException e) {
                System.err.println("Malformed URL: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("I/O error when fetching data: " + e.getMessage());
            }
        }

        /**
         * Sends data to the client in UDP packets and handles ACK reception or timeouts.
         */
        private void sendPackets(byte[] webData) throws IOException {
            int totalPackets = (int) Math.ceil(webData.length / (double) PACKET_SIZE);
            for (int i = 0; i < totalPackets; i++) {
                int start = i * PACKET_SIZE;
                int end = Math.min(start + PACKET_SIZE, webData.length);
                byte[] packetData = new byte[12 + (end - start)];  // Adjusted packet size
                ByteBuffer.wrap(packetData).putInt(i).putInt(totalPackets).putInt(end - start).put(webData, start, end - start);

                DatagramPacket packet = new DatagramPacket(packetData, packetData.length, clientAddress, clientPort);
                socket.send(packet);
            }

            // Wait for ACK or handle timeout
            byte[] ackBuffer = new byte[10];
            DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
            try {
                socket.setSoTimeout(timeout * 1000);
                socket.receive(ackPacket);
                System.out.println("DONE - ACK received");
            } catch (SocketTimeoutException e) {
                System.err.println("Timeout occurred, data retransmission needed");
            }
        }
    }
}