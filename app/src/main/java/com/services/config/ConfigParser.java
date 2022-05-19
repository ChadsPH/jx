package com.services.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;


import com.chadx.sockshttp.R;
import com.kimchangyoun.rootbeerFresh.RootBeer;
import com.services.logger.SkStatus;
import com.services.util.Cripto;
import com.services.util.FileUtils;
import com.services.util.securepreferences.crypto.Cryptor;
import com.services.util.securepreferences.model.SecurityConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Properties;

/**
* @author SlipkHunter
*/
/*tknetwork 04-28-22*/
public class ConfigParser
{
	private static final String TAG = ConfigParser.class.getSimpleName();
	public static final String CONVERTED_PROFILE = "converted Profile";
	
	private static final String
			SETTING_VERSION = "file.appVersionCode",
			SETTING_VALIDADE = "file.validade",
			SETTING_PROTEGER = "file.proteger",
			SETTING_AUTOR_MSG = "file.msg";



	public static boolean convertInputAndSave(InputStream input, Context mContext)
			throws IOException {
		Properties mConfigFile = new Properties();

		Setting settings = new Setting(mContext);
		SharedPreferences.Editor prefsEdit = settings.getPrefsPrivate()
				.edit();

		try {

			InputStream decodedInput = decodeInput(input);

			try {
				mConfigFile.loadFromXML(decodedInput);
			} catch(FileNotFoundException e) {
				throw new IOException("File Not Found");
			} catch(IOException e) {
				throw new Exception("Error Unknown", e);
			}

			// versão check
			int versionCode = Integer.parseInt(mConfigFile.getProperty(SETTING_VERSION));

			if (versionCode > getBuildId(mContext)) {
				throw new IOException(mContext.getString(R.string.alert_update_app));
			}

			// validade check
			String msg = mConfigFile.getProperty(SETTING_AUTOR_MSG);
			boolean mIsProteger = mConfigFile.getProperty(SETTING_PROTEGER).equals("1");
			long mValidade = 0;

			try {
				mValidade = Long.parseLong(mConfigFile.getProperty(SETTING_VALIDADE));
			} catch(Exception e) {
				throw new IOException(mContext.getString(R.string.alert_update_app));
			}

			if (!mIsProteger || mValidade < 0) {
				mValidade = 0;
			}
			else if (mValidade > 0 && isValidadeExpirou(mValidade)){
				throw new IOException(mContext.getString(R.string.error_settings_expired));
			}

			// bloqueia root
			boolean isBloquearRoot = false;
			String _blockRoot = mConfigFile.getProperty("bloquearRoot");
			if (_blockRoot != null) {
				isBloquearRoot = _blockRoot.equals("1");
				if (isBloquearRoot) {
					if (isDeviceRooted(mContext)) {
						throw new IOException(mContext.getString(R.string.error_root_detected));
					}
				}
			}


			try {
				String mServidor = mConfigFile.getProperty(Setting.SERVIDOR_KEY);
				String mServidorPorta = mConfigFile.getProperty(Setting.SERVIDOR_PORTA_KEY);
				String mUsuario = mConfigFile.getProperty(Setting.USUARIO_KEY);
				String mSenha = mConfigFile.getProperty(Setting.SENHA_KEY);
				int mPortaLocal = Integer.parseInt(mConfigFile.getProperty(Setting.PORTA_LOCAL_KEY));
				int mTunnelType = Setting.bTUNNEL_TYPE_SSH_DIRECT;

				String _tunnelType = mConfigFile.getProperty(Setting.TUNNELTYPE_KEY);
				if (!_tunnelType.isEmpty()) {
					/**
					 * Mantêm compatibilidade
					 */
					if (_tunnelType.equals(Setting.TUNNEL_TYPE_SSH_PROXY)) {
						mTunnelType = Setting.bTUNNEL_TYPE_SSH_PROXY;
					}
					else if (!_tunnelType.equals(Setting.TUNNEL_TYPE_SSH_DIRECT)) {
						mTunnelType = Integer.parseInt(_tunnelType);
					}
				}

				if (mServidor == null) {
					throw new Exception();
				}

				String _proxyIp = mConfigFile.getProperty(Setting.PROXY_IP_KEY);
				String _proxyPort = mConfigFile.getProperty(Setting.PROXY_PORTA_KEY);
				prefsEdit.putString(Setting.PROXY_IP_KEY, _proxyIp != null ? _proxyIp : "");
				prefsEdit.putString(Setting.PROXY_PORTA_KEY, _proxyPort != null ? _proxyPort : "");

				prefsEdit.putBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, mConfigFile.getProperty(Setting.PROXY_USAR_DEFAULT_PAYLOAD).equals("1"));

				String _customPayload = mConfigFile.getProperty(Setting.CUSTOM_PAYLOAD_KEY);
				prefsEdit.putString(Setting.CUSTOM_PAYLOAD_KEY, _customPayload != null ? _customPayload : "");

				if (mIsProteger) {
					prefsEdit.putString(Setting.CONFIG_MENSAGEM_KEY, msg != null ? msg : "");

					new Setting(mContext)
							.setModoDebug(false);

					String pedirLogin = mConfigFile.getProperty("file.pedirLogin");
					if (pedirLogin != null)
						prefsEdit.putBoolean(Setting.CONFIG_INPUT_PASSWORD_KEY, pedirLogin.equals("1"));
					else
						prefsEdit.putBoolean(Setting.CONFIG_INPUT_PASSWORD_KEY, false);
				}
				else {
					prefsEdit.putString(Setting.CONFIG_MENSAGEM_KEY, "");
					prefsEdit.putBoolean(Setting.CONFIG_INPUT_PASSWORD_KEY, false);
				}

				prefsEdit.putString(Setting.SERVIDOR_KEY, mServidor);
				prefsEdit.putString(Setting.SERVIDOR_PORTA_KEY, mServidorPorta);
				prefsEdit.putString(Setting.USUARIO_KEY, mUsuario);
				prefsEdit.putString(Setting.SENHA_KEY, mSenha);
				prefsEdit.putString(Setting.PORTA_LOCAL_KEY, Integer.toString(mPortaLocal));

				prefsEdit.putInt(Setting.TUNNELTYPE_KEY, mTunnelType);
				prefsEdit.putBoolean(Setting.CONFIG_PROTEGER_KEY, mIsProteger);
				prefsEdit.putLong(Setting.CONFIG_VALIDADE_KEY, mValidade);
				prefsEdit.putBoolean(Setting.BLOQUEAR_ROOT_KEY, isBloquearRoot);

				String _isDnsForward = mConfigFile.getProperty(Setting.DNSFORWARD_KEY);
				boolean isDnsForward = _isDnsForward != null && _isDnsForward.equals("0") ? false : true;
				String dnsResolver = mConfigFile.getProperty(Setting.DNSRESOLVER_KEY);
				settings.setVpnDnsForward(isDnsForward);
				settings.setVpnDnsResolver(dnsResolver);

				String _isUdpForward = mConfigFile.getProperty(Setting.UDPFORWARD_KEY);
				boolean isUdpForward = _isUdpForward != null && _isUdpForward.equals("1") ? true : false;
				String udpResolver = mConfigFile.getProperty(Setting.UDPRESOLVER_KEY);
				settings.setVpnUdpForward(isUdpForward);
				settings.setVpnUdpResolver(udpResolver);

			} catch(Exception e) {
				if (settings.getModoDebug()) {
					SkStatus.logException("Error Settings", e);
				}
				throw new IOException(mContext.getString(R.string.error_file_settings_invalid));
			}

			return prefsEdit.commit();

		} catch(IOException e) {
			throw e;
		} catch(Exception e) {
			throw new IOException(mContext.getString(R.string.error_file_invalid), e);
		} catch (Throwable e) {
			throw new IOException(mContext.getString(R.string.error_file_invalid));
		}
	}

	public static void convertDataToFile(OutputStream fileOut, Context mContext,
										 boolean mIsProteger, boolean mPedirSenha, boolean isBloquearRoot, String mMensagem, long mValidade)
			throws IOException {

		Properties mConfigFile = new Properties();
		ByteArrayOutputStream tempOut = new ByteArrayOutputStream();

		Setting settings = new Setting(mContext);
		SharedPreferences prefs = settings.getPrefsPrivate();

		try {
			int targerId = getBuildId(mContext);
			// para versões betas
			targerId = 1;

			mConfigFile.setProperty(SETTING_VERSION, Integer.toString(targerId));

			mConfigFile.setProperty(SETTING_AUTOR_MSG, mMensagem);
			mConfigFile.setProperty(SETTING_PROTEGER, mIsProteger ? "1" : "0");
			mConfigFile.setProperty("bloquearRoot", isBloquearRoot ? "1" : "0");

			mConfigFile.setProperty(SETTING_VALIDADE, Long.toString(mValidade));
			mConfigFile.setProperty("file.pedirLogin", mPedirSenha ? "1" : "0");

			String server = prefs.getString(Setting.SERVIDOR_KEY, "");
			String server_port = prefs.getString(Setting.SERVIDOR_PORTA_KEY, "");

			if (mIsProteger && (server.isEmpty() || server_port.isEmpty())) {
				throw new Exception();
			}

			mConfigFile.setProperty(Setting.SERVIDOR_KEY, server);
			mConfigFile.setProperty(Setting.SERVIDOR_PORTA_KEY, server_port);
			mConfigFile.setProperty(Setting.USUARIO_KEY, prefs.getString(Setting.USUARIO_KEY, ""));
			mConfigFile.setProperty(Setting.SENHA_KEY, prefs.getString(Setting.SENHA_KEY, ""));
			mConfigFile.setProperty(Setting.PORTA_LOCAL_KEY, prefs.getString(Setting.PORTA_LOCAL_KEY, "1080"));

			mConfigFile.setProperty(Setting.TUNNELTYPE_KEY, Integer.toString(prefs.getInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_DIRECT)));

			mConfigFile.setProperty(Setting.DNSFORWARD_KEY, settings.getVpnDnsForward() ? "1" : "0");
			mConfigFile.setProperty(Setting.DNSRESOLVER_KEY, settings.getVpnDnsResolver());

			mConfigFile.setProperty(Setting.UDPFORWARD_KEY, settings.getVpnUdpForward() ? "1" : "0");
			mConfigFile.setProperty(Setting.UDPRESOLVER_KEY, settings.getVpnUdpResolver());

			mConfigFile.setProperty(Setting.PROXY_IP_KEY, prefs.getString(Setting.PROXY_IP_KEY, ""));
			mConfigFile.setProperty(Setting.PROXY_PORTA_KEY, prefs.getString(Setting.PROXY_PORTA_KEY, ""));

					String isDefaultPayload = prefs.getBoolean(Setting.PROXY_USAR_DEFAULT_PAYLOAD, true) ? "1" : "0";
					String customPayload = prefs.getString(Setting.CUSTOM_PAYLOAD_KEY, "");
					String ssl = prefs.getString(Setting.CUSTOM_SNI, "");
					String chave = prefs.getString(Setting.CHAVE_KEY, "");
					String nameserver = prefs.getString(Setting.NAMESERVER_KEY, "");
					String dns = prefs.getString(Setting.DNS_KEY, "");

					if (mIsProteger && isDefaultPayload.equals("0") && customPayload.isEmpty()) {
						throw new IOException();
					}
					int tunnelType = prefs.getInt(Setting.TUNNELTYPE_KEY, Setting.bTUNNEL_TYPE_SSH_DIRECT);
					if (tunnelType == Setting.bTUNNEL_TYPE_SLOWDNS) {
						if (mIsProteger && (chave.isEmpty() || nameserver.isEmpty() || dns.isEmpty())) {
							throw new IOException();
						}
					}		
					mConfigFile.setProperty(Setting.PROXY_USAR_DEFAULT_PAYLOAD, isDefaultPayload);
					mConfigFile.setProperty(Setting.CUSTOM_PAYLOAD_KEY, customPayload);
					mConfigFile.setProperty(Setting.CUSTOM_SNI, ssl);
					mConfigFile.setProperty(Setting.CHAVE_KEY, chave);
					mConfigFile.setProperty(Setting.NAMESERVER_KEY, nameserver);
					mConfigFile.setProperty(Setting.DNS_KEY, dns);
		} catch(Exception e) {
			throw new IOException(mContext.getString(R.string.error_file_settings_invalid));
		}

		try {
			mConfigFile.storeToXML(tempOut,
					"Configuration File");
		} catch(FileNotFoundException e) {
			throw new IOException("File Not Found");
		} catch(IOException e) {
			throw new IOException("Error Unknown", e);
		}

		try {
			InputStream input_encoded = encodeInput(
					new ByteArrayInputStream(tempOut.toByteArray()));

			FileUtils.copiarArquivo(input_encoded, fileOut);
		} catch(Throwable e) {
			throw new IOException(mContext.getString(R.string.error_save_settings));
		}
	}


	/**
	 * Criptografia
	 */

	private static Cryptor mCrypto;

	static {
		mCrypto = Cryptor.initWithSecurityConfig(
				new SecurityConfig.Builder("aghsgahhjgsahjgsastytxcxpp09@2@33fs67877dsjsdldkfsk").build());
	}

	private static InputStream encodeInput(InputStream in) throws Throwable {
		//ByteArrayOutputStream out = new ByteArrayOutputStream();

		String strBase64 = mCrypto.encryptToBase64(getBytesArrayInputStream(in)
				.toByteArray());

		//Cripto.encrypt(KEY_PASSWORD, in, out);

		return new ByteArrayInputStream(strBase64.getBytes());
	}

	private static InputStream decodeInput(InputStream in) throws Throwable {
		byte[] byteDecript;

		ByteArrayOutputStream byteArrayOut = getBytesArrayInputStream(in);

		try {
			byteDecript = mCrypto.decryptFromBase64(byteArrayOut.toString());
		} catch (IllegalArgumentException e) {
			// decodifica confg antigas
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Cripto.decrypt("chadx", new ByteArrayInputStream(byteArrayOut.toByteArray()), out);
			byteDecript = out.toByteArray();
		}

		return new ByteArrayInputStream(byteDecript);
	}

	public static ByteArrayOutputStream getBytesArrayInputStream(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[1024];
		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();

		return buffer;
	}


	/**
	 * Utils
	 */

	public static boolean isValidadeExpirou(long validadeDateMillis) {
		if (validadeDateMillis == 0) {
			return false;
		}

		// Get Current Date
		long date_atual = Calendar.getInstance()
				.getTime().getTime();

		if (date_atual >= validadeDateMillis) {
			return true;
		}

		return false;
	}

	public static int getBuildId(Context context) throws IOException {
		try {
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pinfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new IOException("Build ID not found");
		}
	}

	public static boolean isDeviceRooted(Context context) {
        /*for (String pathDir : System.getenv("PATH").split(":")){
			if (new File(pathDir, "su").exists()) {
				return true;
			}
		}

		Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }*/

		RootBeer rootBeer = new RootBeer(context);

		boolean simpleTests = rootBeer.detectRootManagementApps() || rootBeer.detectPotentiallyDangerousApps() || rootBeer.checkForBinary("su")
				|| rootBeer.checkForDangerousProps() || rootBeer.checkForRWPaths()
				|| rootBeer.detectTestKeys() || rootBeer.checkSuExists() || rootBeer.checkForRootNative() || rootBeer.checkForMagiskBinary();
		//boolean experiementalTests = rootBeer.checkForMagiskNative();

		return simpleTests;
	}

}


