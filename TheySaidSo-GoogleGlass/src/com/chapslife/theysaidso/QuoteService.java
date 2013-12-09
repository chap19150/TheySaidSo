package com.chapslife.theysaidso;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

/**
 * @author kchapman
 * 
 */
public class QuoteService extends Service {

	/** Logging tag */
	private static final String TAG = QuoteService.class.getSimpleName();

	// For live card
	private LiveCard mLiveCard = null;

	private static final String sCardId = "quote_service";

	private TimelineManager mTimelineManager;
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public class LocalBinder extends Binder {
		public QuoteService getService() {
			return QuoteService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
        mTimelineManager = TimelineManager.from(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		publishCard(this);
		return START_STICKY;
	}

	@Override
    public void onDestroy() {
		unpublishCard(this);
        super.onDestroy();
	}

	private void publishCard(Context context) {
		Log.d(TAG, "publish card called");
		if (mLiveCard == null) {
			mLiveCard = mTimelineManager.getLiveCard(sCardId);
			mLiveCard.setNonSilent(true);
			mLiveCard.setViews(new RemoteViews(context.getPackageName(),
	                R.layout.activity_main));
	        Intent intent = new Intent(context, MenuActivity.class);
	        mLiveCard.setAction(PendingIntent.getActivity(context, 0,
	                intent, 0));
	        mLiveCard.publish();
		} else {
			return;
		}
	}

	private void updateCard(Context context) {

	}

	private void unpublishCard(Context context) {
		Log.d(TAG, "unpublishCard() called.");
		if (mLiveCard != null && mLiveCard.isPublished()) {
			mLiveCard.unpublish();
			mLiveCard = null;
		}
	}
}
