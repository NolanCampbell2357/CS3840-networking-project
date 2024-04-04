package src;
import java.net.*;
import java.io.*;

public class BlackJackServer {

    public static final int PORT_NUMBER = 6007;

    

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT_NUMBER)) {

            System.out.println("Server started, waiting for connections.");

            while (true) {
                Socket clientSocket = null;

                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {}

                System.out.println("Connection successful, creating a new responder");
                new EchoResponder(clientSocket).start();

            }
        } catch (IOException e) {
            System.out.println("Error listening on port");
            System.exit(1);
        }
    }

    public static class EchoResponder extends Thread {
        private Socket clientSocket;

        public EchoResponder(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            System.out.println("Responder " + this.getId() + " ready to echo client");

            try (
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    InputStream clientStream = clientSocket.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientStream))) {

                String inputLine;
                int input;
                while ((input = clientStream.read()) != -1) {
                    char firstChar = (char) input;
                    inputLine = firstChar + in.readLine();
                    System.out.println("Responder " + this.getId() + ": " + inputLine);
                    out.println(inputLine);

                    if (inputLine.equals("Done"))
                        break;
                }
                System.out.println("Responder " + this.getId() + " completed");
            } catch (IOException e) {
                System.out.println("Error performing I/O on Responder " + this.getId() + ". Connection closed.");
            }
        }
    }
}