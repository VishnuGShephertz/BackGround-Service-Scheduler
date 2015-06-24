
package com.shephertz.app42.android.scheduler;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.shephertz.app42.android.scheduler.WakefulIntentService.AlarmListener;

/**
 * @author Vishnu Garg
 * 
 */
public class BackgroundReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(final Context context, final Intent intent) {
		
		if(intent.getAction()!=null&&intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		schuduleAlarm(context, intent);
		if (intent.getAction()!=null&&intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			Log.d("KeepAliveReceiver", "ConnectivityReceiver invoked...");
			boolean noConnectivity = intent.getBooleanExtra(
					ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
			if (!noConnectivity) {
				ConnectivityManager cm = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo netInfo = cm.getActiveNetworkInfo();
				// only when connected or while connecting...
				if (netInfo != null && netInfo.isConnectedOrConnecting()) {
					Log.d("KeepAliveReceiver",
							"We have internet, start update check and disable receiver!");
					// Start service with wakelock by using WakefulIntentService
					Intent backgroundIntent = new Intent(context,
							BackgroundService.class);
					WakefulIntentService.sendWakefulWork(context,
							backgroundIntent);
					// disable receiver after we started the service
					disableReceiver(context);
				}
			}
		}
	}
	/**
	 * Enables KeepAliveReceiver
	 * 
	 * @param context
	 */
	public static void enableReceiver(Context context) {
		ComponentName component = new ComponentName(context,
				BackgroundReceiver.class);
		context.getPackageManager().setComponentEnabledSetting(component,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
	}

	/**
	 * Disables KeepAliveReceiver
	 * 
	 * @param context
	 */
	public static void disableReceiver(Context context) {
		ComponentName component = new ComponentName(context,
				BackgroundReceiver.class);
		context.getPackageManager().setComponentEnabledSetting(component,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
	}

	/**
	 * @param context
	 * @param intent
	 */
	private void schuduleAlarm(Context context, Intent intent) {
		AlarmListener listener = null;
		try {
			Class<AlarmListener> cls = (Class<AlarmListener>) Class
					.forName("com.shephertz.app42.android.background.BackgroundAlarmListener");
			try {
				listener = cls.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (listener != null) {
			if (intent.getAction() == null) {
				SharedPreferences prefs = context.getSharedPreferences(
						WakefulIntentService.Name, 0);
				prefs.edit()
						.putLong(WakefulIntentService.LastAlarm,
								System.currentTimeMillis()).commit();
				listener.sendBackgroundWork(context);
			} else {
				WakefulIntentService.scheduleAlarms(listener, context, true);
			}
		}
	}




}
