package ortigiaenterprises.platerecognizer;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by DionisioII on 19/07/2016.
 */
public class CustomArrayAdapter extends ArrayAdapter<String> {

    private Typeface tf;
    private LayoutInflater inflater;
    private int resource;
    private String[] objects;

    public CustomArrayAdapter(Context context, int resource,
                              String[] objects, Typeface tf) {
        super(context, resource,objects);
        this.tf = tf;
        this.resource = resource;
        //this.textViewResourceId = textViewResourceId;
        this.objects = objects;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(resource, parent, false);
        }

        TextView text = (TextView) convertView.findViewById(R.id.text1);
        text.setText(objects[position]);
        text.setTypeface(tf);
        return convertView;
    }
}
