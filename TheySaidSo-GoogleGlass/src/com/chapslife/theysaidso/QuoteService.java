package com.chapslife.theysaidso;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
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

import com.google.android.glass.app.Card;
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

	private static final int SPARSE_BOOKNAME = 1;
	private static final int SPARSE_CHAPTER = 2;
	private static final int SPARSE_VERSE = 3;
	private static final int SPARSE_TEXT = 4;
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
		new FetchContent().execute();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mSpeech.shutdown();
	}

	private void publishCard(Context context, SparseArray<String> quoteArray) {
		Log.d(TAG, "updateCard card called");
		mCard = new Card(this);
		String text = quoteArray.get(SPARSE_TEXT);
		String bookname = quoteArray.get(SPARSE_BOOKNAME);
		String chapter = quoteArray.get(SPARSE_CHAPTER);
		String verse = quoteArray.get(SPARSE_VERSE);
		
		if (text != null) {
			mCard.setText(text);
			if (bookname != null && chapter != null && verse != null) {
				mCard.setFootnote(bookname + " " + chapter + ":" + verse);
			}
			mCardId = mTimelineManager.insert(mCard);
			Calendar cal = Calendar.getInstance();
			
			Intent intent = new Intent(this, QuoteService.class);
			PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

			AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			// Start every 30 seconds
			alarm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + AlarmManager.INTERVAL_DAY, pintent);
			stopSelf();
		}
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
			HttpGet httpGet = new HttpGet("http://labs.bible.org/api/?passage=random&type=json");
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
				return (parseJSON(new JSONArray(response)));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(SparseArray<String> result) {
			if (result != null) {
				publishCard(QuoteService.this, result);
			}
		}

		private SparseArray<String> parseJSON(JSONArray object) {
			
			try {
				JSONObject contentsObject = object.getJSONObject(0);
				if (object != null && contentsObject != null) {
					mQuote = new SparseArray<String>();
					mQuote.put(SPARSE_BOOKNAME, contentsObject.optString("bookname"));
					mQuote.put(SPARSE_CHAPTER,
							contentsObject.optString("chapter"));
					mQuote.put(SPARSE_VERSE, contentsObject.optString("verse"));
					mQuote.put(SPARSE_TEXT, contentsObject.optString("text"));
					return mQuote;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
	}

	/**
	 * Read the current quote aloud using the text-to-speech engine.
	 */
	public void readHeadingAloud() {
		if (mQuote != null) {
			String quote = mQuote.get(SPARSE_TEXT);
			mSpeech.speak(quote, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
}
