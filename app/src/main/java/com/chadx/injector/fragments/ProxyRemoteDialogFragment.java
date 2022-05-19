package com.chadx.injector.fragments;

import androidx.fragment.app.DialogFragment;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import com.chadx.sockshttp.R;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import com.chadx.injector.a;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.ViewGroup;
import android.widget.Button;
import com.services.config.Setting;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.content.SharedPreferences;

public class ProxyRemoteDialogFragment extends DialogFragment
	implements View.OnClickListener {

	private Setting mConfig;
	
	private boolean usarProxyAutenticacao;
	private boolean usarPayloadPadrao;
	
	private TextInputEditText proxyRemotoIpEdit;
	private TextInputEditText proxyRemotoPortaEdit;

	private AppCompatCheckBox proxyAutenticacaoCheck;
	private LinearLayout autenticacaoLayout;
	private TextInputEditText autenticacaoUsuario;
	private TextInputEditText autenticacaoSenha;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		
		mConfig = new Setting(getContext());
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		
		usarPayloadPadrao = prefs.getBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, true);
		usarProxyAutenticacao = prefs.getBoolean(Setting.PROXY_USAR_AUTENTICACAO_KEY, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		getDialog().setCanceledOnTouchOutside(false);
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		LayoutInflater li = LayoutInflater.from(getContext());
		View view = li.inflate(R.layout.fragment_proxy_remote, null); 

		proxyRemotoIpEdit = view.findViewById(R.id.fragment_proxy_remoteProxyIpEdit);
		proxyRemotoPortaEdit = view.findViewById(R.id.fragment_proxy_remoteProxyPortaEdit);
		
		proxyAutenticacaoCheck = view.findViewById(R.id.fragment_proxy_remoteUsarAutenticacaoCheck);
		autenticacaoLayout = view.findViewById(R.id.fragment_proxy_remoteAutenticacaoLinearLayout);
		autenticacaoUsuario = view.findViewById(R.id.fragment_proxy_remoteAutenticacaoUsuarioEdit);
		autenticacaoSenha = view.findViewById(R.id.fragment_proxy_remoteAutenticacaoSenhaEdit);
		
		Button cancelButton = view.findViewById(R.id.fragment_proxy_remoteCancelButton);
		Button saveButton = view.findViewById(R.id.fragment_proxy_remoteSaveButton);
		
		cancelButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		
		if (!usarPayloadPadrao) {
			proxyAutenticacaoCheck.setEnabled(false);
			autenticacaoLayout.setVisibility(View.GONE);
		}
		else {
			proxyAutenticacaoCheck.setEnabled(true);
		}
		
		proxyRemotoIpEdit.setText(mConfig.getPrivString(Setting.PROXY_IP_KEY));
		
		if (!mConfig.getPrivString(Setting.PROXY_PORTA_KEY).isEmpty())
			proxyRemotoPortaEdit.setText(mConfig.getPrivString(Setting.PROXY_PORTA_KEY));
		
		proxyAutenticacaoCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton p1, boolean x) {
				usarProxyAutenticacao = x;
				setProxyAutenticacaoView(x);
			}
		});
		
		if (!usarProxyAutenticacao) {
			setProxyAutenticacaoView(usarProxyAutenticacao);
		}
		else {
			proxyAutenticacaoCheck.setChecked(true);
		}
		
		//setCancelable(false);
		
		return new AlertDialog.Builder(getActivity())
			//. setTitle("Proxy Setting")
            .setView(view)
			. show();
	}
	
	private void setProxyAutenticacaoView(boolean usarAutenticacao) {
		if (!usarAutenticacao) {
			autenticacaoLayout.setVisibility(View.GONE);
		}
		else {
			autenticacaoLayout.setVisibility(View.VISIBLE);
		}
	}

	
	/**
	 * onClick implementação
	*/

	@Override
	public void onClick(View view)
	{
		switch (view.getId()) {
			case R.id.fragment_proxy_remoteSaveButton:
				String proxy_ip = proxyRemotoIpEdit.getEditableText().toString();
				String proxy_porta = proxyRemotoPortaEdit.getEditableText().toString();
				
				if (proxy_porta == null || proxy_porta.isEmpty() || proxy_porta.equals("0") ||
					proxy_ip == null || proxy_ip.isEmpty()) {
					Toast.makeText(getContext(), "Proxy inválido", Toast.LENGTH_SHORT)
						.show();
				}
				
				else {
					SharedPreferences.Editor edit = mConfig.getPrefsPrivate().edit();
					
					edit.putBoolean(Setting.PROXY_USAR_AUTENTICACAO_KEY, usarProxyAutenticacao);

					edit.putString(Setting.PROXY_IP_KEY, proxy_ip);
					edit.putString(Setting.PROXY_PORTA_KEY, proxy_porta);

					edit.apply();
					
					a.updateMainViews(getContext());

					dismiss();
				}
			break;

			case R.id.fragment_proxy_remoteCancelButton:
				dismiss();
			break;
		}
	}

}
