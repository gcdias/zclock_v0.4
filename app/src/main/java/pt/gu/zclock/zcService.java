package pt.gu.zclock;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import net.sourceforge.zmanim.AstronomicalCalendar;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import pt.gu.zclock.zcHelper.hebString;

/**
 * Created by GU on 26-04-2015.
 */
public class zcService extends Service{

    private final String       TAG   = "zcService";
    private boolean            debug = false;

    public static final String ZC_SETTINGSUPDATE = "pt.gu.zclock.service";
    public static final String ZC_FORECASTUPDATE  = "pt.gu.zclock.updforecast";
    public static final String ZC_GETFORECAST    = "pt.gu.zclock.getforecast";
    public static final String ZC_LOCATIONUPDATE = "pt.gu.zclock.location";

    private final int    HASHEM_72       = 0;
    private final int    HASHEM_72_SOF   = 1;
    private final int    HASHEM_42       = 2;
    private final String[] romanNumb     = {"I","II","III","IV","V","VI","VII","VIII","IX","X","XI","XII","XIII","XIV","XV","XVI"};
    private final String[] omerSefirot   = {"Chesed","Gevurah","Tiferet","Netzach","Hod","Yesod","Malchut"};
    private final GeoLocation HarHabait  = new GeoLocation("Har Habait", 31.777972f, 35.235806f, 743, TimeZone.getTimeZone("Asia/Jerusalem"));


    private HebrewDateFormatter hebrewFormat;{
        hebrewFormat = new HebrewDateFormatter();
        hebrewFormat.setTransliteratedMonthList(new String[]{"Nissan", "Iyar", "Sivan", "Tamuz", "Av", "Elul", "Tishri", "Mar Cheshvan",
                "Kislev", "Tevet", "Shevat", "Adar", "Adar II", "Adar I" });
        hebrewFormat.setTransliteratedParshiosList(new String[]{
                "Bereshit", "Noach", "Lech-Lecha", "Vayera", "Chaye-Sarah", "Toldot","Vayetzei", "Vayishlach", "Vayeshev", "Miketz", "Vayigash", "Vayechi",
                "Shemot", "Vaera", "Bo", "Beshalach", "Yitro", "Mishpatim", "Terumah", "Tetzaveh", "Ki Tisa", "Vayakchel", "Pekude",
                "Vayikra", "Tzav", "Shemini", "Tazria", "Metzora", "Achre Mot", "Kedoshim", "Emor", "Behar", "Bechukotai",
                "Bamidbar", "Naso", "Behaalotcha", "Shelach", "Korach", "Chukat", "Balak", "Pinchas", "Matot", "Masei",
                "Devarim", "Vaetchanan", "Ekev", "Reeh", "Shoftim", "Ki Tetze", "Ki Tavo", "Nitzavim", "Vayelech", "HaAzinu",
                "Vayakchel Pekude", "Tazria Metzora", "Achre Mot Kedoshim", "Behar Bechukotai", "Chukat Balak",
                "Matot Masei", "Nitzavim Vayelech"});
        hebrewFormat.setTransliteratedHolidayList(new String[]{ "Erev Pesach", "Pesach", "Chol Hamoed Pesach", "Pesach Sheni",
                "Erev Shavuot", "Shavuot", "17 Tammuz", "Tishah B'Av", "Tu B'Av", "Erev Rosh Hashana",
                "Rosh Hashana", "Jejum de Gedalyah", "Erev Yom Kippur", "Yom Kippur", "Erev Sukot", "Sukot",
                "Chol Hamoed Sukot", "Hoshana Rabbah", "Shemini Atzeret", "Simchat Torah", "Erev Chanukah", "Chanukah",
                "10 Tevet", "Tu B'Shvat", "Jejum de Ester", "Purim", "Shushan Purim", "Purim Katan", "Rosh Chodesh",
                "Yom HaShoah", "Yom Hazikaron", "Yom Ha'atzmaut", "Yom Yerushalayim" });
    }
    private Date alotHarHabait = new ComplexZmanimCalendar(HarHabait).getAlos72();

    private zClock                  mClock;
    private zcWeather               mWeather;
    private zcLocation              mLocation;
    private SharedPreferences mPrefs;
    private Context                 mContext;
    private ComplexZmanimCalendar   zCalendar;
    private JewishCalendar          jCalendar;

    private Drawable                mWallpaper;

    private zcHelper.WeatherData[]  weatherForecast;


    private final long              locationUpdateTimeout = 3*60*60*1000;
    private final long              forecastUpdateTimeout = 3*60*60*1000;
    private final long              zmanimUpdateTimeout   = 24*60*60*1000;

