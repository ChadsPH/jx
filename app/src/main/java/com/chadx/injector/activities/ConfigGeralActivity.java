package com.chadx.injector.activities;

import android.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Toast;
import com.chadx.sockshttp.R;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.content.DialogInterface;
import android.app.Dialog;
import android.app.AlertDialog;
import android.view.WindowManager;
import android.widget.AdapterView;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import java.util.List;
import java.util.HashMap;
import android.widget.SimpleAdapter;
import androidx.fragment.app.FragmentManager;
import com.chadx.injector.preference.SettingsPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import com.chadx.injector.preference.SettingsSSHPreference;
import androidx.appcompat.app.ActionBar;
/*tknetwork 04-28-22*/
public class ConfigGeralActivity extends BaseActivity
	implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback
{
	public static String OPEN_SETTINGS_SSH = "openSSHScreen";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();
     //   beginTransaction.replace(R.id.container);
        beginTransaction.commit();
        setupActionBar();

		PreferenceFragmentCompat preference = new SettingsPreference();
		Intent intent = getIntent();
		
		String action = intent.getAction();
		if (action != null && action.equals(OPEN_SETTINGS_SSH)) {
			setTitle(R.string.settings_ssh);
			preference = new SettingsSSHPreference();
		}
		
		// add preference settings
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.fragment_configLinearLayout, preference)
			.commit();

		// toolbar
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		
	}
	
	private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
			actionBar.setDisplayHomeAsUpEnabled(true);
			//    actionBar.setHomeAsUpIndicator(R.drawable.abc_ic_clear_material);
        }
	}
	
	@Override
	public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
		// Instantiate the new Fragment
		final Bundle bundle = pref.getExtras();
		final Fragment fragment = Fragment.instantiate(this, pref.getFragment(), bundle);
        
		fragment.setTargetFragment(caller, 0);
       
		// Replace the existing Fragment with the new Fragment
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.fragment_configLinearLayout, fragment)
			.addToBackStack(null)
			.commit();
		
		return true;
	}

	@Override
	public boolean onSupportNavigateUp()
	{
		onBackPressed();
		return true;
	}
}

