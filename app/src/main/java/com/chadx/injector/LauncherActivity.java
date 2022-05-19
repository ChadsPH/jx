package com.chadx.injector;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.chadx.sockshttp.R;
import com.chadx.injector.activities.BaseActivity;
import android.os.Handler;

/**
 * @author anuragdhunna
 */
public class LauncherActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.open);

        new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Intent i = new Intent(LauncherActivity.this, a.class);
					startActivity(i);
					finish();
				}
			}, 5*1000);
    }

}


