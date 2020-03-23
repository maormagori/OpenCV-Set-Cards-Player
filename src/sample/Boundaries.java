package sample;

import org.opencv.core.Scalar;

public class Boundaries {
    private Scalar lowerBoundary, upperBoundary;
    private String name;

    public Boundaries(String name){
        this.name = name;
        switch (name){
            case Card.Color.GREEN: lowerBoundary = new Scalar(45,100,20);
                upperBoundary = new Scalar(65,255,255);
                break;
            case Card.Color.PURPLE: lowerBoundary = new Scalar(135,100,20);
                upperBoundary = new Scalar(150,255,255);
                break;
            case Card.Color.RED: lowerBoundary = new Scalar(175,100,20);
                upperBoundary = new Scalar(5,255,255);
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
