package pt.gu.zclock;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.WindowManager;

import static pt.gu.zclock.zcPreferences.prefType.BOOLEAN;
import static pt.gu.zclock.zcPreferences.prefType.COLOR;
import static pt.gu.zclock.zcPreferences.prefType.INT;
import static pt.gu.zclock.zcPreferences.prefType.SIZE;
import static pt.gu.zclock.zcPreferences.prefType.STRING;

/**
 * Created by GU on 21-12-2014.
 */
public class zcPreferences extends Activity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    enum prefType{
        INT("integer"),
        FLOAT("dimen"),
        STRING("string"),
        BOOLEAN("bool"),
        COLOR("color"),
        SIZE("dimens");
        
        String id;
        prefType(String code){
            this.id = code;
        }
        int value(){
            return this.ordinal();
        }
    }

    public static final String ACTION_PREFS = "android.appwidget.action.APPWIDGET_CONFIGURE";
    private int                mAppWidgetId;
    private Context            mContext;
    private PrefsFragment      mPrefsFragment;
    private SharedPreferences  sharedPreferences;
    
    private final String       TAG = "zcPreferences";
    private boolean            debug = false;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (debug) Log.d(TAG,"onCreate");

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        this.mContext = getApplicationContext();
        this.sharedPreferences=PreferenceManager.getDefaultSharedPreferences(mContext);

        // Display the fragment as the main content.
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                .beginTransaction();
        mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();

    }

    @Override
    public void onStart(){
        super.onStart();
        if (debug) Log.d(TAG,"onStart");
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        
        if (debug) Log.d(TAG,"onResume: appWidgetId:"+mAppWidgetId);

        setResult(RESULT_CANCELED);
        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            if (debug) Log.d(TAG,"onResume: invalid AppWidgetId");
            //finish();
        }

        //Load preferences for selected mAppWidgetId
        loadWidgetPreferences(mAppWidgetId);

        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause(){
        // Unregister the listener whenever a key changes
        if (debug) Log.d(TAG,"onPause");
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onStop() {
        // Unregister the listener whenever a key changes
        if (debug) Log.d(TAG,"onStop");
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public void onDestroy(){
        //mContext = getApplicationContext();
        //mContext.stopService(new Intent(mContext,zcService.class));
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        if (debug) Log.d(TAG,"onBackPressed: id"+mAppWidgetId);
        Context context = getApplicationContext();
        saveWidgetPreferences(this, mAppWidgetId);

        Intent applySettings = new Intent(zcService.ZC_SETTINGSUPDATE);
        applySettings.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        context.sendBroadcast(applySettings);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        context.startService(new Intent(context, zcService.class));

        super.onBackPressed();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (debug) Log.d(TAG,"onSharedPreferenceChanged "+key);
        setSummary(key, null);
        /*
        //Todo
        Object o = sharedPreferences.contains(key)?sharedPreferences.getAll().get(key):null;
        if (o!=null){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (o instanceof String) editor.putString(key+mAppWidgetId,sharedPreferences.getString(key,""));
            if (o instanceof Boolean) editor.putBoolean(key+mAppWidgetId,sharedPreferences.getString(key,"true"));
        }
        */

    }

    public void loadWidgetPreferences(int appWidgetId) {

        loadPreference(INT,"clockMode",appWidgetId);
        loadPreference(INT,"zmanimMode",appWidgetId);
        loadPreference(INT,"resTimeMins",appWidgetId);
        loadPreference(INT,"szTimeMins",appWidgetId);
        loadPreference(INT,"szPtrHeight",appWidgetId);

        loadPreference(SIZE, "szWeatherFrame", appWidgetId);
        loadPreference(SIZE,"wClockMargin",appWidgetId);
        loadPreference(SIZE,"wClockFrame",appWidgetId);
        loadPreference(SIZE,"wClockPointer",appWidgetId);
        loadPreference(SIZE,"szZmanim_sun",appWidgetId);
        loadPreference(SIZE,"szZmanim_main",appWidgetId);
        loadPreference(SIZE,"szZmanimAlotTzet",appWidgetId);
        loadPreference(SIZE,"szTimemarks",appWidgetId);
        loadPreference(SIZE,"szTime",appWidgetId);
        loadPreference(SIZE,"szDate",appWidgetId);
        loadPreference(SIZE,"szParshat",appWidgetId);
        loadPreference(SIZE,"szShemot",appWidgetId);

        loadPreference(STRING, "tsZmanim_sun",appWidgetId);
        loadPreference(STRING, "tsZmanim_main",appWidgetId);
        loadPreference(STRING, "tsZmanimAlotTzet",appWidgetId);
        loadPreference(STRING, "tsTimemarks",appWidgetId);

        loadPreference(BOOLEAN,"iZmanim_sun",appWidgetId);
        loadPreference(BOOLEAN,"iZmanim_main",appWidgetId);
        loadPreference(BOOLEAN,"iZmanimAlotTzet",appWidgetId);
        loadPreference(BOOLEAN,"iTimemarks",appWidgetId);

        loadPreference(COLOR,"cClockFrameOn",appWidgetId);
        loadPreference(COLOR,"cClockFrameOff",appWidgetId);
        loadPreference(COLOR,"cZmanim_sun",appWidgetId);
        loadPreference(COLOR,"cZmanim_main",appWidgetId);
        loadPreference(COLOR,"cZmanimAlotTzet",appWidgetId);
        loadPreference(COLOR,"cTimemarks",appWidgetId);
        loadPreference(COLOR,"cTime",appWidgetId);
        loadPreference(COLOR,"cDate",appWidgetId);
        loadPreference(COLOR,"cParshat",appWidgetId);
        loadPreference(COLOR,"cShemot",appWidgetId);
        loadPreference(COLOR,"cWallpaper");

        //loadPreference(BOOLEAN,"bAlotTzet72",appWidgetId);
        loadPreference(BOOLEAN,"showWeather",appWidgetId);
        loadPreference(BOOLEAN,"showHebDate",appWidgetId);
        loadPreference(BOOLEAN,"showParashat",appWidgetId);
        loadPreference(BOOLEAN,"showAnaBekoach",appWidgetId);
        loadPreference(BOOLEAN,"show72Hashem",appWidgetId);
        loadPreference(BOOLEAN,"showZmanim",appWidgetId);
        loadPreference(BOOLEAN,"showTimeMarks",appWidgetId);
        loadPreference(BOOLEAN,"bLangHebrew",appWidgetId);
        loadPreference(BOOLEAN,"bClockElapsedTime",appWidgetId);
        loadPreference(BOOLEAN,"bWhiteOnBlack",appWidgetId);
        loadPreference(BOOLEAN,"wpOverlay",appWidgetId);

        loadPreference(INT,"nShemot",appWidgetId);
    }

    private void loadPreference(prefType type, String key, int appWidgetId){
        if (debug) Log.d(TAG,"loadPreference "+key);
        SharedPreferences.Editor ed =sharedPreferences.edit();
        Object value=null;
        int ResId = (type==SIZE) ? 0 : mContext.getResources().getIdentifier(key,type.id,mContext.getPackageName());
        switch (type){
            case INT:
                value = sharedPreferences.getInt(key + appWidgetId, mContext.getResources().getInteger(ResId));
                ed.putString(key,String.valueOf((int)value));
                break;
            case SIZE:
                value = sharedPreferences.getInt(key + appWidgetId,100);
                ed.putString(key,String.valueOf((int)value));
                break;
            case FLOAT:
                value =sharedPreferences.getFloat(key + appWidgetId, mContext.getResources().getDimension(ResId));
                ed.putString(key,String.valueOf((float)value));
                break;
            case STRING:
                value=sharedPreferences.getString(key + appWidgetId, mContext.getResources().getString(ResId));
                ed.putString(key,String.valueOf(value));
                break;
            case BOOLEAN:
                value =sharedPreferences.getBoolean(key + appWidgetId, mContext.getResources().getBoolean(ResId));
                ed.putBoolean(key, (boolean) value);
                break;
            case COLOR:
                value=sharedPreferences.getInt(key + appWidgetId, mContext.getResources().getColor(ResId));
                ed.putInt(key, (int)value);
                break;
        }
        ed.commit();
        setSummary(key,value);
    }

    private void loadPreference(prefType type, String key){
        if (debug) Log.d(TAG,"loadPreference "+key);
        SharedPreferences.Editor ed =sharedPreferences.edit();
        Object value=null;
        int ResId = (type==SIZE) ? 0 : mContext.getResources().getIdentifier(key,type.id,mContext.getPackageName());
        switch (type){
            case INT:
                value = sharedPreferences.getInt(key, mContext.getResources().getInteger(ResId));
                ed.putString(key,String.valueOf((int)value));
                break;
            case SIZE:
                value = sharedPreferences.getInt(key,100);
                ed.putString(key,String.valueOf((int)value));
                break;
            case FLOAT:
                value =sharedPreferences.getFloat(key, mContext.getResources().getDimension(ResId));
                ed.putString(key,String.valueOf((float)value));
                break;
            case STRING:
                value=sharedPreferences.getString(key, mContext.getResources().getString(ResId));
                ed.putString(key,String.valueOf(value));
                break;
            case BOOLEAN:
                value =sharedPreferences.getBoolean(key, mContext.getResources().getBoolean(ResId));
                ed.putBoolean(key, (boolean) value);
                break;
            case COLOR:
                value=sharedPreferences.getInt(key, mContext.getResources().getColor(ResId));
                ed.putInt(key, (int)value);
                break;
        }
        ed.commit();
        setSummary(key,value);
    }

    private void setSummary(String key,Object value){
        if (debug) Log.d(TAG,"setSummary "+key);
        Preference preference = mPrefsFragment.findPreference(key);
        if (preference!=null) {
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                if (value != null) listPreference.setValue(String.valueOf(value));
                listPreference.setSummary(String.valueOf(listPreference.getEntry()));
            }
            if (preference instanceof ColorPreference) {
                ColorPreference colorPreference = (ColorPreference) preference;
                if (value==null) value = colorPreference.getColor();
                Spannable summary = new SpannableString(String.format("Alpha:#%02X Color:#%06X", Color.alpha((int) value), 0xFFFFFF & (int) value));
                summary.setSpan(new ForegroundColorSpan((int) value), 0, summary.length(), 0);
                colorPreference.setSummary(summary);
                //if (updatePreference) colorPreference.setValue(value);
            }
            if (preference instanceof CheckBoxPreference) {
                CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
                if (value==null) value = checkBoxPreference.isChecked();
                checkBoxPreference.setSummary((boolean)value ? "On" : "Off");
                //if (updatePreference) checkBoxPreference.setChecked(b);
            }
        }
    }

    private void saveWidgetPreferences(Context ctx,int appWidgetId) {

        savePreference(INT,"clockMode",appWidgetId);
        savePreference(INT,"zmanimMode",appWidgetId);
        savePreference(INT,"resTimeMins",appWidgetId);
        savePreference(INT,"szTimeMins",appWidgetId);
        savePreference(SIZE,"wClockMargin",appWidgetId);
        savePreference(SIZE,"wClockFrame",appWidgetId);
        savePreference(SIZE,"wClockPointer",appWidgetId);
        savePreference(INT,"szPtrHeight",appWidgetId);

        savePreference(SIZE,"szWeatherFrame",appWidgetId);
        savePreference(SIZE,"szZmanim_sun",appWidgetId);
        savePreference(SIZE,"szZmanim_main",appWidgetId);
        savePreference(SIZE,"szZmanimAlotTzet",appWidgetId);
        savePreference(SIZE,"szTimemarks",appWidgetId);
        savePreference(SIZE,"szTime",appWidgetId);
        savePreference(SIZE,"szDate",appWidgetId);
        savePreference(SIZE,"szParshat",appWidgetId);
        savePreference(SIZE,"szShemot",appWidgetId);

        savePreference(STRING,"tsZmanim_sun",appWidgetId);
        savePreference(STRING,"tsZmanim_main",appWidgetId);
        savePreference(STRING,"tsZmanimAlotTzet",appWidgetId);
        savePreference(STRING,"tsTimemarks",appWidgetId);

        savePreference(BOOLEAN,"iZmanim_sun",appWidgetId);
        savePreference(BOOLEAN,"iZmanim_main",appWidgetId);
        savePreference(BOOLEAN,"iZmanimAlotTzet",appWidgetId);
        savePreference(BOOLEAN,"iTimemarks",appWidgetId);

        savePreference(COLOR,"cClockFrameOn",appWidgetId);
        savePreference(COLOR,"cClockFrameOff",appWidgetId);
        savePreference(COLOR,"cZmanim_sun",appWidgetId);
        savePreference(COLOR,"cZmanim_main",appWidgetId);
        savePreference(COLOR,"cZmanimAlotTzet",appWidgetId);
        savePreference(COLOR,"cTimemarks",appWidgetId);
        savePreference(COLOR,"cTime",appWidgetId);
        savePreference(COLOR,"cDate",appWidgetId);
        savePreference(COLOR,"cParshat",appWidgetId);
        savePreference(COLOR,"cShemot",appWidgetId);
        savePreference(COLOR,"cWallpaper");

        //savePreference(BOOLEAN,"bAlotTzet72",appWidgetId);
        savePreference(BOOLEAN,"showWeather",appWidgetId);
        savePreference(BOOLEAN,"showZmanim",appWidgetId);
        savePreference(BOOLEAN,"showTimeMarks",appWidgetId);
        savePreference(BOOLEAN,"showHebDate",appWidgetId);
        savePreference(BOOLEAN,"showParashat",appWidgetId);
        savePreference(BOOLEAN,"showAnaBekoach",appWidgetId);
        savePreference(BOOLEAN,"show72Hashem",appWidgetId);
        savePreference(BOOLEAN,"bLangHebrew",appWidgetId);
        savePreference( BOOLEAN, "bClockElapsedTime", appWidgetId);
        savePreference( BOOLEAN, "bWhiteOnBlack", appWidgetId);
        savePreference(BOOLEAN,"wpOverlay",appWidgetId);

        savePreference(INT,"nShemot",appWidgetId);

        saveColorTheme(sharedPreferences.getString("colorTheme", "#00C3FF"), appWidgetId);
    }

    private void savePreference(prefType type,String key, int appWidgetId){
        SharedPreferences.Editor ed =sharedPreferences.edit();
        int ResId = (type== SIZE) ? 0 : mContext.getResources().getIdentifier(key,type.id,mContext.getPackageName());
        switch (type){
            case INT:
                ed.putInt(key+appWidgetId,Integer.valueOf(
                        sharedPreferences.getString(key,String.valueOf(mContext.getResources().getInteger(ResId)))));
                break;
            case SIZE:
                ed.putInt(key+appWidgetId,Integer.valueOf(
                        sharedPreferences.getString(key,"100")));
                break;
            case FLOAT:
                ed.putFloat(key+appWidgetId,Float.valueOf(
                        sharedPreferences.getString(key,String.valueOf(mContext.getResources().getDimension(ResId)))));
                break;
            case STRING:
                ed.putString(key+appWidgetId,
                        sharedPreferences.getString(key,mContext.getResources().getString(ResId)));
                break;
            case BOOLEAN:
                ed.putBoolean(key+mAppWidgetId,
                        sharedPreferences.getBoolean(key,mContext.getResources().getBoolean(ResId)));
                break;
            case COLOR:
                ed.putInt(key+appWidgetId,
                        sharedPreferences.getInt(key,mContext.getResources().getColor(ResId)));
                break;
            default: break;
        }
        ed.apply();
    }

    private void savePreference(prefType type,String key){
        SharedPreferences.Editor ed =sharedPreferences.edit();
        int ResId = (type== SIZE) ? 0 : mContext.getResources().getIdentifier(key,type.id,mContext.getPackageName());
        switch (type){
            case INT:
                ed.putInt(key,Integer.valueOf(
                        sharedPreferences.getString(key,String.valueOf(mContext.getResources().getInteger(ResId)))));
                break;
            case SIZE:
                ed.putInt(key,Integer.valueOf(
                        sharedPreferences.getString(key,"100")));
                break;
            case FLOAT:
                ed.putFloat(key,Float.valueOf(
                        sharedPreferences.getString(key,String.valueOf(mContext.getResources().getDimension(ResId)))));
                break;
            case STRING:
                ed.putString(key,
                        sharedPreferences.getString(key,mContext.getResources().getString(ResId)));
                break;
            case BOOLEAN:
                ed.putBoolean(key+mAppWidgetId,
                        sharedPreferences.getBoolean(key,mContext.getResources().getBoolean(ResId)));
                break;
            case COLOR:
                ed.putInt(key,
                        sharedPreferences.getInt(key,mContext.getResources().getColor(ResId)));
                break;
            default: break;
        }
        ed.apply();
    }

    private void saveColorTheme(String colorTheme, int appWidgetId) {
        Log.d(TAG,"saveColorTheme "+colorTheme);
        String[] colorArray = colorTheme.split(",");
        if (colorArray.length<1) return;
        int[][] index ={
                {0,0,0,0,0,1,1,1,1,1},
                {0,0,1,1,1,2,2,2,2,2},
                {0,0,1,2,2,3,3,3,3,3},
                {0,0,1,2,3,4,4,4,4,4},
                {0,0,1,2,3,4,5,5,5,5},
                {0,0,1,2,3,4,5,5,5,6},
                {0,0,1,2,3,4,5,6,6,7},
                {0,0,1,2,3,4,5,6,7,8},
                {0,1,2,3,4,5,6,7,8,9}};
        int i = (colorArray.length - 2) % 9;
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putInt("cClockFrameOn" + appWidgetId, zcHelper.xColor.copyAlpha(sharedPreferences.getInt("cClockFrameOn",0),Color.parseColor(colorArray[index[i][0]])));
        ed.putInt("cClockFrameOff" + appWidgetId, zcHelper.xColor.copyAlpha(sharedPreferences.getInt("cClockFrameOff", 0), Color.parseColor(colorArray[index[i][1]])));
        ed.putInt("cZmanim_sun" + appWidgetId, zcHelper.xColor.copyAlpha(sharedPreferences.getInt("cZmanim_sun", 0), Color.parseColor(colorArray[index[i][2]])));
        ed.putInt("cZmanim_main" + appWidgetId, zcHelper.xColor.copyAlpha(sharedPreferences.getInt("cZmanim_main", 0), Color.parseColor(colorArray[index[i][3]])));
        ed.putInt("cZmanimAlotTzet" + appWidgetId, zcHelper.xColor.copyAlpha(sharedPreferences.getInt("cZmanimAlotTzet", 0), Color.parseColor(colorArray[index[i][4]])));
        ed.putInt("cTime" + appWidgetId, zcHelper.xColor.copyAlpha(sharedPreferences.getInt("cTime", 0), Color.parseColor(colorArray[index[i][5]])));
        ed.putInt("cTimemarks" + appWidgetId, zcHelper.xColor.copyAlpha(sharedPreferences.getInt("cTimemarks", 0), Color.parseColor(colorArray[index[i][6]])));
        ed.putInt("cParshat" + appWidgetId, zcHelper.xColor.copyAlpha(sharedPreferences.getInt("cParshat", 0), Color.parseColor(colorArray[index[i][7]])));
        ed.putInt("cDate" + appWidgetId, zcHelper.xColor.copyAlpha(sharedPreferences.getInt("cDate", 0), Color.parseColor(colorArray[index[i][8]])));
        ed.putInt("cShemot" + appWidgetId, zcHelper.xColor.copyAlpha(sharedPreferences.getInt("cShemot", 0), Color.parseColor(colorArray[index[i][9]])));
        ed.apply();
    }

}
