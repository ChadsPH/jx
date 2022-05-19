package com.chadx.injector.adapter;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import com.chadx.sockshttp.R;
import com.services.config.Setting;
import android.content.Intent;
import android.widget.Button;
import android.os.Bundle;
import com.chadx.injector.a;

public class TunnelActivity extends AppCompatActivity implements View.OnClickListener {

	private Toolbar toolbar_main;
	private RadioButton btnDirect;
    private RadioButton btnHTTP;
	private RadioButton btnSSL;
	private RadioButton btnSslPayload;
	private RadioButton btnSslRp;
	private RadioButton btnSlowDNS;
	private CheckBox customPayload;
	private Setting mConfig;
	private SharedPreferences prefs;
	private Button save;

	private TextView mTextView;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnDirect:
				btnHTTP.setChecked(false);
				btnSSL.setChecked(false);
				btnSslPayload.setChecked(false);
				btnSlowDNS.setChecked(false);
				customPayload.setEnabled(true);
				btnSslRp.setChecked(false);
				if (customPayload.isChecked()) {
					mTextView.setText(getString(R.string.direct) + getString(R.string.custom_payload1));
				} else {
					mTextView.setText(getString(R.string.direct));			
				}
				break;

			case R.id.btnHTTP:
				btnDirect.setChecked(false);
				btnSSL.setChecked(false);
				btnSlowDNS.setChecked(false);
				btnSslPayload.setChecked(false);
				customPayload.setEnabled(true);
				btnSslRp.setChecked(false);
				if (customPayload.isChecked()) {
					mTextView.setText(getString(R.string.http) + getString(R.string.custom_payload1));
				} else {
					mTextView.setText(getString(R.string.http));			
				}
				break;

			case R.id.btnSSL:
				btnHTTP.setChecked(false);
				btnDirect.setChecked(false);
				btnSlowDNS.setChecked(false);
				customPayload.setEnabled(false);
				btnSslPayload.setChecked(false);
				customPayload.setChecked(false);
				btnSslRp.setChecked(false);
				mTextView.setText(getString(R.string.ssl));		
				break;



			case R.id.btnSslPayload:
				btnHTTP.setChecked(false);
				btnDirect.setChecked(false);
				btnSSL.setChecked(false);
				btnSlowDNS.setChecked(false);
				btnSslPayload.setChecked(true);
				customPayload.setEnabled(true);
				btnSslRp.setChecked(false);
				if (customPayload.isChecked()) {
					mTextView.setText(getString(R.string.sslpayload) + getString(R.string.custom_payload1));
				} else {
					mTextView.setText(getString(R.string.sslpayload));			
				}
				break;

			case R.id.btnSlowDNS:
				btnHTTP.setChecked(false);
				btnSSL.setChecked(false);
				btnDirect.setChecked(false);
				customPayload.setEnabled(false);
				btnSslPayload.setChecked(false);
				customPayload.setChecked(false);
				btnSslRp.setChecked(false);
				mTextView.setText(getString(R.string.slowdns));		
				break;

			case R.id.btnSslrp:
				btnHTTP.setChecked(false);
				btnDirect.setChecked(false);
				btnSSL.setChecked(false);
				btnSlowDNS.setChecked(false);
				btnSslPayload.setChecked(false);
				customPayload.setEnabled(true);
				btnSslRp.setChecked(true);
				if (customPayload.isChecked()) {
					mTextView.setText(getString(R.string.sslrp) + getString(R.string.custom_payload1));
				} else {
					mTextView.setText(getString(R.string.sslrp));			
				}
				break;

			case R.id.customPayload:
				if (customPayload.isChecked()) {
					if (btnDirect.isChecked()) {
						mTextView.setText(getString(R.string.direct) + getString(R.string.custom_payload1));
					} else if (btnHTTP.isChecked()) {
						mTextView.setText(getString(R.string.http) + getString(R.string.custom_payload1));
					}else if (btnSslPayload.isChecked()) {
						mTextView.setText(getString(R.string.sslpayload) + getString(R.string.custom_payload1));
					}
				} else {
					if (btnDirect.isChecked()) {
						mTextView.setText(getString(R.string.direct));
					} else if (btnHTTP.isChecked()) {
						mTextView.setText(getString(R.string.http));
					}else if (btnSslPayload.isChecked()) {
						mTextView.setText(getString(R.string.sslpayload));
					}
				}
				break;

