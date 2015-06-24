
package com.shephertz.app42.android.scheduler;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;


abstract public class WakefulIntentService extends IntentService {
	abstract protected void doWakefulWork(Intent intent);
	public static final String Name = "com.shephertz.app42.paas.sdk.android.keepAlive.WakefulIntentService";
	public static final String LastAlarm = "lastKeepAlive";
	private static volatile PowerManager.WakeLock lockStatic = null;

	/**
	 * @param name
	 */
	public WakefulIntentService(String name) {
		super(name);
		setIntentRedelivery(true);
	}

	/**
	 * @param context
	 * @return
	 */
	synchronized private static PowerManager.WakeLock getLock(Context context) {
		if (lockStatic == null) {
			PowerManager powerManager = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			lockStatic = powerManager.newWakeLock(
					PowerManager.PARTIAL_WAKE_LOCK, Name);
			lockStatic.setReferenceCounted(true);
		}
		return (lockStatic);
	}

	/**
	 * @param context
	 * @param intent
	 */
	public static void sendWakefulWork(Context context, Intent intent) {
		getLock(context.getApplicationContext()).acquire();
		context.startService(intent);
	}

	/**
	 * @param context
	 * @param clsService
	 */
	public static void sendWakefulWork(Context context, Class<?> clsService) {
		sendWakefulWork(context, new Intent(context, clsService));
	}

	/**
	 * @param listener
	 * @param ctxt
	 */
	public static void scheduleAlarms(AlarmListener listener, Context ctxt) {
		scheduleAlarms(listener, ctxt, true);
	}

	/**
	 * @param listener
	 * @param ctxt
	 * @param force
	 */
	public static void scheduleAlarms(AlarmListener listener, Context ctxt,
			boolean force) {
		
		SharedPreferences prefs = ctxt.getSharedPreferences(Name, 0);
		long lastAlarm = prefs.getLong(LastAlarm, 0);
		if (lastAlarm == 0
				|| force
				|| (System.currentTimeMillis() > lastAlarm && System
						.currentTimeMillis() - lastAlarm > listener.getMaxInteval())) {
			AlarmManager mgr = (AlarmManager) ctxt
					.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(ctxt, BackgroundReceiver.class);
			PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);
			listener.scheduleAlarms(mgr, pi, ctxt);
		}
	}

	/**
	 * @param ctxt
	 */
	public static void cancelAlarms(Context ctxt) {
		AlarmManager mgr = (AlarmManager) ctxt
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(ctxt, BackgroundReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);
		mgr.cancel(pi);
		ctxt.getSharedPreferences(Name, 0).edit().remove(LastAlarm).commit();
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		PowerManager.WakeLock lock = getLock(this.getApplicationContext());
		if (!lock.isHeld() || (flags & START_FLAG_REDELIVERY) != 0) {
			lock.acquire();
		}
		super.onStartCommand(intent, flags, startId);
		return (START_REDELIVER_INTENT);
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	final protected void onHandleIntent(Intent intent) {
		try {
			doWakefulWork(intent);
		} finally {
			PowerManager.WakeLock lock = getLock(this.getApplicationContext());
			if (lock.isHeld()) {
				try {
					lock.release();
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(),
							"Exception when releasing wakelock", e);
				}
			}
		}
	}

	/**
	 * @author Vishnu
	 */
	public interface AlarmListener {
		void scheduleAlarms(AlarmManager alarmManager, PendingIntent pi,
				Context context);
		void sendBackgroundWork(Context context);
		long getMaxInteval();
	}
}
