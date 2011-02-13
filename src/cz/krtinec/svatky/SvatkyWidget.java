/*
 * This file is part of Svatky Widget.
 *
 * Svatky Widget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Svatky Widget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Svatky Widget.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) Lukas Marek, 2011.
 */

package cz.krtinec.svatky;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.*;


import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;

public class SvatkyWidget extends AppWidgetProvider {
    static Map<String,Holiday> namedays;
	static final String LNG_CHANGE = "LngChange";
    public static final String SVATKY = "Svatky";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
		        int[] appWidgetIds) {
		        // To prevent any ANR timeouts, we perform the update in a service
                for (int i: appWidgetIds) {
                    Intent intent = new Intent(context, UpdateService.class);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, i);
                    context.startService(intent);
                }
		    }


	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.i("SvatkyWidget", "Received " + intent);
		Log.d("SvatkyWidget", "Received " + intent.getAction());

        Bundle extras = intent.getExtras();
        int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
        if(extras!=null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
		if (LNG_CHANGE.equals(intent.getAction())) {
	        RemoteViews views = new RemoteViews("cz.krtinec.svatky", R.layout.svatky);
            SvatkyLocale locale = SvatkyLocale.valueOf(
                    context.getSharedPreferences(SVATKY, 0).getString(String.valueOf(widgetID), SvatkyLocale.cs.toString()));

	        //change locale
            Log.d("SvatkyWidget", "Widget id: " + widgetID);
	        if (SvatkyLocale.cs.equals(locale)) {
	        	locale = SvatkyLocale.sk;
	        } else {
	        	locale = SvatkyLocale.cs;
	        }
            SharedPreferences.Editor editor = context.getSharedPreferences(SVATKY, 0).edit();
            editor.putString(String.valueOf(widgetID), locale.toString());
            editor.commit();

	        updateViews(context, locale, views);
	        AppWidgetManager manager = AppWidgetManager.getInstance(context);
	        manager.updateAppWidget(widgetID, views);
		}
	}



    static void updateViews(Context ctx, SvatkyLocale loc, RemoteViews views) {
            if (namedays == null) {
                namedays = new HashMap<String, Holiday>();
                loadNamedays(ctx, namedays);
            }
			final Calendar calendar = Calendar.getInstance();
			final Date todayDate = calendar.getTime();
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			final Date tommorowDate =  calendar.getTime();
			String today = DateFormat.format("M/d", todayDate).toString();
			String tommorow = DateFormat.format("M/d", tommorowDate).toString();
			Log.i("UpdateService", today + ":" + namedays.get(today));
			Log.i("UpdateService", tommorow + ":" + namedays.get(tommorow));
			views.setTextViewText(R.id.date, DateFormat.format("d.M.yyyy", todayDate).toString());
			if (loc.equals(SvatkyLocale.cs)) {
	            views.setTextViewText(R.id.today, namedays.get(today).cz);
	            views.setTextViewText(R.id.tommorow, namedays.get(tommorow).cz);
	            views.setImageViewResource(R.id.flag, R.drawable.cs_flag);
            } else {
	            views.setTextViewText(R.id.today, namedays.get(today).sk);
	            views.setTextViewText(R.id.tommorow, namedays.get(tommorow).sk);
	            views.setImageViewResource(R.id.flag, R.drawable.sk_flag);
            }
		}

    	static void loadNamedays(Context ctx, Map<String, Holiday> map) {
			BufferedReader br = new BufferedReader(
                    new InputStreamReader(ctx.getResources().openRawResource(R.raw.cz_sk)));
			String line;
			try {
				while ((line = br.readLine()) != null) {
					final String[] split = line.split(",");
					map.put(split[2].trim(), new Holiday(split[0].trim(), split[1].trim()));
				}
			} catch (IOException e) {
                //Don't know what to do so fail gracefully
				throw new IllegalStateException("Cannot open holidays!", e);
			}
		}


	public static class UpdateService extends Service {



		@Override
		public IBinder onBind(Intent intent) {
			// TODO Auto-generated method stub
			return null;		
		}

		@Override
		public void onStart(Intent intent, int startId) {
			
            // Push update for this widget to the home screen
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.svatky);
            Bundle extras = intent.getExtras();
            int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
            if(extras!=null) {
                widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }

            Log.d("UpdateService", "onStart() called with widgetID: " + widgetID);
            SvatkyLocale locale = SvatkyLocale.valueOf(
                    this.getSharedPreferences(SVATKY, 0).getString(String.valueOf(widgetID), SvatkyLocale.cs.toString()));
            updateViews(this, locale , views);
            Intent i = new Intent(getApplicationContext(), SvatkyWidget.class);
            Uri uri = Uri.parse("content://cz.krtinec.svatky/svatky");
            i.setData(uri);
            i.setAction(LNG_CHANGE);
            i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
            PendingIntent broadcastIntent = PendingIntent.getBroadcast(getApplicationContext(), widgetID, i, PendingIntent.FLAG_CANCEL_CURRENT);

            views.setOnClickPendingIntent(R.id.layout, broadcastIntent);
            manager.updateAppWidget(widgetID, views);
            stopSelf();
            
		}
    } 
}

class Holiday {
	String cz, sk;
	Holiday(String cz,String sk) {
		this.cz = cz;
		this.sk = sk;
	}
	@Override
	public String toString() {
		return cz + ";" + sk;
	}
	
	
}
