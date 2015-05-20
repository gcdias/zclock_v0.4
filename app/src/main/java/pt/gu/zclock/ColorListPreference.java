package pt.gu.zclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.preference.ListPreference;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Arrays;

/**
 * Created by GU on 27-04-2015.
 */
public class ColorListPreference extends ListPreference {

    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private CharSequence[] mArrayValues;
    private String mSVPattern;
    private boolean mSetSummary;
    private String mValue;
    private int mValueIndex;
    private int entryIndex = -1;
    private Resources mResources;
    private Drawable[] mDrawable;
    private float radBitmap;
    private float fillRatio;

    public ColorListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorListPreference);
        radBitmap = a.getDimension(R.styleable.ColorListPreference_paletteSize, 32);
        fillRatio = a.getFloat(R.styleable.ColorListPreference_paletteFillRatio, 0.9f);
        mSVPattern = a.getString(R.styleable.ColorListPreference_patternSV);
        mSetSummary = a.getBoolean(R.styleable.ColorListPreference_autoSummary, false);
        a.recycle();

        //Fields
        mResources = context.getResources();
    }

    @Override
    protected void onPrepareDialogBuilder(@NonNull AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        mEntries = getEntries();
        mEntryValues = getEntryValues();
        mValue = getValue();
        mValueIndex = getValueIndex();

        if (mEntries == null || mEntryValues == null) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array.");
        }

        mDrawable = new Drawable[mEntryValues.length];
        mArrayValues = new String[mEntryValues.length];
        Drawable d;

        for (int i = 0; i < mEntryValues.length; i++) {
            d = mResources.getDrawable(R.drawable.prf_colorlist_radiobutton);
            try {
                ((LayerDrawable) d).setDrawableByLayerId(
                        R.id.bitmap,
                        new BitmapDrawable(
                                mResources, getBitmapColorIcon(i)));
            } catch (Exception ignore){
                Log.e("ColorListPreference",ignore.toString());
            }
            mDrawable[i] = d;
        }

        ListAdapter adapter = new ArrayAdapter<CharSequence>(getContext(), R.layout.prf_colorlist, mEntries) {

            class ViewHolder {
                TextView title;
                RadioButton radioBtn;
            }

            ViewHolder holder;

            MyOnClickListener listener = new MyOnClickListener();

            public View getView(int position, View convertView, ViewGroup parent) {

                final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.prf_colorlist, null);
                    holder = new ViewHolder();
                    holder.title = (TextView) convertView.findViewById(R.id.textView);
                    holder.radioBtn = (RadioButton) convertView.findViewById(R.id.radiobutton);
                    convertView.setTag(holder);
                    holder.title.setOnClickListener(listener);
                    holder.radioBtn.setOnClickListener(listener);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.title.setText(mEntries[position]);
                holder.title.setTag(position);
                holder.radioBtn.setChecked(mValueIndex == position);
                holder.radioBtn.setButtonDrawable(mDrawable[position]);
                holder.radioBtn.setTag(position);
                return convertView;

            }
        };

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                entryIndex = index;
                ColorListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
            }
        });

    /*
     * The typical interaction for list-based dialogs is to have
     * click-on-an-item dismiss the dialog instead of the user having to
     * press 'Ok'.
     */
        builder.setPositiveButton(null, null);
    }

    private Bitmap getBitmapColorIcon(int index) {
        int w = (int) radBitmap;
        Bitmap b = Bitmap.createBitmap(w, w, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint pstroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        pstroke.setStyle(Paint.Style.STROKE);
        pstroke.setColor(Color.BLACK);
        pstroke.setStrokeWidth(1.5f);
        RectF r = new RectF(w * (1 - fillRatio), w * (1 - fillRatio), w * fillRatio, w * fillRatio);

        if (mSVPattern != null) {
            float[] hsv_basecolor = new float[3];
            int baseColor = Color.parseColor(mEntryValues[index].toString().split(",")[0]);
            Color.colorToHSV(baseColor, hsv_basecolor);
            String[] pattern = mSVPattern.split(",");
            int len = pattern.length;
            float ang = 360 / len;
            float pad = 6 * (len - 1) / len;
            int color, newcolor;
            float[] hsv_patcolor = new float[3];
            String s = "";
            for (int i = 0; i < len; i++) {
                if (!s.equals("")) s += ",";
                Log.e("colorPref","Color:"+pattern[i]);
                color = Color.parseColor(pattern[i]);
                Color.colorToHSV(color, hsv_patcolor);
                newcolor = Color.HSVToColor(Color.alpha(color), new float[]{hsv_basecolor[0], hsv_patcolor[1], hsv_patcolor[2]});
                p.setColor(newcolor);
                s += String.format("#%06X", newcolor);
                c.drawArc(r, i * ang + pad, ang - pad, true, p);
                c.drawArc(r, i * ang + pad, ang - pad, true, pstroke);
            }
            mArrayValues[index] = s;
            return b;
        } else {
            String[] colors = mEntryValues[index].toString().split(",");
            int len = colors.length;
            float ang = 360 / len;
            for (int i = 0; i < len; i++) {
                p.setColor(Color.parseColor(colors[i]));
                c.drawArc(r, i * ang, ang, true, p);
            }
            mArrayValues[index] = mEntryValues[index];
            return b;
        }
    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (entryIndex >= 0 && mEntryValues != null) {
            String value = (String) mArrayValues[entryIndex];
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    protected void setResult() {
        //this.getDialog().getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        this.getDialog().dismiss();
    }

    /**
     * Returns the index of the given value (in the entry values array).
     *
     * @param value The value whose index should be returned.
     * @return The index of the value, or -1 if not found.
     */
    public int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    public CharSequence[] getArrayValues() {
        return mArrayValues;
    }

    public void setArrayValues(CharSequence[] arrayValues) {
        this.mArrayValues = arrayValues;
    }

    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            entryIndex = (Integer) v.getTag();
            ColorListPreference.this.setResult();
            if (mSetSummary) {
                String header = mEntries[entryIndex].toString() + " ";
                int start = header.length();
                String[] scolors = mArrayValues[entryIndex].toString().split(",");
                Log.e("scolors", String.format("%d", scolors.length));
                char[] chars = new char[scolors.length];
                Arrays.fill(chars, '\u25a0');
                header += new String(chars);
                Spannable summary = new SpannableString(header);
                int color;

                for (int i = 0; i < scolors.length; i++) {
                    color = Color.parseColor(scolors[i]);
                    summary.setSpan(new ForegroundColorSpan(color), start + i, start + i + 1, 0);
                }
                ColorListPreference.this.setSummary(summary);
            }
        }
    }
}
