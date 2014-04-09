package cc.rainwave.android;

import java.net.HttpURLConnection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import cc.rainwave.android.api.Session;
import cc.rainwave.android.api.types.RainwaveException;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * First thing the user sees when starting the app.
 * Provides an easier prompt for getting identified.
 * @author pkilgo
 *
 */
public class LandingActivity extends Activity {

    private Session mSession;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Unpickle a session
        mSession = Session.getInstance();
        mSession.unpickle(this);

        // Skip this activity if the user has logged in.
        if(Rainwave.hasUserInfo(this) || Rainwave.skipLanding(this)) {
            startNowPlaying();
        }

        // We use the throbber while we try and login.
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_landing);
        setListeners();
    }

    public void onActivityResult(int request, int result, Intent data) {
        // Handle result from bar code scanner
        IntentResult ir = IntentIntegrator.parseActivityResult(request, result, data);
        if(ir == null) return;

        String raw = ir.getContents();
        if(raw == null) return;

        Uri uri = Uri.parse(raw);
        final String[] parts = Rainwave.parseUrl(uri, this);

        if(parts != null) {
            ((EditText)findViewById(R.id.land_userId)).setText(parts[0]);
            ((EditText)findViewById(R.id.land_apiKey)).setText(parts[1]);
        }
    }

    /** Set actions for buttons. */
    private void setListeners() {
        findViewById(R.id.land_login).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = ((EditText)findViewById(R.id.land_userId)).getText().toString();
                String key = ((EditText)findViewById(R.id.land_apiKey)).getText().toString();

                if(user != null && user.length() > 0 && key != null & key.length() > 0) {
                    new VerifyCredentials().execute(user, key);
                }
                else {
                    Rainwave.showError(LandingActivity.this, R.string.msg_nullFieldError);
                }
            }
        });

        findViewById(R.id.land_later).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startNowPlaying();
            }
        });

        findViewById(R.id.land_never).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Rainwave.setSkipLanding(LandingActivity.this, true);
                startNowPlaying();
            }
        });

        findViewById(R.id.land_qrButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator.initiateScan(LandingActivity.this);
            }
        });
    }

    private void startNowPlaying() {
        Intent i = new Intent(this, NowPlayingActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    /**
     * Verifies user credentials in background task.
     * 
     * @author pkilgo
     *
     */
    protected class VerifyCredentials extends AsyncTask<String, Integer, String> {
        private String TAG = "VerifyCredentials";

        @Override
        protected void onPreExecute() {
            findViewById(R.id.land_login).setEnabled(false);
            findViewById(R.id.land_later).setEnabled(false);
            findViewById(R.id.land_never).setEnabled(false);
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(String ... args) {
            String userid = args[0];
            String key = args[1];

            mSession.setUserInfo(userid, key);

            try {
                mSession.info();
                return null;
            } catch (RainwaveException e) {
                switch(e.getStatusCode()) {
                case HttpURLConnection.HTTP_FORBIDDEN:
                    return LandingActivity.this.getResources().getString(R.string.msg_authenticationFailure);
                default:
                    Log.w(TAG, "Unexpected exception: " + e.getMessage(), e);
                    return e.getMessage();
                }
            }
        }

        @Override
        protected void onPostExecute(String error) {
            super.onPostExecute(error);

            // 'error' is null when there is no error, or a user-friendly string we should show to the user.
            setProgressBarIndeterminateVisibility(false);

            if(error != null || !mSession.isAuthenticated()) {
                mSession.clearUserInfo();
                findViewById(R.id.land_login).setEnabled(true);
                findViewById(R.id.land_later).setEnabled(true);
                findViewById(R.id.land_never).setEnabled(true);
                setProgressBarIndeterminateVisibility(false);
                Toast.makeText(LandingActivity.this, error, Toast.LENGTH_LONG).show();
                return;
            }

            mSession.pickle(LandingActivity.this);
            startNowPlaying();
        }
    }
}
