package com.background.task.demo.sample;

import android.app.Activity;
import android.os.Bundle;
import com.shephertz.app42.android.scheduler.BackgroundAlarmListener;
import com.shephertz.app42.android.scheduler.WakefulIntentService;

/**
 * @author Vishnu Garg
 * 
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		WakefulIntentService.scheduleAlarms(new BackgroundAlarmListener(), this, false);
	}


}
