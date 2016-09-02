package ortigiaenterprises.platerecognizer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ortigiaenterprises.platerecognizer.sqlDb.MyDBHandler;

/**
 * Created by Iolanda on 25/07/2016.
 */
public class ModificaDbActivity extends AppCompatActivity {
   //resultdb dovrebbe essere passato come parametro al click del bottone Modifica

    private MyDBHandler dbHandler;
    private  EditText Operatoretext;
    private EditText Surveytext;
    private EditText LunghezzaMaxtext;
    private EditText LunghezzaMintext;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_db);
        dbHandler = MyDBHandler.getInstance(getBaseContext());
        Object[] parameters = dbHandler.getParameters();
        Operatoretext=(EditText) findViewById(R.id.operatoreId);
        Surveytext=(EditText) findViewById(R.id.surveyId);
        LunghezzaMaxtext=(EditText) findViewById(R.id.maxTarga);
        LunghezzaMintext=(EditText) findViewById(R.id.minTarga);

        String Operatore=(String) parameters[0];
        Operatoretext.setText(Operatore);
        String Survey=(String) parameters[1];
        Surveytext.setText(Survey);
        int LunghezzaMin= (int) parameters[2];
        LunghezzaMintext.setText(Integer.toString(LunghezzaMin));
        int LunghezzaMax= (int) parameters[3];
        LunghezzaMaxtext.setText(Integer.toString(LunghezzaMax));

        Button b2 = (Button) findViewById(R.id.button);
        b2.setOnClickListener(new View.OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                String newOperatore=Operatoretext.getText().toString();
                String newSurvey=Surveytext.getText().toString();
                int newmax=Integer.parseInt(LunghezzaMaxtext.getText().toString());
                int newmin=Integer.parseInt(LunghezzaMintext.getText().toString());

               dbHandler.updateParametri(newOperatore,newSurvey,newmin,newmax);
                Toast.makeText(getBaseContext(),"Modifiche effettuate con successo!", Toast.LENGTH_SHORT).show();

            }
        });


    }




}
