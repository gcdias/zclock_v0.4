package pt.gu.zclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;

/**
 * Created by GU on 24-12-2014.
 */
public class PrefsFragment
        extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {

    private boolean       debug;
    private final String  TAG = "PrefsFragment";
    private Context       mContext;
    private SharedPreferences sharedPreferences;
    private zcLocation    mLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (debug) Log.e(TAG,"onCreate");

        mContext  = getActivity().getApplicationContext();
        mLocation = new zcLocation(mContext);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.zc_prefs);

        //Reset prefs to default click
        findPreference("resetDefault").setOnPreferenceClickListener(this);

        //Set Click Update Location
        findPreference("updateLocation").setOnPreferenceClickListener(this);

        //Set Click Update Location
        findPreference("removeAll").setOnPreferenceClickListener(this);

        //Update Location
        updateLocationInfo(findPreference("updateLocation"));
    }

    /*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context =getActivity().getApplicationContext();
        View view = super.onCreateView(inflater, container, savedInstanceState);
        Bitmap b = drawableToBitmap(WallpaperManager.getInstance(context).getFastDrawable());
        Drawable d = new BitmapDrawable(
                getResources(),
                BlurBuilder.blur(context,b, 0xa0cccccc, 0.2f, 13f));
        view.setBackground(d);
        return view;
    }
    */

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        if (debug) Log.e(TAG,"onPreferenceClick: "+ key);

        switch (key){
            case "resetDefault":
                setWidgetDefaultPreferences();
                break;
            case "updateLocation":
                updateLocationInfo(preference);
                break;
            case "removeAll":
                break;
        }
        return false;
    }

    private void setWidgetDefaultPreferences(){

        if (debug) Log.e(TAG,"setWidgetDefaultPreferences");

        SharedPreferences.Editor ed = sharedPreferences.edit();

        ed.putString("clockMode","0");

        ed.putString("szZmanim_sun","7");
        ed.putString("szZmanim_main","5");
        ed.putString("szZmanimAlotTzet","5");
        ed.putString("szTimemarks","4");
        ed.putString("szTime","90");
        ed.putString("szDate","26");
        ed.putString("szParshat","26");
        ed.putString("szShemot","144");

        ed.putString("tsZmanim_sun","HH:mm");
        ed.putString("tsZmanim_main","HH:mm");
        ed.putString("tsZmanimAlotTzet","HH:mm");
        ed.putString("tsTimemarks","HH");

        ed.putBoolean("iZmanim_sun",false);
        ed.putBoolean("iZmanim_main",false);
        ed.putBoolean("iZmanimAlotTzet",false);
        ed.putBoolean("iTimemarks",false);

        ed.putInt("cClockFrameOn", 0xff00c3ff);
        ed.putInt("cClockFrameOff", 0x1000c3ff);
        ed.putInt("cZmanim_sun", 0xff00c3ff);
        ed.putInt("cZmanim_main", 0xa00090ff);
        ed.putInt("cZmanimAlotTzet", 0x800090c0);
        ed.putInt("cTimemarks", 0x80ffffff);
        ed.putInt("cTime", 0xc3ffffff);
        ed.putInt("cDate", 0x80ffffff);
        ed.putInt("cParshat", 0x80ffffff);
        ed.putInt("cShemot", 0x07ffffff);

        ed.putString("wClockMargin","32");
        ed.putString("wClockFrame","7");
        ed.putString("wClockPointer","0.34");
        ed.putString("resTimeMins", "2");
        ed.putString("szTimeMins", "10");

        //ed.putBoolean("bAlotTzet72",true);
        ed.putBoolean("showHebDate", true);
        ed.putBoolean("showParashat", true);
        ed.putBoolean("showAnaBekoach", true);
        ed.putBoolean("show72Hashem", true);
        ed.putBoolean("showZmanim", true);
        ed.putBoolean("showTimeMarks", true);

        ed.putString("nShemot","20");

        ed.apply();
    }

    private void updateLocationInfo(Preference preference) {

        if (debug) Log.e(TAG,"updateLocationInfo: "+ preference.getKey());

        String summary = "no gps info";
        if (mLocation.lastUpdate!=0){
            summary = String.format("loc:%s lat:%f long:%f alt:%f",
                    mLocation.locName,
                    mLocation.latitude,
                    mLocation.longitude,
                    mLocation.elevation
            );
        }

        preference.setSummary(summary);
    }

    public static class BlurBuilder {
        private static final float BITMAP_SCALE = 0.2f;
        private static final float BLUR_RADIUS = 13f;

        public static Bitmap blur(View v, int color, float scale, float radius) {
            return blur(v.getContext(), getScreenshot(v),color, scale, radius);
        }

        public static Bitmap blur(Context ctx, Bitmap image, int color, float scale, float radius) {
            int width = Math.round(image.getWidth() * scale);
            int height = Math.round(image.getHeight() * scale);

            Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
            Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

            RenderScript rs = RenderScript.create(ctx);
            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
            Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
            blur.setRadius(radius);
            blur.setInput(tmpIn);
            blur.forEach(tmpOut);
            tmpOut.copyTo(outputBitmap);

            Canvas canvas = new Canvas(outputBitmap);
            canvas.drawColor(color);
            return outputBitmap;
        }


        private static Bitmap getScreenshot(View v) {
            Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            v.draw(c);
            return b;
        }
    }
    private Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
