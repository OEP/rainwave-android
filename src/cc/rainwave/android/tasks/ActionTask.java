package cc.rainwave.android.tasks;

import java.io.IOException;
import java.net.MalformedURLException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cc.rainwave.android.Rainwave;
import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.GenericResult;
import cc.rainwave.android.api.types.RainwaveException;
import cc.rainwave.android.api.types.Song;

/**
 * AsyncTask for submitting a rating for a song. Expects two arguments to
 * <code>execute(Object...params)</code>, which are song_id (int), and rating
 * (float).
 * 
 * @author pkilgo
 * 
 */
public class ActionTask extends AsyncTask<Object, Integer, GenericResult> {
	public static final String TAG = "ActionTask";
	
	private Context mContext;
	
	private Handler mCallback;
	
	private int mAction;

	private Session mSession;
	
	public ActionTask(Context ctx) {
		mContext = ctx;
		
		try {
			mSession = Session.makeSession(ctx);
		} catch (MalformedURLException e) {
			
		}
	}

	@Override
	protected GenericResult doInBackground(Object... params) {
		Log.d(TAG, "Beginning ActionTask.");
		mAction = (Integer) params[0];

		try {
			switch (mAction) {
			case RATE:
				int songId = (Integer) params[1];
				float rating = (Float) params[2];
				return mSession.rateSong(songId, rating);

			case REMOVE:
				Song s = (Song) params[1];
				return mSession.deleteRequest(s).request_delete_return;

			case REORDER:
				Song songs[] = (Song[]) params[1];
				return mSession.reorderRequests(songs).request_reorder_return;

			}

		} catch (IOException e) {
			Log.e(TAG, "IO error: " + e.getMessage());
			Rainwave.showError(mContext, e);
		} catch (RainwaveException e) {
			Log.e(TAG, "API error: " + e.getMessage());
			Rainwave.showError(mContext, e);
		}
		return null;
	}

	protected void onPostExecute(GenericResult result) {
		Log.d(TAG, "ActionTask ended.");
		
		if(mCallback != null) {
			Message msg = mCallback.obtainMessage(mAction);
			Bundle b = msg.getData();
			
			if(result != null && mAction == RATE) {
				b.putFloat("album_rating", result.album_rating);
				b.putFloat("album_rating", result.song_rating);
			}
			
			msg.sendToTarget();
		}
	}

	public static final int REMOVE = 0x439023, RATE = 0x4A73,
			REORDER = 0x4304D34;
}