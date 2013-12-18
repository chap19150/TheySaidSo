package com.chapslife.theysaidso;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseArray;
import android.widget.RemoteViews;

import com.google.android.glass.app.Card;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

/**
 * @author kchapman
 * 
 */
public class QuoteService extends Service {

	/** Logging tag */
	private static final String TAG = QuoteService.class.getSimpleName();

	private TimelineManager mTimelineManager;
	private TextToSpeech mSpeech;
	private SparseArray<String> mQuote;

	private static final int SPARSE_QUOTE = 1;
	private static final int SPARSE_QUOTE_AUTHOR = 2;
	private static final int SPARSE_QUOTE_ID = 3;
	private final LocalBinder mBinder = new LocalBinder();
	private Card mCard = null;
	private long mCardId = -1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
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
		// Even though the text-to-speech engine is only used in response to a
		// menu action, we
		// initialize it when the application starts so that we avoid delays
		// that could occur
		// if we waited until it was needed to start it up.
		mSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				// Do nothing.
			}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		publishCard(this);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// unpublishCard(this);
		super.onDestroy();
	}

	private void publishCard(Context context) {
		Log.d(TAG, "publish card called");
		if (mCard == null) {
			mCard = new Card(this);
			new FetchContent().execute();
		} else {
			return;
		}
	}

	private void updateCard(Context context, SparseArray<String> quoteArray) {
		Log.d(TAG, "updateCard card called");
		if (mCard == null) {
			// Use the default content.
			publishCard(context);
		} else {
			String quote = quoteArray.get(SPARSE_QUOTE);
			String author = quoteArray.get(SPARSE_QUOTE_AUTHOR);
			if (quote != null) {
				mCard.setText(quote);
				if (author != null) {
					mCard.setFootnote(author);
				}
				mCardId = mTimelineManager.insert(mCard);
			}

		}
	}

	private void unpublishCard(Context context) {

	}

	private class FetchContent extends
			AsyncTask<Void, Void, SparseArray<String>> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected SparseArray<String> doInBackground(Void... params) {
			Log.d(TAG, "doInBackground called");
			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet("http://api.theysaidso.com/qod.json");
			String response = "";
			try {
				HttpResponse execute = client.execute(httpGet);
				InputStream content = execute.getEntity().getContent();

				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(content));
				String s = "";
				while ((s = buffer.readLine()) != null) {
					response += s;
				}
				Log.d(TAG, response);
				return (parseJSON(new JSONObject(response)));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(SparseArray<String> result) {
			if (result != null) {
				updateCard(QuoteService.this, result);
			}
		}

		private SparseArray<String> parseJSON(JSONObject object) {
			JSONObject contentsObject = object.optJSONObject("contents");
			if (contentsObject != null) {
				mQuote = new SparseArray<String>();
				mQuote.put(SPARSE_QUOTE, contentsObject.optString("quote"));
				mQuote.put(SPARSE_QUOTE_AUTHOR,
						contentsObject.optString("author"));
				mQuote.put(SPARSE_QUOTE_ID, contentsObject.optString("id"));
				return mQuote;
			}
			return null;
		}
	}

	/**
	 * Read the current quote aloud using the text-to-speech engine.
	 */
	public void readHeadingAloud() {
		if (mQuote != null) {
			String quote = mQuote.get(SPARSE_QUOTE);
			mSpeech.speak(quote, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
}
