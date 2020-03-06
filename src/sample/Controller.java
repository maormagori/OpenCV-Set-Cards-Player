package sample;



import com.sun.deploy.util.ArrayUtil;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.*;
import java.util.List;


import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Controller {

    @FXML
    private ImageView mainImgView, rightImage;
    Mat picture = Imgcodecs.imread("C:/Users/Maor/IdeaProjects/OpenCV_Testing/photos/20200221_161817.jpg");
    Mat greyedPicture = new Mat();
    Mat blurredPicture = new Mat();
    Mat threshedPicture = new Mat();
    Mat hier = new Mat();



    public void buttonPressed() {
        //System.out.println(this.getClass().getResource("/JavaFxAndOpenCV/photos/20200221_162013.jpg").getPath());
        System.out.println("button pressed");

        Image theFuckingImage = mat2Image(picture);

        mainImgView.setImage(theFuckingImage);


    }

    public List<MatOfPoint> findCardsContours() {
        Mat imageWithContours = picture.clone();
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> cardContours = new ArrayList<>();


        /**
         * First we find all of the contours in the threshed picture and store the in a List.
         * if there are no contours we don't do anything.
         */
        Imgproc.findContours(threshedPicture, contours, hier,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE );
        System.out.println("number of contours: " + contours.size());
        if (contours.size()==0)
            return null;

        /**
         * Create a new indexes list and sort it by the area of each contour in the contours list.
         * From largest to smallest. later on will be used to find contour hierarchy based on his index.
         */
        List<Integer> sort_index=new ArrayList<>();
        for (int i=0; i<contours.size();i++)
            sort_index.add(i);

        sort_index.sort((c1,c2) -> {
            if (Imgproc.contourArea(contours.get(c1)) == Imgproc.contourArea(contours.get(c2)) )
                return 0;
            return Imgproc.contourArea(contours.get(c1)) > Imgproc.contourArea(contours.get(c2)) ? -1:1;
        });

        MatOfPoint2f temp = new MatOfPoint2f();
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Mat firstCard;

        for (Integer i :
                sort_index) {
            //double size = Imgproc.contourArea(contours.get(i));
            contours.get(i).convertTo(temp, CvType.CV_32F);
            double peri = Imgproc.arcLength(temp,true);
            Imgproc.approxPolyDP(temp,approxCurve,0.01*peri,true);

            if (hier.get(0,i)[3] == -1 && approxCurve.toList().size() == 4)
                cardContours.add(contours.get(i));
        }

        Imgproc.drawContours(imageWithContours,cardContours, -1, new Scalar(0,255,0),15 );
        loadMat(imageWithContours);



        return cardContours;

    }

    /*public Card processCardsContours(List<MatOfPoint> cardContours){
        double peri;
        MatOfPoint2f approxCurve;
        Point[] conerPoints;
        Point center;
        Mat wrap;

        for (MatOfPoint cardContour:
             cardContours) {
            
        }
    }*/

    /**
     * Flattens an image of a card into a top-down 200x300 perspective.
     *     Returns the flattened, re-sized, grayed image.
     *     See www.pyimagesearch.com/2014/08/25/4-point-opencv-getperspective-transform-example/
     *     and https://github.com/EdjeElectronics/OpenCV-Playing-Card-Detector/blob/1f8365779f88f7f46634114bf2e35427bc1c00d0/Cards.py#L318
     */
    public Mat flattner (Mat image, List<Point> pts, double w,double h){
        Point[] rect = new Point[4];
        //we need to find [top left, top right, bottom right, bottom left] in that exact order.
        //showing the corner colors by this order: 0 = red, 1 = blue, 2 = pink, 3 = orange
        Point tl,tr,br,bl;
        int newWidth = 200, newHeight = 300;
        Comparator<Point> pointSumComparator = new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                return ((Double)(p1.y+p1.x)).compareTo(p2.y+p2.x);
            }
        };
        Comparator<Point> pointDiffComparator = new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                return ((Double)(p1.x-p1.y)).compareTo(p2.x-p2.y);
            }
        };
        tl = Collections.min(pts,pointSumComparator);
        br = Collections.max(pts,pointSumComparator);
        tr = Collections.min(pts,pointDiffComparator);
        bl = Collections.max(pts,pointDiffComparator);

        //If card is vertically oriented
        if (w<=0.8*h){
            rect[0] = tl;
            rect[1] = tr;
            rect[2] = br;
            rect[3] = bl;
        }

        //If card is horizontally oriented
        if (w>=1.2*h){
            rect[0] = bl;
            rect[1] = tl;
            rect[2] = tr;
            rect[3] = br;
        }

        //If card is diamond oriented.
        if (w>0.8*h && w<1.2*h) {
            //If furthest left point is higher than furthest right point,
            //card is tilted to the left.
            if (pts.get(1).y <= pts.get(3).y){
                //If card is titled to the left, approxPolyDP returns points
                //in this order: top right, top left, bottom left, bottom right
                rect[0] = pts.get(1);
                rect[1] = pts.get(0);
                rect[2] = pts.get(3);
                rect[3] = pts.get(2);
            }

            //If furthest left point is lower than furthest right point,
            //card is tilted to the right
            if (pts.get(1).y > pts.get(3).y){
                //If card is titled to the right, approxPolyDP returns points
                //in this order: top left, bottom left, bottom right, top right
                rect[0] = pts.get(0);
                rect[1] = pts.get(3);
                rect[2] = pts.get(2);
                rect[3] = pts.get(3);
            }

        }


        MatOfPoint2f tempRect=new MatOfPoint2f(rect);
        Mat cardImage = new Mat();
        MatOfPoint2f dst = new MatOfPoint2f(new Point(0,0)
                ,new Point(0,newHeight)
                ,new Point(newWidth,newHeight)
                ,new Point(newWidth,0));
        Mat M = Imgproc.getPerspectiveTransform(tempRect,dst);
        Imgproc.warpPerspective(image,cardImage,M,new Size(newWidth,newHeight));
        Imgproc.cvtColor(cardImage,cardImage,Imgproc.COLOR_BGR2GRAY);


        return cardImage;


    }

    public void greyImage(){
        Imgproc.cvtColor(picture, greyedPicture,Imgproc.COLOR_BGR2GRAY);
        loadMat(greyedPicture);
    }

    public void blurImage() {
        Imgproc.GaussianBlur(greyedPicture,blurredPicture,new Size(45,45),0);
        loadMat(blurredPicture);
    }
    //TODO: make the thresh value more adaptive.
    public void threshImage(){
        double[] bkgLevel = greyedPicture.get(greyedPicture.rows()/100,greyedPicture.cols()/2);
        System.out.println("number of items: " + bkgLevel.length);
        System.out.println(bkgLevel[0]);
        //Imgproc.adaptiveThreshold(blurredPicture,threshedPicture,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,25,2);
        Imgproc.threshold(blurredPicture,threshedPicture,bkgLevel[0] + 60,255,Imgproc.THRESH_BINARY);
        loadMat(threshedPicture);
    }

    public void loadMat(Mat mat){
        mainImgView.setImage(mat2Image(mat));
    }

    /**
     * Note: the code in this method is thanks to Luigi De Russis and/or Alberto Sacco
     *         // https://github.com/opencv-java/getting-started/tree/master/FXHelloCV
     *
     * really helpful!
     * @param frame
     * @return
     */
    public static Image mat2Image(Mat frame) {

        int width = frame.width(), height = frame.height(), channels = frame.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        frame.get(0, 0, sourcePixels);

        BufferedImage image = null;
        if (frame.channels() > 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return SwingFXUtils.toFXImage(image, null);
    }

}
