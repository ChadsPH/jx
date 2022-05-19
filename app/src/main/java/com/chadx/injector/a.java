package com.chadx.injector;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.chadx.injector.activities.BaseActivity;
import com.chadx.injector.activities.ConfigGeralActivity;
import com.chadx.injector.activities.ConfigImportFileActivity;
import com.chadx.injector.adapter.LogsAdapter;
import com.chadx.injector.fragments.ClearConfigDialogFragment;
import com.chadx.injector.fragments.CustomSniFragments;
import com.chadx.injector.fragments.ProxyRemoteDialogFragment;
import com.chadx.injector.util.Utils;
import com.chadx.injector.view.PayloadGenerator;
import com.chadx.sockshttp.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.services.LaunchVpn;
import com.services.config.ConfigParser;
import com.services.config.Setting;
import com.services.logger.ConnectionStatus;
import com.services.logger.SkStatus;
import com.services.tunnel.TunnelManagerHelper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import com.google.android.material.navigation.NavigationView;
import com.chadx.injector.activities.ConfigExportFileActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.chadx.injector.preference.SettingsSSHPreference;


import android.util.Log;

import android.widget.LinearLayout;
import android.content.ComponentName;
import android.view.WindowManager.LayoutParams;
import com.google.android.gms.ads.AdSize;
import android.widget.RelativeLayout;
import android.provider.Settings;
import android.content.ClipboardManager;
import android.content.ClipData;
import com.chadx.injector.util.VPNUtils;
import androidx.cardview.widget.CardView;
import com.chadx.injector.adapter.TunnelActivity;
import android.net.VpnService;
import android.content.ActivityNotFoundException;
import com.services.InjectorService;



public class a extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener, SkStatus.StateListener {

	private NavigationView navi;

	private DrawerLayout drawer;

	@Override
	public boolean onNavigationItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.miCheckIP:
				Intent intent2 = new Intent(this, ConfigGeralActivity.class);
				intent2.setAction(ConfigGeralActivity.OPEN_SETTINGS_SSH);
				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(intent2);
				break;

			case R.id.miPaygen:
				SharedPreferences jprefs = mConfig.getPrefsPrivate();
                if (SkStatus.isTunnelActive()) {
                    Toast.makeText(this, R.string.error_tunnel_service_execution,
                                   Toast.LENGTH_SHORT).show();
                } else if (jprefs.getBoolean(Setting.CONFIG_PROTEGER_KEY, false)) {
                    Toast.makeText(this, R.string.payload_locked_msg,
                                   Toast.LENGTH_SHORT).show();
                } else {
					generator();
                }          
				break;

			case R.id.exit:
				//	startActivity(new Intent(getBaseContext(),hRecycler.class));
				String str3 = "android.intent.action.MAIN";
				if(Build.VERSION.SDK_INT<30)
				{
					Intent in = new Intent(str3);
					in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					in.setClassName("com.android.settings", "com.android.settings.RadioInfo");
					startActivity(in);
				} else {
					Intent in2 = new Intent(str3);
					in2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					in2.setClassName("com.android.phone", "com.android.phone.settings.RadioInfo");
					startActivity(in2);
				}
				break;

