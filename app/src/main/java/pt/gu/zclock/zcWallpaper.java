package pt.gu.zclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

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
        Engine e =new zcEngine();
        return e;
    }

    public class zcEngine extends Engine{

        private boolean mVisible = false;
        private final Handler mHandler = new Handler();
        private SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        private int color = 0;

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
            return (time/60000%1440)/1440f;
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
                    p.setShader(new LinearGradient(0,0,0,c.getHeight(),c1,c2, Shader.TileMode.CLAMP));
                    if (castHaze){
                        try{
                            zcHelper.WeatherData w = new zcHelper.WeatherData();
                            String json = mPrefs.getString("currWeather", "");
                            w.fromJSONString(json);
                            ColorMatrix colorMatrix = new ColorMatrix();
                            zcHelper.xColor.adjustSaturation(colorMatrix,w.getAmbientHaze(0.01f,.7f));
                            p.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
                            color = zcHelper.xColor.setAlpha(8,w.getColorCondition(0.6f));
                        } catch (Exception e){
                            Log.e(TAG, e.toString());
                        }

                    }
                    c.drawPaint(p);
                    if (castHaze) c.drawColor(color);
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

    }
}
