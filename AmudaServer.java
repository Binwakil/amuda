
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;

public class AmudaServer {

    private static final int PORT = 11111;
    private static final int PACKET_SIZE = 1000;

    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket(PORT);
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        System.out.println("Server is listening on port " + PORT);

        while (true) {
            socket.receive(receivePacket);
            String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
            String[] requestParts = clientMessage.split(";");
            String webServer = requestParts[0];
            int timeout = Integer.parseInt(requestParts[1]);

            Thread handler = new Handler(socket, receivePacket.getAddress(), receivePacket.getPort(), webServer, timeout);
            handler.start();
        }
    }

    private static class Handler extends Thread {
        DatagramSocket socket;
        InetAddress clientAddress;
        int clientPort;
        String webServer;
        int timeout;

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
                URI uri = new URI(webServer);
                URL url = uri.toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                byte[] webData = buffer.toByteArray();
                sendPackets(webData);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        private void sendPackets(byte[] webData) throws IOException {
            int totalPackets = (int) Math.ceil(webData.length / (double) PACKET_SIZE);
            for (int i = 0; i < totalPackets; i++) {
                int start = i * PACKET_SIZE;
                int end = Math.min(start + PACKET_SIZE, webData.length);
                int payloadSize = end - start;
                byte[] packetData = new byte[12 + payloadSize];
                ByteBuffer.wrap(packetData, 0, 4).putInt(i);
                ByteBuffer.wrap(packetData, 4, 4).putInt(totalPackets);
                ByteBuffer.wrap(packetData, 8, 4).putInt(payloadSize);
                System.arraycopy(webData, start, packetData, 12, payloadSize);

                DatagramPacket packet = new DatagramPacket(packetData, packetData.length, clientAddress, clientPort);
                socket.send(packet);
            }

            // Wait for ACK or timeout
                try {
                    byte[] ackBuffer = new byte[10];  // ACK buffer
                    DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
                    socket.setSoTimeout(timeout * 1000);  // Set the timeout
                    socket.receive(ackPacket);
                    System.out.println("DONE");
                } catch (SocketTimeoutException e) {
                    System.out.println("RESENT");  // If timeout, retransmit
                    sendPackets(webData);  // Retransmit data
                }
            }
    }
}
