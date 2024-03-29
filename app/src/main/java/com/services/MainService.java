package com.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.Context;
import com.services.logger.SkStatus;
import android.os.Binder;
import android.os.Handler;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.List;
import android.os.RemoteException;
import android.annotation.TargetApi;
import android.os.Build;
import android.app.Notification;
import com.services.logger.ConnectionStatus;
import androidx.annotation.NonNull;
import android.app.NotificationManager;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import android.app.PendingIntent;
import android.content.ComponentName;
import java.io.IOException;
import androidx.annotation.RequiresApi;
import com.services.tunnel.TunnelUtils;
import com.services.tunnel.TunnelManagerThread;
import android.content.BroadcastReceiver;
import com.services.config.Setting;
import android.app.NotificationChannel;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import android.app.Activity;
import java.net.InetAddress;
import java.net.UnknownHostException;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.net.LinkAddress;
import android.net.Network;
import android.net.LinkProperties;
import com.services.util.DummyActivity;
import android.graphics.Color;
import com.services.tunnel.DNSTunnelThread;
import android.content.SharedPreferences;
import com.chadx.sockshttp.R;
import java.util.Date;
import com.chadx.injector.a;

public class MainService extends Service implements SkStatus.StateListener {
	private static final String TAG = Service.class.getSimpleName();
	public static final String START_SERVICE = "com.chadx.injector:startTunnel";

	private static final int PRIORITY_MIN = -2;
    private static final int PRIORITY_DEFAULT = 0;
    private static final int PRIORITY_MAX = 2;
    public static boolean isRunning = false;
	private NotificationManager mNotificationManager;

	private Handler mHandler;
	private Setting mPrefs;
	private Thread mTunnelThread;
	private TunnelManagerThread mTunnelManager;
	private ConnectivityManager connMgr;
	private DNSTunnelThread mDnsThread;
	private static SharedPreferences sp;
	private MainReceiver mConnectivityReceiver;
	
	

	@Override
	public void onCreate()
	{
		Log.i(TAG, "onCreate");

		super.onCreate();
		sp = new Setting(this).getPrefsPrivate();
		mPrefs = new Setting(this);
		mHandler = new Handler();

		connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(TAG, "onStartCommand");

		startTunnelBroadcast();

		SkStatus.addStateListener(this);

		if (intent != null && START_SERVICE.equals(intent.getAction()))
            return START_NOT_STICKY;

		String stateMsg = getString(SkStatus.getLocalizedState(SkStatus.getLastState()));
		showNotification(stateMsg, NOTIFICATION_CHANNEL_NEWSTATUS_ID, 0, ConnectionStatus.LEVEL_START, null);

		new Thread(new Runnable() {
				@Override
				public void run() {
					startTunnel();
				}
			}).start();

		//return Service.START_STICKY;
		return Service.START_NOT_STICKY;
	}


	/**
	 * Tunnel
	 */
	 
	public static SharedPreferences getSharedPrefs() {
        return sp;
    }
	
