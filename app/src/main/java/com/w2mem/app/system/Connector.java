package com.w2mem.app.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.w2mem.app.data.DatabaseHelper;

/*
 * Connector class establishes connection with W2mem and
 * loads user defined dictionaries.
 */
public class Connector {
	/* CONSTANTS */
	private static final String LIST = "http://w2mem.com/android_list";
	private static final String DICT = "http://w2mem.com/android_dic";
	
	private static final int BUFFER_SIZE = 8192;
	
	public static final int ERROR_NONE = 0;
	public static final int ERROR_NO_DICTS = 1;
	public static final int ERROR_BAD_INFO = 2;

	/* DATA */
	// last accured error code
	private static int errorCode;

	private static String login;
	private static String password;
	private static InputStream content;
	private static List<Dictionary> dictsList = null;
	
	private static boolean isConnected;

	public static void setLogin(String login) {
		Connector.login = login;
	}
	
	public static void setPassword(String password) {
		Connector.password = password;
	}
	
	public static int getErrorCode() {
		return errorCode;
	}

	public static boolean connect(String login, String password) throws IOException {
		password = MD5(password);
		setPassword(password);
		setLogin(login);
		
		// resets error code
		errorCode = ERROR_NONE;
		// Accessing W2mem
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(LIST + "?login=" + login + "&pass_md=" + password);
		HttpResponse response = null;
		response = client.execute(httpGet);
		
		isConnected = (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
		
		// if connection has been established successfully
		if (isConnected) {
			content = response.getEntity().getContent();
			return parseDicts(content);
		// if connections hasn't been established
		} else {
			// close connection
			response.getEntity().getContent().close();
			errorCode = ERROR_BAD_INFO;
			return false;
		}
	}
	
	public static boolean parseDicts(InputStream content) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(content), BUFFER_SIZE);
		String line = "";
		line = reader.readLine();
		if (line == null) { 
			errorCode = ERROR_NO_DICTS;
			return false;
		}
		if (line.equals("Access denied")) {
			errorCode = ERROR_BAD_INFO;
			return false;
		}
		dictsList = new ArrayList<Dictionary>();
		// Acquiring dictionaries
		while (line != null) {
			String[] parts = line.split("\\=");
			if (parts.length == 2) {
				Dictionary dict = new Dictionary(
						Long.parseLong(parts[1]),
						parts[0]);
				dictsList.add(dict);
			}
			line = reader.readLine();
		}
		return true;
	}

	public static List<Dictionary> getListOfDicts() {
		// prevents from loading before connection
		if (isConnected == false) {
			return null;
		}
		return dictsList;
	}

	public static boolean downloadDict(String name, long id, int param) throws IllegalStateException, IOException {
		long dictId = 0;
		
		DatabaseHelper.open();
		
		if (DatabaseHelper.dictExists(id) == false) {
			dictId = DatabaseHelper.addDict(name, param, id);
		} else {
			// if dictionary with this id already exists -- 
			// remove it's content (to upgrade it) and rename it
			DatabaseHelper.removePairs(dictId);
			// IMPORTANT: renameDict method closes database on completion
			DatabaseHelper.renameDict(name, dictId);
		}

		DefaultHttpClient client = new DefaultHttpClient();
		String http = DICT + "?id=" + id + "&login=" + login + "&pass_md=" + password;
		HttpGet httpGet = new HttpGet(http);
		HttpResponse response = null;
		try {
			response = client.execute(httpGet);
		} catch (Exception e) {}
		
		isConnected = (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
		
		if (isConnected) {
			InputStream stream = response.getEntity().getContent();
				
			DatabaseHelper.open();
			boolean uploadStatus = DatabaseHelper.loadWordPairsFromStream(stream, dictId);
			DatabaseHelper.close();
			return uploadStatus;
		} else {
			response.getEntity().getContent().close();
			return false;
		}
	}
	
	/* MD5 hash */
	public static String MD5(String input) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {}
		return ByteToHexString(md.digest(input.getBytes()));
	}
	
	private static String ByteToHexString(byte hash[]) {
		StringBuffer buf = new StringBuffer(hash.length * 2);
		for (byte item: hash) {
			if ((item & 0xff) < 0x10)
				buf.append("0");
			buf.append(Long.toString(item & 0xff, 16));
		}
		return buf.toString();
	}
}
