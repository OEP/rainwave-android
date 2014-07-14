/*
 * Copyright (c) 2013, Paul M. Kilgo
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of Paul Kilgo nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
 * A short splash screen that greets users on first run.
 * 
 * This is primarily to greet new users and prompt them to run the app in
 * authenticated mode. It explains a bit about how to sign up at the Rainwave
 * web site and offers the option to sign in, do it later, or never sign in.
 * 
 * If the user has previously signed in or they have elected to never sign in,
 * this Activity should finish() itself and start the NowPlayingActivity.
 */
public class LandingActivity extends Activity {

    private Session mSession;
    private RainwavePreferences mPreferences;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Unpickle a session
        mSession = Session.getInstance(this);
        mSession.unpickle();
        mPreferences = RainwavePreferences.getInstance(this);

        // Skip this activity if the user has logged in.
        if(mSession.hasCredentials() || mPreferences.getSkipLanding()) {
            startNowPlaying();
        }

        // We use the throbber while we try and login.
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_landing);
        setListeners();
    }

    @Override
    public void onActivityResult(int request, int result, Intent data) {
        // Handle result from bar code scanner
        IntentResult ir = IntentIntegrator.parseActivityResult(request, result, data);
        if(ir == null) return;

        String raw = ir.getContents();
        if(raw == null) return;

        Uri uri = Uri.parse(raw);
        final String[] parts = Utility.parseUrl(uri);

        if(parts != null) {
            ((EditText)findViewById(R.id.land_userId)).setText(parts[0]);
            ((EditText)findViewById(R.id.land_apiKey)).setText(parts[1]);
        }
    }

    /** Set actions for buttons. */
    private void setListeners() {
        // Clicking the "Login" button verifies the credentials. The AsyncTask
        // handles starting the next activity.
        findViewById(R.id.land_login).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = ((EditText)findViewById(R.id.land_userId)).getText().toString();
                String key = ((EditText)findViewById(R.id.land_apiKey)).getText().toString();

                if(user != null && user.length() > 0 && key != null & key.length() > 0) {
                    new VerifyCredentials().execute(user, key);
                }
                else {
                    Toast.makeText(LandingActivity.this, R.string.msg_nullFieldError, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Clicking the "Later" button starts NowPlayingActivity.
        findViewById(R.id.land_later).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startNowPlaying();
            }
        });

        // Clicking the "Never" button starts NowPlayingActivity and sets a
        // preference that ensures the user will not see the splash screen again.
        findViewById(R.id.land_never).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreferences.setSkipLanding(true);
                startNowPlaying();
            }
        });

        // Clicking on the "QR Code" button starts the Zxing scanner.
        findViewById(R.id.land_qrButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator.initiateScan(LandingActivity.this);
            }
        });
    }

    /** Utility function for starting NowPlayingActivity. */
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
     * If the credentials fail or there is some other exception, shows a Toast
     * message to the user. If the credentials are accepted, NowPlayingActivity
     * is launched.
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

            setProgressBarIndeterminateVisibility(false);

            // If `error` is null this means there was not a problem.
            if(error != null || !mSession.isAuthenticated()) {
                mSession.clearUserInfo();
                findViewById(R.id.land_login).setEnabled(true);
                findViewById(R.id.land_later).setEnabled(true);
                findViewById(R.id.land_never).setEnabled(true);
                setProgressBarIndeterminateVisibility(false);
                Toast.makeText(LandingActivity.this, error, Toast.LENGTH_LONG).show();
                return;
            }

            mSession.pickle();
            startNowPlaying();
        }
    }
}
