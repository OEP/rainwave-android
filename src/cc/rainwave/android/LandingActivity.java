package cc.rainwave.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

/**
 * First thing the user sees when starting the app.
 * Provides an easier prompt for getting identified.
 * @author pkilgo
 *
 */
public class LandingActivity extends Activity {

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_landing);
		
		findViewById(R.id.land_icon).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(LandingActivity.this,NowPlayingActivity.class);
				startActivity(i);				
			}

		});
	}
	
	@Override
	public void onAttachedToWindow() {
	    super.onAttachedToWindow();
	    Window window = getWindow();
	    window.setFormat(PixelFormat.RGBA_8888);
	}
	
}
