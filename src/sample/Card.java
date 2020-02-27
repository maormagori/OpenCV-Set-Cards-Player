package sample;

import org.opencv.core.MatOfPoint;

public class Card {
    private int color, shape, filling;
    private MatOfPoint contour;

    public Card(int c,int s, int f, MatOfPoint contour){
        color = c;
        shape = s;
        filling = f;
        this.contour = contour;
    }

    public final class Color {
        public final int RED = 1, GREEN = 2, PURPLE = 3;
    }

    public final class Shape {
        public final int ELLIPSE = 1, WAVE = 2, DIAMOND = 3;
    }

    public final class FILLING {
        public final int FULL = 1, HOLLOW = 2, STRIPED = 3;
    }
}
