package src;

public class Player {
  public Hand hand;
  public int id;

  public Player(int id) {
    this.hand = new Hand();
    this.id = id;
  }

  public boolean hasBlackjack() {
    if (hand.calculateHand() == 21) {
      return true;
    } else {
      return false;
    }
  }

  public String toString() {
    return "Hand:\n" + hand.toString();
  }
}
