package ortigiaenterprises.platerecognizer.server;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import ortigiaenterprises.platerecognizer.OcrRes;
import ortigiaenterprises.platerecognizer.interfaces.AsyncResponse;
import ortigiaenterprises.platerecognizer.sqlDb.MyDBHandler;

/**
 * Created by DionisioII on 31/07/2016.
 */
public class InterfacciaServer {
    private static InterfacciaServer interfacciaServer = null;
    private static String serverProtocol = "http";
    //private static String serverIP="192.168.42.160";
    private static String serverIP = "172.23.166.217";
    private static int serverPort=8000;
    private static String serverAddress=serverProtocol+"://"+serverIP+":"+serverPort+"/";


    private static String sincronizzaEndpoint = "sincronizza";

    private Context mContext;
    private MyDBHandler DB;

    private boolean synchronizing = false;

    private int mInterval = 60000; // 30 seconds by default, can be changed later
    private Handler mHandler;




    public InterfacciaServer(Context context)
    {
        this.mContext = context;
        DB = MyDBHandler.getInstance(context);
        mHandler = new Handler();
    }

    public static InterfacciaServer getInstance(Context context)
    {
        if (interfacciaServer == null)
            interfacciaServer = new InterfacciaServer(context.getApplicationContext());

        return interfacciaServer;
    }


    public boolean checkConnection()
    {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;

        //Toast.makeText(mContext, "Connessione non disponibile.",
        //Toast.LENGTH_SHORT).show();
        return false;
    }


    public void sincronizzaServer(final ArrayList<OcrRes> OcrList)
    {
            Log.d("start async","vchgdh");
            synchronizing = true;
            JSONObject json = new JSONObject();

            JSONArray encodedImages = new JSONArray();
            JSONArray texts = new JSONArray();
            JSONArray meanConfidences = new JSONArray();
            JSONArray latitudes = new JSONArray();
            JSONArray longitudes = new JSONArray();
            JSONArray datetimes = new JSONArray();
            JSONArray surveys = new JSONArray();
            JSONArray operators = new JSONArray();

            try {


                for (int i = 0; i < OcrList.size(); i++) {
                    encodedImages.put(i,encodeBitmaptoString(OcrList.get(i).getBp()));
                    texts.put(i,OcrList.get(i).getText());
                    meanConfidences.put(i,OcrList.get(i).getMeanConfidence());
                    latitudes.put(i,OcrList.get(i).getLatitude());
                    longitudes.put(i,OcrList.get(i).getLongitude());
                    datetimes.put(i,OcrList.get(i).getDateTime()) ;
                    surveys.put(i,OcrList.get(i).getSurvey());
                    operators.put(i,OcrList.get(i).getOperator());

                }

                json.put(DB.TAG_OCR_BITMAP, encodedImages);
                json.put(DB.TAG_OCR_TEXT, texts);
                json.put(DB.TAG_CONFIDENCE, meanConfidences);
                json.put(DB.TAG_LATITUDE, latitudes);
                json.put(DB.TAG_LONGITUDE, longitudes);
                json.put(DB.TAG_DATATIME, datetimes);
                json.put(DB.TAG_SURVEY, surveys);
                json.put(DB.TAG_OPERATOR, operators);

                //json.put("token",token);
                Toast.makeText(mContext, "json da passare al server: " + json.get("ocrtext")+" ", Toast.LENGTH_SHORT).show();
                Log.d("json al server: ", json.getString("ocrconfidence") + " ");
                if (checkConnection()) {
                    ServerTask asyncTask = new ServerTask(new AsyncResponse() {
                        @Override
                        public void processFinish(String output) {
                           try {
                                    Log.d("output", output +" ");
                                    JSONObject response = new JSONObject(output);
                                    if (response.get("stato").equals("OK")){
                                        stopRepeatingTask();
                                        synchronizing=false;
                                        Toast.makeText(mContext,"dati sincronizzati",Toast.LENGTH_SHORT).show();
                                    }

                                }
                                catch (JSONException e){
                                    e.printStackTrace();

                                }


                            }
                    });
                    String contenutoPost = json.toString();
                    asyncTask.execute(serverAddress + sincronizzaEndpoint, contenutoPost);



                } else {

                    Toast.makeText(mContext, "Connessione non disponibile.", Toast.LENGTH_SHORT).show();

                }

            } catch (JSONException e) {
                e.printStackTrace();

            }


    }


    private String encodeBitmaptoString(Bitmap bm){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    //////SERVER SECTION////////////////////////////////
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {

                sincronizzaServer(DB.getDataFromDb());

            } finally {

                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    public void startRepeatingTask() {
        if(!synchronizing)
        mStatusChecker.run();
    }

    public void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }








}
