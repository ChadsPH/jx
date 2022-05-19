package com.chadx.injector.fragments;

import android.app.Dialog;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.DialogInterface;
import com.chadx.sockshttp.R;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.services.logger.SkStatus;
import com.chadx.injector.a;
import com.services.config.Setting;
import com.chadx.injector.preference.SettingsSSHPreference;

public class ClearConfigDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog dialog = new AlertDialog.Builder(getActivity()).
			create();
		dialog.setTitle(getActivity().getString(R.string.attention));
		dialog.setMessage(getActivity().getString(R.string.alert_clear_settings));

		dialog.setButton(DialogInterface.BUTTON_POSITIVE, getActivity().getString(R.
			string.yes),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Setting.clearSettings(getContext());
					
					// limpa logs
					SkStatus.clearLog();
					
					a.updateMainViews(getContext());
					
					Toast.makeText(getActivity(), R.string.success_clear_settings, Toast.LENGTH_SHORT)
						.show();
				}
			}
		);

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getActivity().getString(R.
			string.no),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismiss();
				}
			}
		);
		
		return dialog;
	}

}
