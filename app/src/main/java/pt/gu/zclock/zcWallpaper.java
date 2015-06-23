package pt.gu.zclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.PictureDrawable;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.support.v4.hardware.display.DisplayManagerCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.applantation.android.svg.SVG;
import com.applantation.android.svg.SVGParser;

import net.sourceforge.zmanim.AstronomicalCalendar;
import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

/**
 * Created by GU on 07-06-2015.
 */
public class zcWallpaper extends WallpaperService{

    private static final String TAG = "zcWallpaper";
    private static final boolean debug = true;

    public static final String intentUpdater = "pt.gu.zclock.zcWallpaper";

    /**
     * Must be implemented to return a new instance of the wallpaper's engine.
     * Note that multiple instances may be active at the same time, such as
     * when the wallpaper is currently set as the active wallpaper and the user
     * is in the wallpaper picker viewing a preview of it as well.
     */
    @Override
    public Engine onCreateEngine() {
        return new zcEngine();
    }

    public class zcEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener{

        private boolean         mVisible = false;
        private final Handler   mHandler = new Handler();
        private int             x_offset=0;
        private int             canvas_width=1;
        private float           xoffset_factor=1;
        private Bitmap          background;
        private int[]           upColors,dwColors;
        private zcHelper.colorGradient uGrad, dGrad;

        private SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        private final Runnable mUpdateDisplay = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            boolean castHaze = mPrefs.getBoolean("wpOverlay",true);
            Canvas c = null;

            try {
                c = holder.lockCanvas();
                if (c != null) {
                    canvas_width = c.getWidth();

                    if (uGrad == null || dGrad == null) updateGradient();

                    int c1 = uGrad.getColor(getDayFraction(System.currentTimeMillis()));
                    int c2 = dGrad.getColor(getDayFraction(System.currentTimeMillis()));
                    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                    if (castHaze){
                        try{
                            zcHelper.WeatherData w = new zcHelper.WeatherData();
                            String json = mPrefs.getString("currWeather", "");
                            w.fromJSONString(json);
                            c1 = zcHelper.xColor.adjustSaturation(c1,-w.getClouds(0.9f, -0.1f));
                            c2 = zcHelper.xColor.adjustSaturation(c2,w.getHumidity(0.2f,0));
                            c2 = zcHelper.xColor.adjustSaturation(c2,-w.getClouds(0.9f, -0.1f));
                        } catch (Exception e){
                            Log.e(TAG, e.toString());
                        }

                    }
                    zcHelper.SolarTime s = new zcHelper.SolarTime(mPrefs.getLong("sunset",0),(double)mPrefs.getFloat("latitude",0));
                    zcHelper.RangeF y = new zcHelper.RangeF(1f,0);
                    float h = c.getHeight();
                    float y0 = y.scale(s.getSunPosition(),0,h*0.7f);
                    p.setShader(new LinearGradient(0, y0, 0, h*0.9f, c1, c2, Shader.TileMode.CLAMP));
                    c.drawPaint(p);
                    if (background==null){
                        updateBackground();
                    } else {
                        p = new Paint(Paint.ANTI_ALIAS_FLAG);
                        p.setColor(Color.BLACK);
                        c.drawBitmap(background, (int)(x_offset*xoffset_factor), c.getHeight() - background.getHeight(), p);
                    }
                }
            } finally {
                if (c != null)
                    holder.unlockCanvasAndPost(c);
            }

            mHandler.removeCallbacks(mUpdateDisplay);
            if (mVisible) {
                mHandler.postDelayed(mUpdateDisplay, 60000);
            }
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                                     float xStep, float yStep, int xPixels, int yPixels) {
            x_offset = xPixels;
            draw();
        }


        @Override
        public void onVisibilityChanged(boolean visible) {

            mVisible = visible;
            if (visible) {
                draw();
                mPrefs.registerOnSharedPreferenceChangeListener(this);
            } else {
                mHandler.removeCallbacks(mUpdateDisplay);
                mPrefs.unregisterOnSharedPreferenceChangeListener(this);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            draw();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            mVisible = false;
            mHandler.removeCallbacks(mUpdateDisplay);
            mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mVisible = false;
            mHandler.removeCallbacks(mUpdateDisplay);
        }

        private void updateBackground(){
            background= zcHelper.xSGV.getBitmap(getApplicationContext(),mPrefs.getString("wptLandscape","svg/mountains.svg"));
            xoffset_factor = background.getWidth()/canvas_width;
        }

        private void updateGradient(){

            float[] pos = new float[]{
                    getDayFraction(mPrefs.getLong("midnight",0)),
                    getDayFraction(mPrefs.getLong("sAstTw",0)),
                    getDayFraction(mPrefs.getLong("sNauTw", 0)),
                    getDayFraction(mPrefs.getLong("sunrise", 0)),
                    getDayFraction(mPrefs.getLong("chatzot", 0)),
                    getDayFraction(mPrefs.getLong("sunset", 0)),
                    getDayFraction(mPrefs.getLong("eNauTw",0)),
                    getDayFraction(mPrefs.getLong("eAstTw", 0))};

            Resources res = getApplicationContext().getResources();
            if (upColors == null) upColors  = new int[]{
                    res.getColor(R.color.zChtL), //0
                    res.getColor(R.color.zAl72), //1
                    res.getColor(R.color.zAl60), //2
                    res.getColor(R.color.zSris), //3
                    res.getColor(R.color.zChtY), //4
                    res.getColor(R.color.zSset), //5
                    res.getColor(R.color.zTz60), //6
                    res.getColor(R.color.zTz72)  //7
            };

            if (dwColors == null) dwColors  = new int[]{
                    res.getColor(R.color.hChtL), //0
                    res.getColor(R.color.hAl72), //1
                    res.getColor(R.color.hAl60), //2
                    res.getColor(R.color.hSris), //3
                    res.getColor(R.color.hChtY), //4
                    res.getColor(R.color.hSset), //5
                    res.getColor(R.color.hTz60), //6
                    res.getColor(R.color.hTz72)  //7
            };

            uGrad = new zcHelper.colorGradient(upColors,pos);
            dGrad = new zcHelper.colorGradient(dwColors,pos);
        }

        private float getDayFraction(long time){
            return time/60000%1440/1440f;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("wptLandscape")){
                background= zcHelper.xSGV.getBitmap(getApplicationContext(),mPrefs.getString("wptLandscape","svg/mountains.svg"));
            } else if (key.equals("latitude") || key.equals("longitude")){
                updateGradient();
            }
        }
    }
}
