package sample;


import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;


import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Controller {

    @FXML
    private ImageView mainImgView;
    @FXML
    private Button loadButton,greyedButton,blurButton,threshButton;
    Mat picture = Imgcodecs.imread("D:/IntelliJ Workspace/JavaFxAndOpenCV/photos/20200221_161937.jpg");
    Mat greyedPicture=new Mat();
    Mat blurredPicture=new Mat();
    Mat threshedPicture=new Mat();







    public void buttonPressed() throws IOException {
        System.out.println("button pressed");

        Image theFuckingImage = mat2Image(picture);




        if (mainImgView == null)
            System.out.println("mainImgview is null");
        if (theFuckingImage == null)
            System.out.println("fuckin image is null");
        if (picture == null)
            System.out.println("mat is null");


        mainImgView.setImage(theFuckingImage);
        
        double[] pixel = greyedPicture.get(greyedPicture.rows()/2, greyedPicture.cols()/100);
        //System.out.println(greyedPicture.cols() + " * " + greyedPicture.rows());



    }

    public void greyImage(){
        Imgproc.cvtColor(picture, greyedPicture,Imgproc.COLOR_BGR2GRAY);
        loadMat(greyedPicture);
    }

    public void blurImage() {
        Imgproc.GaussianBlur(greyedPicture,blurredPicture,new Size(45,45),0);
        loadMat(blurredPicture);
    }

    public void threshImage(){
        double[] pixel = greyedPicture.get(greyedPicture.rows()/2, greyedPicture.cols()/100);
        //Imgproc.threshold(blurredPicture,threshedPicture,0, 255, Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(blurredPicture,threshedPicture,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,25,2);
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
