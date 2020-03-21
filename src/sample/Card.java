package sample;

import org.opencv.core.MatOfPoint;

public class Card {
    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getShape() {
        return shape;
    }

    public void setShape(int shape) {
        this.shape = shape;
    }

    public int getFilling() {
        return filling;
    }

    public void setFilling(int filling) {
        this.filling = filling;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    private int color;
    private int shape;
    private int filling;
    private int amount;
    private MatOfPoint contour;

    public Card(int c,int s, int f, int a, MatOfPoint contour){
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
        System.out.println("Card attributes:");
        System.out.println("\namount: " + amount);
        System.out.println("\nshape: " + shape);
        System.out.println("\nfilling: " + filling);
        System.out.println("\ncolor: " + color);
        return String.format("Card attributes:\namount: {0}\nshape: {1}\nfilling: {2}\ncolor: {3}",
                amount,shape,filling,color);
    }

    public static class Color {
        public static int RED = 1, GREEN = 2, PURPLE = 3;
    }

    public static class Shape {
        public static int ELLIPSE = 1, WAVE = 2, DIAMOND = 3;
    }

    public static class Filling {
        public static int FULL = 1, HOLLOW = 2, STRIPED = 3;
    }
}
