package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

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

  public int spot;

  private AtomicReference<STATE> atomicState = new AtomicReference<STATE>(state);
  private AtomicReference<String> externalMessage = new AtomicReference<String>(null);
  private AtomicReference<String> dealerHand = new AtomicReference<String>("");
  private AtomicReference<String> result = new AtomicReference<String>();
  private AtomicInteger dealerValue = new AtomicInteger(-1);

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
      while (getSTATE() == STATE.QUEUED) {
      }

      out.println("Added to table spot " + spot + "\n");

      while (true) {
        out.println("Starting round, dealing cards\n");
        while (getSTATE() == STATE.WAITING) {
          // System.out.println("Waiting before deal " + state);
        }

        out.println(getExternalMessage());

        if (getSTATE() == STATE.DEALING) {
          player.hand.takeCard(BlackJackServer.deck);
          player.hand.takeCard(BlackJackServer.deck);
          out.println("Your " + player);
        }

        setState(STATE.WAITING);
        while (getSTATE() == STATE.WAITING) {
          // out.println("WAITING " + getSTATE());
        }

        if (getSTATE() == STATE.DEALER_BLACKJACK) {
          out.println("Dealer has BlackJack, here's the hand: ");
          out.println(getExternalMessage());

          if (player.hasBlackjack()) {
            out.println("Dealer and you both have BlackJack, PUSH!");
            setResult("PUSH");
          } else {
            out.println("Dealer beats you, you LOSE!");
            setResult("LOSS");
          }
          setState(STATE.WAITING);

          while (getSTATE() == STATE.WAITING)
            ;
          if (getSTATE() == STATE.RESULT) {
            out.println(externalMessage.getAndSet(null));
            setState(STATE.WAITING);
          }
          continue;
        }

        while (getSTATE() == STATE.WAITING_FOR_TURN || getExternalMessage() != null) {
          if (getExternalMessage() != null) {
            out.println(externalMessage.getAndSet(null));
          }
        }

        String inputLine;
        int input;

        if (getSTATE() == STATE.TURN) {
          out.println("\nYour Turn!");

          if (player.hasBlackjack()) {
            out.println("You have BlackJack! Win!");
            setResult("WON");
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
                  System.out.println("\nBad choice from player " + player.id);
                }

                if (choice == 1) {
                  player.hand.takeCard(BlackJackServer.deck);
                  out.println("\nYou drew a card");
                  out.println(player);
                  if (player.hand.calculateHand() > 20)
                    break;
                } else if (choice == 2) {
                  out.println("\nYou chose to stand");
                  break;
                } else {
                  out.println("\nInvalid Choice");
                }
              } else {
                connectionDead = true;
                break;
              }
            }
            if (connectionDead)
              return;

            if (player.hand.calculateHand() > 21) {
              setResult("BUST");
              out.println("You have BUST!\n");
            } else {
              setResult("STOOD");
            }
          }
          setState(STATE.WAITING_FOR_TURN);
        }

        while (getSTATE() == STATE.WAITING_FOR_TURN) {
          if (getExternalMessage() != null) {
            out.println(externalMessage.getAndSet(null));
          }
        }

        if (getSTATE() == STATE.EVAL_RESULT) {
          if (getDealerValue() > 21) {
            out.println(getDealerHand());
            out.println("\nDealer Busts, you WIN!\n");
            if (getResult() == "STOOD") {
              setResult("WON");
            }
          } else if (getResult() != "STOOD") {
          } else if (getDealerValue() > player.hand.calculateHand()) {
            setResult("LOSS");
            out.println("\nYou LOSE!\n");
          } else if (getDealerValue() < player.hand.calculateHand()) {
            setResult("WON");
            out.println("\nYou Win\n");
          } else {
            setResult("PUSH");
            out.println("\nYou and the dealer tied, PUSH!\n");
          }
          setState(STATE.WAITING);
        }

        while (getSTATE() == STATE.WAITING) { // System.out.println("Waiting after eval result " + state);;
        }

        if (getSTATE() == STATE.RESULT) {
          out.println(getExternalMessage());
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

  public void setState(STATE state) {
    ;
    atomicState.set(state);
  }

  public STATE getSTATE() {
    return atomicState.getAcquire();
  }

  public void setExternalMessage(String message) {
    externalMessage.set(message);
  }

  public void appendExternalMessageIfNotNull(String message) {
    UnaryOperator<String> setIfNotNull = (v) -> {
      if(v==null) {
        return message;
      } else {
        return v+="\n"+message;
      }
    };
   externalMessage.updateAndGet(setIfNotNull);
  }

  public String getExternalMessage() {
    return externalMessage.get();
  }

  public void setDealerValue(int value) {
    dealerValue.set(value);
  }

  public int getDealerValue() {
    return dealerValue.get();
  }

  public void setDealerHand(String hand) {
    dealerHand.set(hand);
  }

  public String getDealerHand() {
    return dealerHand.get();
  }

  public void setResult(String str) {
    result.set(str);
  }

  public String getResult() {
    return result.get();
  }

}