	public synchronized void startTunnel() {

		SkStatus.updateStateString(SkStatus.SSH_INICIANDO, getString(R.string.starting_service_ssh));

		networkStateChange(this, true);

		SkStatus.logInfo(String.format("Ip Local: %s", getIpPublic()));

		try {
			SharedPreferences prefs = mPrefs.getPrefsPrivate();
			int tunnelType = prefs.getInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_DIRECT);

			if (tunnelType == Setting.bTUNNEL_TYPE_SLOWDNS) {
				mPrefs.setBypass(true);
				mDnsThread = new DNSTunnelThread(this);
				mDnsThread.start();
			}
			mTunnelManager = new TunnelManagerThread(mHandler, this);
			mTunnelManager.setOnStopClienteListener(new TunnelManagerThread.OnStopCliente() {
					@Override
					public void onStop() {
						endTunnelService();
					}
				});

			mTunnelThread = new Thread(mTunnelManager);
			mTunnelThread.start();

			SkStatus.logInfo("started Tunnel Thread");

		} catch(Exception e) {
			SkStatus.logException(e);
			endTunnelService();
		}
	}

	public synchronized void stopTunnel() {
		SharedPreferences prefs = mPrefs.getPrefsPrivate();
		int tunnelType = prefs.getInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_DIRECT);

		
		if (mTunnelManager != null) {
			mTunnelManager.stopAll();

			networkStateChange(this, true);

			if (mTunnelThread != null) {

				mTunnelThread.interrupt();

				SkStatus.logInfo("stopped Tunnel Thread");
			}

			mTunnelManager = null;
		}
	}

	protected String getIpPublic() {

		final android.net.NetworkInfo network = connMgr
			.getActiveNetworkInfo();

		if (network != null && network.isConnectedOrConnecting()) {
			return TunnelUtils.getLocalIpAddress();
		}
		else {
            return "Indisponivel";
        }
	}



	@Override
    public IBinder onBind(Intent intent) {
        return null;
    }

	@Override
	public void onDestroy()
	{
		Log.i(TAG, "onDestroy");

		super.onDestroy();

		stopTunnel();

		stopTunnelBroadcast();

		SkStatus.removeStateListener(this);
	}

	@Override
    public void onTaskRemoved(Intent rootIntent){
		Log.d(TAG,"task removed");
		Intent intent = new Intent(this, DummyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
    }

	/* (non-Javadoc)
     * @see android.app.Service#onLowMemory()
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();

        SkStatus.logWarning("Low Memory Warning!");
    }

	public void endTunnelService() {
		mHandler.post(new Runnable() {
				@Override
				public void run() {
					stopForeground(true);

					stopSelf();
					SkStatus.removeStateListener(MainService.this);
				}
			});
	}


	/**
	 * Notificação
	 */
	 
	private void register_connectivity_receiver() {
        this.mConnectivityReceiver = new MainReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        registerReceiver(this.mConnectivityReceiver, filter);
    }

    private void unregister_connectivity_receiver() {
        unregisterReceiver(this.mConnectivityReceiver);
    }

	public static final String NOTIFICATION_CHANNEL_BG_ID = "openvpn_bg";
    public static final String NOTIFICATION_CHANNEL_NEWSTATUS_ID = "openvpn_newstat";
    public static final String NOTIFICATION_CHANNEL_USERREQ_ID = "openvpn_userreq";

	private String type = "Direct Connection";
	private Notification.Builder mNotifyBuilder = null;
	private String lastChannel;

	private void showNotification(final String msg, final String channel,long when, final ConnectionStatus status, Intent intent) {
		SharedPreferences prefs = mPrefs.getPrefsPrivate();
        int icon = getIconByConnectionStatus(status);
		int tunnelType = prefs.getInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_DIRECT);
		if(tunnelType==Setting.bTUNNEL_TYPE_SSH_PROXY){
			type = "Secure Shell (SSH) | "+getString(R.string.app_name);
		}else if(tunnelType==Setting.bTUNNEL_TYPE_SSH_SSLTUNNEL){
			type = "Secure Shell (SSL) | "+getString(R.string.app_name);
		}else if(tunnelType==Setting.bTUNNEL_TYPE_SSL_RP){
			type = "Secure Shell (SSL + Remote) | "+getString(R.string.app_name);
		}else if(tunnelType==Setting.bTUNNEL_TYPE_PAY_SSL){
			type = "Secure Shell (SSL + Payload) | "+getString(R.string.app_name);
		}
		else{
			type = "Direct Connection";
		}
		if (mNotifyBuilder == null) {
			//mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			mNotifyBuilder = new Notification.Builder(this)

        		.setContentTitle(type/*getString(R.string.app_name)*/)
				.setOnlyAlertOnce(true)
        		.setOngoing(true)
				.setWhen(new Date().getTime());
			// Try to set the priority available since API 16 (Jellybean)
        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            	addVpnActionsToNotification(mNotifyBuilder);
        	}

        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            	lpNotificationExtras(mNotifyBuilder, Notification.CATEGORY_SERVICE);
		}

	    int priority = PRIORITY_DEFAULT;
		if (channel.equals(NOTIFICATION_CHANNEL_BG_ID))
            priority = PRIORITY_MIN;
        else if (channel.equals(NOTIFICATION_CHANNEL_USERREQ_ID))
            priority = PRIORITY_MAX;

		mNotifyBuilder.setSmallIcon(icon);
        mNotifyBuilder.setContentText(msg);
		//mNotifyBuilder.setStyle(bigText);
		if (status == ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT) {
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
            mNotifyBuilder.setContentIntent(pIntent);
        }
		else {
			mNotifyBuilder.setContentIntent(getGraphPendingIntent(this));
		}

        if (when != 0)
            mNotifyBuilder.setWhen(when);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            jbNotificationExtras(priority, mNotifyBuilder);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //noinspection NewApi
            mNotifyBuilder.setChannelId(channel);
        }

		String tickerText = msg;
        if (tickerText != null && !tickerText.equals(""))
            mNotifyBuilder.setTicker(tickerText);

        Notification notification = mNotifyBuilder.build();

        int notificationId = channel.hashCode();

		startForeground(notificationId, notification);

		mNotificationManager.notify(notificationId, notification);

		if (lastChannel != null && !channel.equals(lastChannel)) {
            // Cancel old notification
            mNotificationManager.cancel(lastChannel.hashCode());
        }
		lastChannel = channel;

		/*    new Timer().schedule(new TimerTask(){
		 @Override
		 public void run(){
		 mHandler.post(new Runnable() {
		 @Override
		 public void run() {
		 update_notification_event(msg,channel,status);
		 }
		 });
		 }
		 }, 0,1000);*/

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void lpNotificationExtras(Notification.Builder nbuilder, String category) {
        nbuilder.setCategory(category);
        nbuilder.setLocalOnly(true);
    }

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void jbNotificationExtras(int priority,
									  Notification.Builder nbuilder) {
        try {
            if (priority != 0) {
                Method setpriority = nbuilder.getClass().getMethod("setPriority", int.class);
                setpriority.invoke(nbuilder, priority);

               /* Method setUsesChronometer = nbuilder.getClass().getMethod("setUsesChronometer", boolean.class);
                setUsesChronometer.invoke(nbuilder, true);*/
            }

            //ignore exception
        } catch (NoSuchMethodException | IllegalArgumentException |
		InvocationTargetException | IllegalAccessException e) {
            SkStatus.logException(e);
        }
    }

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void addVpnActionsToNotification(Notification.Builder nbuilder) {

		Intent reconnectVPN = new Intent(this, MainReceiver.class);
		reconnectVPN.setAction(MainReceiver.ACTION_SERVICE_RESTART);
      	//PendingIntent reconnectPendingIntent = PendingIntent.getBroadcast(this, 0, reconnectVPN, PendingIntent.FLAG_CANCEL_CURRENT);

		//nbuilder.addAction(R.drawable.ic_cloud_outline, getString(R.string.reconnect), reconnectPendingIntent);

		Intent disconnectVPN = new Intent(this, MainReceiver.class);
		disconnectVPN.setAction(MainReceiver.ACTION_SERVICE_STOP);
      //	PendingIntent disconnectPendingIntent = PendingIntent.getBroadcast(this, 0, disconnectVPN, PendingIntent.FLAG_CANCEL_CURRENT);

       // nbuilder.addAction(R.drawable.ic_cloud_outline, getString(R.string.stop), disconnectPendingIntent);
    }

	private int getIconByConnectionStatus(ConnectionStatus level) {
        switch (level) {
            case LEVEL_CONNECTED:
                return R.drawable.ic_cloud_black_24dp;
            case LEVEL_AUTH_FAILED:
            case LEVEL_NONETWORK:
            case LEVEL_NOTCONNECTED:
			case LEVEL_CONNECTING_NO_SERVER_REPLY_YET:
            case LEVEL_CONNECTING_SERVER_REPLIED:
            case UNKNOWN_LEVEL:
			default:
                return R.drawable.ic_cloud_off_black_24dp;
        }
    }

	// Usado também pelo tunnel VPN
	// Usado também pelo tunnel VPN
	public static PendingIntent getGraphPendingIntent(Context context) {
        // Let the configure Button show the Log

        Intent intent = new Intent(context, a.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

		PendingIntent startLW = PendingIntent.getActivity(context, 0, intent, 0);

		return startLW;
    }


	/**
	 * SkStatus.StateListener
	 */

	@Override
    public void updateState(String state, String msg, int resid, ConnectionStatus level, Intent intent) {

		// If the process is not running, ignore any state,
        // Notification should be invisible in this state

        if (mTunnelThread == null)
            return;

		String channel = NOTIFICATION_CHANNEL_BG_ID;
		if (level.equals(ConnectionStatus.LEVEL_CONNECTED)) {
			channel = NOTIFICATION_CHANNEL_USERREQ_ID;
		}

        String stateMsg = getString(SkStatus.getLocalizedState(SkStatus.getLastState()));
        showNotification(stateMsg, channel, 0, level, null);
    }



	/**
	 * Tunnel Broadcast
	 */

	private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
		@Override
		public void onAvailable(Network net) {
			SkStatus.logDebug("Rede disponivel");
		}

		@Override
		public void onLost(Network net) {
			SkStatus.logDebug("Rede perdida");
		}

		@Override
		public void onUnavailable() {
			SkStatus.logDebug("Rede indisponivel");
		}
	};

	public static final String TUNNEL_SSH_RESTART_SERVICE = Service.class.getName() + "::restartservicebroadcast",
	TUNNEL_SSH_STOP_SERVICE = Service.class.getName() + "::stopservicebroadcast";

	private void startTunnelBroadcast() {
		if (Build.VERSION.SDK_INT >= 24) {
			connMgr.registerDefaultNetworkCallback(networkCallback);
		}

		IntentFilter broadcastFilter = new IntentFilter();
		broadcastFilter.addAction(TUNNEL_SSH_STOP_SERVICE);
		broadcastFilter.addAction(TUNNEL_SSH_RESTART_SERVICE);

		LocalBroadcastManager.getInstance(this)
			.registerReceiver(mTunnelSSHBroadcastReceiver, broadcastFilter);
	}

	private void stopTunnelBroadcast() {
		LocalBroadcastManager.getInstance(this)
			.unregisterReceiver(mTunnelSSHBroadcastReceiver);

		if (Build.VERSION.SDK_INT >= 24)
			connMgr.unregisterNetworkCallback(networkCallback);
	}

	private BroadcastReceiver mTunnelSSHBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action == null) {
				return;
			}

			if (action.equals(TUNNEL_SSH_RESTART_SERVICE)) {
				new Thread(new Runnable() {
						@Override
						public void run() {
							if (mTunnelManager != null) {
								mTunnelManager.reconnectSSH();
							}
						}
					}).start();
			}

			else if (action.equals(TUNNEL_SSH_STOP_SERVICE)) {
				endTunnelService();
			}
		}
	};

	private static String lastStateMsg;

	protected void networkStateChange(Context context, boolean showStatusRepetido) {
		String netstatestring;

		try {
			// deprecated in 29
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			if (networkInfo == null) {
				netstatestring = "not connected";
			} else {
				String subtype = networkInfo.getSubtypeName();
				if (subtype == null)
					subtype = "";
				String extrainfo = networkInfo.getExtraInfo();
				if (extrainfo == null)
					extrainfo = "";

				/*
				 if(networkInfo.getType()==android.net.ConnectivityManager.TYPE_WIFI) {
				 WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				 WifiInfo wifiinfo = wifiMgr.getConnectionInfo();
				 extrainfo+=wifiinfo.getBSSID();

				 subtype += wifiinfo.getNetworkId();
				 }*/


				netstatestring = String.format("%2$s %4$s to %1$s %3$s", networkInfo.getTypeName(),
											   networkInfo.getDetailedState(), extrainfo, subtype);
			}

		} catch (Exception e) {
			netstatestring = e.getMessage();
		}

		if (showStatusRepetido || !netstatestring.equals(lastStateMsg))
			SkStatus.logInfo(netstatestring);

		lastStateMsg = netstatestring;
	}
}
