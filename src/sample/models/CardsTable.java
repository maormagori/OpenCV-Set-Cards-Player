package sample.models;

import org.opencv.core.MatOfPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that represents all the cards being played at that moment.
 * written by Maor Magori on 3/4/20.
 */
public class CardsTable {
    private Map<int[], Card> cards;

    public Map<int[], Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = new HashMap<>();
        for (Card card: cards)
            this.cards.put(card.getAttrs(),card);
    }

    public CardsTable(List<Card> cards){
        setCards(cards);
    }

    public CardsTable(){
        cards = new HashMap<>();
    }

    public void addCard(Card card){
        cards.put(card.getAttrs(),card);
    }

    public boolean removeCard(Card card){
        return cards.remove(card.getAttrs(), card);
    }

    public Set findSet(){
        for (Card card1:
                cards.values()) {
            for (Card card2:
                    cards.values()){
                if (cards.containsKey(card1.getThirdCard(card2)))
                    return new Set(card1,card2,cards.get(card1.getThirdCard(card2)));
            }
        }
        return null;
    }

    /**
     * Three cards which, by the game rules, are a set.
     * written by Maor Magori on 3/4/20.
     */
    public class Set {
        Card card1,card2,card3;

        public Set(Card card1, Card card2, Card card3){
            this.card1 = card1;
            this.card2 = card2;
            this.card3 = card3;
        }

        public List<MatOfPoint> getContours(){
            List<MatOfPoint> contours = new ArrayList();
            contours.add(card1.getContour());
            contours.add(card2.getContour());
            contours.add(card3.getContour());

            return contours;
        }
    }
}
