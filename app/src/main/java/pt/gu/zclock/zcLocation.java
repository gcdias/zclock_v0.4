package pt.gu.zclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;

import net.sourceforge.zmanim.util.GeoLocation;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by GU on 23-04-2015.
 */
public class zcLocation {

    public double   latitude    = 0;
    public double   longitude   = 0;
    public double   elevation   = 0;
    public long     lastUpdate  = 0;
    public String   locName     = "-";

    private Context mContext;
    private String  provider;

    public zcLocation(Context context){
        this.mContext = context.getApplicationContext();
        this.update();
    }

    public boolean update() {

        boolean update = false;
        LocationManager lm = (LocationManager) mContext.getSystemService(
                Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        Location l = null;

        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) {
                provider = providers.get(i);
                break;
            }
        }

        if (l != null) {
            this.latitude = l.getLatitude();
            this.longitude = l.getLongitude();
            this.elevation = l.getAltitude();
            this.lastUpdate = System.currentTimeMillis();
            this.locName = geoLocation().getLocationName();
            update = true;
        }

        saveToSharedPreferences();

        return update;
    }

    private void saveToSharedPreferences(){
        SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        ed.putFloat("latitude",(float)this.latitude);
        ed.putFloat("longitude",(float)this.longitude);
        ed.putFloat("elevation",(float)this.elevation);
        ed.putString("locName",this.locName);
        ed.putLong("lastLocationUpdate",this.lastUpdate);

        ed.commit();
    }

    public GeoLocation geoLocation(){
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(this.latitude, this.longitude, 1);
            if (null != listAddresses && listAddresses.size() > 0) {
                this.locName = listAddresses.get(0).getLocality()+","+listAddresses.get(0).getCountryCode();
            }
        } catch (IOException ignored) {}
        return new GeoLocation(this.locName,
                this.latitude, this.longitude, this.elevation, TimeZone.getDefault());
    }
}