    private boolean            pmScreenOn = true;

    private IntentFilter intentFilter;{
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(ZC_FORECASTUPDATE);
        intentFilter.addAction(ZC_LOCATIONUPDATE);
        intentFilter.addAction(ZC_SETTINGSUPDATE);
        intentFilter.addAction(ZC_GETFORECAST);
    }

    private BroadcastReceiver  intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            ComponentName widgets = new ComponentName(context, zcProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(context);

            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                if (debug) Log.d(TAG + ".onReceive", "Screen OFF, suspending...");
                pmScreenOn = false;
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                if (debug) Log.d(TAG + ".onReceive", "Screen ON: updating");
                pmScreenOn = true;
                updateWidgets(context, manager, manager.getAppWidgetIds(widgets));
            }

            if (pmScreenOn) {
                if (debug) Log.d(TAG + ".onReceive", "Screen on");
                if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                    checkLocation();
                    if (debug) Log.d(TAG + ".onReceive", "Time/Timezone changed");
                    updateWidgets(context, manager, manager.getAppWidgetIds(widgets));
                }

                if (action.equals(Intent.ACTION_TIME_TICK)) {
                    if (debug) Log.d(TAG + ".onReceive", "Time-tick");
                    checkLocation();
                    checkForecast();
                    updateWidgets(context, manager, manager.getAppWidgetIds(widgets));
                }

                if (action.equals(ZC_SETTINGSUPDATE)) {
                    if (debug) Log.d(TAG + ".onReceive", "ZC_SETTINGSUPDATE Intent");
                    int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
                    if (id!=AppWidgetManager.INVALID_APPWIDGET_ID) resetClock(id);
                    updateWidgets(context, manager, manager.getAppWidgetIds(widgets));
                }

