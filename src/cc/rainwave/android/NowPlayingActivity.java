package cc.rainwave.android;

import android.app.Activity;
import android.os.Bundle;

public class NowPlayingActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_nowplaying);
    }
}