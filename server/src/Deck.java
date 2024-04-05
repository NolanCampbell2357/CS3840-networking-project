package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import src.Card.SUIT;
import src.Card.VALUE;

public class Deck {

  private ArrayList<Card> cards;

  public Deck() {
    generate();
  }

  public void generate() {
    makeCards();
    shuffle();
  }

  public void makeCards() {
    cards = new ArrayList<Card>();

    for (SUIT suit : SUIT.values()) {
      for (VALUE value : VALUE.values()) {
        cards.add(new Card(value, suit));
      }
    }
  }

  public void shuffle() {
    Collections.shuffle(cards, new Random());
  }

  public Card draw() {
    if (cards.size() == 0) {
      return null;
    }
    return cards.remove(0);
  }
}
