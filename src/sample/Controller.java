package sample;



import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
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
import sample.models.Card;
import sample.models.CardsTable;

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
    List<Card> playingCards;
    CardsTable table;
    Mat frameWithCards;
    Mat frameWithProcessedCards;
    Image theImage;


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
        theImage = mat2Image(frame);

        mainImgView.setImage(theImage);
    }




    public void CardsButtonPressed(ActionEvent actionEvent) {
        long start, finish;
        start  = System.nanoTime();
        Mat processedFrame = processor.preProcessFrame(frame);
        cardContours = processor.findCardsContours(processedFrame);
        frameWithCards = processor.drawContours(cardContours,frame);
        loadMat(frameWithCards);
        finish = System.nanoTime();
        System.out.println("CardsButtonPressed time elapsed: " + (finish-start)/1000000 + " ms");
    }

    public void ProcessButtonPressed(ActionEvent actionEvent) {
        long start, finish;
        start  = System.nanoTime();
        playingCards = new ArrayList<>();
        List <MatOfPoint> unknownCards = new ArrayList<>();
        for (MatOfPoint cardContour: cardContours){
            Card card = processor.processCardContour(cardContour,frame,threshedSymbols);
            if (card.isCardComplete()) {
                playingCards.add(card);
            }
            else
                unknownCards.add(card.getContour());
        }

        frameWithProcessedCards = processor.drawContours(unknownCards,frameWithCards,new Scalar(0,0,255));
        loadMat(frameWithProcessedCards);
        finish = System.nanoTime();
        System.out.println("ProcessButtonPressed time elapsed: " + (finish-start)/1000000 + " ms");

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

    public void setsButtonPressed(ActionEvent actionEvent) {
        cardInfo.setText("");
        table = new CardsTable(playingCards);
        CardsTable.Set set = table.findSet();
        if (set == null)
            cardInfo.setText("No Set Found! :(");
        else loadMat(processor.drawContours(set.getContours(),frame, new Scalar(0,255,0)));
    }

    public void imageViewClicked(MouseEvent mouseEvent) {
        System.out.println(String.format("(%f,%f)", mouseEvent.getX(),mouseEvent.getY()));
        if (playingCards ==null) {
            cardInfo.setText("Cards not been processed yet!");

        }
        double aspectRatio = theImage.getWidth() / theImage.getHeight();
        double realWidth = Math.min(mainImgView.getFitWidth(), mainImgView.getFitHeight() * aspectRatio);
        double realHeight = Math.min(mainImgView.getFitHeight(), mainImgView.getFitWidth() / aspectRatio);
        Point p = new Point(frame.cols()/(realWidth/mouseEvent.getX())
                ,frame.rows()/(realHeight/mouseEvent.getY()));
        printClosestCard(p);

    }

    private void printClosestCard(Point p) {
        double minDistance = Double.MAX_VALUE;
        Card closestCard = null;
        double distance;
        for (Card c :
                playingCards) {
            distance = Math.sqrt((p.x - c.getCenter().x) + (p.y - c.getCenter().y));
            if (distance<minDistance){
                minDistance = distance;
                closestCard = c;
            }
        }

        cardInfo.setText(closestCard.toString());
    }
}
