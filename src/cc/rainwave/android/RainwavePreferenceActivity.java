package cc.rainwave.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import cc.rainwave.android.api.Session;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class RainwavePreferenceActivity extends PreferenceActivity {

    private RainwavePreferences mPreferences;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPreferences = RainwavePreferences.getInstance(this);
        addPreferencesFromResource(R.xml.preferences);
        setupUI();
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
                Session.getInstance(RainwavePreferenceActivity.this).clearUserInfo();
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
