package cc.rainwave.android;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class RainwavePreferenceActivity extends PreferenceActivity {

		public void onCreate(Bundle icicle) {
			super.onCreate(icicle);
			addPreferencesFromResource(R.xml.preferences);
		}
	
}
