package cc.rainwave.android;

import cc.rainwave.android.listeners.HexadecimalKeyListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.widget.EditText;

public class RainwavePreferenceActivity extends PreferenceActivity {

		public void onCreate(Bundle icicle) {
			super.onCreate(icicle);
			addPreferencesFromResource(R.xml.preferences);
			setupUI();
		}
		
		private void setupUI() {
		    EditTextPreference key = (EditTextPreference) findPreference(Rainwave.PREFS_KEY);
		    EditText field = key.getEditText();
		    field.setKeyListener(new HexadecimalKeyListener());
		}
	
}
