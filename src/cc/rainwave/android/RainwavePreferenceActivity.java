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
        final String parts[] = Rainwave.parseUrl(uri, this);

        if(parts != null) {
            mPreferences.setUserInfo(parts[0], parts[1]);
        }
    }

    public void onListItemClick(ListView list, View v, int position, long id) {
        Log.d("PreferencesActivity", "onListItemClick()");
    }

    private static final String ENTRY_IMPORT_QR = "import_qr";
    private static final String ENTRY_CLEAR_PREFERENCES = "clear_preferences";
}
