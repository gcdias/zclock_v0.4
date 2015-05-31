package pt.gu.zclock;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.BreakIterator;

import static pt.gu.zclock.kblh.Chilufi;
import static pt.gu.zclock.kblh.Gematria;
import static pt.gu.zclock.kblh.Mispar;
import static pt.gu.zclock.kblh.hString;


public class zcActivity extends ActionBarActivity {

    protected static final String ACTION_ACTIVITY="pt.gu.zclock.zcactivity";
    private final boolean       debug   = true;
    private final String        TAG     = "zcActivity";
    private int                 appWidgetId;
    private Typeface            stm;
    private int[]               layouts = new int[]{R.layout.zca_layout02,
                                                    R.layout.zca_layout02,
                                                    R.layout.zca_layout02,
                                                    R.layout.zca_layout5,
                                                    R.layout.zca_layout5,
                                                    R.layout.zca_layout5};
    private int[]               menus   = new int[]{R.layout.zca_layout02,
                                                    R.layout.zca_layout02,
                                                    R.layout.zca_layout02,
                                                    R.layout.zca_layout5,
                                                    R.layout.zca_layout5,
                                                    R.layout.zca_layout5};
    private SharedPreferences   mPrefs;

    private hString             mString;
    private Gematria            mGematria;
    private boolean             matrixTranspose = false;
    private Gematria.Matrix     gMatrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stm = Typeface.createFromAsset(getAssets(), "fonts/stmvelish.ttf");
        mPrefs  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            if (debug) Log.d(TAG, "onResume: invalid AppWidgetId");
            finish();
        }

        int mode = mPrefs.getInt("clockMode"+appWidgetId,0);
        setContentView(layouts[mode]);

        if (mode == 5) {
            mString = new hString(mPrefs.getString("currentPasuk", "(no string)"));
            mString.removeBreakSymbs();
            mGematria = new Gematria(mString);
            drawGematria();
        }
    }

    @Override
    public void onStop(){
        super.onStop();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    private void drawGematria() {

        //hString text = new hString(mPrefs.getString("currentPasuk", "(no string)"));
        //text.removeBreakSymbs();
        //Gematria gematria = new Gematria(text);
        LinearLayout llPasuk = (LinearLayout)findViewById(R.id.ll_pasuk);
        llPasuk.removeAllViews();

        addTextView(R.id.ll_pasuk, R.layout.zcah_pasuk, stm, mString.format(hString.hFormat.Nequdot));


        addTextView(R.id.LayoutGemInfo, R.layout.zca_tvgeminfo, "Milim", mGematria.getMilim().length);
        addTextView(R.id.LayoutGemInfo, R.layout.zca_tvgeminfo, "Otiot", mGematria.getOtiotseq().length());
        addTextView(R.id.LayoutGemInfo, R.layout.zca_tvgeminfo, "Gemat", mGematria.getGematria(Mispar.MisparHechrachi));
        addTextView(R.id.LayoutGemInfo, R.layout.zca_tvgeminfo, "Tagin", mGematria.getGematria(Mispar.MisparTagin));

        matrixTranspose = false;
        setMatrix();

    }

    private void updateWordInfo(String word) {
        Gematria gem = new Gematria(word);
        LinearLayout layout = (LinearLayout)findViewById(R.id.LayoutWordInfo);
        layout.removeAllViews();
        addTextView(R.id.LayoutWordInfo, R.layout.zca_tvgeminfo, word);
        addTextView(R.id.LayoutWordInfo, R.layout.zca_tvgeminfo, getString(R.string.mispar_hechrachi), gem.getGematria(Mispar.MisparHechrachi));
        addTextView(R.id.LayoutWordInfo,R.layout.zca_tvgeminfo,"ATBaSh "+gem.getChilufi(Chilufi.Atbash));
        addTextView(R.id.LayoutWordInfo,R.layout.zca_tvgeminfo,"ATBaCh "+gem.getChilufi(Chilufi.Atbach));
        addTextView(R.id.LayoutWordInfo,R.layout.zca_tvgeminfo,"ALBaM "+gem.getChilufi(Chilufi.Albam));
        addTextView(R.id.LayoutWordInfo,R.layout.zca_tvgeminfo,"AChBi "+gem.getChilufi(Chilufi.Achbi));
        addTextView(R.id.LayoutWordInfo, R.layout.zca_tvgeminfo, "AchasBeta " + gem.getChilufi(Chilufi.AchasBeta));
        addTextView(R.id.LayoutWordInfo, R.layout.zca_tvgeminfo, "AyiqBeker " + gem.getChilufi(Chilufi.AyiqBekher));
    }

    private void setMatrix(){
        int n =mGematria.getOtiotseq().length();
        if (n>3 && n<101){
            String[] m = getResources().getStringArray(R.array.tMatrix)[n-1].split(",");
            gMatrix = new Gematria.Matrix(mGematria.getOtiotseq(),Integer.valueOf(m[1]),Integer.valueOf(m[0]),matrixTranspose);
            if (gMatrix.minDim()<2 || gMatrix.maxDim()>22) return;
            if (debug) Log.d(TAG,String.format("/setMatrix %dx%d",gMatrix.rows,gMatrix.cols));
            TableLayout tb = (TableLayout)findViewById(R.id.tbTorah);
            tb.removeAllViews();
            TableRow.LayoutParams rowParms = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT,1f);
            rowParms.gravity= Gravity.CENTER;
            for (int i =0;i<gMatrix.rows;i++){
                TableRow tr = new TableRow(this);
                tr.setLayoutParams(rowParms);
                for (int j = gMatrix.cols-1; j >-1; j--){

                    TextView textView = (TextView) View.inflate(this, R.layout.zcact_matrix, null);
                    textView.setTypeface(stm);
                    textView.setText(gMatrix.get(i,j));
                    tr.addView(textView);
                }
                tb.addView(tr);
            }
        }
    }

    public void tbGematriaClick(View view) {
        matrixTranspose = !matrixTranspose;
        setMatrix();
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_zc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settings = new Intent(getApplicationContext(), zcPreferences.class).setAction(zcPreferences.ACTION_PREFS);
            settings.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivity(settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addTextView(int layoutId, int layoutRes, String cap, int val) {
        LinearLayout info = (LinearLayout)findViewById(layoutId);
        TextView tv = (TextView) View.inflate(this, layoutRes, null);
        tv.setText(String.format("%s %d", cap, val));
        info.addView(tv);
    }

    private void addTextView(int layoutId, int layoutRes, String cap) {
        LinearLayout info = (LinearLayout)findViewById(layoutId);
        TextView tv = (TextView) View.inflate(this, layoutRes, null);
        tv.setText(cap);
        info.addView(tv);
    }

    private void addTextView(int layoutId, int layoutRes, Typeface t, String cap) {
        LinearLayout info = (LinearLayout)findViewById(layoutId);
        TextView tv = (TextView) View.inflate(this, layoutRes, null);
        tv.setTypeface(t);
        tv.setText(cap);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(cap, TextView.BufferType.SPANNABLE);
        Spannable spans = (Spannable) tv.getText();
        BreakIterator iterator = BreakIterator.getWordInstance();
        iterator.setText(cap);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            String word = cap.substring(start, end);
            if (Character.isLetterOrDigit(word.charAt(0))) {
                ClickableSpan clickSpan = getClickableSpan(word);
                spans.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        info.addView(tv);
    }

    private void setClicableText(String clicableText,int ResId) {
        TextView tv = (TextView) findViewById(ResId);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(clicableText, TextView.BufferType.SPANNABLE);
        Spannable spans = (Spannable) tv.getText();
        BreakIterator iterator = BreakIterator.getWordInstance();
        iterator.setText(clicableText);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            String word = clicableText.substring(start, end);
            if (Character.isLetterOrDigit(word.charAt(0))) {
                ClickableSpan clickSpan = getClickableSpan(word);
                spans.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private ClickableSpan getClickableSpan(final String word) {
        return new ClickableSpan() {
            final String mWord;
            {
                mWord = word;
            }

            @Override
            public void onClick(View widget) {
                Log.d("tapped on:", mWord);
                updateWordInfo(word);
                //Toast.makeText(widget.getContext(), mWord, Toast.LENGTH_SHORT).show();
            }

            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setShadowLayer(3f, 2f, 2f, 0x80808080);
                ds.setColor(Color.BLACK);
            }
        };
    }
}
