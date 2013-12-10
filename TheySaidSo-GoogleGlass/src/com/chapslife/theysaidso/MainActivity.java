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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

    private void doStopService() {
        stopService(new Intent(this, QuoteService.class));
    }

    @Override
    protected void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }

    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.quote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
        case R.id.stop_menu_item:
            doStopService();
            return true;
        case R.id.read_aloud_menu_item:
            mQuoteService.readHeadingAloud();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
