package pt.gu.zclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static pt.gu.zclock.zcHelper.*;
import static pt.gu.zclock.zcHelper.xColor;

/**
 * Created by GU on 03-11-2014.
 */
public class zClock {

    private boolean         debug = false;
    private final String    TAG = "zClock";
    private final int       CLOCK_MODE_DAY_UP=0;
    private final int       CLOCK_MODE_DAY_DOWN=1;
    public boolean          bClockElapsedTime = true;
    private float           dp = Resources.getSystem().getDisplayMetrics().density;
    //private int     resx =Resources.getSystem().getDisplayMetrics().widthPixels;
    //private int     resy=Resources.getSystem().getDisplayMetrics().heightPixels;
    private float           pad = 1 * dp;
    private float[]         textMarksMaxWidth = new float[]{22f * dp, 0};
    private PointF          szClock = new PointF(294*dp,294*dp),
                            centro  = new PointF(szClock.x / 2f, szClock.y / 2f);
    private Point           pxClock;
    private int             raio = (int) (Math.min(szClock.x / 2f, szClock.y / 2f) - pad);
    private float           szFrame = 0.5f * dp,
                            szPointer = 0.17f * dp,
                            szTimeMins = 10f,
                            resTimeMins = 2f,
                            szPtrHeight = 50f,
                            szWeatherPad =2f;

    private PathEffect      patFrame;

    private float           szTime = 45f * dp;
    private Typeface        typeTime =Typeface.create("sans-serif-light", Typeface.NORMAL);

    private int             clockMode = 0;
    private float           angle_offset = 90f;
    private float           newdaytime_angle;
    private float           szTimeLabPad=10f;
    private long            newDayTimeMilis = 0;
    private Bitmap          backgroundBitmap=null;
    private float           lineFeed = 22f;
    private List<timeLabel> timeMarks = new ArrayList();
    private List<dateLabel> Labels = new ArrayList();

    private long            startForecastTime = 0;
    private WeatherData[]   weatherForecast;
    private long            timeDST;

    private Context         mContext;
    private SharedPreferences   sharedPreferences;

    private int             weatherAlpha = 255,
                            wTempMaxC    = 42,
                            wTempMinC    =-8;
    private float           startHue     = 0.60f,
                            adjustSat    = 0.90f;

    private boolean         wForeCond    = true,
                            wRainCond    = false;

    private int     cFrame  = 0xff00c3ff,
                    cTime   = 0xffffffff;

    public zClock(Context context){
        this.mContext  = context.getApplicationContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        this.timeDST   = Calendar.getInstance().getTimeZone().getDSTSavings();
    }

    public void setup(int appWidgetId, long NewDayTime) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        this.clockMode = getIntPref("clockMode", appWidgetId);

        float l        = Math.min(getSizePref("widgetWidth", appWidgetId),getSizePref("widgetHeight", appWidgetId));
        this.szClock   = new PointF(l,l);
        this.pxClock   = new Point(
                                    applyDimension(TypedValue.COMPLEX_UNIT_DIP, l),
                                    applyDimension(TypedValue.COMPLEX_UNIT_DIP, l));
        this.newDayTimeMilis = NewDayTime;
        this.newdaytime_angle = ((newDayTimeMilis / 60000f) % 1440) / 4;
        this.raio = Math.min(pxClock.x / 2, pxClock.y / 2);
        if (clockMode < 3) {
            this.pxClock.x = raio * 2;
            this.pxClock.y = raio * 2;
        }
        this.raio -= pad;
        this.centro = new PointF(this.pxClock.x / 2, this.pxClock.y / 2);

        this.szFrame = getDimensPref("wClockFrame", appWidgetId);
        this.szPointer = getDimensPref("wClockPointer", appWidgetId);
        this.szPtrHeight = getIntPref("szPtrHeight", appWidgetId) / 100f;
        this.resTimeMins = getIntPref("resTimeMins", appWidgetId);
        this.szTimeMins = getIntPref("szTimeMins", appWidgetId) / 100f;
        this.szTime = getDimensPref("szTime", appWidgetId);
        this.patFrame = renderDashPathEffect(this.raio);

        this.cTime = getColorPref("cTime", appWidgetId);
        this.cFrame = getColorPref("cClockFrameOn", appWidgetId);

