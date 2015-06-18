package pt.gu.zclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.PictureDrawable;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;

import com.applantation.android.svg.SVG;
import com.applantation.android.svg.SVGParseException;
import com.applantation.android.svg.SVGParser;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by GU on 01-05-2015.
 */
public class zcHelper {

    private static final String TAG ="zcHelper";
    private static final boolean debug = true;

    public enum owmCode {
        UNKNOWN(Integer.MIN_VALUE),
        /* Thunderstorm */
        THUNDERSTORM_WITH_LIGHT_RAIN(200),
        THUNDERSTORM_WITH_RAIN(201),
        THUNDERSTORM_WITH_HEAVY_RAIN(202),
        LIGHT_THUNDERSTORM(210),
        THUNDERSTORM(211),
        HEAVY_THUNDERSTORM(212),
        RAGGED_THUNDERSTORM(221),
        THUNDERSTORM_WITH_LIGHT_DRIZZLE(230),
        THUNDERSTORM_WITH_DRIZZLE(231),
        THUNDERSTORM_WITH_HEAVY_DRIZZLE(232),
        /* Drizzle */
        LIGHT_INTENSITY_DRIZZLE(300),
        DRIZZLE(301),
        HEAVY_INTENSITY_DRIZZLE(302),
        LIGHT_INTENSITY_DRIZZLE_RAIN(310),
        DRIZZLE_RAIN(311),
        HEAVY_INTENSITY_DRIZZLE_RAIN(312),
        SHOWER_DRIZZLE(321),
        /* Rain */
        LIGHT_RAIN(500),
        MODERATE_RAIN(501),
        HEAVY_INTENSITY_RAIN(502),
        VERY_HEAVY_RAIN(503),
        EXTREME_RAIN(504),
        FREEZING_RAIN(511),
        LIGHT_INTENSITY_SHOWER_RAIN(520),
        SHOWER_RAIN(521),
        HEAVY_INTENSITY_SHOWER_RAIN(522),
        /* Snow */
        LIGHT_SNOW(600),
        SNOW(601),
        HEAVY_SNOW(602),
        SLEET(611),
        SHOWER_SNOW(621),
        /* Atmosphere */
        MIST(701),
        SMOKE(711),
        HAZE(721),
        SAND_OR_DUST_WHIRLS(731),
        FOG(741),
        /* Clouds */
        SKY_IS_CLEAR(800),
        FEW_CLOUDS(801),
        SCATTERED_CLOUDS(802),
        BROKEN_CLOUDS(803),
        OVERCAST_CLOUDS(804),
        /* Extreme */
        TORNADO(900),
        TROPICAL_STORM(901),
        HURRICANE(902),
        COLD(903),
        HOT(904),
        WINDY(905),
        HAIL(906);

        public final int id;

        owmCode(final int code) {
            this.id = code;
        }

        public int index() {
            return this.ordinal();
        }

        owmCode getEnum(int code){
            for (owmCode c:owmCode.values()){
                if (c.id == code) return c;
            }
            return null;
        }

        public float getColorS(){
            switch (this){
                case UNKNOWN: return 0;
        /* Thunderstorm */
                case THUNDERSTORM_WITH_LIGHT_RAIN: return 0.5f;
                case THUNDERSTORM_WITH_RAIN: return 0.4f;
                case THUNDERSTORM_WITH_HEAVY_RAIN: return 0.3f;
                case LIGHT_THUNDERSTORM: return 0.5f;
                case THUNDERSTORM: return 0.3f;
                case HEAVY_THUNDERSTORM: return 0.2f;
                case RAGGED_THUNDERSTORM: return 0.4f;
                case THUNDERSTORM_WITH_LIGHT_DRIZZLE: return 0.5f;
                case THUNDERSTORM_WITH_DRIZZLE: return 0.45f;
                case THUNDERSTORM_WITH_HEAVY_DRIZZLE: return 0.3f;
        /* Drizzle */
                case LIGHT_INTENSITY_DRIZZLE: return 0.3f;
                case DRIZZLE: return 0.3f;
                case HEAVY_INTENSITY_DRIZZLE: return 0.3f;
                case LIGHT_INTENSITY_DRIZZLE_RAIN: return 0.3f;
                case DRIZZLE_RAIN: return 0.3f;
                case HEAVY_INTENSITY_DRIZZLE_RAIN: return 0.3f;
                case SHOWER_DRIZZLE: return 0.2f;
        /* Rain */
                case LIGHT_RAIN: return 0.4f;
                case MODERATE_RAIN: return 0.3f;
                case HEAVY_INTENSITY_RAIN: return 0.2f;
                case VERY_HEAVY_RAIN: return 0.1f;
                case EXTREME_RAIN: return 0.1f;
                case FREEZING_RAIN: return 0.3f;
                case LIGHT_INTENSITY_SHOWER_RAIN: return 0.4f;
                case SHOWER_RAIN: return 0.3f;
                case HEAVY_INTENSITY_SHOWER_RAIN: return 0.3f;
        /* Snow */
                case LIGHT_SNOW: return 0.3f;
                case SNOW: return 0.5f;
                case HEAVY_SNOW: return 0.2f;
                case SLEET: return 0.6f;
                case SHOWER_SNOW: return 0.4f;
        /* Atmosphere */
                case MIST: return 0.7f;
                case SMOKE: return 0.4f;
                case HAZE: return 0.2f;
                case SAND_OR_DUST_WHIRLS: return 0.7f;
                case FOG: return 0.2f;
        /* Clouds */
                case SKY_IS_CLEAR: return 0.9f;
                case FEW_CLOUDS: return 0.85f;
                case SCATTERED_CLOUDS: return 0.82f;
                case BROKEN_CLOUDS: return 0.8f;
                case OVERCAST_CLOUDS: return 0.75f;
        /* Extreme */
                case TORNADO: return 0.7f;
                case TROPICAL_STORM: return 0.7f;
                case HURRICANE: return 0.7f;
                case COLD: return 1;
                case HOT: return 1;
                case WINDY: return 1;
                case HAIL: return 0;

                default: return 0;
            }
        }

