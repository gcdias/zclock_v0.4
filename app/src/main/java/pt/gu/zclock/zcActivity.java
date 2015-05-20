package pt.gu.zclock;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


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
            String text = mPrefs.getString("currentPasuk","(no string)");
            XTextView x = (XTextView)findViewById(R.id.textTorah);
            //x.setTypeface(stm);
            x.setText(text);
        }
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
}
