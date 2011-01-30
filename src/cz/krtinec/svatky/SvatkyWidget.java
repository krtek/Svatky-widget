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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.utils.URIUtils;


import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;

public class SvatkyWidget extends AppWidgetProvider {
	static final String LNG_CHANGE = "LngChange";
	
	 public void onUpdate(Context context, AppWidgetManager appWidgetManager,
		        int[] appWidgetIds) {
		        // To prevent any ANR timeouts, we perform the update in a service
		        context.startService(new Intent(context, UpdateService.class));
		    }
	 
	 

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.i("SvatkyWidget", "Received " + intent);
		Log.i("SvatkyWidget", "Received " + intent.getAction());
		Log.i("SvatkyWidget", "Received " + intent.getData());
		if (LNG_CHANGE.equals(intent.getAction())) {			
	        RemoteViews views = new RemoteViews("cz.krtinec.svatky", R.layout.svatky);
	        //change locale
	        SvatkyLocale loc = SvatkyLocale.valueOf(context.getSharedPreferences("Svatky", 0).getString("lang", SvatkyLocale.cs.toString()));
	        SharedPreferences.Editor editor = context.getSharedPreferences("Svatky", 0).edit();
	        if (loc.equals(SvatkyLocale.cs)) {
	        	editor.putString("lang", SvatkyLocale.sk.abbr);
	        } else {
	        	editor.putString("lang", SvatkyLocale.cs.abbr);
	        }
	        editor.commit();
	        loc = SvatkyLocale.valueOf(context.getSharedPreferences("Svatky", 0).getString("lang", SvatkyLocale.cs.toString()));
	        UpdateService.updateViews(loc, views);
	        AppWidgetManager manager = AppWidgetManager.getInstance(context);
	        ComponentName thisWidget = new ComponentName(context, SvatkyWidget.class);
	        manager.updateAppWidget(thisWidget, views);

		}		
	}


	public static class UpdateService extends Service {
		static Map<String,Holiday> namedays;

		@Override
		public IBinder onBind(Intent intent) {
			// TODO Auto-generated method stub
			return null;		
		}

		@Override
		public void onStart(Intent intent, int startId) {				
			namedays = new HashMap<String, Holiday>();
			loadNamedays(namedays);		
			
            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(this, SvatkyWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            SvatkyLocale loc = SvatkyLocale.valueOf(getApplicationContext().getSharedPreferences("Svatky", 0).getString("lang", SvatkyLocale.cs.abbr));
            
            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.svatky);           
            
            updateViews(loc, views);
            Intent i = new Intent(getApplicationContext(), SvatkyWidget.class);
            Uri uri = Uri.parse("content://cz.krtinec.svatky/svatky");
            i.setData(uri);
            i.setAction(LNG_CHANGE);
            views.setOnClickPendingIntent(R.id.layout, PendingIntent.getBroadcast(getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT));
            manager.updateAppWidget(thisWidget, views);
            stopSelf();
            
		}

		static void updateViews(SvatkyLocale loc, RemoteViews views) {
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

		private void loadNamedays(Map<String, Holiday> map) {
			BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.cz_sk)));
			String line;
			try {
				while ((line = br.readLine()) != null) {
					final String[] split = line.split(",");
					map.put(split[2].trim(), new Holiday(split[0].trim(), split[1].trim()));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
