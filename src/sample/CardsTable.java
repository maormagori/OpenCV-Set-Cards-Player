package sample;

import org.opencv.core.MatOfPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents all the cards being played at that moment.
 * written by Maor Magori on 3/4/20.
 */
public class CardsTable {
    private List<Card> cards;

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public CardsTable(List<Card> cards){
        this.cards = cards;
    }

    public CardsTable(){
        cards = new ArrayList<>();
    }

    public boolean addCard(Card card){
        return cards.add(card);
    }

    public boolean removeCard(Card card){
        return cards.remove(card);
    }

    /**
     * Three cards which, by the game rules, are a set.
     * written by Maor Magori on 3/4/20.
     */
    public class set{
        Card card1,card2,card3;

        public set(Card card1,Card card2,Card card3){
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
