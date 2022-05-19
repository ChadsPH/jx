package com.chadx.injector.preference;

import androidx.preference.PreferenceFragmentCompat;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.preference.Preference;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import androidx.preference.CheckBoxPreference;
import android.content.Intent;
import com.chadx.sockshttp.R;
import com.services.logger.SkStatus;
import com.services.config.SettingsConstants;
import com.services.config.Setting;
import com.services.logger.ConnectionStatus;
import android.os.Handler;
import androidx.preference.ListPreference;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Context;
import android.os.Build;

public class SettingsAdvancedPreference extends PreferenceFragmentCompat
	implements SettingsConstants, SkStatus.StateListener,
		Preference.OnPreferenceChangeListener
{
	private SharedPreferences mPref;

	@Override
    public void onCreatePreferences(Bundle bundle, String s)
	{
        // Load the Preferences from the XML file
        setPreferencesFromResource(R.xml.advanced_settings_preference, s);

		mPref = getPreferenceManager()
			.getDefaultSharedPreferences(getContext());
			
		Setting config = new Setting(getContext());

		/*ListPreference numberMaxThreads = (ListPreference)
			findPreference(MAXIMO_THREADS_KEY);
		numberMaxThreads.setOnPreferenceChangeListener(this);*/
		
		CheckBoxPreference checkDebug = (CheckBoxPreference) findPreference(MODO_DEBUG_KEY);
		checkDebug.setOnPreferenceChangeListener(this);
		
		// update views
		getPreferenceScreen().setEnabled(!SkStatus.isTunnelActive());
		if (!SkStatus.isTunnelActive()) {
			if (new Setting(getContext()).getPrefsPrivate()
					.getBoolean(Setting.CONFIG_PROTEGER_KEY, false)) {
				findPreference(MODO_DEBUG_KEY).setEnabled(false);
			}
		}
		
		// desativa se não suportar
		if (Build.VERSION.SDK_INT < 21) {
			String[] list = {
				FILTER_APPS,
				FILTER_BYPASS_MODE,
				FILTER_APPS_LIST
			};
			for (String key : list) {
				findPreference(key).setEnabled(false);
			}
		}
		else {
			CheckBoxPreference pref = (CheckBoxPreference) findPreference(FILTER_APPS);
			pref.setOnPreferenceChangeListener(this);
			
			enableFilterLayout(config.getIsFilterApps());
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		SkStatus.addStateListener(this);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		SkStatus.removeStateListener(this);
	}

	@Override
	public void updateState(String state, String logMessage, int localizedResId, ConnectionStatus level, Intent intent)
	{
		getView().post(new Runnable() {
			@Override
			public void run() {
				getPreferenceScreen().setEnabled(!SkStatus.isTunnelActive());
			}
		});
	}
	

	/**
	 * Preference.OnPreferenceChangeListener
	 * Implementação
	 */

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue)
	{
		switch(pref.getKey()) {
			case MODO_DEBUG_KEY:
				boolean isDebug = (boolean) newValue;

				if (isDebug) {
					Toast.makeText(getContext(), "Disable after testing is complete",
						Toast.LENGTH_SHORT).show();
				}
			break;

			case FILTER_APPS:
				boolean isEnabled = (boolean) newValue;
				
				enableFilterLayout(isEnabled);
			break;
		}

		return true;
	}
	
	private void enableFilterLayout(boolean is) {
		String[] list = {
			FILTER_BYPASS_MODE,
			FILTER_APPS_LIST
		};

		for (String key : list) {
			findPreference(key).setEnabled(is);
		}
	}
	

	/**
	 * Utils
	 */

	public static void setListPreferenceSummary(ListPreference pref, String value) {
		int index = pref.findIndexOfValue(value);
		if (index >= 0) {
			pref.setSummary(pref.getEntries()[index]);
		}
	}
}
