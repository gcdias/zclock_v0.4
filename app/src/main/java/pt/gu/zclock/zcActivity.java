package pt.gu.zclock;

import android.appwidget.AppWidgetManager;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.BreakIterator;


public class zcActivity extends ActionBarActivity {

    protected static final String ACTION_ACTIVITY="pt.gu.zclock.zcactivity";
    private final boolean       debug   = true;
    private final String        TAG     = "zcActivity";
    private int                 appWidgetId;

    private Context             mContext;
    private SharedPreferences   mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.zc_activity);

        mPrefs  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            if (debug) Log.e(TAG, "onResume: invalid AppWidgetId");
            finish();
        }

        Typeface stm = Typeface.createFromAsset(getAssets(), "fonts/stmvelish.ttf");
        int mode = mPrefs.getInt("clockMode"+appWidgetId,0);
        LinearLayout lClock = (LinearLayout)findViewById(R.id.clock_layout);
        LinearLayout lText = (LinearLayout)findViewById(R.id.torah_layout);
        if (mode <5){
            lClock.setVisibility(View.VISIBLE);
            lText.setVisibility(View.INVISIBLE);
            TextView x = (TextView)findViewById(R.id.textTorah);
            x.setTypeface(stm);
            x.setText("Hello Clock!");
        }


        if (mode == 5) {
            lClock.setVisibility(View.INVISIBLE);
            lText.setVisibility(View.VISIBLE);
            String text = mPrefs.getString("currentPasuk", "(no string)");
            text = zcHelper.hebString.removeBreakSymbs(text);
            text = zcHelper.hebString.toNiqqud(text);
            TextView x = (TextView)findViewById(R.id.textTorah);
            x.setTypeface(stm);
            setClicableText(text, R.id.textTorah);
            TextView info = (TextView)findViewById(R.id.textInfo);
            kblh.Gematria gematria = new kblh.Gematria(text);
            info.setText(String.format("Milim %d, Otiot %d",gematria.MilimCount(),gematria.OtiotCount()));
        }
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
                TextView gem = (TextView)findViewById(R.id.wordTorah);
                kblh.Gematria g = new kblh.Gematria(word);
                gem.setText(String.format("Gematria %d",g.getGematria(kblh.Mispar.MisparHechrachi)));
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
