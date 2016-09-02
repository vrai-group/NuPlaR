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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ortigiaenterprises.platerecognizer.interfaces.viewInterface;

/**
 * Created by DionisioII on 20/07/2016.
 */
public class customDbAdapter extends ArrayAdapter<OcrRes> {

    OcrRes res;


    ArrayList<OcrRes> valori;
    private viewInterface delegate = null;
    private Context mContext;



    public customDbAdapter(Context context, ArrayList<OcrRes> ocrData) {
        super(context,R.layout.custom_raw_database, ocrData);
        this.mContext = context;

        valori = ocrData;



    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater poseInflater = LayoutInflater.from(getContext());
        View customView = poseInflater.inflate(R.layout.custom_raw_database, parent, false);


        ImageView bpImage = (ImageView)customView.findViewById(R.id.bpImageDb);
        TextView text = (TextView)customView.findViewById(R.id.textOcrDb);
        Button deleteButton = (Button) customView.findViewById(R.id.DeleteButton);
        ImageButton infoButton =(ImageButton) customView.findViewById(R.id.infoButton);


        final OcrRes res = getItem(position);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(res);
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delegate.Info(res.getText(),res.getDateTime());
            }
        });
        bpImage.setImageBitmap(res.getBp());
        text.setText(res.getText());



        return customView;

    }

    public void delete(OcrRes res){
        delegate.Delete(res);
        this.remove(res);
        this.notifyDataSetChanged();
        if(valori.size()==0)
            delegate.hideView();
    }

    public void inizializzaInterfaccia(viewInterface interfaccia){
        delegate = interfaccia;
    }


}
