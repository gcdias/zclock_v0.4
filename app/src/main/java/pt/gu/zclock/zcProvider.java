package pt.gu.zclock;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link zcPreferences}
 */
public class zcProvider extends AppWidgetProvider {

    private static boolean      debug                 = true;
    private static String       TAG                   = "zcProvider";

    private static      zcProvider sProvider;

    static synchronized zcProvider getInstance(){
        if (sProvider == null){
            sProvider = new zcProvider();
        }
        return sProvider;
    }

    private SharedPreferences   sharedPreferences;

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        if (debug) Log.e(TAG, "onDisabled");
        context.stopService(new Intent(context.getApplicationContext(),zcService.class));
        if (debug) Toast.makeText(context, "zClock removed", Toast.LENGTH_SHORT).show();
        context.getApplicationContext().stopService(new Intent(context.getApplicationContext(), zcService.class));
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.clear();
        ed.apply();
        super.onDisabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        if (debug) Log.e(TAG, "onUpdate");
        context.startService(new Intent(context,zcService.class));
        context.sendStickyBroadcast(new Intent(zcService.ZC_SETTINGSUPDATE));

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

        // When the user deletes the widget, delete the preference associated with it.

        if (debug) Log.e(TAG, "onDeleted "+appWidgetIds.length);

        for (int appWidgetId : appWidgetIds) {
            removeWidgetPreferences(context, appWidgetId);
        }
        super.onDeleted(context, appWidgetIds);
    }


    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        if (debug) Log.e(TAG,"onAppWidgetOptionsChanged");

        //update widgets
        updateWidgetSize(context, appWidgetId);
        context.sendBroadcast(new Intent(zcService.ZC_SETTINGSUPDATE));
    }

    public void removeWidgetPreferences(Context context,int appWidgetId){

        if (debug) Log.e(TAG,"removeWidgetPreferences");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();

        ed.remove("clockMode"+appWidgetId);

        ed.remove("zmanimMode"+appWidgetId);

        ed.remove("szZmanim_sun"+appWidgetId);
        ed.remove("szZmanim_main"+appWidgetId);
        ed.remove("szZmanimAlotTzet"+appWidgetId);
        ed.remove("szTimemarks"+appWidgetId);
        ed.remove("szTime"+appWidgetId);
        ed.remove("szDate"+appWidgetId);
        ed.remove("szParshat"+appWidgetId);
        ed.remove("szShemot"+appWidgetId);

        ed.remove("cClockFrameOn"+appWidgetId);
        ed.remove("cClockFrameOff"+appWidgetId);
        ed.remove("cZmanim_sun"+appWidgetId);
        ed.remove("cZmanim_main"+appWidgetId);
        ed.remove("cZmanimAlotTzet"+appWidgetId);
        ed.remove("cTimemarks"+appWidgetId);
        ed.remove("cTime"+appWidgetId);
        ed.remove("cDate"+appWidgetId);
        ed.remove("cParshat"+appWidgetId);
        ed.remove("cShemot"+appWidgetId);

        ed.remove("wClockMargin"+appWidgetId);
        ed.remove("wClockFrame"+appWidgetId);
        ed.remove("wClockPoer"+appWidgetId);
        ed.remove("resTimeMins "+appWidgetId);
        ed.remove("szTimeMins"+appWidgetId);

        ed.remove("tsZmanim_sun"+appWidgetId);
        ed.remove("tsZmanim_main"+appWidgetId);
        ed.remove("tsZmanimAlotTzet"+appWidgetId);
        ed.remove("tsTimemarks"+appWidgetId);

        ed.remove("iZmanim_sun"+appWidgetId);
        ed.remove("iZmanim_main"+appWidgetId);
        ed.remove("iZmanimAlotTzet"+appWidgetId);
        ed.remove("iTimemarks"+appWidgetId);

        //ed.remove("bAlotTzet72"+appWidgetId);
        ed.remove("showHebDate"+appWidgetId);
        ed.remove("showParashat"+appWidgetId);
        ed.remove("showAnaBekoach"+appWidgetId);
        ed.remove("show72Hashem"+appWidgetId);
        ed.remove("showZmanim"+appWidgetId);
        ed.remove("showTimeMarks"+appWidgetId);
        ed.remove("nShemot"+appWidgetId);

        ed.apply();

    }

    void updateWidgetSize(Context context, int appWidgetId) {

        if (debug) Log.e(TAG,"updateWidgetSize");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Bundle newOptions = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId);
        int w, h, wCells, hCells;
        if (Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            w = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
            h = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        } else {
            w = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            h = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            wCells = (int) Math.ceil((w + 2) / 72);
            hCells = (int) Math.ceil((h + 2) / 72);
        } else {
            wCells = (int) Math.ceil((w + 30) / 70);
            hCells = (int) Math.ceil((h + 30) / 70);
        }

        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putFloat("widgetWidth" + appWidgetId, (float) w);
        ed.putFloat("widgetHeight" + appWidgetId, (float) h);
        ed.putInt("widgetCellWidth" + appWidgetId, wCells);
        ed.putInt("widgetCellHeight" + appWidgetId, hCells);
        ed.apply();
    }

}
