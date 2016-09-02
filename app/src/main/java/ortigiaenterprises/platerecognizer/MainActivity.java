package ortigiaenterprises.platerecognizer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ortigiaenterprises.platerecognizer.common.GPSManager;
import ortigiaenterprises.platerecognizer.common.Utils;
import ortigiaenterprises.platerecognizer.interfaces.GPSCallback;
import ortigiaenterprises.platerecognizer.interfaces.OnTaskCompleted;
import ortigiaenterprises.platerecognizer.interfaces.viewInterface;
import ortigiaenterprises.platerecognizer.language.LanguageCodeHelper;
import ortigiaenterprises.platerecognizer.model.BitmapWithCentroid;
import ortigiaenterprises.platerecognizer.server.InterfacciaServer;
import ortigiaenterprises.platerecognizer.sqlDb.MyDBHandler;
import ortigiaenterprises.platerecognizer.view.CameraPreview;


public class MainActivity extends AppCompatActivity implements GPSCallback,OnTaskCompleted {
    private CameraPreview cameraPreview;
    private RelativeLayout layout;
    private PlateView plateView;
    private boolean cosoDaCambiare=false;

    Utils utils;
    public boolean isRunningTask = false;



    private TessBaseAPI baseApi;



    private static final String TAG = "MainActivity.java";

    List<Point> platePointList;
    //Da vedere

    TextView resultOCR;
    TextView gpsInfo;
    private View resultView;
    private View parametersVIew;
    private boolean listshow= false;
    private  String OPERATOR = "Operator000";
    private  String SURVEY = "Survey000";
    private int MINLENGHT = 7;
    private int MAXLENGHT = 7;

    protected int pixelMap[];
    protected Bitmap newBitmap;

    protected int downSampleLeft;
    protected int downSampleRight;
    protected int downSampleTop;
    protected int downSampleBottom;



    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;

    MatOfRect plates;

    private float mRelativePlateSize = 0.2f;
    private int mAbsolutePlateSize = 0;
    private GPSManager gpsManager = null;
    private double speed = 0.0;
    private double latitude = 0.0;
    private double longitude = 0.0;

    FloatingActionButton DBfab;
    FloatingActionButton fab;


    private int pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT_OSD;
    private int ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
    private String characterBlacklist="";
    private String characterWhitelist="ABCDEFGHILMNOPQRSTUVWYZ0123456789 ";


    private ProgressDialog dialog; // for initOcr - language download & unzip
    private ProgressDialog indeterminateDialog; // also for initOcr - init OCR engine

    private ArrayList<OcrRes> OcrResList;

    public List<Rect> currentPlates = new ArrayList<Rect>();

    private  boolean calculateOcr= false;

    private MyDBHandler DbHandler;

    private InterfacciaServer server;

