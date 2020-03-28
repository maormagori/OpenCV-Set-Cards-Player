package sample;



import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.*;



import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Controller {

    @FXML
    private ImageView mainImgView, rightImage;
    @FXML
    private BorderPane borderpane;
    @FXML
    private Label cardInfo;

    private Stage stage;
    Mat frame;
    PhotoProcessing processor = new PhotoProcessing();
    Map<String, Mat> threshedSymbols;
    List<MatOfPoint> cardContours = new ArrayList<>();


    /**
     * when the program initializes we load into memory the ThreshedSymbols files for later use.
     * @throws IOException
     */
    public void initialize() throws IOException {
        loadThreshedSymbols();
    }

    public void LoadButtonPressed() {
        System.out.println("Load button pressed");
        cardInfo.setText("");
        stage =  (Stage) borderpane.getScene().getWindow();
        File image = new FileChooser().showOpenDialog(stage);
        if (image == null)
            return;
        String path = flipSlash(image.getAbsolutePath());
        frame = Imgcodecs.imread(path);
        Image theFuckingImage = mat2Image(frame);

        mainImgView.setImage(theFuckingImage);
    }




    public void CardsButtonPressed(ActionEvent actionEvent) {
        Mat processedFrame = processor.preProcessFrame(frame);
        cardContours = processor.findCardsContours(processedFrame);
        Mat frameWithCards = processor.drawContours(cardContours,frame);
        loadMat(frameWithCards);
    }

    public void ProcessButtonPressed(ActionEvent actionEvent) {
        List<Card> playingCards = new ArrayList<>();
        for (MatOfPoint cardContour: cardContours){
            playingCards.add(processor.processCardContour(cardContour,frame,threshedSymbols));
        }

        for (Card card: playingCards){
            cardInfo.setText(cardInfo.getText() + "\n\n" + card.toString());
        }

    }

    /**
     * The openCV library read path with a forward slash so we need to flip them.
     * @param absolutePath
     * @return String
     */
    private String flipSlash(String absolutePath) {
        String path= new String();
        for (char c :
                absolutePath.toCharArray()) {
            if (c=='\\')
                path+='/';
            else
                path+=c;
        }
        return  path;
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

    /**
     * loads Threshed symbols into threshedSymbols map.
     * @throws IOException
     */
    private void loadThreshedSymbols() throws IOException {
        threshedSymbols = new HashMap<String, Mat>();

        String programDir = new File(".").getCanonicalPath();
        System.out.println("Program dir: " + programDir);

        /*
          intilize threshed symbols for later use.
         */
        File symbolsDir = new File(programDir+ "\\photos\\ThreshedSymbols\\");
        File[] symbols = symbolsDir.listFiles();
        for (File symbol :
                symbols) {
            threshedSymbols.put(symbol.getName().split("[.]")[0]
                    ,Imgcodecs.imread(flipSlash(symbol.getAbsolutePath()),Imgcodecs.IMREAD_GRAYSCALE));
        }
    }
}
