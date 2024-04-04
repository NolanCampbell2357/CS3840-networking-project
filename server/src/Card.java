package src;
public class Card {
  public enum SUIT {
    HEARTS,
    SPADES,
    CLUBS,
    DIAMONDS
  }

  public enum VALUE {
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    KING,
    QUEEN,
    JACK,
    ACE
  }

  public VALUE value;
  public SUIT suit;

  public Card(VALUE value, SUIT suit) {
    this.value = value;
    this.suit = suit;
  }
}
