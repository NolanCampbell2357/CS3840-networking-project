package src;
public class Card {
  public enum SUIT {
    HEARTS,
    SPADES,
    CLUBS,
    DIAMONDS
  }

  public enum VALUE {
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    KING(10),
    QUEEN(10),
    JACK(10),
    ACE(11);

    public int intValue;

    VALUE(int intValue) {
      this.intValue = intValue;
    }
  }

  public VALUE value;
  public SUIT suit;

  public Card(VALUE value, SUIT suit) {
    this.value = value;
    this.suit = suit;
  }

  public String toString() {
    return "[" + value + " of " + suit + "] (" + value.intValue + ")";
  }
}
