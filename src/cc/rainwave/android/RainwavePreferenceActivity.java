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

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import cc.rainwave.android.api.Session;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class RainwavePreferenceActivity extends PreferenceActivity {

    private RainwavePreferences mPreferences;

    private Session mSession;

    private SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                String key) {
            if(key.equals(RainwavePreferences.USERID) || key.equals(RainwavePreferences.KEY)) {
                mSession.clearStations();
                mSession.unpickle();
            }
        }
    };

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPreferences = RainwavePreferences.getInstance(this);
        mSession = Session.getInstance(this);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(mListener);
        setupUI();
    }

    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
           .unregisterOnSharedPreferenceChangeListener(mListener);
    }

    private void setupUI() {
        Preference qr = findPreference(ENTRY_IMPORT_QR);
        qr.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference p) {
                IntentIntegrator.initiateScan(RainwavePreferenceActivity.this);
                return true;
            }
        });

        Preference clear = findPreference(ENTRY_CLEAR_PREFERENCES);
        clear.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference p) {
                mPreferences.clear();
                return true;
            }
        });
    }

    public void onActivityResult(int request, int result, Intent data) {
        IntentResult ir = IntentIntegrator.parseActivityResult(request, result, data);
        if(ir == null) return;


        String raw = ir.getContents();
        if(raw == null) return;
        Uri uri = Uri.parse(raw);
        final String parts[] = Utility.parseUrl(uri);

        if(parts != null) {
            mPreferences.setUserInfo(parts[0], parts[1]);
        }
    }

    private static final String ENTRY_IMPORT_QR = "import_qr";
    private static final String ENTRY_CLEAR_PREFERENCES = "clear_preferences";
}
