package com.shephertz.app42.android.scheduler;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.shephertz.app42.android.scheduler.WakefulIntentService.AlarmListener;

/**
 * @author Vishnu Garg
 * 
 */
public class BackgroundAlarmListener implements AlarmListener {
	/* (non-Javadoc)
	 * @see com.shephertz.app42.android.background.WakefulIntentService.AlarmListener#scheduleAlarms(android.app.AlarmManager, android.app.PendingIntent, android.content.Context)
	 */
	@Override
	public void scheduleAlarms(AlarmManager alarmManager, PendingIntent pi,
			Context context) {
		Log.i("BackgroundAlarmListener", "Schedule update check...");
		// every day at 9 pm
		Calendar calendar = Calendar.getInstance();
		// if it's after or equal 9 am schedule for next day
		if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 21) {
			calendar.add(Calendar.DAY_OF_YEAR, 1); // add, not set!
		}
		calendar.set(Calendar.HOUR_OF_DAY, 21);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		alarmManager.setInexactRepeating(AlarmManager.RTC,
				calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
	} 

	/* (non-Javadoc)
	 * @see com.shephertz.app42.android.background.WakefulIntentService.AlarmListener#sendBackgroundWork(android.content.Context)
	 */
	@Override
	public void sendBackgroundWork(Context context) {
		// TODO Auto-generated method stub
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		// only when connected or while connecting...
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			Log.d("BackgroundAlarmListener",
					"We have internet, start update check directly now!");
			Intent backgroundIntent = new Intent(context,
					BackgroundService.class);
			WakefulIntentService.sendWakefulWork(context, backgroundIntent);
		} else {
			Log.d("BackgroundAlarmListener",
					"We have no internet, enable ConnectivityReceiver!");
			// enable receiver to schedule update when internet is available!
			BackgroundReceiver.enableReceiver(context);
		}
	} 
	
	/* (non-Javadoc)
	 * @see com.shephertz.app42.android.background.WakefulIntentService.AlarmListener#getMaxInteval()
	 */
	@Override
	public long getMaxInteval() {
		// TODO Auto-generated method stub
		return (AlarmManager.INTERVAL_DAY + 60 * 1000);
	}
}
