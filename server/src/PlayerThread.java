package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerThread extends Thread {

  public enum STATE {
    QUEUED,
    DEALING,
    DEALER_BLACKJACK,
    WAITING,
    WAITING_FOR_TURN,
    TURN,
    RESULT,
    EVAL_RESULT
  }

  private Socket clientSocket;
  private Player player;
  private STATE state = STATE.QUEUED;
  private AtomicReference<STATE> atomicState = new AtomicReference<STATE>(state);

  public int spot;
  public String externalMessage = null;

  public String result;

  public int dealerValue = -1;
  public String dealerHand = "";

  public PlayerThread(Socket clientSocket, int playerId) {
    this.clientSocket = clientSocket;
    this.player = new Player(playerId);
  }

  @Override
  public void run() {
    System.out.println("Player " + player.id + " ready");

    try (
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        InputStream clientStream = clientSocket.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(clientStream))) {

      if (getSTATE() == STATE.QUEUED) {
        out.println("Connected to table, please wait...");
      }
      while (getSTATE() == STATE.QUEUED)
        ;

      out.println("Added to table spot " + spot + "\n");

      while (true) {
        out.println("Starting round, dealing cards\n");
        while (getSTATE() == STATE.WAITING) {
         // System.out.println("Waiting before deal " + state);
        }
          ;

          out.println(externalMessage);
          
        if (getSTATE() == STATE.DEALING) {
          player.hand.takeCard(BlackJackServer.deck);
          player.hand.takeCard(BlackJackServer.deck);
          out.println("Your " + player);
        }

        setState(STATE.WAITING);
        while (getSTATE() == STATE.WAITING) {
         // out.println("WAITING " + getSTATE());
        }
          ;

        if (getSTATE() == STATE.DEALER_BLACKJACK) {
          out.println("Dealer has BlackJack, here's the hand: ");
          out.println(externalMessage);

          if (player.hasBlackjack()) {
            out.println("Dealer and you both have BlackJack, PUSH!");
            result = "PUSH";
          } else {
            out.println("Dealer beats you, you LOSE!");
            result = "LOSS";
          }
          setState(STATE.WAITING);

          while (getSTATE() == STATE.WAITING)
            ;
          if (getSTATE() == STATE.RESULT) {
            out.println(externalMessage);
            setState(STATE.WAITING);
          }
          continue;
        }

        while (getSTATE() == STATE.WAITING_FOR_TURN || externalMessage != null) {
          if (externalMessage != null) {
            out.println(externalMessage);
            externalMessage = null;
          }
        }

        String inputLine;
        int input;

        if (getSTATE() == STATE.TURN) {
          out.println("Your Turn!");

          if (player.hasBlackjack()) {
            out.println("You have BlackJack! Win!");
            result = "WON";
          } else {
            boolean connectionDead = false;
            while (true) {
              out.println("Would you like to:\n    hit (1)\nor\n    stand (2)");
              out.println("request");

              if ((input = clientStream.read()) != -1) {
                char firstChar = (char) input;
                inputLine = firstChar + in.readLine();

                int choice = -1;
                try {
                  choice = Integer.valueOf(inputLine);
                } catch (NumberFormatException e) {
                  System.out.println("Bad choice from player " + player.id);
                }

                if (choice == 1) {
                  player.hand.takeCard(BlackJackServer.deck);
                  out.println("You drew a card");
                  out.println(player);
                  if (player.hand.calculateHand() > 20)
                    break;
                } else if (choice == 2) {
                  out.println("You chose to stand");
                  break;
                } else {
                  out.println("Invalid Choice");
                }
              } else {
                connectionDead = true;
                break;
              }
            }
            if (connectionDead)
              return;

            if (player.hand.calculateHand() > 21) {
              result = "BUST";
              out.println("You have BUST!");
            } else {
              result = "STOOD";
            }
          }
          setState(STATE.WAITING_FOR_TURN);
        }

        while (getSTATE() == STATE.WAITING_FOR_TURN) {
          if (externalMessage != null) {
            out.println(externalMessage);
            externalMessage = null;
          }
        }

        if (getSTATE() == STATE.EVAL_RESULT) {
          if (dealerValue > 21) {
            out.println(dealerHand);
            out.println("Dealer Busts, you WIN!");
            if (result == "STOOD") {
              result = "WON";
            }
          } else if (result != "STOOD") {
          } else if (dealerValue > player.hand.calculateHand()) {
            result = "LOSS";
            out.println("You LOSE!");
          } else if (dealerValue < player.hand.calculateHand()) {
            result = "WON";
            out.println("You Win");
          } else {
            result = "PUSH";
            out.println("You and the dealer tied, PUSH!");
          }
          setState(STATE.WAITING);
        }

        while (getSTATE() == STATE.WAITING)
          //System.out.println("Waiting after eval result " + state);;
        
          if (getSTATE() == STATE.RESULT) {
          out.println(externalMessage);
          setState(STATE.WAITING);
        }
      }

    } catch (IOException e) {
      System.out.println("Error performing I/O on Responder " + this.getId() + ". Connection closed.");
    }
    System.out.println("Client end");
  }

  public void clearHand() {
    player.hand.clear();
  }

  public void setState(STATE state) {;
    atomicState.set(state);
  }

  public STATE getSTATE() {
    return atomicState.getAcquire();
  }
  
}