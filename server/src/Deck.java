package src;

import java.util.ArrayList;
import java.util.Random;

import src.Card.SUIT;
import src.Card.VALUE;

public class Deck {

  private Card[] cards;
  private int currentPosition = 0;

  public Deck() {

    cards = new Card[52];

    int i = 0;
    for (SUIT suit : SUIT.values()) {
      for (VALUE value : VALUE.values()) {
        cards[i++] = new Card(value, suit);
      }
    }
  }

  public void shuffle() {
    Random rn = new Random();
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < cards.length; j++) {
        Card temp = cards[i];

        int rand = rn.nextInt(cards.length);

        cards[i] = cards[rand];
        cards[rand] = temp;
      }
    }
    currentPosition = 0;
  }

  public Card nextCard() {
    if (currentPosition > 51) {
      return null;
    }
    return cards[currentPosition];
  }
}