    private int mInterval = 60000; // 30 seconds by default, can be changed later
    private Handler mHandler;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    try {
                        // Load Haar training result file from application resources
                        // This file from opencv_traincascade tool.
                        // Load res/cascade-europe.xml file
                        InputStream is = getResources().openRawResource(R.raw.europe);

                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "europe.xml"); // Load XML file according to R.raw.cascade
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(
                                mCascadeFile.getAbsolutePath());
                        mJavaDetector.load( mCascadeFile.getAbsolutePath() );
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from "
                                    + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                }
                break;
                case LoaderCallbackInterface.INIT_FAILED:
                {

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);


        Boolean checkOpenCV = OpenCVLoader.initAsync(
                OpenCVLoader.OPENCV_VERSION_3_1_0,
                getApplicationContext(),
                mLoaderCallback);
        cameraPreview = new CameraPreview(this, plateView);
        if(checkOpenCV)
        {
            try {
                layout = (RelativeLayout) findViewById(R.id.RelLayout);
                plateView = new PlateView(this);
                cameraPreview = new CameraPreview(this, plateView);
                layout.addView(cameraPreview, 1);
                layout.addView(plateView, 2);
                resultOCR = new TextView(getApplicationContext());
                //resultOCR.setText("Welcome to UIT-ANPR");

                //RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                  //      resultOCR.getLayoutParams());
                //lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                //lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                //lp.setMargins(0, 0, 0, 10);
                //resultOCR.setTextSize(30);
                //resultOCR.setBackgroundColor(Color.WHITE);
                //resultOCR.setTextColor(Color.RED);
                //resultOCR.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                //resultOCR.setLayoutParams(lp);

                gpsInfo = (TextView) findViewById(R.id.gpsviewtext);
                gpsInfo.bringToFront();


                //RelativeLayout.LayoutParams lpGPS = new RelativeLayout.LayoutParams(
                  //      gpsInfo.getLayoutParams());
                //lpGPS.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                //lpGPS.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                //lpGPS.setMargins(0, 0, 0, 10);
                //gpsInfo.setTextSize(15);
                //gpsInfo.setBackgroundColor(Color.WHITE);
                //gpsInfo.setTextColor(Color.RED);
                //gpsInfo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                //gpsInfo.setLayoutParams(lpGPS);
            } catch (Exception e1) {
                Log.e("MissingOpenCVManager",e1.toString());
            }

            gpsManager = new GPSManager();

            gpsManager.startListening(getApplicationContext());
            gpsManager.setGPSCallback(this);

            utils = new Utils(getBaseContext());
            platePointList = new ArrayList<Point>();


        }
        OcrResList = new ArrayList<OcrRes>();
        resultView = findViewById(R.id.resultView);
        parametersVIew = findViewById(R.id.parametersView);
        initOcrEngine();
        DbHandler = MyDBHandler.getInstance(this);
        initParameters();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickBtn();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                  //      .setAction("Action", null).show();
            }
        });

         DBfab = (FloatingActionButton) findViewById(R.id.DBfab);
        DBfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatabase();

                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //      .setAction("Action", null).show();
            }
        });

        server = InterfacciaServer.getInstance(this);
        mHandler = new Handler();


    }

    private void initParameters(){
        Object[] parameters = DbHandler.getParameters();
        OPERATOR =(String) parameters[0];
        SURVEY = (String) parameters[1];
        MINLENGHT=(int) parameters[2];
        MAXLENGHT = (int) parameters[3];

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,
                this, mLoaderCallback);

        if (cosoDaCambiare)
        {
            initParameters();
            showDatabase();
        }
        cosoDaCambiare=true;

    }

    public void onDestroy() {
        super.onDestroy();
    }

    // This class is for Viewing a detected plate
    public class PlateView extends View implements Camera.PreviewCallback,
            OnTaskCompleted {
        public static final int SUBSAMPLING_FACTOR = 4;
        public Rect[] platesArray;

        List<Point> currentPlatePointList = new ArrayList<Point>();


        public PlateView(MainActivity context) throws IOException {
            super(context);
        }

        public void onPreviewFrame(final byte[] data, final Camera camera) {

            try {
                Camera.Size size = camera.getParameters().getPreviewSize();

                // Get image from camera and process it to see
                // are there any plate on it ?
                if(!listshow){
                processImage(data, size.width, size.height);}
                camera.addCallbackBuffer(data);
            } catch (RuntimeException e) {
                // The camera has probably just been released, ignore.
                Log.e(TAG,e.toString());
            }
        }

        protected void processImage(byte[] data, int width, int height) {

            // First, downsample our image and convert it into a grayscale
            int f = SUBSAMPLING_FACTOR;
            mRgba = new Mat(height, width, CvType.CV_8UC4);
            mGray = new Mat(height, width, CvType.CV_8UC1);

            Mat mYuv = new Mat(height +height/2, width, CvType.CV_8UC1);
            mYuv.put(0, 0, data);

            Imgproc.cvtColor(mYuv, mGray, Imgproc.COLOR_YUV420sp2GRAY);
            Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV2RGB_NV21, 3);

            if (mAbsolutePlateSize == 0) {
                int heightGray = mGray.rows();
                if (Math.round(heightGray * mRelativePlateSize) > 0) {
                    mAbsolutePlateSize = Math.round(heightGray
                            * mRelativePlateSize);
                }
            }

            // This variable is used to to store the detected plates in the result
            plates = new MatOfRect();

            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(
                        mGray,
                        plates,
                        1.1,
                        2,
                        2,
                        new Size(mAbsolutePlateSize, mAbsolutePlateSize),
                        new Size()
                );

            postInvalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);

            paint.setTextSize(20);
            if (plates != null && !listshow) {
                paint.setStrokeWidth(5);
                paint.setStyle(Paint.Style.STROKE);

                platesArray = plates.toArray();



                currentPlates.clear();

                for (int i = 0; i < platesArray.length; i++) {
                    int x = platesArray[i].x;
                    int y = platesArray[i].y;
                    int w = platesArray[i].width + platesArray[i].width/2;
                    int h = platesArray[i].height;

                    // Draw a Green Rectangle surrounding the Number Plate !
                    // Congratulations ! You found the plate area :-)

                    canvas.drawRect(x, y, (x + w), (y + h), paint);





                    Point platePoint = new Point(platesArray[i].x,
                            platesArray[i].y);
                    currentPlatePointList.add(platePoint);
                    currentPlates.add(platesArray[i]);


                }



                if (platesArray.length > 0) {
                    platePointList.clear();
                    platePointList.addAll(currentPlatePointList);
                } else {
                    platePointList.clear();
                }

                // If isHasNewPlate --> get sub images (ROI) --> Add to Adapter
                // (from currentPlates)
                if ((calculateOcr) && !isRunningTask) {
                    Log.e(TAG, "START DoOCR task!!!!");
                    new DoOCR(currentPlates, mRgba, this).execute();
                }
            }else{
                postInvalidate();
                invalidate();}

        }

        public void updateResult(String result) {
			/*  This function is not completed yet
			    We will add the Neural network to recognize
			    the numbers, characters in the plate later
            */
            // TODO Auto-generated method stub
            resultOCR.setText(result);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

            String toFile = "Date: " + timeStamp + "\n";
            toFile += "Latitude: " + latitude + "\n";
            toFile += "Longitude: " + longitude + "\n";
            //toFile += "Speed: " + String.valueOf(utils.roundDecimal(utils.convertSpeed(speed), 2)) + "km/h" + "\n";
            toFile += "NP: " + result + "\n\n\n";
            if(result!=null && !TextUtils.isEmpty(result) && result!=""){
                FileWriter f;
                try{
                    f = new FileWriter(Environment.getExternalStorageDirectory() + "/UIT-ANPR.txt", true);
                    Log.e("DON", "BAT DAU GHI FILE 2222");
                    f.write(toFile);
                    f.flush();
                    f.close();
                }catch (Exception e) {
                    // TODO: handle exception
                }
            }

            //foundNumberPlate.setText(result);

        }



        public class DoOCR extends AsyncTask<Void, Bitmap, Boolean> {

            private List<Rect> currentPlatesOnAsy;
            private Mat originImageOnAsy;
            private OnTaskCompleted listener;

            public DoOCR(List<Rect> currentPlates, Mat originImage,
                         OnTaskCompleted listener) {
                this.currentPlatesOnAsy = new ArrayList<Rect>(currentPlates);
                this.originImageOnAsy = originImage;
                this.listener = listener;
            }

            @Override
            protected void onPreExecute() {
                isRunningTask = true;
                baseApi.setPageSegMode(pageSegmentationMode);
                baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, characterBlacklist);
                baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, characterWhitelist);
                //baseApi.readConfigFile();


            }

            @Override
            protected Boolean doInBackground(Void... params) {

                try {
                    Iterator<Rect> iterator = currentPlatesOnAsy.iterator();//per ogni rettangolo verde


                    while (iterator.hasNext()) {

                        Rect plateRect = iterator.next();//prendo il rettangolo su cui lavorare
                        OcrRes result = null;


                        if (baseApi.getPageSegMode() == TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR) {
                            Log.d("Single char", " ON");
                            result = performSingleCharOcrOnPlate(plateRect, originImageOnAsy);

                        } else {
                            Log.d("OCR on Whole Plate", baseApi.getPageSegMode() + "");
                            if (plateRect.width > 15 && plateRect.height > 15)
                                result = performOcrPlate(plateRect, originImageOnAsy);
                        }
                        result.setText(result.getText().replaceAll("\n", ""));
                        result.setText(result.getText().replaceAll(" ", ""));
                        result.setText(result.getText().replaceAll("-", ""));
                        Log.d("result", result.getText());
                        if (result != null && result.getText().length() >= MINLENGHT && result.getText().length() <= MAXLENGHT) {
                            OcrResList.add(result);
                            Log.d("message: ", result.getText() + " ");
                        }

                    }
                    if (OcrResList.size() > 0)
                        return true;
                    else
                        return false;

                }catch (Error e){
                    return false;
                }

            }

            @Override
            protected void onProgressUpdate(Bitmap... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(Boolean success) {

                isRunningTask = false;
                if (success){
                    calculateOcr=false;
                    showOcrResults(OcrResList);
                    Log.d("OCR:", " trovato qualcosa");
                }
                else
                {
                    Log.d("OCR:"," vuoto");
                }



            }

        }

    }



    /**
     * This method is called to automatically crop the image so that whitespace
     * is removed.
     *
     * @param w
     *            The width of the image.
     * @param h
     *            The height of the image
     */
    protected void findBounds(final int w, final int h) {
        // top line
        for (int y = 0; y < h; y++) {
            if (!hLineClear(y)) {
                this.downSampleTop = y;
                break;
            }

        }
        // bottom line
        for (int y = h - 1; y >= 0; y--) {
            if (!hLineClear(y)) {
                this.downSampleBottom = y;
                break;
            }
        }
        // left line
        for (int x = 0; x < w; x++) {
            if (!vLineClear(x)) {
                this.downSampleLeft = x;
                break;
            }
        }

        // right line
        for (int x = w - 1; x >= 0; x--) {
            if (!vLineClear(x)) {
                this.downSampleRight = x;
                break;
            }
        }
    }

    /**
     * This method is called internally to see if there are any pixels in the
     * given scan line. This method is used to perform autocropping.
     *
     * @param y
     *            The horizontal line to scan.
     * @return True if there were any pixels in this horizontal line.
     */
    protected boolean hLineClear(final int y) {
        final int w = this.newBitmap.getWidth();
        for (int i = 0; i < w; i++) {
            if (this.pixelMap[(y * w) + i] != -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is called to determine ....
     *
     * @param x
     *            The vertical line to scan.
     * @return True if there are any pixels in the specified vertical line.
     */
    protected boolean vLineClear(final int x) {
        final int w = this.newBitmap.getWidth();
        final int h = this.newBitmap.getHeight();
        for (int i = 0; i < h; i++) {
            if (this.pixelMap[(i * w) + x] != -1) {
                return false;
            }
        }
        return true;
    }

    protected OcrRes performSingleCharOcrOnPlate(Rect plateRect,Mat originImageOnAsy){

        BitmapWithCentroid tempBitmap;
        long start, timeRequired;
        start = System.currentTimeMillis();
        Mat plateImage;
        List<BitmapWithCentroid> charList = new ArrayList<BitmapWithCentroid>();

        int x = plateRect.x, y = plateRect.y, w = plateRect.width, h = plateRect.height;

        Rect roi = new Rect((int) (x), (int) (y), (int) (w),
                (int) (h));

        plateImage = new Mat(roi.size(), originImageOnAsy.type());

        plateImage = originImageOnAsy.submat(roi);//sottomatrice dell'immagine originale con le dimensioni di roi

        Mat plateImageResized = new Mat();

        Imgproc.resize(plateImage, plateImageResized, new Size(680,
                492));

        Mat plateImageGrey = new Mat();


        Imgproc.cvtColor(plateImageResized, plateImageGrey,
                Imgproc.COLOR_BGR2GRAY, 1);




        Imgproc.medianBlur(plateImageGrey, plateImageGrey, 3);
        //Imgproc.Canny(plateImageGrey, plateImageGrey, 1,3);
        Imgproc.adaptiveThreshold(plateImageGrey, plateImageGrey,
                255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 85, 5);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Mat hierarchy = new Mat(plateImageGrey.rows(),
                plateImageGrey.cols(), CvType.CV_8UC1,
                new Scalar(0));

        Imgproc.findContours(plateImageGrey, contours, hierarchy,
                Imgproc.CHAIN_APPROX_SIMPLE, Imgproc.RETR_LIST);


        timeRequired = System.currentTimeMillis() - start;
        Log.e(TAG, "Time for find countour: " + timeRequired);
        Log.e(TAG, "Start loop!!!" + contours.size());
        start = System.currentTimeMillis();


        for (int i = 0; i < contours.size(); i++) {
            List<Point> goodpoints = new ArrayList<Point>();
            Mat contour = contours.get(i);
            int num = (int) contour.total();
            int buff[] = new int[num * 2]; // [x1, y1, x2, y2, ...]
            contour.get(0, 0, buff);
            for (int q = 0; q < num * 2; q = q + 2) {
                goodpoints.add(new Point(buff[q], buff[q + 1]));
            }

            MatOfPoint points = new MatOfPoint();
            points.fromList(goodpoints);
            Rect boundingRect = Imgproc.boundingRect(points);

            if (((boundingRect.height / boundingRect.width) >= 1.5)
                    && ((boundingRect.height / boundingRect.width) <= 3.0)
                    && ((boundingRect.height * boundingRect.width) >= 5000)) {

                int cx = boundingRect.x + (boundingRect.width / 2);
                int cy = boundingRect.y + (boundingRect.height / 2);

                Point centroid = new Point(cx, cy);

                if (centroid.y >= 120 && centroid.y <= 400
                        && centroid.x >= 100 && centroid.x <= 590) {

                    int calWidth = (boundingRect.width + 5)
                            - (boundingRect.width + 5) % 4;

                    Rect cr = new Rect(boundingRect.x,
                            boundingRect.y, calWidth,
                            boundingRect.height);

                    Mat charImage = new Mat(
                            cr.size(),
                            plateImageResized.type());

                    charImage = plateImageResized.submat(cr); // sottoimmagine per il singolo carattere

                    Mat charImageGrey = new Mat(charImage.size(),
                            charImage.type());
                    Imgproc.cvtColor(charImage, charImageGrey,
                            Imgproc.COLOR_BGR2GRAY, 1);

                    Imgproc.adaptiveThreshold(charImageGrey,
                            charImageGrey, 255,
                            Imgproc.ADAPTIVE_THRESH_MEAN_C,
                            Imgproc.THRESH_BINARY, 85, 5);

                    Bitmap charImageBitmap = Bitmap.createBitmap(
                            charImageGrey.width(),
                            charImageGrey.height(),
                            Bitmap.Config.ARGB_8888);

                    org.opencv.android.Utils.matToBitmap(
                            charImageGrey, charImageBitmap);





                    tempBitmap = new BitmapWithCentroid(
                            charImageBitmap, centroid);
                    charList.add(tempBitmap);
                }
            }
            // }
        }

        timeRequired = System.currentTimeMillis() - start;
        Log.e(TAG, "Passed the loop");
        Log.e(TAG, "Time for OCR: " + timeRequired);

        start = System.currentTimeMillis();
        Collections.sort(charList);

        //SampleData data = new SampleData('?', DOWNSAMPLE_WIDTH,
        //      DOWNSAMPLE_HEIGHT);
        String recognizedText = "";
        Log.e("listachar: ", charList.size()+" ");

        for (int index = 0; index < charList.size(); index++) {
            newBitmap = charList.get(index).getBitmap();

            baseApi.setImage(ReadFile.readBitmap(newBitmap));
            recognizedText += baseApi.getUTF8Text();


            //recognizedText += baseApi.getUTF8Text();

            final int wi = newBitmap.getWidth();
            final int he = newBitmap.getHeight();

            pixelMap = new int[newBitmap.getHeight()
                    * newBitmap.getWidth()];
            newBitmap.getPixels(pixelMap, 0, newBitmap.getWidth(),
                    0, 0, newBitmap.getWidth(),
                    newBitmap.getHeight());


            findBounds(wi, he);
                        /*
                        ratioX = (double) (downSampleRight - downSampleLeft)
                                / (double) data.getWidth();
                        ratioY = (double) (downSampleBottom - downSampleTop)
                                / (double) data.getHeight();

                        for (int yy = 0; yy < data.getHeight(); yy++) {
                            for (int xx = 0; xx < data.getWidth(); xx++) {
                                if (downSampleRegion(xx, yy)) {
                                    data.setData(xx, yy, true);
                                } else {
                                    data.setData(xx, yy, false);
                                }
                            }
                        }

                        final double input[] = new double[20 * 50];
                        int idx = 0;
                        for (int yy = 0; yy < data.getHeight(); yy++) {
                            for (int xx = 0; xx < data.getWidth(); xx++) {
                                input[idx++] = data.getData(xx, yy) ? 0.5
                                        : -0.5;
                            }
                        }
                        */
            double normfac[] = new double[1];
            double synth[] = new double[1];

            //int best = net.winner(input, normfac, synth);

            //recognizedText += net.getMap()[best];
            Log.e(TAG, "Plate number:" + recognizedText);
        }

        //recognizedText = utils.formatPlateNumber(recognizedText);

        Log.e(TAG, "Plate number:" + recognizedText);
        timeRequired = System.currentTimeMillis() - start;
        Log.e(TAG, "Time: " + timeRequired);
        Bitmap bpResult = Bitmap.createBitmap(plateImage.width(),plateImage.height(),Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(
                plateImage, bpResult);

        return new OcrRes(bpResult,recognizedText,plateRect);
    }

    public OcrRes performOcrPlate(Rect plateRect, Mat originImageOnAsy){
        String recognizedText;
        long start, timeRequired;
        start = System.currentTimeMillis();
        Mat plateImage;


        int x = plateRect.x, y = plateRect.y, w = plateRect.width, h = plateRect.height;

        Rect roi = new Rect((int) (x), (int) (y), (int) (w),
                (int) (h));

        plateImage = new Mat(roi.size(), originImageOnAsy.type());

        plateImage = originImageOnAsy.submat(roi);//sottomatrice dell'immagine originale con le dimensioni di roi

        Mat plateImageResized = new Mat();

        Imgproc.resize(plateImage, plateImageResized, new Size(680,
                550));

        Mat plateImageGrey = new Mat();


        Imgproc.cvtColor(plateImageResized, plateImageGrey,
                Imgproc.COLOR_BGR2GRAY, 1);
        Imgproc.GaussianBlur(plateImageGrey, plateImageGrey, new Size(3, 3), 3);
        Imgproc.adaptiveThreshold(plateImageGrey, plateImageGrey, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 85, 5);

        ////ROXANA + IOlaz
        Mat immagineclone = plateImageGrey.clone();
        Imgproc.threshold(immagineclone, immagineclone, 0, 255, Imgproc.THRESH_OTSU+Imgproc.THRESH_BINARY);
        Mat element;
        //element=org.opencv.imgproc.Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(17,3));
        // org.opencv.imgproc.Imgproc.morphologyEx(plateImageGrey,plateImageGrey,MOP_CLOSE,element);
        int erosion_size = 5;
        element=org.opencv.imgproc.Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(2 * erosion_size + 1, 2 * erosion_size + 1),new Point(erosion_size, erosion_size) );
        org.opencv.imgproc.Imgproc.erode(immagineclone, immagineclone, element);

        //INIZIO FINDCONTOURS
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        // org.opencv.imgproc.Imgproc.findContours(immagineclone,contours,hierarchy,0,1);
        //ArrayList<MatOfPoint> contours_poly = new ArrayList<MatOfPoint>();
        //contours_poly.addAll(contours);
        //ArrayList<Rect> boundRect= new ArrayList<>();
        //MatOfPoint2f matofp1= new MatOfPoint2f();
        //MatOfPoint2f matofp2= new MatOfPoint2f();
        //NUOVA PARTE
        Mat mask=Mat.zeros(immagineclone.size(),CvType.CV_8UC1);
        org.opencv.imgproc.Imgproc.findContours(immagineclone,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE,new Point(0,0));
        Rect newrect=new Rect();
        for(int i=0;i <contours.size();i++) {
            if (contours.get(i).toList().size()>100) //&& contours.size()>i && contours_poly.size()>i && contours.get(i).toArray().length!=0 && contours_poly.get(i).toArray().length!=0)
            {
                //contours.get(i).convertTo(matofp1, CvType.CV_32FC2);
                //Imgproc.approxPolyDP(matofp1,matofp2, 3, true );
                //matofp2.convertTo(contours_poly.get(i), CvType.CV_32S);
                // Rect appRect=Imgproc.boundingRect(contours_poly.get(i));
                Rect appRect=Imgproc.boundingRect(contours.get(i));
                Mat maskROI=new Mat(mask,appRect);
                maskROI.setTo(new Scalar(0,0,0));
                Imgproc.drawContours(mask, contours,i, new Scalar(255, 255, 255), Core.FILLED);
                double r = (double)Core.countNonZero(maskROI)/(appRect.width*appRect.height);//numero pixel che non sono bianchi sull'immagine a cui applico la maschera
                if (r > .35 && (appRect.height > 8 && appRect.width > 8))
                {   if(appRect.area()>newrect.area())
                    newrect=appRect;
                    //Imgproc.rectangle(plateImageGrey, appRect.br() , new Point( appRect.br().x-appRect.width ,appRect.br().y-appRect.height),  new Scalar(255,0, 0));
                }

            }

        }


        Mat matrect=plateImageGrey.submat(newrect);
        Imgproc.threshold(matrect, matrect, 0, 255,Imgproc.THRESH_OTSU+Imgproc.THRESH_BINARY);

        Bitmap plateImageBitmap = Bitmap.createBitmap(
                matrect.width(),
                matrect.height(),
                Bitmap.Config.ARGB_8888);

        org.opencv.android.Utils.matToBitmap(
                matrect, plateImageBitmap);
        baseApi.setImage(ReadFile.readBitmap(plateImageBitmap));

        ////Roxana + Iolaz*/

        //Imgproc.threshold(plateImageGrey,plateImageGrey,0,255, Imgproc.Ada);


        /*Bitmap plateImageBitmap = Bitmap.createBitmap(
                plateImageGrey.width(),
                plateImageGrey.height(),
                Bitmap.Config.ARGB_8888);

        org.opencv.android.Utils.matToBitmap(
                plateImageGrey, plateImageBitmap);
        baseApi.setImage(ReadFile.readBitmap(plateImageBitmap));*/
        recognizedText = baseApi.getUTF8Text();
        Log.d("UTF8: ", baseApi.getUTF8Text() + " ");







        timeRequired = System.currentTimeMillis() - start;

        Log.e(TAG, "Time for OCR on Plate: " + timeRequired);

        OcrRes FinalResult = new OcrRes(plateImageBitmap,recognizedText,plateRect);
        FinalResult.setMeanConfidence(baseApi.meanConfidence());

        return FinalResult;

    }

    public void updateResult(String result) {
        //foundNumberPlate.setText(result);
        resultOCR.setText(result);
    }

    public void onGPSUpdate(Location location) {
        // TODO Auto-generated method stub
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        speed = location.getSpeed();

        //String speedString = String.valueOf(utils.roundDecimal(utils.convertSpeed(speed), 2));
        String unitString = "km/h";

        String gpsInfoText = "Lat: " + latitude + "\n";
        gpsInfoText += "Long: " + longitude + "\n";
        //gpsInfoText += "Speed: " + speedString + " " + unitString;

        gpsInfo.setText(gpsInfoText);
    }

    void initOcrEngine(){
        baseApi = new TessBaseAPI();





        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = new ProgressDialog(this);
        indeterminateDialog = new ProgressDialog(this);
        indeterminateDialog.setTitle("Please wait");

        String languageName = LanguageCodeHelper.getOcrLanguageName(this, "eng");

        File storageDirectory = getStorageDirectory();

        if(storageDirectory!= null){

            new OcrInitAsyncTask(this, baseApi, dialog, indeterminateDialog, "eng", languageName, TessBaseAPI.OEM_TESSERACT_ONLY)
                    .execute(storageDirectory.toString());
        }

    }

    private File getStorageDirectory() {
        //Log.d(TAG, "getStorageDirectory(): API level is " + Integer.valueOf(android.os.Build.VERSION.SDK_INT));

        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (RuntimeException e) {
            Log.e(TAG, "Is the SD card visible?", e);
            showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable.");
        }

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            // We can read and write the media
            //    	if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 7) {
            // For Android 2.2 and above

            try {
                return getExternalFilesDir(Environment.MEDIA_MOUNTED);
            } catch (NullPointerException e) {
                // We get an error here if the SD card is visible, but full
                Log.e(TAG, "External storage is unavailable");
                showErrorMessage("Error", "Required external storage (such as an SD card) is full or unavailable.");
            }

            //        } else {
            //          // For Android 2.1 and below, explicitly give the path as, for example,
            //          // "/mnt/sdcard/Android/data/edu.sfsu.cs.orange.ocr/files/"
            //          return new File(Environment.getExternalStorageDirectory().toString() + File.separator +
            //                  "Android" + File.separator + "data" + File.separator + getPackageName() +
            //                  File.separator + "files" + File.separator);
            //        }

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            Log.e(TAG, "External storage is read-only");
            showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable for data storage.");
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            // to know is we can neither read nor write
            Log.e(TAG, "External storage is unavailable");
            showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable or corrupted.");
        }
        return null;
    }

    void showErrorMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setOnCancelListener(new FinishListener(this))
                .setPositiveButton( "Done", new FinishListener(this))
                .show();
    }

    public void showOcrResults(ArrayList<OcrRes> aResults){
        DBfab.hide();
        fab.hide();

        OcrRes[] array =  aResults.toArray(new OcrRes[aResults.size()]);
        ArrayList<OcrRes> Lista = new ArrayList<OcrRes>(Arrays.asList(array));
        customAdapter ocrAdapter = new customAdapter(getApplicationContext(),Lista);

        ListView ocrListView = (ListView)findViewById(R.id.ocrListView);
        ocrListView.setAdapter(ocrAdapter);


        Log.d("poi", "rto");
        gpsInfo.setVisibility(View.GONE);
        plateView.setVisibility(View.GONE);
        resultView.setVisibility(View.VISIBLE);
        parametersVIew.setVisibility(View.GONE);
        resultView.bringToFront();
        listshow=true;

        ocrAdapter.inizializzaInterfaccia(new viewInterface() {
            @Override
            public void hideView() {
                onBackPressed();
            }

            @Override
            public void Save(OcrRes valueToSave) {
                valueToSave.setOperator(OPERATOR);
                valueToSave.setSurvey(SURVEY);
                valueToSave.setLatitude(latitude);
                valueToSave.setLongitude(longitude);
                valueToSave.setDateTime(findDateTime());

                DbHandler.insertTodb(valueToSave);
            }

            @Override
            public void Delete(OcrRes valueToDelete) {

            }

            @Override
            public void Info(String NumberPlate,String DateTime) {

            }
        });
    }

    private void showDatabase(){
        TextView operatorText = (TextView) findViewById(R.id.operatoreId);
        operatorText.setText(OPERATOR);
        TextView surveyText = (TextView) findViewById(R.id.surveyId);
        surveyText.setText(SURVEY);
        TextView minLengthText = (TextView) findViewById(R.id.minTarga);
        minLengthText.setText(Integer.toString(MINLENGHT));
        TextView maxLength =(TextView) findViewById(R.id.maxTarga);
        maxLength.setText(Integer.toString(MAXLENGHT));
        Button parametersButton = (Button) findViewById(R.id.button);
        parametersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), ModificaDbActivity.class);
                startActivity(i);
            }
        });

        Button sincronizzaButton = (Button) findViewById(R.id.sincronizza);


        final ArrayList<OcrRes> arrayList =  DbHandler.getDataFromDb();

        sincronizzaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                server.startRepeatingTask();
            }
        });

        if(arrayList.size()>0) {
            fab.hide();
            DBfab.hide();
            customDbAdapter dbAdapter = new customDbAdapter(getApplicationContext(), arrayList);
            ListView ocrListView = (ListView) findViewById(R.id.ocrListView);
            ocrListView.setAdapter(dbAdapter);



            dbAdapter.inizializzaInterfaccia(new viewInterface() {
                @Override
                public void hideView() {
                    onBackPressed();

                }

                @Override
                public void Save(OcrRes valueToSave) {

                }

                @Override
                public void Delete(OcrRes valueToDelete) {
                    DbHandler.deleteTodb(valueToDelete.getText(), valueToDelete.getDateTime());
                }

                @Override
                public void Info(String NumberPlate,String DateTime) {
                    Intent i = new Intent(getBaseContext(), InfoDbActivity.class);

                    i.putExtra("NumberPlate",NumberPlate);
                    i.putExtra("DateTime",DateTime);
                    startActivity(i);
                }
            });
        }else{
            Toast.makeText(getApplicationContext(),"database vuoto",Toast.LENGTH_SHORT).show();
        }
        gpsInfo.setVisibility(View.GONE);
        resultView.setVisibility(View.VISIBLE);
        plateView.setVisibility(View.GONE);
        parametersVIew.setVisibility(View.VISIBLE);
        resultView.bringToFront();

        listshow = true;

    }




    private String findDateTime(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return df.format(cal.getTime());
    }

    public void onClickBtn(){

        calculateOcr=true;

    }

    @Override
    public void onBackPressed() {

        if(listshow){
            resultView.setVisibility(View.GONE);
            gpsInfo.setVisibility(View.VISIBLE);
            plateView.setVisibility(View.VISIBLE);
            parametersVIew.setVisibility(View.GONE);
            OcrResList.clear();
            fab.show();
            DBfab.show();
            listshow=false;
        }else
            super.onBackPressed();

    }









}
