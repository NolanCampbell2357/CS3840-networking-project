package src;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class BlackJackServer {

  public static final int PORT_NUMBER = 6007;
  public static final int MAX_PLAYERS = 2;

  public static PlayerThread[] players = new PlayerThread[MAX_PLAYERS];
  public static ArrayList<PlayerThread> queuedPlayers = new ArrayList<PlayerThread>();
  public static Deck deck = new Deck();

  public static boolean roundActive = false;

  public static int lastPlayerId = 0;

  public static void main(String[] args) {

    GameThread game = new GameThread();
    game.start();

    try (ServerSocket serverSocket = new ServerSocket(PORT_NUMBER)) {

      System.out.println("BlackJack table started, waiting for players.");

      while (true) {
        Socket clientSocket = null;

        try {
          clientSocket = serverSocket.accept();
        } catch (IOException e) {
        }

        System.out.println("Connected to player, adding them to table");
        PlayerThread playerThread = new PlayerThread(clientSocket, lastPlayerId++);
        playerThread.start();
        // add players to queue, make it game threads problem
        queuedPlayers.add(playerThread);
      }
    } catch (IOException e) {
      System.out.println("Unknown Error");
      System.exit(1);
    }
  }
}