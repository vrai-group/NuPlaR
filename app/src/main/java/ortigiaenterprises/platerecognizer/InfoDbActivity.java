package ortigiaenterprises.platerecognizer;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import ortigiaenterprises.platerecognizer.sqlDb.MyDBHandler;

public class InfoDbActivity extends AppCompatActivity {
    private ImageView immagine;
    private OcrRes resultdb;
    private MyDBHandler dbHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_db);
        dbHandler = MyDBHandler.getInstance(getBaseContext());
        immagine=(ImageView) findViewById(R.id.bpImage);
        TextView OcrText=(TextView) findViewById(R.id.OcrText);
        TextView surveyText =(TextView) findViewById(R.id.Survey);
        TextView operatorText=(TextView) findViewById(R.id.Operatore);
        TextView confidenceText=(TextView) findViewById(R.id.Confidence);
        TextView dataText=(TextView) findViewById(R.id.Datetime);
        TextView latitudeText=(TextView) findViewById(R.id.Latitude);
        TextView longitudeText=(TextView) findViewById(R.id.Longitude);

        Bundle extras = getIntent().getExtras();
        String number = extras.getString("NumberPlate");

        String dateTime = extras.getString("DateTime");


        resultdb = dbHandler.getSingleOcrRes(number,dateTime);

        Bitmap image= resultdb.getBp();
        immagine.setImageBitmap(image);
        String text= resultdb.getText();
        OcrText.setText(text);
        String survey=resultdb.getSurvey();
        surveyText.setText(survey);
        String operatore=resultdb.getOperator();
        operatorText.setText(operatore);
        int confidence=resultdb.getMeanConfidence();
        confidenceText.setText(Integer.toString(confidence));
        String datatime=resultdb.getDateTime();
        dataText.setText(datatime);
        Double latitudine=resultdb.getLatitude();
        longitudeText.setText(Double.toString(latitudine));
        Double longitudine= resultdb.getLongitude();
        latitudeText.setText(Double.toString(longitudine));

    }

}
