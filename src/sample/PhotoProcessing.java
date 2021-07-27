package sample;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import sample.models.Boundaries;
import sample.models.Card;

import java.util.*;

public class PhotoProcessing {

    private final int THRESH = 60;

    private final int CARD_WIDTH = 200;
    private final int CARD_HEIGHT = 300;

    final int SYMBOL_WIDTH = 140;
    final int SYMBOL_HEIGHT = 65;


    /**
     * Returns a greyed, blurred and threshed image.
     * TODO: Make threshing more adaptive.
     * @param frame
     * @return greyed, blurred and threshed frame.
     */
    public Mat preProcessFrame(Mat frame){
        //First we grey the frame;
        Mat greyedPicture = new Mat();
        Imgproc.cvtColor(frame, greyedPicture,Imgproc.COLOR_BGR2GRAY);

        //secondly we blur the frame to remove noise.
        Mat blurredPicture = new Mat();
        Imgproc.GaussianBlur(greyedPicture,blurredPicture,new Size(85,85),0);

        //Lastly we thresh the image and return the threshold of the frame;
        Mat threshedPicture = new Mat();

        //We sample one pixel from the background to know what's the background color.
        double[] bkgLevel = greyedPicture.get(greyedPicture.rows()/100,greyedPicture.cols()/2);
        System.out.println(bkgLevel[0]);
        Imgproc.threshold(blurredPicture,threshedPicture,bkgLevel[0] + THRESH,255,Imgproc.THRESH_BINARY);

        return threshedPicture;
    }

    /**
     * returns contours which meets the threshold conditions to be a card.
     * @param threshedFrame
     * @return List<MatOfPoint> cardContours
     */
    public List<MatOfPoint> findCardsContours(Mat threshedFrame) {
        //We define to contours list. One for all of the contours found in the frame
        //and a second one which will be only cards contours.
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> cardContours = new ArrayList<>();


        /*
          First we find all of the contours in the threshed picture and store them in a list.
          if there are no contours we don't do anything.
         */
        Mat hier = new Mat();
        Imgproc.findContours(threshedFrame, contours, hier,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE );
        System.out.println("Number of contours in the frame: " + contours.size());
        if (contours.size()==0)
            return null;

        /*
          Create a new indexes list and sort it by the area of each contour in the contours list.
          From largest to smallest. later on will be used to find contour hierarchy based on his index.
         */
        List<Integer> sort_index=new ArrayList<>();
        for (int i=0; i<contours.size();i++)
            sort_index.add(i);

        sort_index.sort((c1,c2) -> {
            if (Imgproc.contourArea(contours.get(c1)) == Imgproc.contourArea(contours.get(c2)) )
                return 0;
            return Imgproc.contourArea(contours.get(c1)) > Imgproc.contourArea(contours.get(c2)) ? -1:1;
        });

        //We add a contour to cardsContour if the contour has no parents and if he has 4 corners
        // (allowing 5 cornered cards for now).
        MatOfPoint2f temp = new MatOfPoint2f();
        MatOfPoint2f approxCurve = new MatOfPoint2f();

        for (Integer i :
                sort_index) {
            contours.get(i).convertTo(temp, CvType.CV_32F);
            double peri = Imgproc.arcLength(temp,true);
            Imgproc.approxPolyDP(temp,approxCurve,0.01*peri,true);

            //I'm allowing 5 corners for now. The problem is that the card's rounded corners are
            //seen as a line for approxPolyDP but for some a reason it's only one of the corners.
            //TODO: Fix the problem with the rounded corners.
            if (hier.get(0,i)[3] == -1 && (approxCurve.toList().size() == 4 || approxCurve.toList().size() == 5)) {
                System.out.println("Added a new card!");
                cardContours.add(contours.get(i));
            }
        }

        return cardContours;
    }

    /**
     * Returns the frame with the contours drawn in blue.
     * @param contours
     * @param frame
     * @return The frame with the contours drawn in blue
     */
    public Mat drawContours(List<MatOfPoint> contours,Mat frame){
        return drawContours(contours,frame,new Scalar(255,0,0));
    }

    public Mat drawContours(List<MatOfPoint> contours,Mat frame, Scalar color){
        Mat imageWithContours = frame.clone();
        Imgproc.drawContours(imageWithContours,contours, -1, color,15 );
        return imageWithContours;
    }

