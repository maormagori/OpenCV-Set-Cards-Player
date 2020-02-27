package sample;



import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;


import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Controller {

    @FXML
    private ImageView mainImgView;
    Mat picture = Imgcodecs.imread("D:/IntelliJ Workspace/JavaFxAndOpenCV/photos/20200221_161937.jpg");
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
        //Imgproc.adaptiveThreshold(blurredPicture,threshedPicture,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,25,2);
        Imgproc.threshold(blurredPicture,threshedPicture,150,255,Imgproc.THRESH_BINARY);
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
