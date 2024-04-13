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
          nextPlayer.setState(STATE.WAITING);
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
        player.setDealerHand("");
        player.setDealerValue(-1);
        player.setExternalMessage(null);
        player.setExternalMessage("");
        player.setState(STATE.WAITING);
        player.clearHand();
        dealer.hand.clear();
      }

      // Setup round
      BlackJackServer.deck.generate();

      // Deal dealer
      dealer.hand.takeCard(BlackJackServer.deck);
      dealer.hand.takeCard(BlackJackServer.deck);

      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        player.setExternalMessage("Dealer's first card is " + dealer.hand.getCard(0).toString()
            + ", the second card is face-down.\n");
      }

      // Give each player two cards
      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        player.setState(STATE.DEALING);
        System.out.println("Dealing a player");
        while (player.getSTATE() == STATE.DEALING) {
          System.out.println("dealing");
        }
          ;
          System.out.println("Finished dealing");
        player.setExternalMessage(null);
      }

      // Handle dealer has blackjack
      if (dealer.hasBlackjack()) {
        System.out.println("DEALER HAS BLACKJACK");
        for (PlayerThread player : BlackJackServer.players) {
          if (player == null)
          continue;
          player.setExternalMessage(dealer.hand.toString());
          player.setState(STATE.DEALER_BLACKJACK);
        }
        while (!playersWaiting())
          ;
        String resultMessage = "";
        for (PlayerThread player : BlackJackServer.players) {
          if (player == null)
          continue;
          resultMessage = "player in spot " + player.spot + " finished with result: " + player.getResult();
        }
        for (PlayerThread player : BlackJackServer.players) {
          if (player == null)
          continue;
          player.setExternalMessage(resultMessage);
          player.setState(STATE.RESULT);
        }
        while (!playersWaiting())
          ;
        continue;
      }

      // play round for each player
      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        player.setState(STATE.WAITING_FOR_TURN);
      }
      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        player.setState(STATE.TURN);
        for (PlayerThread messagePlayer : BlackJackServer.players) {
          if (messagePlayer == null)
            continue;
          messagePlayer.appendExternalMessageIfNotNull("Player " + player.spot + " is taking their turn");
        }
        while (player.getSTATE() == STATE.TURN) {
          if (!player.isAlive()) {
            break;
          }
        }
        for (PlayerThread messagePlayer : BlackJackServer.players) {
          if (messagePlayer == null)
            continue;
          messagePlayer.appendExternalMessageIfNotNull("Player " + player.spot + " finished their turn and " + player.getResult());
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
        player.setDealerValue(dealer.hand.calculateHand());
        player.setDealerHand("Dealer's " + dealer.toString());
        player.setState(STATE.EVAL_RESULT);
      }
      while (!playersWaiting())
        ;
      String resultMessage = "";
      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        resultMessage += "player in spot " + player.spot + " finished with result: " + player.getResult() + "\n";
      }
      for (PlayerThread player : BlackJackServer.players) {
        if (player == null)
          continue;
        player.setExternalMessage(resultMessage);
        player.setState(STATE.RESULT);
      }
      while (!playersWaiting())
        ;
    }
  }

  public boolean playersWaiting() {
    for (PlayerThread player : BlackJackServer.players) {
      if (player == null)
        continue;
      if (player.getSTATE() != STATE.WAITING)
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
