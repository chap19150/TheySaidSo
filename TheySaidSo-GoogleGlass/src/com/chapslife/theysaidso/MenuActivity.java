/**
 * test
 */
package com.chapslife.theysaidso;

import android.app.Activity;
import android.content.ComponentName;
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
public class MenuActivity extends Activity {
    
    private static final String TAG = MenuActivity.class.getSimpleName();
    private QuoteService mQuoteService;
    private boolean mResumed;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected() called.");
            mQuoteService = ((QuoteService.LocalBinder) service).getService();
            openOptionsMenu();
        }

        public void onServiceDisconnected(ComponentName className) {
            
            mQuoteService = null;
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, QuoteService.class), serviceConnection, 0);
    }

    
    @Override
    public void onResume() {
        super.onResume();
        mResumed = true;
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.quote, menu);
        return true;
    }

    @Override
    public void openOptionsMenu() {
        Log.d(TAG, "openOptionsMenu() called.");
        if (mResumed && mQuoteService != null) {
            super.openOptionsMenu();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.stop_menu_item:
                stopService(new Intent(this, QuoteService.class));
                return true;
            case R.id.read_aloud_menu_item:
                mQuoteService.readHeadingAloud();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);

        unbindService(serviceConnection);

        // We must call finish() from this method to ensure that the activity ends either when an
        // item is selected from the menu or when the menu is dismissed by swiping down.
        finish();
    }
}