                if (action.equals(ZC_GETFORECAST)) {
                    if (debug) Log.d(TAG + ".onReceive", "ZC_GETFORECAST Intent");
                    mWeather.updateForecast(mLocation.latitude, mLocation.longitude);
                }
            }

            if (action.equals(ZC_FORECASTUPDATE)){
                if (debug) Log.d(TAG + "onReceive", "Forecast Update");
                weatherForecast = mWeather.get24hForecast(System.currentTimeMillis());
                mClock.setForecastData(weatherForecast);
                saveCurrentWeather();
            }
        }
    };

    public zcService(){}

    @Override
    public void onCreate() {
        super.onCreate();
        if (debug) Log.d(TAG, "onCreate");

        this.mContext          = getApplicationContext();
        this.mPrefs            = PreferenceManager.getDefaultSharedPreferences(mContext);
        this.mLocation         = new zcLocation(mContext);
        this.zCalendar         = new ComplexZmanimCalendar(mLocation.geoLocation());
        this.mWeather          = new zcWeather(mContext);
        this.mClock            = new zClock(mContext);
        this.jCalendar         = new JewishCalendar();

        registerReceiver(intentReceiver, intentFilter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (debug) Log.d(TAG, "onDestroy");
        unregisterReceiver(intentReceiver);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (debug) Log.d(TAG, String.format("onStartCommand: %s,%d,%d", intent.getAction(),flags,startId));
        registerReceiver(intentReceiver, intentFilter);
        return START_STICKY;
    }

    public void checkLocation() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (System.currentTimeMillis()- mPrefs.getLong("lastLocationUpdate",0) > locationUpdateTimeout) {
            mLocation.update();
        }
    }

    public void checkForecast() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (System.currentTimeMillis()- mPrefs.getLong("lastForecastUpdate",0L) > forecastUpdateTimeout || weatherForecast == null) {
            if (debug) Log.d(TAG,"/updateForecast: updating");
            mWeather.updateForecast(mLocation.latitude, mLocation.longitude);
        }
    }

    private void saveCurrentWeather() {
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putString("currWeather",weatherForecast[0].toJSONString());
        ed.apply();
    }

    private void updateZmanin(int appWidgetId){
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        long newday = getNewDayTime(appWidgetId), now = System.currentTimeMillis();

        if (now- mPrefs.getLong("lastZmanimUpdate",0) > zmanimUpdateTimeout) {

            if (debug) Log.d(TAG, String.format("updateZmanim: %d,%d",now,newday));
            mLocation.update();
            zCalendar = new ComplexZmanimCalendar(mLocation.geoLocation());
            SharedPreferences.Editor ed = mPrefs.edit();
            ed.putLong("lastZmanimUpdate",newday);
            ed.putLong("zHour",zCalendar.getTemporalHour());
            ed.putLong("sunrise",zCalendar.getSunrise().getTime());
            ed.putLong("sunset",zCalendar.getSunset().getTime());
            ed.putLong("eAstTw",zCalendar.getEndAstronomicalTwilight().getTime());
            ed.putLong("sAstTw", zCalendar.getBeginAstronomicalTwilight().getTime());
            ed.putLong("eNauTw",zCalendar.getEndNauticalTwilight().getTime());
            ed.putLong("sNauTw",zCalendar.getBeginNauticalTwilight().getTime());
            ed.putLong("chatzot",zCalendar.getChatzos().getTime());
            ed.putLong("midnight",zCalendar.getSolarMidnight().getTime());
            ed.apply();
            resetClock(appWidgetId);
        }

        if (!mClock.hasTimeMarks()){
            resetClock(appWidgetId);
        }
    }

    public Bitmap renderBitmap(int appWidgetId){

        //mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        int cMode = getIntPref("clockMode", appWidgetId);
        long newday = getNewDayTime(appWidgetId);

        boolean bkgDark = getBoolPref("bWhiteOnBlack", appWidgetId);

        switch (cMode) {

            default:
                if (debug) Log.d(TAG, "mClock mode <3");
                updateZmanin(appWidgetId);
                mClock.setup(appWidgetId,newday);

                if (getBoolPref("show72Hashem", appWidgetId)) {
                    mClock.setBackgroundPicture(
                            getHashemNames(mClock.getPxClock(), appWidgetId, HASHEM_72, bkgDark ? 0x08ffffff : 0x08000000, 0));
                } else {
                    mClock.setBackgroundPicture(null);
                }

                return mClock.draw(appWidgetId,newday);

            case 3:
                return getHashemNames(getWidgetSizePrefs(appWidgetId, true),
                        appWidgetId, HASHEM_72, bkgDark ? 0xffffffff : 0xff000000, bkgDark ? 0x80000000 : 0x80ffffff);

            case 4:
                return getHashemNames(getWidgetSizePrefs(appWidgetId, true),
                        appWidgetId, HASHEM_42, bkgDark ? 0xffffffff : 0xff000000, bkgDark ? 0x80000000 : 0x80ffffff);

            case 5:
                return renderPasuk(getWidgetSizePrefs(appWidgetId, true),
                        appWidgetId, 0, bkgDark ? 0xffffffff : 0xff000000, bkgDark ? 0x80000000 : 0x80ffffff);

        }
    }

    public void resetClock(int appWidgetId) {

        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        final Typeface tfThin = Typeface.create(mContext.getString(R.string.font_thin), Typeface.NORMAL);
        final Typeface tfCond = Typeface.create(mContext.getString(R.string.font_condensed), Typeface.NORMAL);
        final Typeface tfSTAM = Typeface.createFromAsset(mContext.getAssets(), "fonts/stmvelish.ttf");

        boolean bHeb = getBoolPref("bLangHebrew", appWidgetId);
        hebrewFormat.setHebrewFormat(bHeb);

        long newday = getNewDayTime(appWidgetId);

        jCalendar = new JewishCalendar(new Date(System.currentTimeMillis()+getNewDayShift(newday)));

        mClock.setup(appWidgetId, getNewDayTime(appWidgetId));

        mClock.resetLabelEvents();

        int cTime  = getColorPref("cTime",appWidgetId);
        int cDate = zcHelper.xColor.copyAlpha(getColorPref("cDate", appWidgetId), cTime);
        int cParshat = zcHelper.xColor.copyAlpha(getColorPref("cParshat", appWidgetId), cTime);

        if (getBoolPref("showHebDate", appWidgetId)) {
            mClock.addLabel(hebrewFormat.format(jCalendar),
                    getDimensPref("szDate", appWidgetId),
                    cDate,
                    bHeb ? tfSTAM : tfCond,
                    getDimensPref("wClockMargin", appWidgetId));

            if (jCalendar.getDayOfOmer()>-1) {
                int o = jCalendar.getDayOfOmer()-1;
                String sep = bHeb ? hebrewFormat.getHebrewOmerPrefix() : mContext.getResources().getString(R.string.omerSep);
                String omer = String.format("%s (%s %s %s)",hebrewFormat.formatOmer(jCalendar), omerSefirot[o%7],sep,omerSefirot[(o/7)]);
                mClock.addLabel(omer,
                        getDimensPref("szParshat", appWidgetId),
                        cParshat,
                        bHeb ? tfSTAM : tfThin,
                        getDimensPref("wClockMargin", appWidgetId));
            }

            if (jCalendar.isYomTov()){
                String yomtov =hebrewFormat.formatYomTov(jCalendar);
                if (jCalendar.getYomTovIndex() == JewishCalendar.PESACH) yomtov += " " + romanNumb[jCalendar.getDayOfOmer()];
                if (jCalendar.getYomTovIndex() == JewishCalendar.CHANUKAH) yomtov += " " + romanNumb[jCalendar.getDayOfChanukah()+1];
                mClock.addLabel(
                        yomtov,
                        getDimensPref("szParshat", appWidgetId),
                        cParshat,
                        bHeb ? tfSTAM : tfThin,
                        getDimensPref("wClockMargin", appWidgetId));
            }

            if (jCalendar.isErevYomTov()){
                mClock.addLabel(
                        bHeb ? " ערב" : "Erev " + hebrewFormat.formatYomTov(jCalendar),
                        getDimensPref("szParshat", appWidgetId),
                        cParshat,
                        bHeb ? tfSTAM : tfThin,
                        getDimensPref("wClockMargin", appWidgetId));
            }

            if (jCalendar.isRoshChodesh()) {
                mClock.addLabel(hebrewFormat.formatRoshChodesh(jCalendar),
                        getDimensPref("szParshat", appWidgetId),
                        cParshat,
                        bHeb ? tfSTAM : tfThin,
                        getDimensPref("wClockMargin", appWidgetId));
            }
        }

        if (getBoolPref("showParashat", appWidgetId)) {
            String s = getParshaHashavua(bHeb, false);
            if (s.length() != 0) {
                mClock.addLabel(s,
                        getDimensPref("szParshat", appWidgetId),
                        cParshat,
                        bHeb ? tfSTAM : tfThin,
                        getDimensPref("wClockMargin", appWidgetId));
            }
        }

        setZmanimMarks(appWidgetId);
    }

    private long getNewDayShift(long newdaytime) {
        return 86400000-(newdaytime%86400000);
    }

    private void setZmanimMarks(int appWidgetId) {

        final Typeface tfCondN = Typeface.create(mContext.getString(R.string.font_condensed), Typeface.NORMAL);
        final Typeface tfLight = Typeface.create(mContext.getString(R.string.font_light), Typeface.NORMAL);
        final Typeface tfRegularB = Typeface.create(mContext.getString(R.string.font_regular), Typeface.BOLD);

        mClock.resetTimeMarks();

        int cTime  = getColorPref("cTime",appWidgetId);
        int cFrame  = getColorPref("cClockFrameOn",appWidgetId);
        int cHours = zcHelper.xColor.copyAlpha(getColorPref("cTimemarks", appWidgetId), cTime);
        int czSun = zcHelper.xColor.copyAlpha(getColorPref("cZmanim_sun", appWidgetId), cFrame);
        int czMain = zcHelper.xColor.copyAlpha(getColorPref("cZmanim_main", appWidgetId), cFrame);

        //mClock Hours 0-23h
        if (getBoolPref("showTimeMarks", appWidgetId)) {
            Date[] timeHours = new Date[24];
            Calendar c = Calendar.getInstance();
            Date d;
            for (int i = 0; i < 24; i++) {
                d = c.getTime();
                d.setHours(i);
                d.setMinutes(0);
                timeHours[i] = d;
            }
            mClock.addMarks(tfRegularB,
                    cHours,
                    getDimensPref("szTimemarks", appWidgetId),
                    getStringPref("tsTimemarks", appWidgetId),
                    getBoolPref("iTimemarks", appWidgetId),
                    timeHours);
        }

        int zAlot = getIntPref("zmanimAlot", appWidgetId);
        int zTzet = getIntPref("zmanimTzet", appWidgetId);

        Date alot, tzet,shmaMGA,shmaGRA,tfilaMGA,tfilaGRA;
        switch (zAlot) {
            case 1:
                alot = zCalendar.getAlos60(); break;
            case 2: alot = zCalendar.getAlos72();break;
            case 3: alot = zCalendar.getAlos90();break;
            case 4: alot = zCalendar.getAlos120();break;
            case 5: alot = zCalendar.getAlos16Point1Degrees();break;
            case 6: alot = zCalendar.getAlos18Degrees();break;
            case 7: alot = zCalendar.getAlos19Point8Degrees();break;
            case 8: alot = zCalendar.getAlos26Degrees();break;
            default: alot = zCalendar.getSeaLevelSunrise();break;
        }
        switch (zTzet) {
            case 1: tzet = zCalendar.getTzais60(); break;
            case 2: tzet = zCalendar.getTzais72();break;
            case 3: tzet = zCalendar.getTzais90();break;
            case 4: tzet = zCalendar.getTzais120();break;
            case 5: tzet = zCalendar.getTzais16Point1Degrees();break;
            case 6: tzet = zCalendar.getTzais18Degrees();break;
            case 7: tzet = zCalendar.getTzais19Point8Degrees();break;
            case 8: tzet = zCalendar.getTzais26Degrees();break;
            default: tzet = zCalendar.getSeaLevelSunset();break;
        }

        mClock.setNewDayTimeMilis(tzet.getTime());

        Date d1 = (zTzet == 0) ? zCalendar.getTzais72() : zCalendar.getSunset();
        Date d2 = (zAlot == 0) ? zCalendar.getAlos16Point1Degrees() : zCalendar.getSunrise();

        mClock.addMarks(tfRegularB,
                czSun,
                getDimensPref("szZmanim_sun", appWidgetId),
                getStringPref("tsZmanim_sun", appWidgetId),
                getBoolPref("iZmanim_sun", appWidgetId),
                new Date[]{
                        tzet,
                        alot,
                        d1,
                        d2,
                        zCalendar.getChatzos(),
                        zCalendar.getSolarMidnight()});

        if (jCalendar.getDayOfWeek() == 6) {
            mClock.addMarks(tfRegularB,
                    0xff800000,
                    getDimensPref("szZmanim_sun", appWidgetId),
                    getStringPref("tsZmanim_sun", appWidgetId),
                    getBoolPref("iZmanim_sun", appWidgetId),
                    new Date[]{zCalendar.getCandleLighting()});
        }

        if (jCalendar.getDayOfWeek() == 7) {
            mClock.addMarks(tfRegularB,
                    0xff804000,
                    getDimensPref("szZmanim_sun", appWidgetId),
                    getStringPref("tsZmanim_sun", appWidgetId),
                    getBoolPref("iZmanim_sun", appWidgetId),
                    new Date[]{tzet});
        }

        if (getBoolPref("showZmanim", appWidgetId)) {

            mClock.addMarks(tfLight,
                    czMain,
                    getDimensPref("szZmanim_main", appWidgetId),
                    getStringPref("tsZmanim_main", appWidgetId),
                    getBoolPref("iZmanim_main", appWidgetId),
                    new Date[]{
                            alotHarHabait,
                            zCalendar.getSunriseOffsetByDegrees(100.2D),
                            zCalendar.getSofZmanShmaMGA(),
                            zCalendar.getSofZmanShmaGRA(),
                            zCalendar.getSofZmanTfilaMGA(),
                            zCalendar.getSofZmanTfilaGRA(),
                            zCalendar.getMinchaKetana(),
                            zCalendar.getMinchaGedola(),
                            zCalendar.getPlagHamincha()
                    /*new Date[]{
                            alotHarHabait,
                            zCalendar.getSunriseOffsetByDegrees(100.2D),
                            zCalendar.getSofZmanShma(alot, tzet),
                            zCalendar.getSofZmanTfila(alot, tzet),
                            zCalendar.getMinchaKetana(alot, tzet),
                            zCalendar.getMinchaGedola(alot, tzet),
                            zCalendar.getPlagHamincha(alot, tzet)*/
                    });

        }

        mClock.updateTimeMarks();
    }

    private Bitmap getHashemNames(PointF size,int appWidgetId,int type, int colorForeground, int colorBackground){

        //PointF wdgtSize = getWidgetSizePrefs(appWidgetId, true);
        int index;
        int glowSteps = (colorBackground == 0) ? 0 : 3;
        float f = 0;
        String s1 = "", s2[] = null;
        if (type < 2) {
            index = (int) (System.currentTimeMillis()/ 60000 / getIntPref("nShemot", appWidgetId)) % 72;
            try {
                s1 = hebString.decodeB64String(mContext.getResources().getStringArray(R.array.short_shemot)[type])[index];
                s2 = hebString.decodeB64String(mContext.getResources().getStringArray(R.array.long_shemot)[0]);
                f = 0f;
            } catch (UnsupportedEncodingException ignored) {
                if (debug) Log.d("getHashemNames","UnsupportedEncoding");
            }

        } else {
            index =jCalendar.getDayOfWeek();
            try {
                s1 = new String(
                        Base64.decode(
                                mContext.getResources().getStringArray(R.array.short_ab)[index - 1], Base64.DEFAULT), "UTF-8");
                s2 = new String[]{new String(
                        Base64.decode(
                                mContext.getResources().getStringArray(R.array.long_ab)[index - 1], Base64.DEFAULT), "UTF-8")};
            } catch (UnsupportedEncodingException ignored) {
            }
            f = 0;
        }

        return renderText(
                size,
                Typeface.createFromAsset(mContext.getAssets(), "fonts/stmvelish.ttf"),
                s1, s2,
                colorForeground, 0, colorForeground, f,
                glowSteps, colorBackground, 13);
    }

    @Nullable
    private String[] getCurrentPasuk() {
        int l = 0, index;
        int d = jCalendar.getDayOfWeek() - 1;
        //int dm = (int) ((zCalendar.getSunset().getTime() - zCalendar.getSunrise().getTime()) / 60000);
        int m = (int) (System.currentTimeMillis() / 60000) % 1440;
        String pasuk="", ref="", sefer="";
        try {
            int i = getParshaHashavuaIndex();
            if (debug) Log.d("Parsha index",String.valueOf(i));

            String[] source = mContext.getResources().getStringArray(R.array.torah)[i].split("#");
            int[] yom = hebString.decodeB64Int(source[0]);
            String[] parsha;

            if (i>52) {
                int[] p  = hebString.decodeB64Int(source[1]);

                //String[] p1 = new String(Base64.decode(mContext.getResources().getStringArray(R.array.torah)[p[0]].split("#")[1], Base64.DEFAULT), "UTF-8").split("\\r?\\n");
                String[] p1 = hebString.decodeB64String(mContext.getResources().getStringArray(R.array.torah)[p[0]].split("#")[1]);

                //String[] p2 = new String(Base64.decode(mContext.getResources().getStringArray(R.array.torah)[p[1]].split("#")[1], Base64.DEFAULT), "UTF-8").split("\\r?\\n");
                String[] p2 = hebString.decodeB64String(mContext.getResources().getStringArray(R.array.torah)[p[1]].split("#")[1]);

                parsha = new String[p1.length+p2.length];
                System.arraycopy(p1,0,parsha,0,p1.length);
                System.arraycopy(p2,0,parsha,p1.length,p2.length);
            } else {
                //parsha = new String(Base64.decode(source[1], Base64.DEFAULT), "UTF-8").split("\\r?\\n");
                parsha = hebString.decodeB64String(source[1]);
            }

            int v = 0;
            for (int n = 0; n < d; n++) {
                v += yom[n];
            }

            index = 1 + v + (m * yom[d] / 1440);
            if (debug) Log.d("Parashat", String.format("parsha %d day %d line %d/%d", i, d + 1, v, index));
            pasuk = parsha[index];
            sefer = parsha[0];
            int iref = pasuk.indexOf(" ");
            ref = (iref > 0) ? pasuk.substring(0, iref) : "error";
            pasuk = (iref > 0) ? pasuk.substring(iref + 1) : String.format("ERRO %d/%d", index, l);
        } catch (Exception ignored) {
            if (debug) Log.d(TAG,"decoding pasuk error: "+ignored.toString());
            return null;
        }
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putString("currentPasuk", pasuk);
        ed.putString("currentRef",ref);
        ed.putString("currentSefer",sefer);
        ed.apply();
        return new String[]{ref,pasuk};
    }

    private String getParshaHashavua(boolean inHebrew, boolean parshaNameOnly) {
        int day = jCalendar.getDayOfWeek();
        String result = (parshaNameOnly) ? "" : (inHebrew) ? ((day % 7 == 0) ? "שבת " : "פרשת השבוע ") : ((day % 7 == 0) ? "Shabbat " : "Parashat Hashavua ");
        Calendar c = Calendar.getInstance();
        c.set(jCalendar.getGregorianYear(),jCalendar.getGregorianMonth(),jCalendar.getGregorianDayOfMonth()+7-day,0,0);
        hebrewFormat.setHebrewFormat(inHebrew);
        String p =hebrewFormat.formatParsha(new JewishCalendar(c.getTime()));
        return (p.equals("")) ? "" : result + p ;
    }

    private int getParshaHashavuaIndex() {
        Calendar c = Calendar.getInstance();
        c.set(jCalendar.getGregorianYear(), jCalendar.getGregorianMonth(), jCalendar.getGregorianDayOfMonth() + 7 - jCalendar.getDayOfWeek(), 0, 0);
        return new JewishCalendar(c).getParshaIndex();
    }

    private long getNewDayTime(int appWidgetId) {

        switch (getIntPref("zmanimMode", appWidgetId)) {
            case 1:
                return zCalendar.getTzais60().getTime();
            case 2:
                return zCalendar.getTzais72().getTime();
            case 3:
                return zCalendar.getTzais90().getTime();
            case 4:
                return zCalendar.getTzais120().getTime();
            case 5:
                return zCalendar.getTzais16Point1Degrees().getTime();
            case 6:
                return zCalendar.getTzais18Degrees().getTime();
            case 7:
                return zCalendar.getTzais19Point8Degrees().getTime();
            case 8:
                return zCalendar.getTzais26Degrees().getTime();
            default:
                return zCalendar.getSunset().getTime();
        }
    }

    //region draw methods
    //updated to zcZmanim
    private Bitmap renderPasuk( PointF size, int appWidgetId, int type, int fColor, int bColor) {

        boolean bkgDark = getBoolPref("bWhiteOnBlack", appWidgetId);
        Bitmap bitmap = Bitmap.createBitmap((int) size.x, (int) size.y, Bitmap.Config.ARGB_8888);
        bitmap = renderBackground(bitmap, bkgDark ? 0x80000000 : 0x80ffffff, 13f);
        final Typeface tfStam = Typeface.createFromAsset(mContext.getAssets(), "fonts/stmvelish.ttf");
        final Typeface tfCondN = Typeface.create(mContext.getString(R.string.font_thin), Typeface.NORMAL);

        String[] currentPasuk = getCurrentPasuk();
        if (currentPasuk==null) return bitmap;

        currentPasuk[1] = hebString.removeBreakSymbs(currentPasuk[1]);
        currentPasuk[1] = hebString.toNiqqud(currentPasuk[1]);

        //float y = bitmap.getHeight() + 13f;

        //ref
        renderTextBlock (bitmap,
                        tfCondN,
                        currentPasuk[0],
                        bkgDark ? 0xa0ffffff : 0xa0000000,
                        26f,
                        42f,
                        0.98f);
        //pasuk
        renderTextBlock (bitmap,
                        tfStam,
                        currentPasuk[1],
                        bkgDark ? 0xffffffff : 0xff000000,
                        50f,
                        50f,
                        0.90f); //tehilim
        //Gematria: milim, otiot
        currentPasuk[1] = hebString.removeMaqaf(currentPasuk[1]);
        currentPasuk[1] = hebString.toOtiot(currentPasuk[1]);
        int milim       = hebString.getMilimNumber(currentPasuk[1]), otiot = hebString.getOtiotNumber(currentPasuk[1]);
        renderTextBlock (bitmap,
                        tfCondN,
                        String.format("milim %d, otiot %d",milim,otiot),
                        bkgDark ? 0xa0ffffff : 0xa0000000,
                        18f,
                        bitmap.getHeight() -20f,
                        0.9f);
        return bitmap;
    }
    private Bitmap renderText(PointF size,
                              Typeface typeface,
                              String title, String[] subtitle,
                              int title_color, float title_size,
                              int subtitle_color, float subtitle_size,
                              int glowSteps,
                              int bkgColor,
                              float corners) {

        Bitmap bitmap = Bitmap.createBitmap((int) size.x, (int) size.y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        PointF centro = new PointF(size.x / 2, size.y / 2);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        p.setColor(bkgColor);
        canvas.drawRoundRect(new RectF(0, 0, size.x, size.y), corners, corners, p);
        float y_pos;
        p.setTypeface(typeface);
        p.setTextAlign(Paint.Align.CENTER);

        //Draw title
        if (title != null) {
            Rect b = new Rect();
            if (title_size == 0) {
                p.setTextSize(100);
                p.getTextBounds(title, 0, title.length(), b);
                p.setTextSize(Math.min(90 * size.x / b.width(), 50 * size.y / b.height()));
            } else p.setTextSize(title_size);
            p.setColor(title_color);
            p.getTextBounds(title, 0, title.length(), b);
            y_pos = b.height();
            if (glowSteps > 0) {
                float blur_rad = mClock.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22 / glowSteps);
                p.setAlpha(128);
                for (int i = 0; i < glowSteps; i++) {
                    canvas.drawText(title, centro.x, y_pos, p);
                    blur_rad = blur_rad / 2;
                }
                p.setMaskFilter(null);
            }
            p.setColor(title_color);
            canvas.drawText(title, centro.x, y_pos, p);
        }


        //Draw subtitle
        if (subtitle != null) {
            Rect b = new Rect();
            if (subtitle_size == 0) {
                p.setTextSize(100);
                p.getTextBounds(subtitle[0], 0, subtitle[0].length(), b);
                subtitle_size = Math.min(80 * size.x / b.width(), 50 * size.y / b.height() / subtitle.length);
            }
            p.setTextSize(subtitle_size);
            p.getTextBounds(subtitle[0], 0, subtitle[0].length(), b);
            y_pos = size.y*0.56f;
            for (String st : subtitle) {
                p.setColor(subtitle_color);
                canvas.drawText(st, centro.x, y_pos - 2.5f * (p.descent() + p.ascent()), p);
                y_pos += b.height();
            }
        }

        return bitmap;
    }

    //updated to zcZmanim
    private Bitmap renderBackground(Bitmap bitmap, int bkgColor, float corners) {
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(bkgColor);
        canvas.drawRoundRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), corners, corners, p);
        return bitmap;
    }

    //updated to zcZmanim
    private Bitmap renderTextBlock(Bitmap bitmap,
                                   Typeface typeface,
                                   String text,
                                   int color, float size, float yPos, float margin) {

        Canvas canvas = new Canvas(bitmap);

        float slmargin = bitmap.getWidth() * (1-margin);
        float slwidth = bitmap.getWidth() * margin;
        TextPaint p = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTypeface(typeface);
        p.setTextSize(size);
        canvas.translate(slmargin/2, yPos);
        canvas.save();
        StaticLayout sl;
        do {
            sl = new StaticLayout("" + text, p, (int) slwidth, Layout.Alignment.ALIGN_NORMAL, margin, 0.5f, false);
            p.setTextSize(--size);
        } while ((sl.getHeight() > bitmap.getHeight() * margin));

        sl.draw(canvas);
        canvas.restore();
        return bitmap;
    }
    //endregion

    private PointF getWidgetSizePrefs( int appWidgetId, boolean applyDimension) {
        float w = applyDimension ?
                mClock.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getSizePref("widgetWidth", appWidgetId)) :
                getSizePref("widgetWidth", appWidgetId);
        float h = applyDimension ?
                mClock.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getSizePref("widgetHeight", appWidgetId)) :
                getSizePref("widgetWidth", appWidgetId);
        return new PointF(w, h);
    }


    private void updateWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        if (debug) Log.d(TAG,"updateAppWidgets");

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            if (debug) Log.d(TAG,"updateAppWidget id:"+appWidgetId);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);

            //Intent intent = new Intent(context, zcPreferences.class).setAction(zcPreferences.ACTION_PREFS);
            Intent intent = new Intent(context, zcActivity.class).setAction(zcActivity.ACTION_ACTIVITY);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.imageView, pendingIntent);

            updateWidgetSize(context, appWidgetId);
            remoteViews.setImageViewBitmap(R.id.imageView,renderBitmap(appWidgetId));
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        }
    }

    private void updateWidgetSize(Context context, int appWidgetId) {

        if (debug) Log.d(TAG,"updateWidgetSize");

        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Bundle newOptions = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId);
        int w, h, wCells, hCells;
        if (Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            w = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
            h = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        } else {
            w = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            h = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            wCells = (int) Math.ceil((w + 2) / 72);
            hCells = (int) Math.ceil((h + 2) / 72);
        } else {
            wCells = (int) Math.ceil((w + 30) / 70);
            hCells = (int) Math.ceil((h + 30) / 70);
        }

        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putFloat("widgetWidth" + appWidgetId, (float) w);
        ed.putFloat("widgetHeight" + appWidgetId, (float) h);
        ed.putInt("widgetCellWidth" + appWidgetId, wCells);
        ed.putInt("widgetCellHeight" + appWidgetId, hCells);
        ed.apply();
    }

    //region SharedPreferences helper Methods

    private int     getIntPref( String key, int appWidgetId) {
        int ResId = mContext.getResources().getIdentifier(key, "integer", mContext.getPackageName());
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(key + appWidgetId, mContext.getResources().getInteger(ResId));
    }

    private float   getDimensPref( String key, int appWidgetId) {
        int ResId = mContext.getResources().getIdentifier(key, "dimen", mContext.getPackageName());
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(key + appWidgetId, 100) / 100f * mContext.getResources().getDimension(ResId);
    }

    private float   getSizePref( String key, int appWidgetId) {
        int ResId = mContext.getResources().getIdentifier(key, "dimen", mContext.getPackageName());
        return mPrefs.getFloat(key + appWidgetId, mContext.getResources().getDimension(ResId));
    }

    private String  getStringPref( String key, int appWidgetId) {
        int ResId = mContext.getResources().getIdentifier(key, "string", mContext.getPackageName());
        return PreferenceManager.getDefaultSharedPreferences(mContext).getString(key + appWidgetId, mContext.getResources().getString(ResId));
    }

    private boolean getBoolPref( String key, int appWidgetId) {
        int ResId = mContext.getResources().getIdentifier(key, "bool", mContext.getPackageName());
        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(key + appWidgetId, mContext.getResources().getBoolean(ResId));
    }

    private int     getColorPref( String key, int appWidgetId) {
        int ResId = mContext.getResources().getIdentifier(key, "color", mContext.getPackageName());
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(key + appWidgetId, mContext.getResources().getColor(ResId));
    }
    //endregion

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
