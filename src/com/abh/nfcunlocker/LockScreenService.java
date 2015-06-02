package com.abh.nfcunlocker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class LockScreenService extends Service{

	private BroadcastReceiver mReceiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
            unregisterReceiver(mReceiver);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        // When service stopped, stop it from being in the foreground
        // And remove persistent notification
//        stopForeground(true);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		Log.d("Mine","inside service");
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);

        // Start receiver for SCREEN_OFF event
        mReceiver = new LockScreenReceiver();
        registerReceiver(mReceiver, filter);

		
	}

}