        public float getColorV(){
            switch (this){
                case UNKNOWN: return 0;
        /* Thunderstorm */
                case THUNDERSTORM_WITH_LIGHT_RAIN: return 0.5f;
                case THUNDERSTORM_WITH_RAIN: return 0.4f;
                case THUNDERSTORM_WITH_HEAVY_RAIN: return 0.3f;
                case LIGHT_THUNDERSTORM: return 0.5f;
                case THUNDERSTORM: return 0.3f;
                case HEAVY_THUNDERSTORM: return 0.2f;
                case RAGGED_THUNDERSTORM: return 0.4f;
                case THUNDERSTORM_WITH_LIGHT_DRIZZLE: return 0.5f;
                case THUNDERSTORM_WITH_DRIZZLE: return 0.45f;
                case THUNDERSTORM_WITH_HEAVY_DRIZZLE: return 0.3f;
        /* Drizzle */
                case LIGHT_INTENSITY_DRIZZLE: return 0.8f;
                case DRIZZLE: return 0.7f;
                case HEAVY_INTENSITY_DRIZZLE: return 0.7f;
                case LIGHT_INTENSITY_DRIZZLE_RAIN: return 0.6f;
                case DRIZZLE_RAIN: return 0.5f;
                case HEAVY_INTENSITY_DRIZZLE_RAIN: return 0.4f;
                case SHOWER_DRIZZLE: return 0.6f;
        /* Rain */
                case LIGHT_RAIN: return 0.6f;
                case MODERATE_RAIN: return 0.5f;
                case HEAVY_INTENSITY_RAIN: return 0.4f;
                case VERY_HEAVY_RAIN: return 0.3f;
                case EXTREME_RAIN: return 0.2f;
                case FREEZING_RAIN: return 0.5f;
                case LIGHT_INTENSITY_SHOWER_RAIN: return 0.7f;
                case SHOWER_RAIN: return 0.5f;
                case HEAVY_INTENSITY_SHOWER_RAIN: return 0.4f;
        /* Snow */
                case LIGHT_SNOW: return 0.8f;
                case SNOW: return 0.7f;
                case HEAVY_SNOW: return 0.6f;
                case SLEET: return 0.6f;
                case SHOWER_SNOW: return 0.4f;
        /* Atmosphere */
                case MIST: return 0.8f;
                case SMOKE: return 0.7f;
                case HAZE: return 0.8f;
                case SAND_OR_DUST_WHIRLS: return 0.7f;
                case FOG: return 0.7f;
        /* Clouds */
                case SKY_IS_CLEAR: return 0.95f;
                case FEW_CLOUDS: return 0.85f;
                case SCATTERED_CLOUDS: return 0.8f;
                case BROKEN_CLOUDS: return 0.72f;
                case OVERCAST_CLOUDS: return 0.65f;
        /* Extreme */
                case TORNADO: return 0.7f;
                case TROPICAL_STORM: return 0.7f;
                case HURRICANE: return 0.7f;
                case COLD: return 1;
                case HOT: return 1;
                case WINDY: return 1;
                case HAIL: return 0;

                default: return 0;
            }
        }
    }
    
    public static class Range{
        private int max;
        private int min;
        
        public Range (int max,int min){
            this.max = max;
            this.min = min;
        }
        
        public int get (int value){
            if (value >max) return max;
            if (value <min) return min;
            return value;
        }

        public int scale (int value, Range newRange){
            return (int)this.scaleF(value, newRange);
        }

        public float scaleF (int value, Range newRange){
            float v = this.get(value);
            float s = this.getSize();
            float l = newRange.getSize();
            if (s == 0|| l == 0) return (int)v;
            s = (v-this.min)/s;
            return newRange.min+s*l;
        }

        public int getSize(){
            return this.max-this.min;
        }

    }

    public static class RangeF{
        private float max;
        private float min;

        public RangeF (float max,float min){
            this.max = max;
            this.min = min;
        }

        public float get (float value){
            if (value >max) return max;
            if (value <min) return min;
            return value;
        }
        
        public float scale (float value, RangeF newRange){
            float v = this.get(value);
            float s = this.getSize();
            float l = newRange.getSize();
            if (s == 0 || l == 0) return v;
            s = (v-this.min)/s;
            return newRange.min+s*l;
        }

        public float scale (float value, float max, float min){
            RangeF r = new RangeF(max,min);
            return this.scale(value,r);
        }
        
        public float getSize(){
            return this.max-this.min;
        }
    }

    public static class xColor extends Color {

        public static int mix(int c1, int c2, float p){
            float   q = 1-p;
            int     a = (int)((float)alpha(c1)*q+(float)alpha(c2)*p);
            int     r = (int)((float)red(c1)*q+(float)red(c2)*p);
            int     g = (int)((float)green(c1)*q+(float)green(c2)*p);
            int     b = (int)((float)blue(c1)*q+(float)blue(c2)*p);
            return argb(a, r, g, b);
        }
        
        public static int setAlpha(int alpha, int color){
            return ( alpha << 24 ) | ( color & 0x00ffffff);
        }
        public static int copyAlpha(int source, int dest){
            return setAlpha(alpha(source),dest);
        }

        public static int setHue(float hue, int color){
            float hsv[] = new float[3];
            int a = alpha(color);
            colorToHSV(color,hsv);
            hsv[0] = hue;
            return HSVToColor(a, hsv);
        }

        public static float getHue(int color){
            float hsv[] = new float[3];
            colorToHSV(color,hsv);
            return hsv[0];
        }
        
        public static float adjustHue(int color, float hueShift){
            float hsv[] = new float[3];
            int a = alpha(color);
            colorToHSV(color,hsv);
            hsv[0] = (hsv[0]+hueShift)%360f;
            return HSVToColor(a, hsv); 
        }

        public static int copyHue(int source, int dest) {
            float h = getHue(source);
            return setHue(h,dest);
        }

        public static int setSaturation(float sat, int color){
            float hsv[] = new float[3];
            int a = alpha(color);
            colorToHSV(color,hsv);
            hsv[1] = sat;
            return HSVToColor(a, hsv);
        }
        
        public static float getSaturation(int color){
            float hsv[] = new float[3];
            int a = alpha(color);
            colorToHSV(color,hsv);
            return hsv[1];
        }

        public static int adjustSaturation(int color, float satShift){
            float hsv[] = new float[3];
            int a = alpha(color);
            colorToHSV(color,hsv);
            RangeF rsat= new RangeF(1f,0f);
            hsv[1] = rsat.get(satShift+hsv[1]);
            return HSVToColor(a, hsv);
        }
        
        public static int copySaturation(int source, int dest){
            float s = getSaturation(source);
            return setSaturation(s,dest);
        }

        public static int setLuminosity(int color,float lum){
            float ahsl[] = ColorToAHSL(color);
            ahsl[3] = lum;
            return AHSLToColor(ahsl);
        }

        public static float getLuminosity(int color){
            float ahsl[] = ColorToAHSL(color);
            return ahsl[3];
        }

        public static int adjustLuminosity(int color, float lumShift){
            float ahsl[] = ColorToAHSL(color);
            RangeF rsat= new RangeF(1f,0f);
            ahsl[3] = rsat.get(lumShift+ahsl[3]);
            return AHSLToColor(ahsl);
        }

        public static int setVal(float val, int color){
            float hsv[] = new float[3];
            int a = alpha(color);
            colorToHSV(color,hsv);
            hsv[2] = val;
            return HSVToColor(a, hsv);
        }

        public static float getVal(int color){
            float hsv[] = new float[3];
            colorToHSV(color,hsv);
            return hsv[2];
        }

        public static int adjustVal(int color, float valShift){
            float hsv[] = new float[3];
            int a = alpha(color);
            colorToHSV(color,hsv);
            RangeF rsat= new RangeF(1f,0f);
            hsv[2] = rsat.get(valShift+hsv[2]);
            return HSVToColor(a,hsv);
        }

        public static int getAHSV(int alpha, float hue, float sat, float val){
            return HSVToColor(alpha, new float[]{hue, sat,val});
        }

        public static int getAHSL(int alpha, float hue, float sat, float lum){
            return AHSLToColor(new float[]{alpha,hue,sat,lum});
        }

        public static float[] ColorToAHSL(int color){
            float hsv[] = new float[3];
            colorToHSV(color,hsv);
            float hsl[] = HSVtoHSL(hsv);
            return new float[]{alpha(color),hsl[0],hsl[1],hsl[2]};
        }

        public static int AHSLToColor(float[] ahsl){
            float ahsv[] = AHSLtoAHSV(ahsl);
            return HSVToColor((int) ahsv[0], new float[]{ahsv[1], ahsv[2], ahsv[3]});
        }

        public static float[] AHSVtoAHSL(float[] ahsv){
            float ahsl[] = new float[4];
            ahsl[0] = ahsv[0];
            ahsl[1] = ahsv[1];
            ahsl[2] = ahsv[2] * ahsv[3];
            ahsl[3] = (2-ahsv[1])*ahsv[3];
            ahsl[2] /= (ahsl[3]<=1) ? ahsl[3] : 2-ahsl[3];
            ahsl[3] /= 2;
            return ahsl;
        }

