package com.w2mem.app.data;

import android.content.SharedPreferences;

/**
 * SettingsHelper class provides application-wide access to
 * application's settings. It allows both to extract and save
 * all required for proper work of application data, including:
 * 
 * > Application version;
 * > User login;
 * > User password;
 * > User's choice whether to save his password;
 * > User's choice whether to shuffle dictionaries while practicing.
 *  
 * IMPORTANT DatabaseHelper MUST BE initialized by calling
 * initialize(Context context) method, where context variable
 * is Android application's context.
 * 
 * @author Karamanov Anton
 */
public class SettingsHelper {
	
	public static final String PREFS_NAME = "settings";
	
	class DEFAULT {
		private static final int     APP_VERSION = 0;
		private static final boolean SAVE_USER_INFO = false;
		private static final String  LOGIN    = "";
		private static final String  PASSWORD = "";
		private static final boolean SHUFFLED = false;
	}
	
	class KEYS {
		private static final String APP_VERSION	   = "app_version";
		private static final String SAVE_USER_INFO = "save_user_info";
		private static final String LOGIN		   = "login";
		private static final String PASSWORD	   = "password";
		private static final String SHUFFLED	   = "shuffled";
	}
	
	private static SharedPreferences settings = null;
	private static SharedPreferences.Editor editor = null;
	
	public static void initialize(SharedPreferences settings) {
		SettingsHelper.settings = settings;
		editor = settings.edit();
	}
	
	public static void commit() {
		editor.commit();
		editor = settings.edit();
	}
	
	public static int getAppVersion() {
		return settings.getInt(KEYS.APP_VERSION, DEFAULT.APP_VERSION);
	}
	
	public static boolean getSaveUserInfo() {
		return settings.getBoolean(KEYS.SAVE_USER_INFO, DEFAULT.SAVE_USER_INFO);
	}
	
	public static String getLogin() {
		return settings.getString(KEYS.LOGIN, DEFAULT.LOGIN);
	}
	
	public static String getPassword() {
		String password = settings.getString(KEYS.PASSWORD, DEFAULT.PASSWORD);
		String login = settings.getString(KEYS.LOGIN, DEFAULT.LOGIN);
		String decrypted_password = decrypt(password, login);
		return decrypted_password;
	}
	
	public static void updateAppVersion(int newVersion) {
		editor.putInt(KEYS.APP_VERSION, newVersion);
		commit();
	}
	
	public static void saveLogin(String login) {
		editor.putBoolean(KEYS.SAVE_USER_INFO, true);
		editor.putString(KEYS.LOGIN, login);
		commit();
	}
	
	public static void savePassword(String login, String password) {
		editor.putBoolean(KEYS.SAVE_USER_INFO, true);
		String encrypted_password = encrypt(password, login);
		editor.putString(KEYS.PASSWORD, encrypted_password);
		commit();
	}
	
	public static void clearUserInfo() {
		//editor.remove(KEYS.LOGIN);
		editor.remove(KEYS.PASSWORD);
		editor.remove(KEYS.SAVE_USER_INFO);
		commit();
	}
	
	public static void saveShuffleState(boolean state) {
		editor.putBoolean(KEYS.SHUFFLED, state);
		commit();
	}
	
	public static boolean getShuffleState() {
		return settings.getBoolean(KEYS.SHUFFLED, DEFAULT.SHUFFLED);
	}
	
	/* PASSWORD PROTECTION */
	/* password encryption */
	static public String encrypt(String pass, String key) {
		byte[] k = key.getBytes();

		byte t = 0;
		for (int i = 0; i < k.length; i++)
			t += k[i];

		t /= k.length;

		byte[] p = pass.getBytes();
		for (int i = 0; i < p.length; i++)
			p[i] = (byte) ((p[i] + (byte) t) % 256);
		return new String(p);
	}

	/* password decryption */
	static public String decrypt(String pass, String key) {
		byte[] k = key.getBytes();

		byte t = 0;
		for (int i = 0; i < k.length; i++)
			t += k[i];

		t /= k.length;

		byte[] p = pass.getBytes();
		for (int i = 0; i < p.length; i++)
			p[i] = (byte) ((p[i] - (byte) t) % 256);
		return new String(p);
	}
	
}