        this.typeTime = Typeface.create(mContext.getString(R.string.font_thin), Typeface.NORMAL);//Typeface.createFromAsset(mContext.getAssets(), "fonts/noto_sans.ttf");
        this.szWeatherPad = getDimensPref("szTimemarks", appWidgetId);
    }

    public void setMarks() {
        updateTimeMarks();
    }

    public void resetLabelEvents(){

        Labels.clear();
    }


    public void setNewDayTimeMilis(long newday){
        this.newDayTimeMilis = newday;
        this.newdaytime_angle = ((newDayTimeMilis / 60000f) % 1440) / 4;
    }

    public void setForecastData(WeatherData[] data){
        this.weatherForecast = data;
    }

    public Bitmap draw(int appWidgetId, long newdaytimeMilis) {

        setup(appWidgetId, newdaytimeMilis);

        textMarksMaxWidth = getMaxTextWidth();

        updateTimeMarks();

        Bitmap bitmap = Bitmap.createBitmap(pxClock.x, pxClock.y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        //Draw background
        if (backgroundBitmap != null) {
            p.setColor(Color.WHITE);
            canvas.drawBitmap(backgroundBitmap, 0, 0, p);
        }

        //Draw Clock background Frame
        p.setColor(xColor.setAlpha(0x10, cFrame));
        p.setStrokeWidth(szFrame);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(centro.x, centro.y, raio, p);

        //float time = getTimeMins();
        float time_angle = getTimeAngle(System.currentTimeMillis());    //(time / 4) % 360;

        switch (clockMode) {
            case CLOCK_MODE_DAY_UP:
                angle_offset = -90;
                break;
            case CLOCK_MODE_DAY_DOWN:
                angle_offset = 90;
                break;
            default:
                angle_offset = -time_angle;
        }

        //Elapsed Time
        p.setColor(cFrame);
        p.setPathEffect(patFrame);
        if (debug) Log.d("Clock.draw", String.format("ta:%f ndta:%f", time_angle, newdaytime_angle));
        float angStart = bClockElapsedTime ? newdaytime_angle : time_angle;
        float angLenght = bClockElapsedTime ? time_angle - newdaytime_angle : newdaytime_angle - time_angle;
        if (angLenght < 0) angLenght += 360;
        canvas.drawArc(new RectF(centro.x - raio, centro.y - raio, centro.x + raio, centro.y + raio),
                angStart + angle_offset, angLenght, false, p);

        //Weather Forecast
        if (getBoolPref("showWeather",appWidgetId) && (weatherForecast!=null) && wForeCond) {
            if (weatherForecast.length!=0) {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(getDimensPref("szWeatherFrame", appWidgetId));
                //float r2 = raio - szFrame/2 - (getBoolPref("iTimemarks",appWidgetId) ? 2 * szTimeLabPad:0) - textMarksMaxWidth[1] - szWeatherPad;
                float r2 = raio - szFrame/2-szWeatherPad;
                canvas.save();
                canvas.rotate(getTimeAngle(this.startForecastTime - timeDST) + angle_offset, canvas.getWidth() / 2, canvas.getHeight() / 2);
                p.setShader(getForecastShader());
                canvas.drawArc(new RectF(centro.x - r2, centro.y - r2, centro.x + r2, centro.y + r2),
                        0, 359f, false, p);

                if (wRainCond) {
                    p = new Paint(Paint.ANTI_ALIAS_FLAG);
                    p.setColor(cFrame);
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(szFrame / 6f);
                    p.setShader(getCloudnRainShader());
                    canvas.drawArc(new RectF(centro.x - r2, centro.y - r2, centro.x + r2, centro.y + r2),
                            0, 359f, false, p);
                }
                canvas.restore();
            }
        }

        //Clock Pointer
        p.reset();
        p.setAntiAlias(true);
        float fp = raio * (1 - this.szPtrHeight / 2);
        p.setStrokeWidth(raio * this.szPtrHeight);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(cFrame);
        canvas.drawArc(new RectF(centro.x - fp, centro.y - fp, centro.x + fp, centro.y + fp), time_angle + angle_offset - 0.5f, 0.5f, false, p);

        Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        float line;
        Rect bounds = new Rect();
        String s;

        if (timeMarks.size()!=0){
            //Draw timemarks
            timeMarkPaths();
            //tp.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            for (timeLabel z : timeMarks) {
                lineFeed = applyDimension(TypedValue.COMPLEX_UNIT_PX, z.size);
                tp.setColor(z.color);
                tp.setTextSize(lineFeed);
                tp.setTypeface(z.type);
                tp.setTextAlign((z.alignRight) ? Paint.Align.RIGHT : Paint.Align.LEFT);
                canvas.drawTextOnPath(z.toString(), z.path, 0, 0, tp);
            }
        }


        //tp = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

        //Draw Current Time
        tp.setTypeface(typeTime);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setTextSize(applyDimension(TypedValue.COMPLEX_UNIT_PX, szTime));
        tp.setColor(cTime);
        s = new SimpleDateFormat("HH:mm").format(new Date());
        tp.getTextBounds(s, 0, s.length(), bounds);
        line = centro.y + bounds.height() / 2;  // -(tp.ascent()+tp.descent())/2;
        canvas.drawText(s, centro.x, line, tp);

        if (Labels.size()!=0) {
            //Draw Labels
            for (dateLabel t : Labels) {
                lineFeed = applyDimension(TypedValue.COMPLEX_UNIT_PX, t.size);
                tp.setTypeface(t.type);
                tp.setColor(t.color);
                tp.setTextSize(lineFeed);
                tp.getTextBounds(t.label, 0, t.label.length(), bounds);
                line += 1.5f * bounds.height();// -(tp.ascent()+tp.descent())/2;
                canvas.drawText(t.label, centro.x, line, tp);
            }
        }

        return bitmap;

    }

    private Shader getCloudnRainShader() {


        int[]   res = new int[this.weatherForecast.length];
        float[] pos = new float[this.weatherForecast.length];

        if (debug) Log.d("Clock.zcP.Temp","from URL Weather Data");

        this.startForecastTime = this.weatherForecast[0].getTime();

        int i=0;

        long delta = weatherForecast[weatherForecast.length-1].getTime()-startForecastTime;

        for (WeatherData w : this.weatherForecast){
            int c = Color.argb((int)(w.clouds_all*2.55f),255,255,255);
            pos[i]   = ((float)w.getTime()-(float)this.startForecastTime)/(float)delta;
            if (debug) Log.d(TAG,"wClouds: "+String.format("Color %08X [Dt%d id%d T%f] @ %.2f",c,w.getTime(),w.weather_id,w.main_temp,pos[i]));
            res[i++] = c;
        }

        return new SweepGradient(centro.x, centro.y, res, pos);

    }

    private Shader getForecastShader() {

        int[]   res = new int[this.weatherForecast.length];
        float[] pos = new float[this.weatherForecast.length];

        if (debug) Log.d("Clock.zcP.Temp","from URL Weather Data");

        this.startForecastTime = this.weatherForecast[0].getTime();

        int i=0;

        long delta = weatherForecast[weatherForecast.length-1].getTime()-startForecastTime;

        for (WeatherData w : this.weatherForecast){
            int c = w.getColorCondition(this.startHue);
            pos[i]   = ((float)w.getTime()-(float)this.startForecastTime)/(float)delta;
            if (debug) Log.d(TAG,"wData: "+String.format("Color %08X [Dt%d id%d T%f] @ %.2f",c,w.getTime(),w.weather_id,w.main_temp,pos[i]));
            res[i++] = c;
        }

        return new SweepGradient(centro.x, centro.y, res, pos);
    }

    public int applyDimension(int typedValyeUnit, float value) {
        return (int) TypedValue.applyDimension(typedValyeUnit, value, Resources.getSystem().getDisplayMetrics());
    }

    private DashPathEffect renderDashPathEffect(float raio) {
        float f = (float) (Math.PI * raio * this.resTimeMins / 360);
        float fill = this.szTimeMins<0.5f ? 1f : f-1f;
        float stro = f-fill;
        //return new DashPathEffect(new float[]{f * this.szTimeMins, f * (1 - this.szTimeMins)}, 0);
        return new DashPathEffect(new float[]{fill, stro}, 0);
    }

    public PointF getPxClock() {
        return new PointF(pxClock.x, pxClock.y);
    }

    private PointF getWidgetSizePrefs( int appWidgetId, boolean applyDimension) {
        float w = applyDimension ?
                applyDimension(TypedValue.COMPLEX_UNIT_DIP, getSizePref("widgetWidth", appWidgetId)) :
                getSizePref("widgetWidth", appWidgetId);
        float h = applyDimension ?
                applyDimension(TypedValue.COMPLEX_UNIT_DIP, getSizePref("widgetHeight", appWidgetId)) :
                getSizePref("widgetWidth", appWidgetId);
        return new PointF(w, h);
    }

    public void resetTimeMarks() {
        timeMarks.clear();
    }

    public void addMarks(Typeface typeface, int color, float size, String format, boolean inside, Date[] timearray) {
        for (Date t : timearray) {
            timeMarks.add(new timeLabel(t, size, color, typeface, format, inside));
        }
    }

    public void addLabel(String text, float size, int color, Typeface typeface, float pad) {
        Labels.add(new dateLabel(text, size, color, typeface, pad));
    }

    public void setBackgroundPicture(Bitmap b) {
        backgroundBitmap = b;
    }




    private float getTimeAngle(long timeMilis){
        return timeMilis / 60000f % 1440 / 4 %360;
    }

    public void updateTimeMarks() {
        raio = (int) (Math.min(pxClock.x / 2, pxClock.y / 2) - textMarksMaxWidth[0] - szFrame/2 - szTimeLabPad);
        this.patFrame = renderDashPathEffect(this.raio);
        timeMarkPaths();
    }

    private float[] getMaxTextWidth() {
        if (timeMarks.size()!=0) {
            float res_out = 0f, res_in = 0f;
            Paint tp = new Paint();
            for (timeLabel z : timeMarks) {
                lineFeed = applyDimension(TypedValue.COMPLEX_UNIT_PX, z.size);
                tp.setTypeface(z.type);
                tp.setTextSize(lineFeed);
                if (z.insideFrame)
                    res_in = Math.max(res_in, tp.measureText(z.toString()));
                else
                    res_out = Math.max(res_out, tp.measureText(z.toString()));
            }
            return new float[]{res_out, res_in};
        } else {
            return new float[]{22f * dp, 0};
        }

    }

    private void timeMarkPaths() {

        //float r1a = szFrame + szTimeLabPad, r1, r2;
        float r1a = szFrame/2 + szTimeLabPad, r1, r2;
        final double Pi2Deg = Math.PI / 180;
        double angle, cos, sin;
        for (timeLabel z : timeMarks) {
            if (z.insideFrame) {
                r2 = raio - r1a-szWeatherPad;
                r1 = r2 - textMarksMaxWidth[1];
            } else {
                r1 = raio + r1a;
                r2 = r1 + textMarksMaxWidth[0];
            }
            Path p = new Path();
            angle = (z.toRadianAngle() + angle_offset) * Pi2Deg;
            cos = Math.cos(angle);
            sin = Math.sin(angle);
            if (cos < 0) {
                p.moveTo((float) (r2 * cos) + centro.x, (float) (r2 * sin) + centro.y);
                p.lineTo((float) (r1 * cos) + centro.x, (float) (r1 * sin) + centro.y);
                z.alignRight = !z.insideFrame;
            } else {
                p.moveTo((float) (r1 * cos) + centro.x, (float) (r1 * sin) + centro.y);
                p.lineTo((float) (r2 * cos) + centro.x, (float) (r2 * sin) + centro.y);
                z.alignRight = z.insideFrame;
            }
            z.path = p;
        }
    }

    //region Preference Helper

    private int     getIntPref( String key, int appWidgetId) {
        int ResId = mContext.getResources().getIdentifier(key, "integer", mContext.getPackageName());
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(key + appWidgetId, mContext.getResources().getInteger(ResId));
    }

    private float   getDimensPref( String key, int appWidgetId) {
        int ResId = mContext.getResources().getIdentifier(key, "dimen", mContext.getPackageName());
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(key + appWidgetId, 100) / 100f * mContext.getResources().getDimension(ResId);
    }

    private float   getSizePref(String key, int appWidgetId) {
        int ResId = mContext.getResources().getIdentifier(key, "dimen", mContext.getPackageName());
        return sharedPreferences.getFloat(key + appWidgetId, mContext.getResources().getDimension(ResId));
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

    public boolean hasTimeMarks() {
        return timeMarks.size()!=0 && Labels.size()!=0;
    }

    public void setStartForecastTime(long dt) {
        this.startForecastTime = dt;
    }

    //endregion

    class timeLabel {
        private Date date = new Date(System.currentTimeMillis());
        private float size = 12f;
        private int color = Color.WHITE;
        private Typeface type;
        private boolean alignRight = true;
        private Path path;
        private String format = "HH:mm";
        private String label;
        private boolean insideFrame = false;

        timeLabel(Date date, float size, int color, Typeface t, String format, boolean insideFrame) {
            this.date = date;
            this.size = size;
            this.color = color;
            this.type = t;
            this.format = format;
            this.insideFrame = insideFrame;
        }

        @Override
        public String toString() {
                SimpleDateFormat s = new SimpleDateFormat(this.format);
                return s.format(this.date);
        }

        public float toRadianAngle() {
                float f = (this.date.getTime() / 60000f) % 1440;
                return f * 360f / 1440f;
        }

        public timeLabel addHours(int hours) {
            this.date = new Date(this.date.getTime() + 3600000 * hours);
            return this;
        }

        public timeLabel addMinutes(int minutes) {
            this.date = new Date(this.date.getTime() + 60000 * minutes);
            return this;
        }
    }

    class dateLabel {
        private String label;
        private float size;
        private int color;
        private Typeface type;
        private float pad;

        dateLabel(String text, float size, int color, Typeface t, float pad) {
            this.label = text;
            this.size = size;
            this.color = color;
            this.type = t;
            this.pad = pad;
        }
    }
}
