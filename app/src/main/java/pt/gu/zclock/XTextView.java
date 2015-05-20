package pt.gu.zclock;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by GU on 20-05-2015.
 */
public class XTextView extends TextView {

    private final boolean   debug = true;
    private final String    TAG   = "XTextView";

    public XTextView(Context context) {
        super(context);
    }

    public XTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont(context, attrs);
    }

    public XTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont(context, attrs);
    }

    private void setFont(Context context, AttributeSet attrs) {

        setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/stmvelish.ttf"));
        /*
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XTextView);
        String font = a.getString(R.styleable.XTextView_fontName);
        Typeface tf;

        try {
            tf = Typeface.createFromAsset(context.getAssets(), font);
        } catch (Exception e) {
            if (debug) Log.e(TAG, "Could not get typeface: " + e.getMessage());
            return;
        }

        setTypeface(tf);
        a.recycle();
        */
    }
}