			case R.id.miSettings:
				Intent intent = new Intent(a.this, ConfigGeralActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				a.this.startActivity(intent);

				break;

			case R.id.jabol2:
				AlertDialog.Builder mBuilder = new AlertDialog.Builder(a.this);
				mBuilder.setTitle("Hardware ID");
				mBuilder.setMessage(VPNUtils.getHWID());
				mBuilder.setCancelable(false);
				mBuilder.setPositiveButton("COPY", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface mDialogInterface, int mInt)
						{
							((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("HWID", VPNUtils.getHWID()));
						}
					});
				mBuilder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface mDialog, int mInt)
						{
							mDialog.cancel();
						}
					});
				mBuilder.show();
				break;
		}

		return false;
	}
	private static final int START_VPN_PROFILE = 70;
	private AdmobHelper mAdsHelper;
    private static final String[] tabTitle = {"HOME","LOGS","HELP"};
	private static final String TAG = a.class.getSimpleName();
	private static final String UPDATE_VIEWS = "MainUpdate";
	public static final String OPEN_LOGS = "com.slipkprojects.sockshttp:openLogs";
	private DrawerPanelMain mDrawerPanel;
	private PayloadGenerator paygen;
	private Setting mConfig;
	private Toolbar toolbar_main;
	private Handler mHandler;
	private LinearLayout cmodsniLayout;
	private CardView ViewGone;
	private CardView ViewGone1;
	private TextView cmodsniText;
	private LinearLayout mainLayout;
	private LinearLayout proxyInputLayout;
	private TextView proxyText;
	private RadioGroup metodoConexaoRadio;
	private androidx.cardview.widget.CardView payloadLayout;
	private TextInputEditText payloadEdit;
	private CheckBox customPayloadSwitch;
	private Button starterButton;
	private androidx.cardview.widget.CardView configMsgLayout;
	private TextView configMsgText;
	private LinearLayout payload_menu;
	private TextView activitymainTextView1;
	private LogsAdapter mLogAdapter;
    private RecyclerView logList;
	private ViewPager vp;

	private String[] torrentList = new String[] {
		"com.termux",
		"com.tdo.showbox",
		"com.nitroxenon.terrarium",
		"com.pklbox.translatorspro",
		"com.xunlei.downloadprovider",
		"com.epic.app.iTorrent",
		"hu.bute.daai.amorg.drtorrent",
		"com.mobilityflow.torrent.prof",
		"com.brute.torrentolite",
		"com.nebula.swift",
		"tv.bitx.media",
		"com.DroiDownloader",
		"bitking.torrent.downloader",
		"org.transdroid.lite",
		"com.mobilityflow.tvp",
		"com.gabordemko.torrnado",
		"com.frostwire.android",
		"com.vuze.android.remote",
		"com.akingi.torrent",
		"com.utorrent.web",
		"com.paolod.torrentsearch2",
		"com.delphicoder.flud.paid",
		"com.teeonsoft.ztorrent",
		"megabyte.tdm",
		"com.bittorrent.client.pro",
		"com.mobilityflow.torrent",
		"com.utorrent.client",
		"com.utorrent.client.pro",
		"com.bittorrent.client",
		"torrent",
		"com.AndroidA.DroiDownloader",
		"com.indris.yifytorrents",
		"com.delphicoder.flud",
		"com.oidapps.bittorrent",
		"dwleee.torrentsearch",
		"com.vuze.torrent.downloader",
		"megabyte.dm",
		"com.fgrouptech.kickasstorrents",
		"com.jrummyapps.rootbrowser.classic",
		"com.bittorrent.client",
		"hu.tagsoft.ttorrent.lite",
		"co.we.torrent"};

	private CheckBox startSSHCheckbox;
	private CheckBox dnsCheckBox;

	private TabLayout tabs;



	@Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		mHandler = new Handler();
		mConfig = new Setting(this);
		mDrawerPanel = new DrawerPanelMain(this);
		paygen = new PayloadGenerator(this);
		mAdsHelper = new AdmobHelper(this);
		SharedPreferences prefs = getSharedPreferences(MainApp.PREFS_GERAL, Context.MODE_PRIVATE);
		//getWindow().setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		boolean showFirstTime = prefs.getBoolean("connect_first_time", true);
		int lastVersion = prefs.getInt("last_version", 0);
		new TorrentDetection(this, torrentList).init();
		// se primeira vez
		if (showFirstTime)
        {
            SharedPreferences.Editor pEdit = prefs.edit();
            pEdit.putBoolean("connect_first_time", false);
            pEdit.apply();

			Setting.setDefaultConfig(this);

			showBoasVindas();
        }


		try {
			int idAtual = ConfigParser.getBuildId(this);

			if (lastVersion < idAtual) {
				SharedPreferences.Editor pEdit = prefs.edit();
				pEdit.putInt("last_version", idAtual);
				pEdit.apply();

				// se estiver atualizando
				if (!showFirstTime) {
					if (lastVersion <= 12) {
						Setting.setDefaultConfig(this);
						Setting.clearSettings(this);

						Toast.makeText(this, "Settings have been cleaned to avoid bugs",
									   Toast.LENGTH_LONG).show();
					}
				}

			}
		} catch(IOException e) {}


		// set layout
		doLayout();
		doTabs();
		// verifica se existe algum problema


		// recebe local dados
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_VIEWS);
		filter.addAction(OPEN_LOGS);

		LocalBroadcastManager.getInstance(this).registerReceiver(mActivityReceiver, filter);

		doUpdateLayout();
		//MobileAds.initialize(this, "ca-app-pub-4327217352829821~9017714353");
	}

	public void tunnelz(View v)
	{
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		boolean isRunning = SkStatus.isTunnelActive();
		if (!isRunning) {
			if (!prefs.getBoolean(Setting.CONFIG_PROTEGER_KEY, false)) {
				startActivity(new Intent(this, TunnelActivity.class));
			}
		}
	}

	public void hype(View v)
	{
		AlertDialog.Builder builder1 = new AlertDialog.Builder(a.this);

		builder1.setTitle(Html.fromHtml("<b>Message me if bug<b>"));
		builder1.setMessage(Html.fromHtml("Facebook Account: <br><a href=\"https://www.facebook.com/adik016\">Developers Facebook Account!</a><br><br>App Website: <br><a href=\"https://ajax-vpn.radzph.com/\">AjaxInjector website</a>"));
		builder1.setCancelable(false);
		builder1.setPositiveButton("ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
            });
		AlertDialog Alert1 = builder1.create();
		Alert1 .show();
		((TextView)Alert1.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

	}
	

	public void doTabs() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mLogAdapter = new LogsAdapter(layoutManager,this);
        logList = (RecyclerView) findViewById(R.id.recyclerLog);
        logList.setAdapter(mLogAdapter);
        logList.setLayoutManager(layoutManager);
        mLogAdapter.scrollToLastPosition();
        vp = (ViewPager)findViewById(R.id.viewpager);
        tabs = (TabLayout)findViewById(R.id.tablayout);
        vp.setAdapter(new MyAdapter(Arrays.asList(tabTitle)));
        vp.setOffscreenPageLimit(3);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);

		tabs.setupWithViewPager(vp);
        vp.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
            {
                @Override
                public void onPageSelected(int position)
                {
                    if (position == 0) {
                        toolbar_main.getMenu().clear();
                        getMenuInflater().inflate(R.menu.main_menu, toolbar_main.getMenu());
                    } else if (position == 1) {                                        
                        toolbar_main.getMenu().clear();
                        getMenuInflater().inflate(R.menu.logs_menu, toolbar_main.getMenu());
                    } else if (position == 2) {
		      			toolbar_main.getMenu().clear();
						//               getMenuInflater().inflate(R.menu.main_menu, toolbar_main.getMenu());
					}


                }
            });


	}

	public class MyAdapter extends PagerAdapter
    {

        @Override
        public int getCount()
        {
            // TODO: Implement this method
            return 3;
        }

        @Override
        public boolean isViewFromObject(View p1, Object p2)
        {
            // TODO: Implement this method
            return p1 == p2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            int[] ids = new int[]{R.id.tab1, R.id.tab2, R.id.tab3 };
            int id = 0;
            id = ids[position];
            // TODO: Implement this method
            return findViewById(id);
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            // TODO: Implement this method
            return titles.get(position);
        }

        private List<String> titles;
        public MyAdapter(List<String> str)
        {
            titles = str;
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

	/**
	 * Layout
	 */

	private void doLayout() {
		setContentView(R.layout.activity_main_drawer);
		final SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
		drawer = (DrawerLayout)findViewById(R.id.drawer);
		navi = (NavigationView)findViewById(R.id.navigation);
		mAdsHelper.setMobileAdsId("ca-app-pub-5486948590530041~3981027283");
        mAdsHelper.setBannerId("ca-app-pub-5486948590530041/7260166437");
        mAdsHelper.setBannerSize(AdSize.BANNER);
        mAdsHelper.setBannerView((RelativeLayout)findViewById(R.id.adView));
        mAdsHelper.setIntertitialId("ca-app-pub-5486948590530041/7554091155");
		//  mAdsHelper.setShowInterAdsAuto(true);
        mAdsHelper.buildAdsRequest();
		mAdsHelper.loadBannerAdsRequest();
		toolbar_main = (Toolbar) findViewById(R.id.toolbar_main);
		setSupportActionBar(toolbar_main);

		navi.setNavigationItemSelectedListener(this);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,toolbar_main,R.string.app_name,R.string.app_name);
		toggle.syncState();
		drawer.setDrawerListener(toggle);
		int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (permissionCheck != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
		}
		startSSHCheckbox = (CheckBox) findViewById(R.id.startSSHCheckbox);
		startSSHCheckbox.setText("Start SSH");
		startSSHCheckbox.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12);
		startSSHCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mPref.edit().putBoolean("startSSHCheck",isChecked).apply();
				}
			});
		dnsCheckBox=(CheckBox) findViewById(R.id.useDns);
		dnsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mPref.edit().putBoolean("startDns",isChecked).apply();
				}
			});
		dnsCheckBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12);

		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		mLogAdapter = new LogsAdapter(layoutManager,this);
		mLogAdapter.scrollToLastPosition();

		mainLayout = (LinearLayout) findViewById(R.id.activity_mainLinearLayout);

		starterButton = (Button) findViewById(R.id.activity_starterButtonMain);
	
		payload_menu = (LinearLayout)findViewById(R.id.payload_menu);
		proxyInputLayout = (LinearLayout) findViewById(R.id.activity_mainProxyLayout);
		proxyText = (TextView) findViewById(R.id.activity_mainProxyText);
		activitymainTextView1 = (TextView)findViewById(R.id.activitymainTextView1);
		ViewGone = (CardView) findViewById(R.id.ViewGone);
		ViewGone1 = (CardView) findViewById(R.id.ViewGone1);
		cmodsniLayout = (LinearLayout) findViewById(R.id.cmods_snilayout);
		cmodsniText = (TextView) findViewById(R.id.cmods_snitext);
		cmodsniText.setText(mConfig.getPrivString(Setting.CUSTOM_SNI));
		cmodsniLayout.setOnClickListener(new OnClickListener(){
				SharedPreferences prefs = mConfig.getPrefsPrivate();
				@Override
				public void onClick(View p1) {
					if (!prefs.getBoolean(Setting.CONFIG_PROTEGER_KEY, false)) {
						doSaveData();

		      		DialogFragment fragSni = new CustomSniFragments();
					fragSni.show(getSupportFragmentManager(), "customSni");
					}
				}
			});

		metodoConexaoRadio = (RadioGroup) findViewById(R.id.activity_mainMetodoConexaoRadio);
		customPayloadSwitch = (CheckBox) findViewById(R.id.activity_mainCustomPayloadSwitch);

		starterButton.setOnClickListener(this);
		proxyInputLayout.setOnClickListener(this);
		dnsCheckBox.setOnClickListener(this);

		payloadLayout = (androidx.cardview.widget.CardView) findViewById(R.id.activity_mainInputPayloadLinearLayout);
		payloadEdit = (TextInputEditText) findViewById(R.id.etPayload);

		configMsgLayout = (androidx.cardview.widget.CardView) findViewById(R.id.activity_mainMensagemConfigLinearLayout);
		configMsgText = (TextView) findViewById(R.id.activity_mainMensagemConfigTextView);

		// fix bugs
		if (mConfig.getPrefsPrivate().getBoolean(Setting.CONFIG_PROTEGER_KEY, false)) {

		}
		else {
			payloadEdit.setText(mConfig.getPrivString(Setting.CUSTOM_PAYLOAD_KEY));
		}

		metodoConexaoRadio.setOnCheckedChangeListener(this);
		customPayloadSwitch.setOnCheckedChangeListener(this);

	}
	private void generator(){

		paygen.setDialogTitle(getString(R.string.pay_gen));
		paygen.setCancelListener(getString(R.string.cancel), new PayloadGenerator.OnCancelListener(){

                @Override
                public void onCancelListener() {
                }
            });
        paygen.setGenerateListener(getString(R.string.gen), new PayloadGenerator.OnGenerateListener(){

                @Override
                public void onGenerate(String payloadGenerated) {              
					SharedPreferences prefs = mConfig.getPrefsPrivate();
					if (!prefs.getBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, true)){
						payloadEdit.setText(payloadGenerated);     
					} else {
						Toast.makeText(a.this, R.string.custom_payload_msg, Toast.LENGTH_SHORT).show();
					}                                              
				}
            });
        paygen.show();
	}



	private void doUpdateLayout() {
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		final SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isRunning = SkStatus.isTunnelActive();
		int tunnelType = prefs.getInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_DIRECT);

		setStarterButton(starterButton, this);
		setPayloadSwitch(tunnelType, !prefs.getBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, true));

		startSSHCheckbox.setEnabled(!isRunning);
		startSSHCheckbox.setChecked(mPref.getBoolean("startSSHCheck",true));
		dnsCheckBox.setChecked(mPref.getBoolean("startDns",true));
		dnsCheckBox.setEnabled(!isRunning);

		String proxyStr = getText(R.string.def_value).toString();

		if (prefs.getBoolean(Setting.CONFIG_PROTEGER_KEY, false)) {
			proxyStr = "*******";
			proxyInputLayout.setEnabled(false);
			customPayloadSwitch.setEnabled(false);
			payloadLayout.setVisibility(View.VISIBLE);
			payloadLayout.setEnabled(false);
			payloadEdit.setText("Locked");
		}
		else {
			String proxy = mConfig.getPrivString(Setting.PROXY_IP_KEY);

			if (proxy != null && !proxy.isEmpty())
				proxyStr = String.format("%s:%s", proxy, mConfig.getPrivString(Setting.PROXY_PORTA_KEY));
			proxyInputLayout.setEnabled(!isRunning);
		} 

		proxyText.setText(proxyStr);
		String sniStr = getText(R.string.ssl_value).toString();


		if (prefs.getBoolean(Setting.CONFIG_PROTEGER_KEY, false)) {
			sniStr = "*******";
			cmodsniLayout.setEnabled(false);
			payloadLayout.setVisibility(View.VISIBLE);
			payloadLayout.setEnabled(false);
			customPayloadSwitch.setEnabled(false);
			payloadEdit.setText("Locked");
		}
		else {
			String sni = mConfig.getPrivString(Setting.CUSTOM_SNI);

			if (sni != null && !sni.isEmpty())
				sniStr = String.format(mConfig.getPrivString(Setting.CUSTOM_SNI));
			cmodsniLayout.setEnabled(!isRunning);
		} 

		cmodsniText.setText(sniStr);

		SharedPreferences.Editor edit = mConfig.getPrefsPrivate().edit();

		switch (tunnelType) {
			case Setting.bTUNNEL_TYPE_SSH_PROXY:
				((AppCompatRadioButton) findViewById(R.id.activity_mainSSHProxyRadioButton))
					.setChecked(true);
				activitymainTextView1.setText(getString(R.string.http));
				proxyInputLayout.setVisibility(View.VISIBLE);
				payloadLayout.setVisibility(View.VISIBLE);
				ViewGone.setVisibility(View.GONE);
				ViewGone1.setVisibility(View.VISIBLE);
				cmodsniLayout.setVisibility(View.GONE);
				customPayloadSwitch.setClickable(true);
				payload_menu.setVisibility(View.VISIBLE);
				customPayloadSwitch.setVisibility(View.VISIBLE);
				break;

			case Setting.bTUNNEL_TYPE_SSH_DIRECT:
				((AppCompatRadioButton) findViewById(R.id.activity_mainSSHDirectRadioButton))
					.setChecked(true);
				activitymainTextView1.setText(getString(R.string.direct));
				edit.putInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_DIRECT);
				proxyInputLayout.setVisibility(View.GONE);
				payloadLayout.setVisibility(View.GONE);
				ViewGone.setVisibility(View.GONE);
				ViewGone1.setVisibility(View.GONE);
				cmodsniLayout.setVisibility(View.GONE);
				customPayloadSwitch.setClickable(true);
				payload_menu.setVisibility(View.VISIBLE);
				customPayloadSwitch.setVisibility(View.VISIBLE);
				break;

			case Setting.bTUNNEL_TYPE_SSH_SSLTUNNEL:
				((AppCompatRadioButton) findViewById(R.id.activity_mainSSLProxyRadioButton))
					.setChecked(true);
				activitymainTextView1.setText(getString(R.string.ssl));
				customPayloadSwitch.setClickable(false);
				customPayloadSwitch.setChecked(false);
				ViewGone1.setVisibility(View.GONE);
				proxyInputLayout.setVisibility(View.GONE);
				payloadLayout.setVisibility(View.VISIBLE);
				ViewGone.setVisibility(View.VISIBLE);
				payload_menu.setVisibility(View.GONE);
				cmodsniLayout.setVisibility(View.VISIBLE);
				customPayloadSwitch.setVisibility(View.GONE);
				break;
			case Setting.bTUNNEL_TYPE_PAY_SSL:
				((AppCompatRadioButton) findViewById(R.id.activity_mainSSLPayloadRadioButton))
					.setChecked(true);
				activitymainTextView1.setText(getString(R.string.sslpayload));
				proxyInputLayout.setVisibility(View.GONE);
				payloadLayout.setVisibility(View.VISIBLE);
				ViewGone.setVisibility(View.VISIBLE);
				cmodsniLayout.setVisibility(View.VISIBLE);
				ViewGone1.setVisibility(View.GONE);
				payload_menu.setVisibility(View.VISIBLE);
				customPayloadSwitch.setClickable(true);
				customPayloadSwitch.setVisibility(View.VISIBLE);
				break;

			case Setting.bTUNNEL_TYPE_SLOWDNS:
				edit.putInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SLOWDNS).apply();
				final SharedPreferences slowprefs = mConfig.getPrefsPrivate();
				final String chave = slowprefs.getString(Setting.CHAVE_KEY, "chaveKey");
				final String nameserver = slowprefs.getString(Setting.NAMESERVER_KEY, "serverNameKey");
				final String dns = slowprefs.getString(Setting.DNS_KEY, "dnsKey");
				slowprefs.getString(Setting.CHAVE_KEY, chave);
				slowprefs.getString(Setting.NAMESERVER_KEY, nameserver);
				slowprefs.getString(Setting.DNS_KEY, dns);
				activitymainTextView1.setText(getString(R.string.slowdns));
			//	view_all_layout.setEnabled(false);
				proxyInputLayout.setVisibility(View.GONE);
				payloadLayout.setVisibility(View.GONE);
				ViewGone.setVisibility(View.GONE);
				ViewGone1.setVisibility(View.GONE);
				cmodsniLayout.setVisibility(View.GONE);
				payload_menu.setVisibility(View.GONE);
				customPayloadSwitch.setClickable(false);
				customPayloadSwitch.setVisibility(View.GONE);
				break;

			case Setting.bTUNNEL_TYPE_SSL_RP:
				edit.putInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSL_RP);
				activitymainTextView1.setText(getString(R.string.sslrp));
				proxyInputLayout.setVisibility(View.VISIBLE);
				payloadLayout.setVisibility(View.VISIBLE);
				ViewGone.setVisibility(View.VISIBLE);
				ViewGone1.setVisibility(View.VISIBLE);
				cmodsniLayout.setVisibility(View.VISIBLE);
				payload_menu.setVisibility(View.VISIBLE);
				customPayloadSwitch.setClickable(true);
				customPayloadSwitch.setVisibility(View.VISIBLE);
				break;
		}

		int msgVisibility = View.GONE;
		int loginVisibility = View.GONE;
		String msgText = "";
		boolean enabled_radio = !isRunning;

		if (prefs.getBoolean(Setting.CONFIG_PROTEGER_KEY, false)) {


			String msg = mConfig.getPrivString(Setting.CONFIG_MENSAGEM_KEY);
			if (!msg.isEmpty()) {
				msgText = msg.replace("\n", "<br/>");
				msgVisibility = View.VISIBLE;
			}

			if (mConfig.getPrivString(Setting.PROXY_IP_KEY).isEmpty() ||
				mConfig.getPrivString(Setting.PROXY_PORTA_KEY).isEmpty()) {
				enabled_radio = false;
			}
		}


		configMsgText.setText(msgText.isEmpty() ? "" : Html.fromHtml(msgText));
		configMsgLayout.setVisibility(msgVisibility);

		// desativa/ativa radio group
		for (int i = 0; i < metodoConexaoRadio.getChildCount(); i++) {
			metodoConexaoRadio.getChildAt(i).setEnabled(enabled_radio);
		}
	}


	private synchronized void doSaveData() {
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		SharedPreferences.Editor edit = prefs.edit();

		if (mainLayout != null && !isFinishing())
			mainLayout.requestFocus();

		if (!prefs.getBoolean(Setting.CONFIG_PROTEGER_KEY, false)) {
			if (payloadEdit != null && !prefs.getBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, true)) {
				edit.putString(Setting.CUSTOM_PAYLOAD_KEY, payloadEdit.getText().toString());
			}

		}
		int tunnelType = prefs.getInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_DIRECT);
		if (tunnelType == Setting.bTUNNEL_TYPE_SLOWDNS) {
			edit.putString(Setting.SERVIDOR_KEY, "127.0.0.1");
			edit.putString(Setting.SERVIDOR_PORTA_KEY, "2222");

		}

		edit.apply();
	}


	/**
	 * Tunnel SSH
	 */

	public void startOrStopTunnel(Activity activity) {
		if (SkStatus.isTunnelActive()) {
			TunnelManagerHelper.stopSocksHttp(activity);

		}
		else {
			// oculta teclado se vísivel, tá com bug, tela verde
			//Utils.hideKeyboard(activity);
			if(startSSHCheckbox.isChecked()){
				this.startService(new Intent(this, InjectorService.class).setAction("START"));
				Toast.makeText(a.this, "Start SSH is running",
							   Toast.LENGTH_SHORT).show();
			}else{
				this.startService(new Intent(this, InjectorService.class).setAction("STOP"));
				Toast.makeText(a.this, "Start SSH is off",
							   Toast.LENGTH_SHORT).show();
			}

			Setting config = new Setting(activity);

			Intent intent = new Intent(activity, LaunchVpn.class);
			intent.setAction(Intent.ACTION_MAIN);

			if (config.getHideLog()) {
				intent.putExtra(LaunchVpn.EXTRA_HIDELOG, true);
			}

			activity.startActivity(intent);
		}
	}


	

	private void setPayloadSwitch(int tunnelType, boolean isCustomPayload) {
		SharedPreferences prefs = mConfig.getPrefsPrivate();

		boolean isRunning = SkStatus.isTunnelActive();

		customPayloadSwitch.setChecked(isCustomPayload);

		if (prefs.getBoolean(Setting.CONFIG_PROTEGER_KEY, false)) {
			payloadEdit.setEnabled(false);

			if (mConfig.getPrivString(Setting.CUSTOM_PAYLOAD_KEY).isEmpty()) {
				customPayloadSwitch.setEnabled(false);
			}
			else {
				customPayloadSwitch.setEnabled(!isRunning);
			}

			if (!isCustomPayload && tunnelType == Setting.bTUNNEL_TYPE_SSH_PROXY)
				payloadEdit.setText(Setting.PAYLOAD_DEFAULT);
			else
				payloadEdit.setText("*******");
		}
		else {
			customPayloadSwitch.setEnabled(!isRunning);

			if (isCustomPayload) {
				payloadEdit.setText(mConfig.getPrivString(Setting.CUSTOM_PAYLOAD_KEY));
				payloadEdit.setEnabled(!isRunning);
			}
			else if (tunnelType == Setting.bTUNNEL_TYPE_SSH_PROXY) {
				payloadEdit.setText(Setting.PAYLOAD_DEFAULT);
				payloadEdit.setEnabled(false);
			}
		}

		if (isCustomPayload || tunnelType == Setting.bTUNNEL_TYPE_SSH_PROXY) {
			payloadLayout.setVisibility(View.VISIBLE);
		}
		else {
			payloadLayout.setVisibility(View.GONE);
		}
	}

	public void setStarterButton(Button starterButton, Activity activity) {
		String state = SkStatus.getLastState();
		boolean isRunning = SkStatus.isTunnelActive();

		if (starterButton != null) {
			int resId;

			SharedPreferences prefsPrivate = new Setting(activity).getPrefsPrivate();

			if (ConfigParser.isValidadeExpirou(prefsPrivate
											   .getLong(Setting.CONFIG_VALIDADE_KEY, 0))) {
				resId = R.string.expired;
				starterButton.setEnabled(false);

				if (isRunning) {
					startOrStopTunnel(activity);
				}

			}
			else if (SkStatus.SSH_INICIANDO.equals(state)) {
				resId = R.string.stop;
				starterButton.setEnabled(false);
				mAdsHelper.loadIntertitial();
				mAdsHelper.loadBannerAdsRequest();
			}
			else if (SkStatus.SSH_PARANDO.equals(state)) {
				resId = R.string.state_stopping;
				starterButton.setEnabled(false);

			}
			else {
				resId = isRunning ? R.string.stop : R.string.start;
				starterButton.setEnabled(true);

			}

			starterButton.setText(resId);
		}
	}



	@Override
	public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
		super.onPostCreate(savedInstanceState, persistentState);
		if (mDrawerPanel.getToogle() != null)
			mDrawerPanel.getToogle().syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDrawerPanel.getToogle() != null)
			mDrawerPanel.getToogle().onConfigurationChanged(newConfig);
	}


	private boolean isMostrarSenha = false;

	@Override
	public void onClick(View p1)
	{
		SharedPreferences prefs = mConfig.getPrefsPrivate();

		switch (p1.getId()) {
			case R.id.activity_starterButtonMain:
				doSaveData();
				startOrStopTunnel(this);

				break;

			case R.id.activity_mainProxyLayout:
				if (!prefs.getBoolean(Setting.CONFIG_PROTEGER_KEY, false)) {
					doSaveData();

					DialogFragment fragProxy = new ProxyRemoteDialogFragment();
					fragProxy.show(getSupportFragmentManager(), "proxyDialog");
				}
				break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup p1, int p2)
	{
		SharedPreferences.Editor edit = mConfig.getPrefsPrivate().edit();

		switch (p1.getCheckedRadioButtonId()) {
			case R.id.activity_mainSSHDirectRadioButton:
				edit.putInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_DIRECT);
				proxyInputLayout.setVisibility(View.GONE);
				payloadLayout.setVisibility(View.VISIBLE);
				ViewGone.setVisibility(View.GONE);
				cmodsniLayout.setVisibility(View.GONE);
				customPayloadSwitch.setClickable(false);
				payload_menu.setVisibility(View.VISIBLE);
				customPayloadSwitch.setVisibility(View.GONE);
				activitymainTextView1.setText("Direct > SSH (Custom Payload)");
				break;

			case R.id.activity_mainSSHProxyRadioButton:
				edit.putInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_PROXY);
				proxyInputLayout.setVisibility(View.VISIBLE);
				payloadLayout.setVisibility(View.VISIBLE);
				ViewGone.setVisibility(View.GONE);
				ViewGone1.setVisibility(View.VISIBLE);
				cmodsniLayout.setVisibility(View.GONE);
				customPayloadSwitch.setClickable(true);
				payload_menu.setVisibility(View.VISIBLE);
				customPayloadSwitch.setVisibility(View.VISIBLE);
				activitymainTextView1.setText("HTTP Proxy > SSH (Custom Payload)");
				break;
			case R.id.activity_mainSSLProxyRadioButton:
				edit.putInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_SSLTUNNEL);
				edit.putBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, true).apply();
				customPayloadSwitch.setClickable(false);
				customPayloadSwitch.setChecked(false);
				proxyInputLayout.setVisibility(View.GONE);
				payloadLayout.setVisibility(View.VISIBLE);
				ViewGone.setVisibility(View.VISIBLE);
				ViewGone1.setVisibility(View.GONE);
				payload_menu.setVisibility(View.GONE);
				cmodsniLayout.setVisibility(View.VISIBLE);
				customPayloadSwitch.setVisibility(View.GONE);
				activitymainTextView1.setText("SSL/TLS > SSH");
				break;
			case R.id.activity_mainSSLPayloadRadioButton:
				edit.putInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_PAY_SSL);
				proxyInputLayout.setVisibility(View.GONE);
				payloadLayout.setVisibility(View.VISIBLE);
				ViewGone.setVisibility(View.VISIBLE);
				ViewGone1.setVisibility(View.VISIBLE);
				cmodsniLayout.setVisibility(View.VISIBLE);
				payload_menu.setVisibility(View.VISIBLE);
				customPayloadSwitch.setClickable(true);
				customPayloadSwitch.setVisibility(View.VISIBLE);
				activitymainTextView1.setText("SSL + HTTP PAYLOAD");
				break;
		}

		edit.apply();

		doSaveData();
		doUpdateLayout();
	}

	@Override
	public void onCheckedChanged(CompoundButton p1, boolean p2)
	{
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		SharedPreferences.Editor edit = prefs.edit();

		switch (p1.getId()) {
			case R.id.activity_mainCustomPayloadSwitch:
				edit.putBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, !p2);
				setPayloadSwitch(prefs.getInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_DIRECT), p2);
				break;
		}

		edit.apply();

		doSaveData();
	}

	protected void showBoasVindas() {
		new AlertDialog.Builder(this)
            . setTitle(R.string.attention)
            . setMessage(R.string.first_start_msg)
			. setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int p) {
					// ok
				}
			})
			. setCancelable(false)
            . show();
	}

	@Override
	public void updateState(final String state, String msg, int localizedResId, final ConnectionStatus level, Intent intent)
	{
		mHandler.post(new Runnable() {
				@Override
				public void run() {
					doUpdateLayout();

				}
			});

		switch (state) {
			case SkStatus.SSH_CONECTADO:
				// carrega ads banner
				//showInterstitial();



		}
	}



	/**
	 * Recebe locais Broadcast
	 */

	private BroadcastReceiver mActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;

            if (action.equals(UPDATE_VIEWS) && !isFinishing()) {
				doUpdateLayout();
			}
		}
    };


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerPanel.getToogle() != null && mDrawerPanel.getToogle().onOptionsItemSelected(item)) {
            return true;
        }
		switch (item.getItemId()) {
			case R.id.miLimparConfig:
				if (!SkStatus.isTunnelActive()) {
					DialogFragment dialog = new ClearConfigDialogFragment();
					dialog.show(getSupportFragmentManager(), "alertClearConf");
				} else {
					Toast.makeText(this, R.string.error_tunnel_service_execution, Toast.LENGTH_SHORT)
						.show();
				}
				break;

			case R.id.miSetting:
				Intent intent = new Intent(a.this, ConfigGeralActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				a.this.startActivity(intent);

				break;

			case R.id.miLimparLogs:
                logClear();
                //Snackbar.make(Html.fromHtml(findViewById(R.id.snackbar), "<strong><font color='#04D000'>Log Clear</font></strong>",-1).show());
                //mNotifyBuilder.setContentText(Html.fromHtml("<font color='#B71C1C'>Download↓</font>" + DataTransferGraph.Count(sdown, true) + " / " + DataTransferGraph.Count(download, false) + " |<font color='#304FFE'>Upload↑</font>" + DataTransferGraph.Count(sup, true) + " / " + DataTransferGraph.Count(upload, false)));

                break;

            case R.id.miCopyLogs:
                int sdk = android.os.Build.VERSION.SDK_INT;
                if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(getLog());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); 
                    android.content.ClipData clip = android.content.ClipData.newPlainText("text label",getLog());
                    clipboard.setPrimaryClip(clip);
                }
                Toast.makeText(a.this, "Log Copied", Toast.LENGTH_SHORT).show();
				//  MDToast ddToast = MDToast.makeText(SocksHttpMainActivity.this,"Logs Copied",MDToast.LENGTH_SHORT,MDToast.TYPE_SUCCESS);ddToast.show();
                //Snackbar.make(findViewById(R.id.snackbar), "Log Copied",-1).show();
				//     SkStatus.logInfo("<font color='black'>Logs Copied!</font>");
                break;

			case R.id.miSettingImportar:
				if (SkStatus.isTunnelActive()) {
					Toast.makeText(this, R.string.error_tunnel_service_execution,
								   Toast.LENGTH_SHORT).show();
				}
				else {
					Intent intentImport = new Intent(this, ConfigImportFileActivity.class);
					startActivity(intentImport);
				}
				break;

			case R.id.miSettingExportar:
				SharedPreferences prefs = mConfig.getPrefsPrivate();

				if (SkStatus.isTunnelActive()) {
					Toast.makeText(this, R.string.error_tunnel_service_execution,
								   Toast.LENGTH_SHORT).show();
				}
				else if (prefs.getBoolean(Setting.CONFIG_PROTEGER_KEY, false)) {
					Toast.makeText(this, R.string.error_settings_blocked,
								   Toast.LENGTH_SHORT).show();
				}
				else {
					Intent intentExport = new Intent(this, ConfigExportFileActivity.class);
					startActivity(intentExport);
				}
				break;

		}

		return super.onOptionsItemSelected(item);
	}

	private String getLog()
    {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < mLogAdapter.getItemCount(); i++) {
			str.append(mLogAdapter.getItem(i) +"\n");
		}
		return str.toString();
	}

	private void logClear(){
		// mLogAdapter.clearLog();
		SkStatus.clearLog();
        //Toast.makeText(SocksHttpMainActivity.this, "Log Clear", Toast.LENGTH_SHORT).show();
		// SkStatus.logInfo("<font color='red'>Log Cleared!</font>");
    }

	@Override
	public void onBackPressed() {


		showExitDialog();

	}

	@Override
    public void onResume() {
        super.onResume();



		//doSaveData();
		//doUpdateLayout();

		SkStatus.addStateListener(this);


    }

	@Override
	protected void onPause()
	{
		super.onPause();

		doSaveData();

		SkStatus.removeStateListener(this);


	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();



		LocalBroadcastManager.getInstance(this)
			.unregisterReceiver(mActivityReceiver);


	}


	/**
	 * DrawerLayout Listener
	 */





	/**
	 * Utils
	 */

	public static void updateMainViews(Context context) {
		Intent updateView = new Intent(UPDATE_VIEWS);
		LocalBroadcastManager.getInstance(context)
			.sendBroadcast(updateView);
	}

	public void showExitDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this). create();
		dialog.setTitle("HTTP A-Jax Exit");
		dialog.setMessage(getString(R.string.alert_exit));
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Utils.exitAll(a.this);
				}
			}
		);

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// minimiza app
					dialog.dismiss();
					/*Intent startMain = new Intent(Intent.ACTION_MAIN);
					 startMain.addCategory(Intent.CATEGORY_HOME);
					 startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					 startActivity(startMain);*/
				}
			}
		);
		dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Restart", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					p1.dismiss();
					restart_app();
				}
			});
		dialog.show();
	}

	private void restart_app() {
		Context context = MainApp.getApp();
		PackageManager packageManager = context.getPackageManager();
		Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
		ComponentName componentName = intent.getComponent();
		Intent mainIntent = Intent.makeRestartActivityTask(componentName);
		context.startActivity(mainIntent);
		Runtime.getRuntime().exit(0);
	}

	/**
	 Unity ads start her jabol ka
	 **/



	/**
	 End of Unity ads here wag mong jabolin
	 **/

}









