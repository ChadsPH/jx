package com.chadx.injector.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.chadx.sockshttp.R;
import com.services.MainService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

public class Utils
{
	private static Utils mInstance;
	protected volatile String APP_BASE = "com.chadx.sockshttp";
	private Context mContext;
	
	@SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static void copyToClipboard(Context context, String text) throws Exception {
		int sdk = android.os.Build.VERSION.SDK_INT;
            
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context
					.getSystemService(context.CLIPBOARD_SERVICE);
                clipboard.setText(text);
		}
		else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
					.getSystemService(context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData
					.newPlainText(
					"Message", text);
                clipboard.setPrimaryClip(clip);
         }
	}
	
	public static PackageInfo getAppInfo(Context context){
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void overrideFont(Context context, String defaultFontNameToOverride, String customFontFileNameInAssets) {
        try {
            final Typeface customFontTypeface = Typeface.createFromAsset(context.getAssets(), customFontFileNameInAssets);

            final Field defaultFontTypefaceField = Typeface.class.getDeclaredField(defaultFontNameToOverride);
            defaultFontTypefaceField.setAccessible(true);
            defaultFontTypefaceField.set(null, customFontTypeface);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(),0).show();
        }
    }
	
	
	public static String readFromAssets(Context context, String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

        // do reading, usually loop until end of file reading
        StringBuilder sb = new StringBuilder();
        String mLine = reader.readLine();
        while (mLine != null) {
            sb.append(mLine + '\n'); // process line
            mLine = reader.readLine();
        }
        reader.close();
        return sb.toString();
    }
	
	public static void hideKeyboard(Activity activity) {
		InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		
        if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (activity.getCurrentFocus() != null)
                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
					0);
        }
    }
	
	public static void init(Context context) {
		if (mInstance == null) {
			mInstance = new Utils(context);
		}
	}

	public Utils(Context n) {
		mContext = n;
	}

	
	public static void xs() {
		if (mInstance == null) return;
		mInstance.appProtect();
	}

	public void appProtect() {
		if (!APP_BASE.equals(mContext.getPackageName().toLowerCase()) ||
			!mContext.getString(R.string.app_name).toLowerCase().equals("http ajax")) {
			throw new RuntimeException();
		}
	}
	
	public static void exitAll(Activity activity) {
		Intent stopTunnel = new Intent(MainService.TUNNEL_SSH_STOP_SERVICE);
		LocalBroadcastManager.getInstance(activity)
			.sendBroadcast(stopTunnel);

		if (Build.VERSION.SDK_INT >= 16) {
			activity.finishAffinity();
		}

		System.exit(0);
	}
}
