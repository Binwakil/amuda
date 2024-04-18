import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

public class AmudaClient {

    private static final int SERVER_PORT = 11111;
    private static final String SERVER_ADDRESS = "localhost";

    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(5000); // Example timeout

        Scanner scanner = new Scanner(System.in); // Declare the scanner object

        // ...

        System.out.print("Enter web server URL: ");
        String webServer = scanner.nextLine();
        System.out.print("Enter timer value in seconds: ");
        int timeout = Integer.parseInt(scanner.nextLine());

        String request = webServer + ";" + timeout;
        byte[] requestBytes = request.getBytes(StandardCharsets.UTF_8);
        InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);
        DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, serverAddress, SERVER_PORT);
        socket.send(requestPacket);

        Map<Integer, byte[]> receivedPackets = new HashMap<>();
        boolean receiving = true;

        while (receiving) {
            byte[] buffer = new byte[1012]; // PACKET_SIZE + 12 bytes for headers
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                ByteBuffer wrapped = ByteBuffer.wrap(packet.getData());
                int packetNumber = wrapped.getInt();
                int totalPackets = wrapped.getInt();
                int payloadSize = wrapped.getInt();
                byte[] payload = new byte[payloadSize];
                System.arraycopy(packet.getData(), 12, payload, 0, payloadSize);
                receivedPackets.put(packetNumber, payload);

                if (receivedPackets.size() == totalPackets) {
                    receiving = false;
                    // Send ACK here
                    System.out.println("All packets received. Data assembly:");
                    for (int i = 0; i < totalPackets; i++) {
                        System.out.write(receivedPackets.get(i));
                    }
                    System.out.println("\nOK");
                }
            } catch (SocketTimeoutException e) {
                System.out.println("FAIL");
                receiving = false;
            }
        }

        socket.close();
    }
}
