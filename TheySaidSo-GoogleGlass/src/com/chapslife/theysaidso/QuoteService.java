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
import android.util.Log;
import android.util.SparseArray;
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

    private static final int SPARSE_QUOTE = 1;
    private static final int SPARSE_QUOTE_AUTHOR = 2;
    private static final int SPARSE_QUOTE_ID = 3;
    
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
            mLiveCard.setNonSilent(false);
            mLiveCard.setViews(new RemoteViews(context.getPackageName(), R.layout.activity_main));
            Intent intent = new Intent(context, MenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(context, 0, intent, 0));
            mLiveCard.publish();
            new FetchContent().execute();
        } else {
            return;
        }
    }

    private void updateCard(Context context, SparseArray<String> quoteArray) {
        Log.d(TAG, "updateCard card called");
        if (mLiveCard == null) {
            // Use the default content.
            publishCard(context);
        } else {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.activity_main);
            String quote = quoteArray.get(SPARSE_QUOTE);
            if(quote != null){
                remoteViews.setCharSequence(R.id.livecard_content, "setText", quote);
                mLiveCard.setViews(remoteViews);
                // Do we need to re-publish ???
                // Unfortunately, the view does not refresh without this....
                Intent intent = new Intent(context, MenuActivity.class);
                mLiveCard.setAction(PendingIntent.getActivity(context, 0, intent, 0));
                // Is this if() necessary???? or Is it allowed/ok not to call
                // publish() when updating????
                if(! mLiveCard.isPublished()) {
                    mLiveCard.publish();
                }
            }
            
        }
    }

    private void unpublishCard(Context context) {
        Log.d(TAG, "unpublishCard() called.");
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }
    }

    private class FetchContent extends AsyncTask<Void, Void, SparseArray<String>> {

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

                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                  response += s;
                }
                Log.d(TAG, response);
                return(parseJSON(new JSONObject(response)));
              } catch (Exception e) {
                e.printStackTrace();
              }
            return null;
        }

        protected void onPostExecute(SparseArray<String> result) {
            if(result != null){
                updateCard(QuoteService.this, result);
            }
        }
        
        private SparseArray<String> parseJSON(JSONObject object){
            SparseArray<String> quote = new SparseArray<String>();
            JSONObject contentsObject = object.optJSONObject("contents");
            if(contentsObject != null){
                quote.put(SPARSE_QUOTE, contentsObject.optString("quote"));
                quote.put(SPARSE_QUOTE_AUTHOR, contentsObject.optString("author"));
                quote.put(SPARSE_QUOTE_ID, contentsObject.optString("id"));
            }
            return quote;
        }
    }
}
