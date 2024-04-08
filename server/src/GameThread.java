package src;

import src.PlayerThread.STATE;

public class GameThread extends Thread {
  public Player dealer = new Player(-1);

  public GameThread() {
  }

  @Override
  public void run() {
    while (true) {
      // Manage players
      int openSpot = -1;
      System.out.println(spotOpen());
      while ((openSpot = spotOpen()) != -1 && !BlackJackServer.queuedPlayers.isEmpty()) {
        System.out.println("Hit open spot");
        PlayerThread nextPlayer = BlackJackServer.queuedPlayers.remove(0);
        if (nextPlayer.isAlive()) {
          System.out.println("Switch state");
          nextPlayer.spot = openSpot + 1;
          nextPlayer.state = STATE.WAITING;
          BlackJackServer.players[openSpot] = nextPlayer;
        }
      }

      boolean noPlayers = true;
      for (PlayerThread player : BlackJackServer.players) {
        if (player != null) {
          noPlayers = false;
          break;
        }
      }
      if (noPlayers)
        continue;

      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        player.dealerHand = "";
        player.dealerValue = -1;
        player.externalMessage = null;
        player.result = "";
        player.state = STATE.WAITING;
        player.clearHand();
      }

      // Setup round
      BlackJackServer.deck.generate();

      // Deal dealer
      dealer.hand.takeCard(BlackJackServer.deck);
      dealer.hand.takeCard(BlackJackServer.deck);

      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        player.externalMessage = "Dealer's first card is " + dealer.hand.getCard(0).toString()
            + ", the second card is face-down.";
      }

      // Give each player two cards
      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        player.state = STATE.DEALING;
        while (player.state == STATE.DEALING)
          ;
        player.externalMessage = null;
      }

      // Handle dealer has blackjack
      if (dealer.hasBlackjack()) {
        for (PlayerThread player : BlackJackServer.players) {
          if (player == null)
          continue;
          player.externalMessage = dealer.hand.toString();
        }
        while (!playersWaiting())
          ;
        String resultMessage = "";
        for (PlayerThread player : BlackJackServer.players) {
          if (player == null)
          continue;
          resultMessage = "player in spot " + player.spot + " finished with result: " + player.result;
        }
        for (PlayerThread player : BlackJackServer.players) {
          if (player == null)
          continue;
          player.externalMessage = resultMessage;
          player.state = STATE.RESULT;
        }
        while (!playersWaiting())
          ;
        continue;
      }

      // play round for each player
      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        player.state = STATE.WAITING_FOR_TURN;
      }
      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        player.state = STATE.TURN;
        for (PlayerThread messagePlayer : BlackJackServer.players) {
          if (messagePlayer == null)
            continue;
          messagePlayer.externalMessage = "Player " + player.spot + " is taking their turn";
        }
        while (player.state == STATE.TURN) {
          if (!player.isAlive()) {
            break;
          }
        }
        for (PlayerThread messagePlayer : BlackJackServer.players) {
          if (messagePlayer == null)
            continue;
          messagePlayer.externalMessage = "Player " + player.spot + " finished their turn and " + player.result;
        }
      }

      // Play dealer
      while (dealer.hand.calculateHand() < 17) {
        dealer.hand.takeCard(BlackJackServer.deck);
      }

      // Evaluate wins/losses
      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        player.dealerValue = dealer.hand.calculateHand();
        player.dealerHand = "Dealer's " + dealer.toString();
        player.state = STATE.EVAL_RESULT;
      }
      while (!playersWaiting())
        ;
      String resultMessage = "";
      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        resultMessage += "player in spot " + player.spot + " finished with result: " + player.result + "\n";
      }
      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        player.externalMessage = resultMessage;
        player.state = STATE.RESULT;
      }
      while (!playersWaiting())
        ;
    }
  }

  public boolean playersWaiting() {
    for (PlayerThread player : BlackJackServer.players) {
      if (player == null)
        continue;
      if (player.state != STATE.WAITING)
        return false;
    }
    return true;
  }

  public int spotOpen() {
    for (int i = 0; i < BlackJackServer.players.length; i++) {
      if (BlackJackServer.players[i] == null) {
        return i;
      }
    }
    return -1;
  }
}