        public static float[] HSVtoHSL(float[] hsv){
            float hsl[] = new float[3];
            hsl[0] = hsv[0];
            hsl[1] = hsv[1] * hsv[2];
            hsl[2] = (2-hsv[0])*hsv[2];
            hsl[1] /= (hsl[2]<=1) ? hsl[2] : 2-hsl[2];
            hsl[2] /= 2;
            return hsl;
        }

        public static float[] AHSLtoAHSV(float[] ahsl){
            float ahsv[] = new float[4];
            ahsv[0] = ahsl[0];
            ahsv[1] = ahsl[1];
            ahsl[3] *= 2;
            ahsl[2] *= (ahsl[3]<=1) ? ahsl[3] : 2-ahsl[3];
            ahsv[3] = (ahsl[2]+ahsl[3])/2;
            ahsv[2] = 2*ahsl[2] / (ahsl[3]+ahsl[2]);
            return ahsv;
        }

        public static float[] HSLtoHSV(float[] hsl){
            float hsv[] = new float[3];
            hsv[0] = hsl[0];
            hsl[2] *= 2;
            hsl[1] *= (hsl[2]<=1) ? hsl[2] : 2-hsl[2];
            hsv[2] = (hsl[1]+hsl[2])/2;
            hsv[1] = 2*hsl[1] / (hsl[2]+hsl[1]);
            return hsv;
        }

        private static double DELTA_INDEX[] = {
                0,    0.01, 0.02, 0.04, 0.05, 0.06, 0.07, 0.08, 0.1,  0.11,
                0.12, 0.14, 0.15, 0.16, 0.17, 0.18, 0.20, 0.21, 0.22, 0.24,
                0.25, 0.27, 0.28, 0.30, 0.32, 0.34, 0.36, 0.38, 0.40, 0.42,
                0.44, 0.46, 0.48, 0.5,  0.53, 0.56, 0.59, 0.62, 0.65, 0.68,
                0.71, 0.74, 0.77, 0.80, 0.83, 0.86, 0.89, 0.92, 0.95, 0.98,
                1.0,  1.06, 1.12, 1.18, 1.24, 1.30, 1.36, 1.42, 1.48, 1.54,
                1.60, 1.66, 1.72, 1.78, 1.84, 1.90, 1.96, 2.0,  2.12, 2.25,
                2.37, 2.50, 2.62, 2.75, 2.87, 3.0,  3.2,  3.4,  3.6,  3.8,
                4.0,  4.3,  4.7,  4.9,  5.0,  5.5,  6.0,  6.5,  6.8,  7.0,
                7.3,  7.5,  7.8,  8.0,  8.4,  8.7,  9.0,  9.4,  9.6,  9.8,
                10.0
        };

        public static void adjustHue(ColorMatrix cm, float value)
        {
            value = cleanValue(value, 180f) / 180f * (float) Math.PI;
            if (value == 0){
                return;
            }

            float cosVal = (float) Math.cos(value);
            float sinVal = (float) Math.sin(value);
            float lumR = 0.213f;
            float lumG = 0.715f;
            float lumB = 0.072f;
            float[] mat = new float[]
                    {
                            lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
                            lumR + cosVal * (-lumR) + sinVal * (0.143f), lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
                            lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0,
                            0f, 0f, 0f, 1f, 0f,
                            0f, 0f, 0f, 0f, 1f };
            cm.postConcat(new ColorMatrix(mat));
        }

        public static void adjustBrightness(ColorMatrix cm, float value) {
            value = cleanValue(value,100);
            if (value == 0) {
                return;
            }

            float[] mat = new float[]
                    {
                            1,0,0,0,value,
                            0,1,0,0,value,
                            0,0,1,0,value,
                            0,0,0,1,0,
                            0,0,0,0,1
                    };
            cm.postConcat(new ColorMatrix(mat));
        }

        /**
         *
         * @param cm ColorMatrix
         * @param value (0-100)
         */
        public static void adjustContrast(ColorMatrix cm, int value) {
            value = (int)cleanValue(value,100);
            if (value == 0) {
                return;
            }
            float x;
            if (value < 0) {
                x = 127 + value / 100*127;
            } else {
                x = value % 1;
                if (x == 0) {
                    x = (float)DELTA_INDEX[value];
                } else {
                    //x = DELTA_INDEX[(p_val<<0)]; // this is how the IDE does it.
                    x = (float)DELTA_INDEX[(value<<0)]*(1-x) + (float)DELTA_INDEX[(value<<0)+1] * x; // use linear interpolation for more granularity.
                }
                x = x*127+127;
            }

            float[] mat = new float[]
                    {
                            x/127,0,0,0, 0.5f*(127-x),
                            0,x/127,0,0, 0.5f*(127-x),
                            0,0,x/127,0, 0.5f*(127-x),
                            0,0,0,1,0,
                            0,0,0,0,1
                    };
            cm.postConcat(new ColorMatrix(mat));

        }

        /**
         *
         * @param cm ColorMatrix
         * @param value (0-100)
         */
        public static void adjustSaturation(ColorMatrix cm, float value) {
            value = cleanValue(value,100);
            if (value == 0) {
                return;
            }

            float x = 1+((value > 0) ? 3 * value / 100 : value / 100);
            float lumR = 0.3086f;
            float lumG = 0.6094f;
            float lumB = 0.0820f;

            float[] mat = new float[]
                    {
                            lumR*(1-x)+x,lumG*(1-x),lumB*(1-x),0,0,
                            lumR*(1-x),lumG*(1-x)+x,lumB*(1-x),0,0,
                            lumR*(1-x),lumG*(1-x),lumB*(1-x)+x,0,0,
                            0,0,0,1,0,
                            0,0,0,0,1
                    };
            cm.postConcat(new ColorMatrix(mat));
        }

        protected static float cleanValue(float p_val, float p_limit) {
            return Math.min(p_limit, Math.max(-p_limit, p_val));
        }

        public static ColorFilter adjustColor(int brightness, int contrast, int saturation, int hue){
            ColorMatrix cm = new ColorMatrix();
            adjustHue(cm, hue);
            adjustContrast(cm, contrast);
            adjustBrightness(cm, brightness);
            adjustSaturation(cm, saturation);

            return new ColorMatrixColorFilter(cm);
        }
    }

    public static class colorGradient{

        private int size;
        private int arraysize;
        private int[] colors;
        private float[] positions;

        public colorGradient(int[] colors,float[] positions){
            this.colors=colors;
            this.positions=positions;
        }

        public int getColor(float pos){
            int c1 = 0,c2 =0;
            float x=0,len =0;
            int lastIndex = positions.length-1;
            if (pos < positions[0]){
                c1 = colors[lastIndex];
                c2 = colors[0];
                len = 1-positions[lastIndex]+positions[0];
                x = (1-positions[lastIndex]+pos)/len;
                if (debug) Log.e(TAG,String.format("/colorGrad/getColor %08X-%08X [%.2f]",c1,c2,x));
                return xColor.mix(c1,c2,x);
            } else if (pos > positions[lastIndex]){
                c1 = colors[lastIndex];
                c2 = colors[0];
                len = 1-positions[lastIndex]+positions[0];
                x = (pos-positions[lastIndex])/len;
                if (debug) Log.e(TAG,String.format("/colorGrad/getColor %08X-%08X [%.2f]",c1,c2,x));
                return xColor.mix(c1,c2,x);
            } else {
                for (int i=0;i<positions.length-1;i++) {
                    if (pos >= positions[i] && pos<positions[i+1]) {
                        if (pos == positions[i]) return colors[i];
                        c1 = colors[i];
                        c2 = colors[i+1];
                        x = (pos-positions[i])/(positions[i+1]-positions[i]);
                        break;
                    }
                }
                if (debug) Log.e(TAG,String.format("/colorGrad/getColor %08X-%08X [%.2f]",c1,c2,x));
                return xColor.mix(c1,c2,x);
            }

            /*if (pos<positions[0] || pos>positions[lastIndex]){
                hi = 0;
                lo = lastIndex;
            } else {
                for (int i=0;i<positions.length;i++) {
                    if (positions[i] >= pos) hi=i;
                    if (positions[i] <= pos) lo=i;
                }
            }
            float   len = (positions[hi]-positions[lo] + 1) % 1;
            if (len == 0) return colors[hi];
            float   x1 = ((pos - positions[lo] + 1) % 1)/len;

            return xColor.mix(colors[hi],colors[lo],x1);
            */
        }
    }

