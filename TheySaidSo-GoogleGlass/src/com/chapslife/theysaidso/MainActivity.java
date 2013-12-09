/**
 * test
 */
package com.chapslife.theysaidso;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * @author kchapman
 * 
 */
public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();
	private boolean mIsBound = false;
	private QuoteService mQuoteService;

	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d(TAG, "onServiceConnected() called.");
			mQuoteService = ((QuoteService.LocalBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "onServiceDisconnected() called.");
			mQuoteService = null;
		}
	};

	private void doUnbindService() {
		if (mIsBound) {
			unbindService(serviceConnection);
			mIsBound = false;
		}
	}

	private void doStartService() {
		startService(new Intent(this, QuoteService.class));
	}

	@Override
	protected void onDestroy() {
		doUnbindService();
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate() called.");

		// Service to control the life cycle of a LiveCard.
		// bind() does not work. We need to call start() explicitly...
		// doBindService();
		doStartService();
		
	}
}
