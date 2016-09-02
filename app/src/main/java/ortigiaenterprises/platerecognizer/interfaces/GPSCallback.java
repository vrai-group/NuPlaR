package ortigiaenterprises.platerecognizer.interfaces;

import android.location.Location;

/**
 * Created by DionisioII on 19/07/2016.
 */
public interface GPSCallback {
    public abstract void onGPSUpdate(Location location);
}