    /**
     * Takes a card's contour and returns a a playing cards object with the properties of the
     * card in the contour.
     * @param cardContour
     * @param frame
     * @param threshedSymbols
     * @return a Playing card.
     */
    public Card processCardContour(MatOfPoint cardContour, Mat frame, Map<String, Mat> threshedSymbols){
        double peri;
        MatOfPoint2f approxCurve = new MatOfPoint2f(), cardContour2f=new MatOfPoint2f();
        List<Point> cornerPoints;
        int width, height;
        Point center;
        Mat wrap;
        Card finalCard = new Card(cardContour);
        List<Mat> symbolsInCard;

        //We find the card's corners.
        cardContour.convertTo(cardContour2f,CvType.CV_32F);
        peri = Imgproc.arcLength(cardContour2f,true);
        Imgproc.approxPolyDP(cardContour2f,approxCurve,0.01*peri,true);
        cornerPoints = approxCurve.toList();

        //We find the height and width of the card contour
        width = Imgproc.boundingRect(cardContour).width;
        height = Imgproc.boundingRect(cardContour).height;

        //We find the card's center point using the average x and y of his corners.
        double xSum=0,ySum=0;
        for (Point p :
                cornerPoints) {
            xSum+=p.x;
            ySum+=p.y;
        }
        center = new Point(xSum/4,ySum/4);
        finalCard.setCenter(center);

        //We flatten the card's Image.
        wrap = flattner(frame,cornerPoints,width,height);

        //We find the game symbols in the card's image
        symbolsInCard = findSymbolsInCard(wrap);

        //At the moment we dont do anything if the card has more than 3 symbols.
        if (symbolsInCard.size()>3) {
            System.out.println("Too many symbols in a card!");
            finalCard.setAmount(0);
        }
        else
            finalCard.setAmount(symbolsInCard.size());

        if (symbolsInCard.size() != 0){
            String[] symbolAndFilling = matchCardSymbol(symbolsInCard.get(0), threshedSymbols);
            finalCard.setShape(symbolAndFilling[0]);
            finalCard.setFilling(symbolAndFilling[1]);
        }
        finalCard.setColor(matchCardColor(wrap));
        return finalCard;

    }

    /**
     * Flattens an image of a card into a top-down 200x300 perspective.
     *     Returns the flattened and re-sized  image.
     *     See www.pyimagesearch.com/2014/08/25/4-point-opencv-getperspective-transform-example/
     *     and https://github.com/EdjeElectronics/OpenCV-Playing-Card-Detector/blob/1f8365779f88f7f46634114bf2e35427bc1c00d0/Cards.py#L318
     */
    private Mat flattner (Mat image, List<Point> pts, double w,double h){
        Point[] rect = new Point[4];
        //we need to find [top left, top right, bottom right, bottom left] in that exact order.
        Point tl,tr,br,bl;

        //These comparators are used to compare the y's and x's of the corners.
        Comparator<Point> pointSumComparator = (p1, p2) -> ((Double)(p1.y+p1.x)).compareTo(p2.y+p2.x);
        Comparator<Point> pointDiffComparator = (p1, p2) -> ((Double)(p1.x-p1.y)).compareTo(p2.x-p2.y);

        //finding the corners using their x's and y's
        //to understand why it works like this please read: www.pyimagesearch.com/2014/08/25/4-point-opencv-getperspective-transform-example/
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

        //after finding the card's orientation we change his dimensions.
        MatOfPoint2f tempRect=new MatOfPoint2f(rect);
        Mat cardImage = new Mat();
        MatOfPoint2f dst = new MatOfPoint2f(new Point(0,0)
                ,new Point(0,CARD_HEIGHT)
                ,new Point(CARD_WIDTH,CARD_HEIGHT)
                ,new Point(CARD_WIDTH,0));
        Mat M = Imgproc.getPerspectiveTransform(tempRect,dst);
        Imgproc.warpPerspective(image,cardImage,M,new Size(CARD_WIDTH,CARD_HEIGHT));

        return cardImage;
    }

