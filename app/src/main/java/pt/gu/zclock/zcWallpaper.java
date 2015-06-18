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

/**
 * Created by GU on 07-06-2015.
 */
public class zcWallpaper extends WallpaperService{

    private static final String TAG = "zcWallpaper";
    private static final boolean debug = true;

    public static final String intentUpdater = "pt.gu.zclock.zcWallpaper";

    private static PictureDrawable svgPicture;
    private static SVG svgBackground;

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

    public class zcEngine extends Engine{

        private boolean mVisible = false;
        private final Handler mHandler = new Handler();
        private SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        private int color = 0;
        private int x_offset=0;

        private Bitmap background;
        private boolean checkBackground = true;

        private int[] upColors,dwColors;
        private zcHelper.colorGradient uGrad, dGrad;

        private final Runnable mUpdateDisplay = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        private BroadcastReceiver broadcastUpdater = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(intentUpdater)){
                    updateGradient();
                }
            }
        };

        private IntentFilter intentFilter = new IntentFilter();
        {
            intentFilter.addAction(intentUpdater);
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            boolean castHaze = mPrefs.getBoolean("wpOverlay",true);
            Canvas c = null;

            try {
                c = holder.lockCanvas();
                if (c != null) {
                    updateGradient();
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
                    zcHelper.SolarTime s = new zcHelper.SolarTime(mPrefs.getLong("sunsettime",0),(double)mPrefs.getFloat("latitude",0));
                    zcHelper.RangeF y = new zcHelper.RangeF(1f,0);
                    float h = c.getHeight();
                    float y0 = y.scale(s.getSunPosition(),0,h*0.8f);
                    p.setShader(new LinearGradient(0, y0, 0, h, c1, c2, Shader.TileMode.CLAMP));
                    c.drawPaint(p);
                    if (checkBackground){
                        background= zcHelper.xSGV.getBitmap(getApplicationContext(),"svg/mountains.svg",c.getWidth(),(int)(h/7));
                        checkBackground = (background!=null);
                    }
                    if (!checkBackground&&background!=null){
                        c.drawBitmap(background, x_offset, c.getHeight()-background.getHeight(), p);
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
                registerReceiver(broadcastUpdater,intentFilter);
                draw();
            } else {
                unregisterReceiver(broadcastUpdater);
                mHandler.removeCallbacks(mUpdateDisplay);
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
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mVisible = false;
            mHandler.removeCallbacks(mUpdateDisplay);
        }

        private void updateGradient(){

            float[] pos = new float[]{
                    getDayFraction(mPrefs.getLong("midnight", 0)),
                    getDayFraction(mPrefs.getLong("alot72", 0)),
                    getDayFraction(mPrefs.getLong("sunrisetime", 0)),
                    getDayFraction(mPrefs.getLong("midday", 0)),
                    getDayFraction(mPrefs.getLong("sunsettime", 0)),
                    getDayFraction(mPrefs.getLong("tzait72", 0))};

            Resources res = getApplicationContext().getResources();
            if (upColors == null) upColors  = new int[]{
                    res.getColor(R.color.zChtL),
                    res.getColor(R.color.zAlot),
                    res.getColor(R.color.zSris),
                    res.getColor(R.color.zChtY),
                    res.getColor(R.color.zSset),
                    res.getColor(R.color.zTzet)
            };

            if (dwColors == null) dwColors  = new int[]{
                    res.getColor(R.color.hChtL),
                    res.getColor(R.color.hAlot),
                    res.getColor(R.color.hSris),
                    res.getColor(R.color.hChtY),
                    res.getColor(R.color.hSset),
                    res.getColor(R.color.hTzet)
            };

            uGrad = new zcHelper.colorGradient(upColors,pos);
            dGrad = new zcHelper.colorGradient(dwColors,pos);
        }

        private float getDayFraction(long time){
            return time/60000%1440/1440f;
        }

    }
}