    public static class WeatherData{

        public long   dt;
        public double main_temp;
        public double main_temp_min;
        public double main_temp_max;
        public double main_pressure;
        public double main_sea_level;
        public double main_grnd_level;
        public int    main_humidity;
        public int    main_temp_kf;
        public int    weather_id;
        public int    clouds_all;
        public double rain_3h;
        public double wind_speed;
        public double wind_deg;
        public String sys_pod;


        public long getTime(){
            return dt*1000;
        }

        public void setTime(Long milis){
            this.dt = milis/1000;
        }

        public float getCelsius(){
            return (float)this.main_temp-273f;
        }

        public void setCelsius(float celsius){
            this.main_temp = (double)celsius+273;
        }
        
        public boolean parseJSONObject(JSONObject json) {
            try {
                this.dt = json.getLong("dt");
                JSONObject jMain = json.getJSONObject("main");
                this.main_temp      = jMain.getDouble("temp");
                this.main_temp_min  = jMain.getDouble("temp_min");
                this.main_temp_max  = jMain.getDouble("temp_max");
                this.main_pressure  = jMain.getDouble("pressure");
                this.main_sea_level = jMain.getDouble("sea_level");
                this.main_grnd_level= jMain.getDouble("grnd_level");
                this.main_humidity  = jMain.getInt("humidity");
                this.main_temp_kf   = jMain.getInt("temp_kf");
                this.weather_id     = json.getJSONArray("weather").getJSONObject(0).getInt("id");
                this.clouds_all     = tryInt(json,"clouds","all");
                this.rain_3h        = tryDouble(json,"rain","3h");
                this.wind_speed     = tryDouble(json,"wind","speed");
                this.wind_deg       = tryDouble(json,"wind","deg");
                this.sys_pod        = json.getJSONObject("sys").getString("pod");
                return true;
            } catch (JSONException ignore){
                if (debug) Log.d(TAG, "/WeatherData/parseJSON: "+ignore.toString());
                return false;
            }
        }

        public String toJSONString(){
            Gson g = new Gson();
            return g.toJson(this);
        }

        public void fromJSONString(String json){
            Gson g = new Gson();
            try{
                BufferedReader b = new BufferedReader(new StringReader(json));
                WeatherData w = g.fromJson(b,WeatherData.class);
                this.dt=w.dt;
                this.main_temp=w.main_temp;
                this.main_temp_min=w.main_temp_min;
                this.main_temp_max=w.main_temp_max;
                this.main_pressure=w.main_pressure;
                this.main_sea_level=w.main_sea_level;
                this.main_grnd_level=w.main_grnd_level;
                this.main_humidity=w.main_humidity;
                this.main_temp_kf=w.main_temp_kf;
                this.weather_id=w.weather_id;
                this.sys_pod=w.sys_pod;
                this.clouds_all=w.clouds_all;
                this.wind_speed=w.wind_speed;
                this.wind_deg=w.wind_deg;
                this.rain_3h=w.rain_3h;
            } catch (Exception e){
                Log.e(TAG,e.toString());
            }
        }

        private double tryDouble (JSONObject obj,String parent, String child){
            try{
                return obj.getJSONObject(parent).getDouble(child);
            } catch (JSONException ignore){
                if (debug) Log.d(TAG, "/WeatherData/tryDouble: "+ ignore.toString());
                return 0;
            }
        }

        private int tryInt (JSONObject obj,String parent, String child){
            try {
                return obj.getJSONObject(parent).getInt(child);
            } catch (JSONException ignore) {
                if (debug) Log.d(TAG, "/WeatherData/tryInt"+ignore.toString());
                return 0;
            }
        }

        public float getHueTemperature(float maxHue,float maxTemp,float minHue, float minTemp){
            RangeF temp = new RangeF(maxTemp,minTemp);
            return temp.scale(this.getCelsius(),maxHue,minHue);
        }

        public float getHumidity(float maxValue,float minValue){
            RangeF hum = new RangeF(100,0);
            return hum.scale((float)this.main_humidity,maxValue,minValue);
        }

        public float getClouds(float maxValue,float minValue){
            RangeF clouds = new RangeF(100,0);
            return clouds.scale((float)this.clouds_all,maxValue,minValue);
        }

        public float getRain(float maxRain, float maxValue,float minValue){
            if (this.rain_3h == 0 || this.rain_3h == Double.NaN) return 0f;
            RangeF rain = new RangeF(maxRain,0);
            return rain.scale((float)this.rain_3h,maxValue,minValue);
        }

        public float getColorSatWeatherCondition(){
            owmCode c = getEnum(this.weather_id);
            return c.getColorS();
        }

        public float getColorSatWeatherCondition(float max, float min){
            owmCode c = getEnum(this.weather_id);
            RangeF s = new RangeF(1,0);
            return s.scale(c.getColorS(),max,min);
        }

        public float getColorValWeatherCondition(){
            owmCode c = getEnum(this.weather_id);
            return c.getColorV();
        }

        public int getColorCondition(int alpha,float maxTempScale, float minTempScale, float startHue, float adjustsat){
            float[] hsv = {0,0.5f,0.5f};
            float t = (float)this.main_temp-273f;
            if (t>maxTempScale) t=maxTempScale;
            if (t<minTempScale)  t=minTempScale;
            hsv[0] = 360*startHue*(1-(t-minTempScale)/(maxTempScale-minTempScale));
            owmCode c = getEnum(this.weather_id);
            hsv[1] = c.getColorS() * adjustsat;
            hsv[2] = c.getColorV();
            return Color.HSVToColor(alpha, hsv);
        }

        public int getColorCondition(float startHue){
            float[] hsv = {0,0.5f,0.5f};
            RangeF tRange = new RangeF(42f,-10f);
            float t = tRange.get(this.getCelsius());
            hsv[0]  = tRange.scale(t, new RangeF(0, 360 * startHue));
            owmCode c = getEnum(this.weather_id);
            hsv[1]  = c.getColorS();
            hsv[2]  = c.getColorV();
            Range a = new Range(100,0);
            int aa = a.get(this.clouds_all);
            int alpha = a.scale(aa,new Range(90,216));
            //int alpha = new Range(90,216).scale(this.clouds_all,new Range(0,100));
            if (debug) Log.d(TAG,String.format("getColorCondition/alpha: %d, clouds %d",alpha, this.clouds_all));
            return Color.HSVToColor(alpha, hsv);
        }

        public float getAmbientHaze(float max, float min){
            RangeF r = new RangeF(1,0);
            return r.scale((this.clouds_all*2+this.main_humidity)/300f,max,min);
        }


