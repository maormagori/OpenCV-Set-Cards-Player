package sample;

import org.opencv.core.Scalar;

/**
 * Created BY Maor Magori on 23.3.20. This class represents the color it was given as a parameter
 * by defining his HSV boundaries.
 */
public class Boundaries {
    private Scalar lowerBoundary, upperBoundary;
    private String name;

    public Boundaries(String name){
        this.name = name;
        switch (name){
            case Card.Color.GREEN: lowerBoundary = new Scalar(50,100,20);
                upperBoundary = new Scalar(70,255,255);
                break;
            case Card.Color.PURPLE: lowerBoundary = new Scalar(130,100,20);
                upperBoundary = new Scalar(145,255,255);
                break;
            case Card.Color.RED: lowerBoundary = new Scalar(170,100,20);
                upperBoundary = new Scalar(179,255,255);
                break;
            default: name = "Wrong color name entered!";
                break;
        }
    }
    public String getColor(){
        return name;
    }
    public Scalar getLowerBoundary(){
        return lowerBoundary;
    }
    public Scalar getUpperBoundary(){
        return upperBoundary;
    }
}
