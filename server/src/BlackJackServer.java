package src;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class BlackJackServer {

  public static final int PORT_NUMBER = 6007;
  public static final int MAX_PLAYERS = 7;

  public static Deck deck = new Deck();
  public static Player dealer = new Player(-1);
  public static ArrayList<Player> players = new ArrayList<Player>();
  public static ArrayList<Player> queuedPlayers = new ArrayList<Player>();

  public static void main(String[] args) {

    try (ServerSocket serverSocket = new ServerSocket(PORT_NUMBER)) {

      System.out.println("BlackJack table started, waiting for players.");

      while (true) {
        Socket clientSocket = null;

        try {
          clientSocket = serverSocket.accept();
        } catch (IOException e) {
        }

        System.out.println("Connected to player, adding them to table");
        new Client(clientSocket).start();

      }
    } catch (IOException e) {
      System.out.println("Unknown Error");
      System.exit(1);
    }
  }

  public static class Client extends Thread {
    private Socket clientSocket;

    public Client(Socket clientSocket) {
      this.clientSocket = clientSocket;
    }

    public void run() {
      System.out.println("Player " + this.getId() + " ready");

      try (
          PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
          InputStream clientStream = clientSocket.getInputStream();
          BufferedReader in = new BufferedReader(new InputStreamReader(clientStream))) {

        String inputLine;
        int input;

        // put logic here to handle a round is currently playing and player is in queue

        while ((input = clientStream.read()) != -1) {
          // Put logic here to handle pr-round activities if this is the first player

          // Logic for player to take turn during round

          // Logic for last player to clean up round

          // Example use of sockets
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