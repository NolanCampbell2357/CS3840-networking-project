package src;

import java.util.ArrayList;

import src.Card.VALUE;

public class Hand {
  private ArrayList<Card> cards;

  public Hand() {
    cards = new ArrayList<Card>();
  }

  public void takeCard(Deck deck) {
    cards.add(deck.draw());
  }

  public String toString() {
    String output = "Value: " + calculateHand() + "\n" + "Hand:\n";

    for (Card card : cards) {
      output += card.toString() + "\n";
    }
    return output;
  }

  public int calculateHand() {
    int value = 0;
    int aceCount = 0;

    for (Card card : cards) {
      value += card.value.intValue;
      if (card.value == VALUE.ACE) {
        aceCount++;
      }
    }
    if (value > 21 && aceCount > 0) {
      while (aceCount > 0 && value > 21) {
        aceCount--;
        value -= 10;
      }
    }
    return value;
  }

  public Card getCard(int idx) {
    return cards.get(idx);
  }
}
