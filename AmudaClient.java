import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

/**
 * The AmudaClient class implements a UDP client that sends a request to a server,
 * It serves as part of Task 2 in the COSC 650 Course Project by Amuda Group members:
 * - Almustapha Wakili
 * - Amuda Kamorudeen
 * - Binkam Deepak
 * - Ciana Hoggard
 *htt
 * This class sends a user-specified web server URL and a timer value to the server,
 * receives multiple data packets in response, and handles packet reassembly and timeout.
 * If all packets are received and reassembled before the timeout, the client prints "OK" 
 * and the complete data. If a timeout occurs before all packets are received, it prints "FAIL".
 */
public class AmudaClient {

    private static final int SERVER_PORT = 11111;  // Port number where the server is listening.
    private static final String SERVER_ADDRESS = "localhost";  // Server address.

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);  // Set the socket timeout to 5000ms.

            Scanner scanner = new Scanner(System.in);  // Scanner for user input.
            System.out.print("Enter web server URL: ");
            String webServer = scanner.nextLine();  // Read the web server URL from user.
            System.out.print("Enter timer value in seconds: ");
            int timeout = Integer.parseInt(scanner.nextLine());  // Read the timer value in seconds.

            // Prepare and send the request packet to the server.
            String request = webServer + ";" + timeout;  // Format the request with web server URL and timeout.
            byte[] requestBytes = request.getBytes(StandardCharsets.UTF_8);  // Convert the request string into bytes.
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);  // Resolve the server's IP address.
            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, serverAddress, SERVER_PORT);
            socket.send(requestPacket);  // Send the request packet to the server.

            Map<Integer, byte[]> receivedPackets = new HashMap<>();  // Map to store received packets by their sequence number.
            boolean receiving = true;

            // Listen for incoming data packets from the server.
            while (receiving) {
                byte[] buffer = new byte[1012]; // Buffer to hold incoming packet data (payload plus headers).
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);  // Attempt to receive a packet.
                    ByteBuffer wrapped = ByteBuffer.wrap(packet.getData());  // Wrap the packet's data for processing.
                    int packetNumber = wrapped.getInt();  // Extract the packet number.
                    int totalPackets = wrapped.getInt();  // Extract the total number of packets expected.
                    int payloadSize = wrapped.getInt();  // Extract the payload size from the packet.
                    byte[] payload = new byte[payloadSize];  // Allocate byte array for the payload.
                    System.arraycopy(packet.getData(), 12, payload, 0, payloadSize);  // Copy the payload from the packet.
                    receivedPackets.put(packetNumber, payload);  // Store the payload indexed by packet number.

                    // Check if all packets have been received.
                    if (receivedPackets.size() == totalPackets) {
                        receiving = false;
                        // Send an ACK to the server indicating all packets were received.
                        byte[] ackMessage = "ACK".getBytes(StandardCharsets.UTF_8);
                        DatagramPacket ackPacket = new DatagramPacket(ackMessage, ackMessage.length, serverAddress, SERVER_PORT);
                        socket.send(ackPacket);
                        // Assemble and print the received data in sequence.
                        System.out.println("All packets received. Data assembly:");
                        for (int i = 0; i < totalPackets; i++) {
                            System.out.write(receivedPackets.get(i));
                        }
                        System.out.println("\nOK");
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("FAIL");  // Print "FAIL" if a timeout occurs before all packets are received.
                    receiving = false;
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());  // Handle and print any other exceptions.
        }
    }
}
