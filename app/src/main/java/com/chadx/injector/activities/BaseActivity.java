package com.chadx.injector.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.chadx.injector.preference.SettingsPreference;
import com.services.config.Setting;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Context;
import com.chadx.injector.preference.LocaleHelper;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import static android.content.pm.PackageManager.GET_META_DATA;

import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.media.Ringtone;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import org.json.JSONObject;
import android.content.pm.PackageInfo;
import com.chadx.injector.util.Utils;
import org.json.JSONException;
import com.chadx.sockshttp.R;
import android.app.Notification;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.app.NotificationChannel;
import android.net.ConnectivityManager;
import androidx.core.app.NotificationCompat;

/**
 * Created by Pankaj on 03-11-2017.
 */
public abstract class BaseActivity extends AppCompatActivity
{
	public static int mTheme = 0;

	private ConnectivityManager connMgr;

	private NotificationManager mNotificationManager;
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//setTheme(ThemeUtil.getThemeId(mTheme));
		
		setModoNoturnoLocal();
		//Sulod sa onCreate MainBase.

			connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		resetTitles();
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.setLocale(base));
	}
	
	public void setModoNoturnoLocal() {
		boolean is = new Setting(this)
			.getModoNoturno().equals("on");

		int night_mode = is ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
		//AppCompatDelegate.setDefaultNightMode(night_mode);
		getDelegate().setLocalNightMode(night_mode);
	}
	
	protected void resetTitles() {
		try {
			ActivityInfo info = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA);
			if (info.labelRes != 0) {
				setTitle(info.labelRes);
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	

	private void errorUpdateDialog(String error) {
		new AlertDialog.Builder(this)
			.setTitle("Update error!!!")
			.setMessage(getString(R.string.alert_update_error) +
						"Error:" + error)
			.setPositiveButton("Ok", null)
			.create().show();
	}

	public void notif1() {
		NotificationCompat.Builder b = new NotificationCompat.Builder(this);
		b.setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
			.setSmallIcon(R.drawable.icon)
            .setTicker(getString(R.string.bugs_fixed))
            .setContentTitle(getString(R.string.alert_update_app))
            .setContentText(getString(R.string.alert_update_app_now))
            .setContentInfo(getString(R.string.app_name));


		NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(1, b.build());
	}

	private void notif2(){
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
		r.play();
	}
	private void updateNotif(){

		NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE); 
		Notification.Builder notification = new Notification.Builder(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notification.setChannelId(this.getPackageName() + ".icodetunnel");
			createNotification(notificationManager, this.getPackageName() + ".icodetunnel");
		}

		notification.setContentTitle("New Application Update")
			.setContentText("A new application version is detected.")
			.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
			.setDefaults(Notification.DEFAULT_ALL)
			.setPriority(Notification.PRIORITY_HIGH)
			.setShowWhen(true)
			.setSmallIcon(R.drawable.icon);
		notificationManager.notify(4130,notification.getNotification());

	}

	private void createNotification(NotificationManager notificationManager, String id)
	{
		NotificationChannel mNotif = new NotificationChannel(id, "Developer", NotificationManager.IMPORTANCE_HIGH);
		mNotif.setShowBadge(true);
		notificationManager.createNotificationChannel(mNotif);
		// TODO: Implement this method
	}
	
}
