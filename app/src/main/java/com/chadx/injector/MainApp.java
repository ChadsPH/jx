package com.chadx.injector;

import android.app.Application;
import android.content.SharedPreferences;
import com.services.config.Setting;
import android.content.res.Configuration;
import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import com.services.MainCore;
import com.chadx.injector.util.Utils;

public class MainApp extends Application
{
	public static final String PREFS_GERAL = "SocksHttpGERAL";
	private static MainApp mApp;
	private static SharedPreferences sp;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		mApp = this;
		sp = new Setting(this).getPrefsPrivate();
		MainCore.init(this);
		Utils.init(this);
		Utils.xs();
	  	//Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		com.chadx.injector.util.Utils.overrideFont(getApplicationContext(), "SERIF", "GoogleSans-Regular.ttf");
	}
	
	public static MainApp getApp() {
		return mApp;
	}
	
	public static SharedPreferences getSharedPrefs() {
		return sp;
	}
}
