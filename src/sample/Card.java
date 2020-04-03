package sample;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

public class Card {

    private String color = "Unknown";
    private String shape = "Unknown";
    private String filling = "Unknown";
    private int amount;
    private Point center;

    public MatOfPoint getContour() {
        return contour;
    }

    public void setContour(MatOfPoint contour) {
        this.contour = contour;
    }

    private MatOfPoint contour;

    /**
     * returning the value of the card's attrs as int array [color,shape,filling,amount]
     * @return
     */
    public int[] getAttrs(){
        int[] attrs = new int[4];
        switch (color){
            case Color.RED: attrs[0] = 0;
            break;
            case Color.GREEN: attrs[0] = 1;
                break;
            case Color.PURPLE: attrs[0] = 2;
                break;
        }

        switch (shape){
            case Shape.DIAMOND: attrs[1] = 0;
                break;
            case Shape.ELLIPSE: attrs[1] = 1;
                break;
            case Shape.WAVE: attrs[1] = 2;
                break;
        }

        switch (filling){
            case Filling.FULL: attrs[2] = 0;
                break;
            case Filling.HOLLOW: attrs[2] = 1;
                break;
            case Filling.STRIPED: attrs[2] = 2;
                break;
        }

        attrs[3] = amount-1;
        return attrs;
    }

    /**
     * returns the attrs of the cards that completes a set with self and other.
     * @param card
     * @return
     */
    public int[] getThirdCard( Card card){
        int[] self = this.getAttrs();
        int[] other = this.getAttrs();
        int[] attrs = new int[4];

        for (int i = 1; i<attrs.length;i++)
            attrs[i]=3-(self[i]+other[i]);

        return attrs;
    }
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getFilling() {
        return filling;
    }

    public void setFilling(String filling) {
        this.filling = filling;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public Card(String c, String s, String f, int a, MatOfPoint contour){
        color = c;
        shape = s;
        filling = f;
        amount=a;
        this.contour = contour;
    }

    public Card(MatOfPoint contour){
        this.contour = contour;
    }

    @Override
    public String toString() {
        return String.format("Card attributes:\namount: %d \nshape: %s \nfilling: %s \ncolor: %s",
                amount,shape,filling,color);
    }


    //Still not sure how and where to use these.
    public static class Color {
        public static final String RED = "Red",GREEN = "Green",PURPLE = "Purple";
    }

    public static class Shape {
        public static final String ELLIPSE = "Ellipse", WAVE = "Wave", DIAMOND = "Diamond";
    }

    public static class Filling {
        public static final String FULL = "Full", HOLLOW = "Hollow", STRIPED = "Striped";
    }
}