    /**
     * This is method is used to find The game symbols in the card.
     * @param wrap
     * @return The bounding rect of each symbol.
     */
    private List<Mat> findSymbolsInCard (Mat wrap){
        //We grey and thresh the card's image.
        Mat threshedCard = new Mat();
        Mat greyCard =new Mat();
        Imgproc.cvtColor(wrap,greyCard,Imgproc.COLOR_BGR2GRAY);
        double bkglevel = Math.max(wrap.get(10,200/2)[0],wrap.get(290,200/2)[0]);
        /*May use this method in the future.
         Imgproc.threshold(cardImage,threshedCard,1,255,Imgproc.THRESH_OTSU|Imgproc.THRESH_BINARY_INV);
         */
        Imgproc.threshold(greyCard,threshedCard,bkglevel-THRESH,255,Imgproc.THRESH_BINARY_INV);

        //Similar to findning cards contours we filter the symbols contours from all the contours found in the card.
        List<MatOfPoint> contoursInCard = new ArrayList();
        List<Mat> symbolsInCard = new ArrayList<>();
        Mat hierInCard = new Mat();
        Imgproc.findContours(threshedCard,contoursInCard,hierInCard,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0;i<contoursInCard.size();i++) {
            Rect boundingRect=Imgproc.boundingRect(contoursInCard.get(i));

            //As of right now a symbol is added if his contour has no parent and his width is larger than
            //half the card's width.
            if (hierInCard.get(0, i)[3] == -1 && boundingRect.width>=200/2)
                symbolsInCard.add(new Mat(threshedCard,boundingRect));
        }
        System.out.println("symbols in card: " + symbolsInCard.size());

        return symbolsInCard;
    }

    /**
     * Compares the first symbol in the card to the saved, threshed symbols and returns a string
     * array in which the first value is the shape and the second one is the filling.
     * @param symbol
     * @return
     */
    private String[] matchCardSymbol(Mat symbol, Map<String, Mat> threshedSymbols){
        String bestSymbolDiffName = "";
        long bestSymbolDiff = Integer.MAX_VALUE;
        long diff;
        Mat dst = new Mat();

        //After resizing the symbol to a fixed size, we compare it with all the saved symbols and
        //the symbol with the least diffrence is probably the card's symbol/
        Imgproc.resize(symbol, symbol,new Size(SYMBOL_WIDTH,SYMBOL_HEIGHT));
        for (Map.Entry<String, Mat> entry :
                threshedSymbols.entrySet()) {
            Core.absdiff(symbol,entry.getValue(),dst);
            diff = getWhitePixles(dst);
            if (diff<bestSymbolDiff){
                bestSymbolDiff = diff;
                bestSymbolDiffName = entry.getKey();
            }
        }

        //The symbols files are saved as "Shape_Filling"
        return bestSymbolDiffName.split("_");

    }

    /**
     * Checks the amount of red, green, purple in the card's pic and returns the color with max
     * amount of pixels in the pic.
     * @param wrap
     * @return The most apparent color in the card.
     */
    private String matchCardColor(Mat wrap){
        //The Mat format is bgr so we convert it to hsv to better find the colors.
        Mat hsv = new Mat();
        Imgproc.cvtColor(wrap,hsv,Imgproc.COLOR_BGR2HSV,0);

        //Defining the colors boundaries.
        Boundaries[] colors = {new Boundaries(Card.Color.GREEN),
                new Boundaries(Card.Color.PURPLE),
                new Boundaries(Card.Color.RED)};

        //Finding the color with the most pixels found.
        Mat coloredPixels = new Mat();
        int foundPixels;
        int maxPixels = 0;
        String color = "N/A";

        for (Boundaries b :
                colors) {
            Core.inRange(hsv,b.getLowerBoundary(),b.getUpperBoundary(),coloredPixels);
            foundPixels = getWhitePixles(coloredPixels);
            if (b.getColor() == Card.Color.RED){
                Core.inRange(hsv,new Scalar(0,100,20),new Scalar(5,255,255),coloredPixels);
                foundPixels+= getWhitePixles(coloredPixels);
            }
            if (foundPixels>maxPixels){
                maxPixels = foundPixels;
                color = b.getColor();
            }
        }
        System.out.println("Max pixels found: " + maxPixels);
        return color;
    }

    /**
     * given a one-channel Mat, returns the amount of white pixels.
     * @param mat
     * @return
     */
    private int getWhitePixles(Mat mat) {
        int whitePixels = 0;
        if (mat.channels()>1)
            System.out.println("Too many channels! cannot count the amount of white pixels!");
        for(int i=0; i<mat.size().height; i++)
            for (int j=0; j<mat.size().width; j++)
                if (mat.get(i,j)[0]==255)
                    whitePixels++;
        return whitePixels;
    }
}
