
package com.shephertz.app42.android.scheduler;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author Vishnu Garg
 * 
 */
public class BackgroundService extends WakefulIntentService {
	public final int TypeWifi  = 1;
	public final int TypeMobile = 2;
	public final int TypeNotConnected = 0;

	public BackgroundService() {
		super("BackgroundService");
		
	}
	/*
	 *  
     * Asynchronous background operations of service, with wakelock
	 * (non-Javadoc)
	 * @see
	 * com.example.keepalivetask.WakefulIntentService#doWakefulWork(android.
	 * content.Intent)
	 */
	@Override
	protected void doWakefulWork(Intent intent) {
		int status = getConnectivityStatus(this);
		if (status != TypeNotConnected) {
			
		}
		
	}
	/** 
	 * This function checks connectivityStatus If connectivity Changes
	 * @param context
	 * @return
	 */
	public  int getConnectivityStatus(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (null != activeNetwork) {
			if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
				return TypeWifi;
			if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
				return TypeMobile;
		}
		return TypeNotConnected;
	}
	
}
