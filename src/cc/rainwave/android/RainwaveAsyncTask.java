package cc.rainwave.android;

import android.os.AsyncTask;
import android.util.Log;
import cc.rainwave.android.api.types.RainwaveException;

/**
 * Generic class for dispatching API tasks and handling their success or failure.
 *
 * This model fits any task whose error model is to raise RainwaveException if
 * an error occurred. This will then dispatch either onFailure() or onSuccess()
 * in the onPostExecute() task depending on if that exception was raised.
 *
 * @param <Params> as in AsyncTask
 * @param <Progress> as in AsyncTask
 * @param <Result> as in AsyncTask
 */
public abstract class RainwaveAsyncTask<Params, Progress, Result>
extends AsyncTask<Params, Progress, Result> {
    private RainwaveException mRaised;
    private static final String TAG = "RainwaveAsyncTask";

    public final Result doInBackground(Params ... args) {
        try {
            return getResult(args);
        } catch (RainwaveException e) {
            mRaised = e;
            Log.d(TAG, "API error", e);
            return null;
        }
    }

    protected final void onPostExecute(Result result) {
        if(mRaised != null){
            onFailure(mRaised);
        }
        else {
            onSuccess(result);
        }
    }

    protected abstract Result getResult(Params ... args) throws RainwaveException;

    /** Called if RainwaveException was not raised in getResult(). */
    protected void onSuccess(Result result) {
        // do nothing
    }

    /** Called if RainwaveException was raised in getResult(). */
    protected void onFailure(RainwaveException raised) {
        // do nothing
    }
}
