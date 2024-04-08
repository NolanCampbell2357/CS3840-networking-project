package src;

import java.io.*;
import java.net.*;

public class BlackJackClient {

  public static final int PORT_NUMBER = 6007;
  public static final String SERVER_HOSTNAME = "127.0.0.1";

  public static void main(String[] args) {

    System.out.println("Attempting to connect to host " +
        SERVER_HOSTNAME + " on port " + PORT_NUMBER);

    try (
        Socket echoSocket = new Socket(SERVER_HOSTNAME, PORT_NUMBER);
        PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

      System.out.println("Connection successful");

      String userInput;

      while (true) {
        String response = in.readLine();

        if (response.equals("request")) {
          System.out.print("input: ");
          while ((userInput = stdIn.readLine()) != null) {
            out.println(userInput);
           System.out.println(userInput);
            break;
          }
          continue;
        } else {
          System.out.println(response);
        }
      }
    } catch (UnknownHostException e) {
      System.err.println("Host " + SERVER_HOSTNAME + " unknown");
      System.exit(1);
    } catch (IOException e) {
      System.err.println("Could not get I/O from " + SERVER_HOSTNAME);
      System.exit(1);
    }

    System.out.println("Client end");
  }
}