        public int getCloudnRainColor(){
            float[] hsv = {0,0.5f,0.5f};
            hsv[0] = 0;
            hsv[1] = 0;
            hsv[2] = 1;
            int alpha = (int)(255*(1f-(float)this.clouds_all/100f));
            return Color.HSVToColor(alpha,hsv);
        }

        private owmCode getEnum(int code){
            for (owmCode c:owmCode.values()){
                if (c.id == code) return c;
            }
            return owmCode.UNKNOWN;
        }
    }

    public static class themeManager{

        private int mainColor;
        private int contrast;

        public static void setColorTheme(Context context, int appWidgetId, int mainColor, int contrast){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            SharedPreferences.Editor ed = sharedPreferences.edit();
            ed.putInt("cClockFrameOn" + appWidgetId, xColor.setAlpha(contrast, mainColor));
            ed.putInt("cClockFrameOff" + appWidgetId, xColor.setAlpha((int)(contrast*0.1f),mainColor));
            ed.putInt("cZmanim_sun" + appWidgetId, xColor.setAlpha((int)(contrast*0.7f), mainColor));
            ed.putInt("cZmanim_main" + appWidgetId, xColor.setAlpha((int)(contrast*0.8f),mainColor));
            ed.putInt("cZmanimAlotTzet" + appWidgetId, xColor.setAlpha((int)(contrast*0.8f),mainColor));
            ed.putInt("cTime" + appWidgetId, xColor.setAlpha(contrast, mainColor));
            ed.putInt("cTimemarks" + appWidgetId, xColor.setAlpha((int)(contrast*0.8f), mainColor));
            ed.putInt("cParshat" + appWidgetId, xColor.setAlpha((int)(contrast*0.8f), mainColor));
            ed.putInt("cDate" + appWidgetId, xColor.setAlpha((int)(contrast*0.8f), mainColor));
            ed.putInt("cShemot" + appWidgetId, xColor.setAlpha((int)(contrast*0.1f),mainColor));
            ed.apply();
        }
    }

    public static class timeEvents {
        private Date[] date;
        private float size;
        private int color;
        private Typeface type;
        private boolean alignRight;
        private String format;
        private boolean insideFrame = false;

        public timeEvents(Date[] date, float size, int color, Typeface t, String format, boolean insideFrame) {
            this.date = date;
            this.size = size;
            this.color = color;
            this.type = t;
            this.format = format;
            this.insideFrame = insideFrame;
        }

        public int lenght(){
            return this.date.length;
        }

        public void setPaint(Paint p){
            p.setColor(this.color);
            p.setTextSize(this.getSizePx());
            p.setTypeface(this.type);
            p.setTextAlign((this.alignRight) ? Paint.Align.RIGHT : Paint.Align.LEFT);
        }


        public Path getPath(int index, PointF centro, float pad, float raio, float angle_offset,float markWidth){
            float r1, r2;
            if (this.insideFrame) {
                r2 = raio - pad;
                r1 = r2 - markWidth;
            } else {
                r1 = raio + pad;
                r2 = r1 + markWidth;
            }
            Path p = new Path();
            double angle = (this.dateToAngle(index) + angle_offset) * Math.PI / 180;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            if (cos < 0) {
                p.moveTo((float) (r2 * cos) + centro.x, (float) (r2 * sin) + centro.y);
                p.lineTo((float) (r1 * cos) + centro.x, (float) (r1 * sin) + centro.y);
                this.alignRight = !this.insideFrame;
            } else {
                p.moveTo((float) (r1 * cos) + centro.x, (float) (r1 * sin) + centro.y);
                p.lineTo((float) (r2 * cos) + centro.x, (float) (r2 * sin) + centro.y);
                this.alignRight = this.insideFrame;
            }
            return p;
        }

        public float getSizePx(){
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, this.size, Resources.getSystem().getDisplayMetrics());
        }

        public boolean isInsideFrame(){ return insideFrame;}

        public boolean isAlignRight() { return alignRight;}

        public void    setAlignRight(boolean alignRight){ this.alignRight = alignRight;}

        public String getString(int index) {
            if (index>this.date.length-1) return null;
            SimpleDateFormat s = new SimpleDateFormat(this.format);
            return s.format(this.date[index]);
        }

        public String getSample() {
            SimpleDateFormat s = new SimpleDateFormat(this.format);
            return s.format(new Date(0,0,0,23,59,59));
        }

        public float dateToAngle(int index) {
            if (index>this.date.length-1) return 0;
            float f = (this.date[index].getTime() / 60000f) % 1440;
            return f * 360f / 1440f;
        }

        public static float timeToRadAngle(long time){
            float f = (time / 60000f) % 1440;
            return f * (float)Math.PI / 720f;
        }

        public static float getMinutes(long time){
            float f = (time / 60000f) % 1440;
            return f * (float)Math.PI / 720f;
        }

