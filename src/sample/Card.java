package sample;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

public class Card {
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

    private String color = "Unknown";
    private String shape = "Unknown";
    private String filling = "Unknown";
    private int amount;

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    private Point center;
    private MatOfPoint contour;

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
