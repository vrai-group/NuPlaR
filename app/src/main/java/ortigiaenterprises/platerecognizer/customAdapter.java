package ortigiaenterprises.platerecognizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import ortigiaenterprises.platerecognizer.interfaces.viewInterface;

/**
 * Created by DionisioII on 19/07/2016.
 */
public class customAdapter extends ArrayAdapter<OcrRes> {


    Bitmap bp;
    String ocrText;

    ArrayList<OcrRes> valori;
    private viewInterface delegate = null;
    private Context mContext;




    public customAdapter(Context context, ArrayList<OcrRes> ocrResults) {
        super(context,R.layout.custom_raw_ocr, ocrResults);
        this.mContext = context;
        // this.tf = tf;
        //DB = MyDBHandler.getInstance(context);
        valori = ocrResults;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater poseInflater = LayoutInflater.from(getContext());
        View customView = poseInflater.inflate(R.layout.custom_raw_ocr, parent, false);
        final EditText text = (EditText)customView.findViewById(R.id.ocrText);
        final Spinner ocrSpinner = (Spinner)customView.findViewById(R.id.ocrSpinner);
        ImageView bpImage = (ImageView)customView.findViewById(R.id.bpImage);

        Button saveButton = (Button) customView.findViewById(R.id.SaveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DB.insertToDb(ocrText,bp);
                Toast.makeText(getContext(),"salva nel database", Toast.LENGTH_SHORT).show();
                metod(text.getText().toString());
            }
        });


        OcrRes res = getItem(position);





        String[] array =alternativeResults(res.getText());
        Typeface typeface= Typeface.createFromAsset(getContext().getAssets(), "font/arial.ttf");
        CustomArrayAdapter adapter = new CustomArrayAdapter(getContext(),R.layout.spinner_item, array,typeface);
        ocrSpinner.setAdapter(adapter);
        ocrSpinner.setSelection(0);
        ocrSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                text.setText(ocrSpinner.getSelectedItem().toString());
                text.setTextSize(27);
                Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "font/arial.ttf");
                text.setTypeface(typeface);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        ocrText=res.getText();
        bp=res.getBp();

        bpImage.setImageBitmap(res.getBp());

        return customView;

    }

    private void setSpinnerValue(Spinner spinner,String[] array, String value){
        int index = 0;

        for(int i =0; i<array.length;i++){
            if (value.equals(array[i])) index=i;
        }

        spinner.setSelection(index);

    }

    public void metod(String text){

        for(int i=0; i<valori.size();i++){
            if(ocrText.equals(valori.get(i).getText())){

                valori.get(i).setText(text);
                delegate.Save(valori.get(i));
                this.remove(valori.get(i));

                this.notifyDataSetChanged();
                if(valori.size()== 0)
                    delegate.hideView();

            }

        }

    }
    public String[] alternativeResults(String s) {
        ArrayList<String> results=new ArrayList<String>();
        results.add(s);

        if (s.length() == 7) {
            String inizio = s.substring(0, 2);
            String centro = s.substring(2, 5);
            String fine = s.substring(5);
            inizio = inizio.replaceAll("1", "I");
            inizio = inizio.replaceAll("2", "S");
            inizio = inizio.replaceAll("3", "B");
            inizio = inizio.replaceAll("4", "A");
            inizio = inizio.replaceAll("5", "S");
            inizio = inizio.replaceAll("6", "G");
            inizio = inizio.replaceAll("7", "T");
            inizio = inizio.replaceAll("8", "B");
            inizio = inizio.replaceAll("9", "O");
            inizio = inizio.replaceAll("0", "O");
            centro= centro.replaceAll("I","1");
            centro= centro.replaceAll("A","4");
            centro= centro.replaceAll("L","4");
            centro= centro.replaceAll("G","6");
            centro= centro.replaceAll("T","1");
            centro= centro.replaceAll("B","8");
            centro= centro.replaceAll("O","0");
            centro= centro.replaceAll("Q","0");
            centro=centro.replaceAll("D","0");
            centro= centro.replaceAll("U","0");
            centro= centro.replaceAll("Z","2");
            centro= centro.replaceAll("S","5");
            if(centro.contains("[A-Z]")==false){
                centro = centro.replaceAll("[A-Z]","9");
            }
            fine = fine.replaceAll("1", "I");

            fine = fine.replaceAll("2", "S");
            fine = fine.replaceAll("3", "B");
            fine = fine.replaceAll("4", "A");
            fine = fine.replaceAll("5", "S");
            fine = fine.replaceAll("6", "G");
            fine = fine.replaceAll("7", "T");
            fine = fine.replaceAll("8", "B");
            fine = fine.replaceAll("9", "O");
            fine = fine.replaceAll("0", "O");
            results.add(inizio.concat(centro).concat(fine));
            if(centro.contains("5"))
            {
                String newcentro=centro.replaceAll("5","3");
                results.add(inizio.concat(newcentro).concat(fine));
                newcentro= centro.replaceAll("5","6");
                results.add(inizio.concat(newcentro).concat(fine));
            }
            if(centro.contains("8"))
            {
                String newcentro=centro.replaceAll("8","6");
                results.add(inizio.concat(newcentro).concat(fine));
                newcentro=centro.replaceAll("8","3");
                results.add(inizio.concat(newcentro).concat(fine));

            }
            if(inizio.contains("I")||fine.contains("I"))
            {
                String newinizio=inizio.replaceAll("I","T");
                String newfine=fine.replaceAll("I","T");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("I","J");
                newfine=fine.replaceAll("I","J");
                results.add(newinizio.concat(centro).concat(newfine));
            }
            if(inizio.contains("T")||fine.contains("T"))
            {
                String newinizio=inizio.replaceAll("T","I");
                results.add(newinizio.concat(centro).concat(fine));
                String newfine=fine.replaceAll("T","I");
                results.add(inizio.concat(centro).concat(newfine));
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("T","J");
                newfine=fine.replaceAll("T","J");
                results.add(newinizio.concat(centro).concat(newfine));
            }

            if(inizio.contains("J")||fine.contains("J"))
            {
                String newinizio=inizio.replaceAll("J","I");
                String newfine=fine.replaceAll("J","I");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("J","T");
                newfine=fine.replaceAll("J","T");
                results.add(newinizio.concat(centro).concat(newfine));
            }

            if (inizio.contains("O")||fine.contains("O"))
            {
                String newinizio=inizio.replaceAll("O","Q");
                String newfine=fine.replaceAll("O","Q");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("O","U");
                newfine=fine.replaceAll("O","U");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("O","C");
                newfine=fine.replaceAll("O","C");
                results.add(newinizio.concat(centro).concat(newfine));
            }

            if (inizio.contains("Q")||fine.contains("Q"))
            {
                String newinizio=inizio.replaceAll("Q","O");
                String newfine=fine.replaceAll("Q","O");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("Q","U");
                newfine=fine.replaceAll("Q","U");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("Q","G");
                newfine=fine.replaceAll("Q","G");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("Q","C");
                newfine=fine.replaceAll("Q","C");
                results.add(newinizio.concat(centro).concat(newfine));
            }

            if (inizio.contains("U")||fine.contains("U"))
            {
                String newinizio=inizio.replaceAll("U","Q");
                String newfine=fine.replaceAll("U","Q");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("U","O");
                newfine=fine.replaceAll("U","O");
                results.add(newinizio.concat(centro).concat(newfine));
            }

            if (inizio.contains("F")||fine.contains("F"))
            {
                String newinizio=inizio.replaceAll("F","E");
                String newfine=fine.replaceAll("F","E");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("F","H");
                newfine=fine.replaceAll("F","H");
                results.add(newinizio.concat(centro).concat(newfine));
            }

            if (inizio.contains("E")||fine.contains("E"))
            {
                String newinizio=inizio.replaceAll("E","F");
                String newfine=fine.replaceAll("E","F");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("E","H");
                newfine=fine.replaceAll("E","H");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("E","G");
                newfine=fine.replaceAll("E","G");
                results.add(newinizio.concat(centro).concat(newfine));
            }


            if (inizio.contains("H")||fine.contains("H"))
            {
                String newinizio=inizio.replaceAll("H","E");
                String newfine=fine.replaceAll("H","E");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("H","F");
                newfine=fine.replaceAll("H","F");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("H","M");
                newfine=fine.replaceAll("H","M");
                results.add(newinizio.concat(centro).concat(newfine));
            }

            if (inizio.contains("G")||fine.contains("G"))
            {
                String newinizio=inizio.replaceAll("G","E");
                String newfine=fine.replaceAll("G","E");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("G","Q");
                newfine=fine.replaceAll("G","Q");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("G","C");
                newfine=fine.replaceAll("G","C");
                results.add(newinizio.concat(centro).concat(newfine));
            }

            if (inizio.contains("C")||fine.contains("C"))
            {
                String newinizio=inizio.replaceAll("C","G");
                String newfine=fine.replaceAll("C","G");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("C","Q");
                newfine=fine.replaceAll("C","Q");
                results.add(newinizio.concat(centro).concat(newfine));
                newinizio=inizio.replaceAll("C","O");
                newfine=fine.replaceAll("C","O");
                results.add(newinizio.concat(centro).concat(newfine));
            }


        }
        return results.toArray(new String[results.size()]);
    }



    public void inizializzaInterfaccia(viewInterface interfaccia){
        delegate = interfaccia;
    }
}