			case R.id.saveButton:
				doSave();	
				break;
		}
	}

	private void doSave() {
		SharedPreferences.Editor edit = mConfig.getPrefsPrivate().edit();

		if (btnDirect.isChecked()) {
			edit.putInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_DIRECT);	

		} else if (btnHTTP.isChecked()) {
			edit.putInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_PROXY);	

		} else if (btnSSL.isChecked()) {
			edit.putInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_SSLTUNNEL);	

	    } else if (btnSslPayload.isChecked()) {
			edit.putInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_PAY_SSL);	

	    } else if (btnSlowDNS.isChecked()) {
			edit.putInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SLOWDNS);	

	    } else if (btnSslRp.isChecked())
		{
			edit.putInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSL_RP);
		}

		if (customPayload.isChecked()) {
			edit.putBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, false);

		} else {
			edit.putBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, true);
		}
		edit.apply();
		startActivity(new Intent(this, a.class));
		a.updateMainViews(getApplicationContext());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_mode);
		mConfig = new Setting(this);
		prefs = mConfig.getPrefsPrivate();
		toolbar_main = (Toolbar) findViewById(R.id.toolbar_main);
		setSupportActionBar(toolbar_main);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setupButton();
	}

	private void setupButton() {
		mTextView = (TextView) findViewById(R.id.tunneltypeTextView1);
		btnDirect = (RadioButton) findViewById(R.id.btnDirect);
		btnDirect.setOnClickListener(this);
		btnHTTP = (RadioButton) findViewById(R.id.btnHTTP);
		btnHTTP.setOnClickListener(this);
		btnSslPayload = (RadioButton) findViewById(R.id.btnSslPayload);
		btnSslPayload.setOnClickListener(this);
		btnSSL = (RadioButton) findViewById(R.id.btnSSL);
		btnSSL.setOnClickListener(this);
		btnSlowDNS = (RadioButton) findViewById(R.id.btnSlowDNS);
		btnSlowDNS.setOnClickListener(this);
		btnSslRp = (RadioButton)findViewById(R.id.btnSslrp);
		btnSslRp.setOnClickListener(this);

		customPayload = (CheckBox) findViewById(R.id.customPayload);
		customPayload.setOnClickListener(this);

		save = (Button) findViewById(R.id.saveButton);
		save.setOnClickListener(this);

		int tunnelType = prefs.getInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_DIRECT);

	    customPayload.setChecked(!prefs.getBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, true));

		switch (tunnelType) {
			case Setting.bTUNNEL_TYPE_SSH_DIRECT:
				btnDirect.setChecked(true);
				btnHTTP.setChecked(false);
				btnSSL.setChecked(false);
				btnSlowDNS.setChecked(false);
				customPayload.setEnabled(true);
				btnSslPayload.setChecked(false);
				btnSslRp.setChecked(false);
				if (!prefs.getBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, true)) {
					mTextView.setText(getString(R.string.direct) + getString(R.string.custom_payload1));
				} else {
					mTextView.setText(getString(R.string.direct));			
				}
				break;

			case Setting.bTUNNEL_TYPE_SSH_PROXY:
				btnHTTP.setChecked(true);
				btnDirect.setChecked(false);
				btnSSL.setChecked(false);
				btnSlowDNS.setChecked(false);
				btnSslPayload.setChecked(false);
				customPayload.setEnabled(true);
				btnSslRp.setChecked(false);
				if (!prefs.getBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, true)) {
					mTextView.setText(getString(R.string.http) + getString(R.string.custom_payload1));
				} else {
					mTextView.setText(getString(R.string.http));			
				}
				break;

			case Setting.bTUNNEL_TYPE_SSH_SSLTUNNEL:
				btnSSL.setChecked(true);
				btnHTTP.setChecked(false);
				btnDirect.setChecked(false);
				btnSlowDNS.setChecked(false);
				btnSslPayload.setChecked(false);
				customPayload.setEnabled(false);
				customPayload.setChecked(false);
				btnSslRp.setChecked(false);
				mTextView.setText(getString(R.string.ssl));			
				break;

			case Setting.bTUNNEL_TYPE_SLOWDNS:
				btnSlowDNS.setChecked(true);
				btnHTTP.setChecked(false);
				btnSSL.setChecked(false);
				btnDirect.setChecked(false);
				btnSslPayload.setChecked(false);
				customPayload.setEnabled(false);
				customPayload.setChecked(false);
				btnSslRp.setChecked(false);
				mTextView.setText(getString(R.string.slowdns));		
				break;

			case Setting.bTUNNEL_TYPE_PAY_SSL:
				btnSlowDNS.setChecked(false);
				btnHTTP.setChecked(false);
				btnSSL.setChecked(false);
				btnDirect.setChecked(false);
				btnSslPayload.setChecked(true);
				customPayload.setEnabled(true);
				btnSslRp.setChecked(false);
				mTextView.setText(getString(R.string.sslpayload));
				if (!prefs.getBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, true)) {
					mTextView.setText(getString(R.string.sslpayload) + getString(R.string.custom_payload1));
				} else {
					mTextView.setText(getString(R.string.sslpayload));			
				}
				break;

			case Setting.bTUNNEL_TYPE_SSL_RP:
				btnSlowDNS.setChecked(false);
				btnHTTP.setChecked(false);
				btnSSL.setChecked(false);
				btnDirect.setChecked(false);
				btnSslPayload.setChecked(false);
				customPayload.setEnabled(true);
				btnSslRp.setChecked(true);
				mTextView.setText(getString(R.string.sslrp));
				if (!prefs.getBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, true)) {
					mTextView.setText(getString(R.string.sslrp) + getString(R.string.custom_payload1));
				} else {
					mTextView.setText(getString(R.string.sslrp));			
				}
				break;
		}
	}


}