        public float getMaxWidth() {
            Paint p = new Paint();
            setPaint(p);
            return p.measureText(getSample());
        }

    }

    public static class timeLabel {
        private Date date;
        private float size;
        private int color;
        private Typeface type;
        private boolean alignRight;
        private Path path;
        private String format;
        private String label;
        private boolean insideFrame = false;

        public timeLabel(Date date, float size, int color, Typeface t, String format, boolean insideFrame) {
            this.date = date;
            this.size = size;
            this.color = color;
            this.type = t;
            this.format = format;
            this.insideFrame = insideFrame;
        }

        public void setPaint(Paint p){
            p.setColor(this.color);
            p.setTextSize(this.getSizePx());
            p.setTypeface(this.type);
            p.setTextAlign((this.alignRight) ? Paint.Align.RIGHT : Paint.Align.LEFT);
        }

        public Path getPath(){return this.path;}

        public void setPath(PointF centro, float pad, float raio, float angle_offset,float markWidth){
            float r1, r2;
            if (this.insideFrame) {
                r2 = raio - pad;
                r1 = r2 - markWidth;
            } else {
                r1 = raio + pad;
                r2 = r1 + markWidth;
            }
            Path p = new Path();
            double angle = (this.toRadianAngle() + angle_offset) * Math.PI / 180;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            if (cos < 0) {
                p.moveTo((float) (r2 * cos) + centro.x, (float) (r2 * sin) + centro.y);
                p.lineTo((float) (r1 * cos) + centro.x, (float) (r1 * sin) + centro.y);
                this.alignRight = !this.insideFrame;
            } else {
                p.moveTo((float) (r1 * cos) + centro.x, (float) (r1 * sin) + centro.y);
                p.lineTo((float) (r2 * cos) + centro.x, (float) (r2 * sin) + centro.y);
                this.alignRight = this.insideFrame;
            }
            this.path = p;
        }

        public float getSizePx(){
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, this.size, Resources.getSystem().getDisplayMetrics());
        }

        public boolean isInsideFrame(){ return insideFrame;}

        public boolean isAlignRight() { return alignRight;}

        public void    setAlignRight(boolean alignRight){ this.alignRight = alignRight;}

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

    public static class dateEvent {
        private String label;
        private float size;
        private int color;
        private Typeface type;
        private float pad;
        private float currLine = 0f;

        public dateEvent(String text, float size, int color, Typeface t, float pad) {
            this.label = text;
            this.size = size;
            this.color = color;
            this.type = t;
            this.pad = pad;
        }

        public void setPaint(Paint p){
            p.setTypeface(this.type);
            p.setColor(this.color);
            p.setTextSize(this.getSizePx());
        }

        public void setPaint(Paint p, float factor){
            p.setTypeface(this.type);
            p.setColor(this.color);
            p.setTextSize(this.getSizePx());
            Rect bounds = new Rect();
            p.getTextBounds(this.label, 0, this.label.length(), bounds);
            currLine += factor * bounds.height();
        }

        public float getSizePx(){
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, this.size, Resources.getSystem().getDisplayMetrics());
        }

        public String getLabel(){
            return label;
        }

        public float getCurrLine(){
            return currLine;
        }

        public void setCurrLine(float line){
            this.currLine = line;
        }

        public void resetCurrLine(){
            currLine =0f;
        }
    }

    public static class hebString {

        public static String[] decodeB64String(String source) throws UnsupportedEncodingException {
            return new String(Base64.decode(source, Base64.DEFAULT), "UTF-8").split("\\r?\\n");
        }

        public static int[] decodeB64Int(String source) throws UnsupportedEncodingException{
            String[] s = new String(Base64.decode(source, Base64.DEFAULT), "UTF-8").split("\\r?\\n");
            int res[] = new int[s.length];
            for(int i=0;i<s.length;i++){
                res[i]=Integer.parseInt(s[i]);
            }
            return res;
        }

        public static String removeBreakSymbs(String txt){
            txt = txt.replace("{\u05e1}", "\u05c8")
                    .replace("{\u05e4}","\u05c9")
                    .replace("{\u05e8}","\u05ca")
                    .replace("{\u05e9}","\u05cb");
            return txt;
        }

        public static String removeMaqaf(String txt){
            return txt.replace("\u05be"," ");
        }

        public static String toNiqqud(String txt) {
            String res = "";
            for (char c : txt.toCharArray())
                if (c < 0x041 || c == 0x05be || (c > 0x05af && c < 0x05eb && c!=0x05bd)) {
                    res += c;
                }
            return res;
        }

        public static String toOtiot(String txt) {
            String res = "";
            for (char c : txt.toCharArray()) {
                if ((c > 0x05cf && c < 0x05eb) || c==0x020) res += c;
            }
            return res;
        }

        public static String getOtiotSequence(String txt) {
            String res = "";
            for (char c : txt.toCharArray()) {
                if (c > 0x05cf && c < 0x05eb) res += c;
            }
            return res;
        }

        public static int getMilimNumber(String s) {
            s = toOtiot(s);
            if (debug) Log.d(TAG,s);
            return s.split("\\s+").length;

        }

        public static int getOtiotNumber(String s) {
            String seq =getOtiotSequence(s);
            if (debug) Log.d(TAG,seq);
            return seq.length();
        }

        /*

            public static class HebString{

        public static final int OTIOT   = 0;
        public static final int NEQUDOT = 1;
        public static final int TAAMIM  = 2;

        private String text;
        private String result;

        public HebString(String text){
            this.text = text;
            this.result = text;
        }

        public String getString(int flags){
            if ((flags&OTIOT)==OTIOT){

            }
            return this.text;
        }

        public String[] getMilim(){
            String s = removeBreakSymbs(this.result);
            s = removeMaqaf(s);
            s = toOtiot();
            return s.split("//s+");
        }

        public enum GEMATRIA{
            MisparHechrachi (new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400 }),  //0-Standard
            MisparGadol     (new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 500, 20, 30, 600, 40, 700, 50, 60, 70, 800, 80, 900, 90, 100, 200, 300, 400}),      //1-Sofit
            Milui72         (new int[] { 111, 412, 83, 434, 10, 12, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406 }),          //2
            Milui63         (new int[] { 111, 412, 83, 434, 15, 13, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406 }),          //3
            Milui45         (new int[] { 111, 412, 83, 434, 6, 13, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406 }),          //4
            Milui52         (new int[] { 111, 412, 83, 434, 10, 12, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406 }),          //5
            MisparSiduri    (new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400 }),     //6-Contagem ordinal 1>22
            MisparKatan     (new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400 }),      //7-sem zeros iod=1, kaf=2,...
            MisparKidmi     (new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400 }),      //8-Standard triangular 1,3,6...(n^2+n)/2
            MisparPerati    (new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400 }),     //9-Standard quadratico 1,4,9...n^2
            MisparNeelam    (new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400 });      //10

            private int[] table;
            GEMATRIA(int[] _table){
                this.table = _table;
            }

            public int[] getTable(){
                return this.table;
            }
        }

        public int getGematria(GEMATRIA gematria, String s){
            int g = 0;
            int[] t = gematria.getTable();
            for (char c : s.toCharArray())
            {
                int i = c - 0x05d0;
                g += (i >= 0 && i < 27) ? (char)(t[i] + 0x05cf) : 0;
            }
            return g;
        }

        public int[] getGematria(GEMATRIA gematria){
            String[] milim = getMilim();
            int[] res = new int[milim.length];
            for (int i=0;i<milim.length;i++){
                res[i] = getGematria(gematria,milim[i]);
            }
            return res;
        }

        public static enum CHILUFI {
            Albam       (new int[]{13,15,17,18,19,21,23,24,25,26,27,27,1,2,2,3,3,4,5,6,6,7,7,8,9,10,12}),     //
            Atbash      (new int[]{27,26,25,24,23,21,19,18,17,15,13,13,12,10,10,9,9,8,7,6,6,5,5,4,3,2,1}),     //
            Achbi       (new int[]{12,10,9,8,7,6,5,4,3,2,1,1,27,26,26,25,25,24,23,20,21,19,19,18,17,15,13}),      //
            AyiqBekher  (new int[]{10,12,13,15,17,18,19,21,23,24,5,25,26,6,27,7,11,14,16,8,20,9,22,1,2,3,4}), //
            AchasBeta   (new int[]{8,9,10,12,13,15,17,18,19,21,23,23,24,25,25,26,26,1,2,3,3,4,4,5,6,7,27}),  //
            Atbach      (new int[]{9,8,7,6,5,4,3,2,1,23,20,21,19,18,18,16,17,15,13,11,12,10,10,27,26,25,24});

            private int[] table;
            CHILUFI(int[] _table){
                this.table = _table;
            }

            private int[] getTable(){
                return this.table;
            }
        }

        public String getChilufi(CHILUFI ch){
            String res = "";
            int[] tbl = ch.getTable();
            for (char c : result.toCharArray())
            {
                int i = c - 0x05d0;
                res += (i >= 0 && i < 27) ? (char)(tbl[i] + 0x05cf) : c;
            }
            return res;
        }

        public void removeBreakSymbs(){
            this.result = removeBreakSymbs(this.result);
        }

        public String removeBreakSymbs(String s){
            s = s.replace("{\u05e1}", "\u05c8")
                    .replace("{\u05e4}","\u05c9")
                    .replace("{\u05e8}","\u05ca")
                    .replace("{\u05e9}", "\u05cb");
            return s;
        }

        public void removeMaqaf(){
            this.result = removeMaqaf(this.result);
        }

        public String removeMaqaf(String s){
            s = s.replace("\u05be"," ");
            return s;
        }

        public String toNiqqud() {
            String res = "";
            for (char c : result.toCharArray())
                if (c < 0x041 || c == 0x05be || (c > 0x05af && c < 0x05eb && c!=0x05bd)) {
                    res += c;
                }
            result = res;
            return res;
        }

        public String toOtiot() {
            String res = "";
            for (char c : result.toCharArray()) {
                if ((c > 0x05cf && c < 0x05eb) || c==0x020) res += c;
            }
            result = res;
            return res;
        }

        public String getOtiotSequence() {
            String res = "";
            for (char c : result.toCharArray()) {
                if (c > 0x05cf && c < 0x05eb) res += c;
            }
            result = res;
            return res;
        }

        public int getMilimNumber() {
            String s = toOtiot();
            if (debug) Log.d(TAG,s);
            return s.split("\\s+").length;

        }

        public int getOtiotNumber() {
            String seq = getOtiotSequence();
            if (debug) Log.d(TAG, seq);
            return seq.length();
        }
    }

        */

        /*
        //region class enumerations

        public enum chilufi {
            Albam,      //
            Atbash,     //
            Achbi,      //
            AyiqBekher, //
            AchasBeta,  //
            Atbach      //
        }

        public enum Mispar {

            //http://www.jewishencyclopedia.com/articles/6571-gematria

            Hechrachi,      //1. Normal: m' mechrach, mispar hechrachi

            MeugalKlali,    //2. Ciclico ou menor: m qatan, mispar me'ugal klali, hagilgul chezrat

            Qidmi,          //3. Inclusivo: mispar qdimi (letra valor triangular)

            Musafi,          //4. Aditorio: mispar musafi quando nº. externo de palavras ou letras é adicionado

            MereviaKlali,   //5. Quadratico da palavra: mispar merevia klali: valor da palavra * valor de cada letra = quadrado da palavra

            MereviaPerati,  //6. Quadratico da letra: mispar merevia perati: soma dos quadrados da letras

            Shemi72,        //7. Nominal: mispar shemi: valor do nome da letra
            Shemi63,
            Shemi52,
            Shemi45,

            Mispari,        //8. Numeral: mispar mispari: valor do nome do numero da letra

            MispariHagadol, //9. Numeral Maior: mispari hagadol: numeral com integração: (yod) = yod = esrim;

            Chitzuni,       //10. Externo: mispar chitzuni: (contagem de letras) todas as letras valem 1 não aplicado a YHVH (Asis Rimonim 36b)

            Gadol,          //11. Maior: mispar gadol: contagem das formas finais 500-900 (mispar gadol mntzpkh)

            Kaful,          //12. Multiplo: mispar kaful: (cf.III.D.c) multiplicação das letras

            Chalqi,         //13. Quociente: mispar chalqui: (cf. III.D.d)

            MeaqavKlali,    //14. Cubico da palavra: m' meshalosh klali, mispar me'akav klali: valor cubico da palavra

            MeaqavPerati,   //15. Cubico da letra: mispar me'akav perati: cubo da letra normal (cf. Chayyath, Minchat Yehudi)

            EserKlalot,     //16. Involução primeira decada: eser mispar klalot: cf.III.D.a

            HaklalotKlalot, //17. Involução segunda decada: haklalot mispar klalot:

            ShemiSheni,     //18. Dupla integração: mispar shemi sheni

            Temuri,         //19. Permutação: mispar temuri (II.2.c), quando o valor das letras permutadas

            Revua           //20-22. Quaternionico: mispar revua: da palavra (20), integrada (21) e integração dupla (22)
        }

        public enum gematriaTable {
            MisparHechrachi,  //0-Standard
            MisparGadol,      //1-Sofit
            Milui72,          //2
            Milui63,          //3
            Milui45,          //4
            Milui52,          //5
            MisparSiduri,     //6-Contagem ordinal 1>22
            MisparKatan,      //7-sem zeros iod=1, kaf=2,...
            MisparKidmi,      //8-Standard triangular 1,3,6...(n^2+n)/2
            MisparPerati,     //9-Standard quadratico 1,4,9...n^2
            MisparNeelam      //10
        }

        public enum gematriaMethod {
            Sum,    //0,
            Mul,    //1,
            Div,    //2,
            Sub     //3;
        }

        //endregion

        public final int hebKEEPSPACE= 1;
        public final int hebOTIOT    = 2;
        public final int hebNEQUDOT  = 4;
        public final int hebTAAMIM   = 8;
        public final int hebHEBPUNCT = 16;
        public final int hebPUNCT    = 32;

        private String hebstring;
        private String hebniqqud;
        private String otsequence;

        public hebString(String string){
            this.hebstring  = string;
            this.otsequence = getOtsequence();
        }

        @Override
        public String toString(){
            return hebstring;
        }

        public String toString(int flags){

            String res = "";
            for (char c : hebstring.toCharArray()) {
                if (
                        ((flags & hebKEEPSPACE) == hebKEEPSPACE && c == 0x0020) ||
                                ((flags & hebOTIOT) == hebOTIOT && c > 0x05ef && c < 0x05eb) ||
                                ((flags & hebNEQUDOT) == hebNEQUDOT && c > 0x05af && c < 0x05c8 && c != 0x05be && c != 0x05c0 && c != 0x05c3 && c != 0x05c6) ||
                                ((flags & hebTAAMIM) == hebTAAMIM && c > 0x0590 && c < 0x05af) ||
                                ((flags & hebKEEPSPACE) == hebKEEPSPACE && c == 0x0020) ||
                                ((flags & hebHEBPUNCT) == hebHEBPUNCT && (c == 0x05be || c == 0x05c0 || c == 0x05c3 || c == 0x05c6 || c == 0x05f3 || c == 0x05f4)) ||
                                ((flags & hebPUNCT) == hebPUNCT && (c > 0x0020 && c < 0x0040))
                        )
                    res += c;
            }
            return res;
        }

        private String getOtsequence(){
            String res = "";
            for (char c : hebstring.toCharArray()) {
                if (c > 0x05ef && c < 0x05eb) res += c;
            }
            return res;
        }

        private int[] getFactorArray(){
            int i = otsequence.length();
            Vector<Integer> fa = new Vector<>();
            for (int j=i;j>0;j--){

            }
            return new int[27];
        }

        public class Gematria{

            private int[] gematriaTable;

            private Mispar _msp;

            private double _gematria;

            private String _word;

            public Gematria(Mispar mispar, String word){
                _msp = mispar;
                setMispar();
                this._word = word;
            }

            private int[] setMispar(){
                switch (_msp){

                    case Hechrachi:
                        //1. Normal: m' mechrach, mispar hechrachi
                    case Musafi:
                        //4. Aditorio: mispar musafi quando nº externo de palavras ou letras é adicionado
                        return new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400 };

                    case MeugalKlali:
                        //2. Ciclico ou valor menor: m qatan, mispar me'ugal klali, hagilgul chezrat
                        return new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 2, 3, 4, 4, 5, 5, 6, 7, 8, 8, 9, 9, 1, 2, 3, 4 };

                    case Qidmi:
                        //3. Inclusivo: mispar qdimi (letra valor triangular)
                        return new int[] { 1,3,6,10,15,21,28,36,45,55,210,210,465,820,820,1275,1275,1830,2485,3240,3240,4095,4095,5050,20100,45150,80200};

                    case MereviaKlali:
                        //5. Quadratico da palavra: mispar merevia klali: valor da palavra * valor de cada letra = quadrado da palavra

                    case MereviaPerati:
                        //6. Quadratico da letra: mispar merevia perati: soma dos quadrados da letras

                    case Shemi72:
                        //7. Nominal: mispar shemi: valor do nome da letra
                        gematriaTable = new int[] { 111, 412, 83, 434, 10, 12, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406 };
                        break;

                    case Shemi63:
                        gematriaTable = new int[] { 111, 412, 83, 434, 15, 13, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406 };
                        break;

                    case Shemi52:
                        gematriaTable = new int[] { 111, 412, 83, 434, 10, 12, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406 };
                        break;

                    case Shemi45:
                        gematriaTable = new int[] { 111, 412, 83, 434, 6, 13, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406 };
                        break;

                    case Mispari:
                        //8. Numeral: mispar mispari: valor do nome do numero da letra

                    case MispariHagadol: //9. Numeral Maior: mispari hagadol: numeral com integração: (yod) = yod = esrim;


                    case Chitzuni:       //10. Externo: mispar chitzuni: (contagem de letras) todas as letras valem 1 não aplicado a YHVH (Asis Rimonim 36b)

                    case Gadol:
                        //11. Maior: mispar gadol: contagem das formas finais 500-900 (mispar gadol mntzpkh)
                        return new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 500, 20, 30, 600, 40, 700, 50, 60, 70, 800, 80, 900, 90, 100, 200, 300, 400 };

                    case Kaful:          //12. Multiplo: mispar kaful: (cf.III.D.c) multiplicação das letras

                    case Chalqi:         //13. Quociente: mispar chalqui: (cf. III.D.d)

                    case MeaqavKlali:    //14. Cubico da palavra: m' meshalosh klali, mispar me'akav klali: valor cubico da palavra

                    case MeaqavPerati:   //15. Cubico da letra: mispar me'akav perati: cubo da letra normal (cf. Chayyath, Minchat Yehudi)

                    case EserKlalot:     //16. Involução primeira decada: eser mispar klalot: cf.III.D.a

                    case HaklalotKlalot: //17. Involução segunda decada: haklalot mispar klalot:

                    case ShemiSheni:     //18. Dupla integração: mispar shemi sheni

                    case Temuri:         //19. Permutação: mispar temuri (II.2.c), quando o valor das letras permutadas

                    case Revua:          //20-22. Quaternionico: mispar revua: da palavra (20), integrada (21) e integração dupla (22)



                        //case Siduri: gematriaTable = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 23, 11, 12, 24, 13, 25, 14, 15, 16, 26, 17, 18, 16, 19, 20, 21, 22 }; break;    //6-Contagem ordinal 1>22
                        //case Perati: gematriaTable = new int[] {}; break;    //9-Standard quadratico 1,4,9...n^2
                        //case Neelam: gematriaTable = new int[] {}; break;   //10
                }
                return null;
            }

            public void setMisparMethod(Mispar value) {
                _msp = value;
                int[] tbl;
                switch (_msp) {
                    case Hechrachi:
                        tbl = new int[]{
                                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400
                        };
                        _gematria = GetGematria(tbl);
                        break;
                    case MeugalKlali:
                        tbl = new int[] {
                                1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 5, 2, 3, 6, 4, 7, 5, 6, 7, 8, 8, 9, 9, 1, 2, 3, 4
                        } ;
                        _gematria = GetGematria(tbl);
                        break;
                    case Qidmi:
                        tbl = new int[] {
                                1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 210, 210, 465, 820, 820, 1275, 1275, 1830, 2485, 3240, 3240, 4095, 4095, 5050, 20100, 45150, 80200
                        } ;
                        _gematria = GetGematria(tbl);
                        break;
                    case Musafi:
                        tbl = new int[] {
                                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400
                        } ;
                        _gematria = GetGematria(tbl);
                        _gematria += OtiotCount();
                        break;
                    case MereviaKlali:
                        tbl = new int[] {
                                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400
                        } ;
                        _gematria = GetGematria(tbl);
                        _gematria *= _gematria;
                        break;
                    case MereviaPerati:
                        tbl = new int[] {
                                1, 4, 9, 16, 25, 36, 49, 64, 81, 100, 400, 400, 900, 1600, 1600, 2500, 2500, 3600, 4900, 6400, 6400, 8100, 8100, 10000, 40000, 90000, 160000
                        } ;
                        _gematria = GetGematria(tbl);
                        break;
                    case Shemi72:
                        tbl = new int[] {
                                111, 412, 83, 434, 10, 12, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406
                        } ;
                        _gematria = GetGematria(tbl);
                        break;
                    case Shemi63:
                        tbl = new int[] {
                                111, 412, 83, 434, 15, 13, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406
                        } ;
                        _gematria = GetGematria(tbl);
                        break;
                    case Shemi45:
                        tbl = new int[] {
                                111, 412, 83, 434, 6, 13, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406
                        } ;
                        _gematria = GetGematria(tbl);
                        break;
                    case Shemi52:
                        tbl = new int[] {
                                111, 412, 83, 434, 10, 12, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406
                        } ;
                        _gematria = GetGematria(tbl);
                        break;
                }
            }

            private int GetGematria(int[]tbl){
                return Get(tbl);
            }

            private int Get(int[] table) {
                int g = 0;
                for (char c : _word.toCharArray()) {
                    int i = c - 0x05d0;
                    if (i >= 0 && i < 27) g += (char) (table[i] + 0x05cf);
                }
                return g;
            }

            private int OtiotCount() {
                int g = 0;
                for (char c : _word.toCharArray()) {
                    int i = c - 0x05d0;
                    g += (i >= 0 && i < 27) ? 1 : 0;
                }
                return g;
            }

            private int MilimCount() {
                if (_word == null) return 0;
                //String[] sep = {" ", "-", "?"};
                String[] s = _word.split(" |-|/?");
                return s.length;
            }
        }
        */
    }

    public static class SolarTime{

        public static final long dayMilis = 24*60*60*1000;

        private long time;
        private double latitude;
        private double rLat;
        private long sunset;

        public SolarTime(long time, long sunset, double latitude){
            this.time = time;
            this.sunset = sunset;
            this.latitude = latitude;
            this.rLat = latitude/180*Math.PI;
        }

        public SolarTime(long sunset, double latitude){
            this.time = Calendar.getInstance().getTimeInMillis();
            this.sunset = sunset;
            this.latitude = latitude;
        }

        public double getSolarAngle(){
            double sunDec = getSunDeclination();
            return Math.asin(Math.cos(getHourAngle(time))*Math.cos(sunDec)*Math.cos(rLat)+Math.sin(sunDec)*Math.sin(rLat));
        }

        public double getHourAngle(double time){
            return Math.PI*(time%dayMilis/dayMilis*2-1);
        }

        public double getSunDeclination(){
            //return Math.atan(-Math.cos(getHourAngle(sunset))/Math.tan(rLat));
            int n = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
            return (23.45f*Math.PI/180*Math.sin(2*Math.PI*(284+n)/36.25f));
        }

        public float getSunPosition(){
            float belowHorz = (float)Math.PI/12;
            zcHelper.RangeF solarRange = new zcHelper.RangeF((float) (Math.PI/7.675f),-belowHorz);
            float solarAng =(float) (belowHorz+this.getSolarAngle());
            return solarRange.scale(solarAng,1f,0f);
        }

        public int getSunLightColor(float s){
            float l = getSunPosition();
            int rb = (int)(254+l);
            float h = xColor.getHue(Color.rgb(rb,(int)(253+2*l),rb));
            return xColor.getAHSL(255,h,s,l);
        }
    }

    public static class xSGV{

        public static Bitmap getBitmap(Context context,String assetPath, int width, int height){
            try{
                SVG svg = SVGParser.getSVGFromAsset(context.getAssets(), assetPath);
                PictureDrawable pd = svg.createPictureDrawable();
                Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(b);
                canvas.drawPicture(pd.getPicture());
                return b;
            } catch (Exception ex){
                Log.e(TAG,"/xSVG: "+ex.toString());
                return null;
            }
        }

        public static Bitmap getBitmap(Context context, String assetPath){
            try{
                SVG svg = SVGParser.getSVGFromAsset(context.getAssets(), assetPath);
                PictureDrawable pd = svg.createPictureDrawable();
                Bitmap b = Bitmap.createBitmap(pd.getIntrinsicWidth(), pd.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(b);
                canvas.drawPicture(pd.getPicture());
                return b;
            } catch (Exception ex){
                Log.e(TAG,"/xSVG: "+ex.toString());
                return null;
            }
        }
    }

}
