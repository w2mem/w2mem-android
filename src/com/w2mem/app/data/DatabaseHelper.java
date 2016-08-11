package com.w2mem.app.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.w2mem.app.R;
import com.w2mem.app.system.Dictionary;
import com.w2mem.app.system.WordPair;

/**
 * DatabaseHelper class provides application-wide access to
 * dictionary and word pairs data. All its methods and field
 * are static, to ensure that it can be accessed from anywhere
 * in application while preserving data that is required for 
 * DatabaseHelper to work properly.
 * 
 * IMPORTANT DatabaseHelper MUST BE initialized by calling
 * initialize(Context context) method, where context variable
 * is Android application's context.
 * 
 * @author Karamanov Anton
 */
public class DatabaseHelper {
	
	// TODO Create and handle NotInitialized exception
	
	private static Resources res;
	private static String packageName;
	
	private static SQLiteHelper dbHelper = null;
	private static SQLiteDatabase database = null;
	
	private static final int BUFFER_SIZE = 8192;
	
	public static void initialize(Context context) {
		res = context.getResources();
		packageName = context.getPackageName();
		dbHelper = new SQLiteHelper(context);
	}
	
	public static void open() {
		database = dbHelper.getWritableDatabase();
	}
	
	public static void close() {
		database.close();
	}
	
	/* DICTIONARIES */
	/* 
	 * Retrieves default dictionaries from raw data and stores them in
	 * dictionary SQLite database.
	 */
	public static void loadDefaultDicts() throws IOException {
		String[] defaultDictsNames = res.getStringArray(R.array.standartTopics);
		String[] defaultDictsResIdentifiers = res.getStringArray(R.array.standartOriginalTopics);

		open();
		for (int i = 0; i < defaultDictsNames.length; i++) {

			// gets current dictionary data
			String dictName = defaultDictsNames[i];
			InputStream rawDictContent = res.openRawResource(
					res.getIdentifier(defaultDictsResIdentifiers[i], "raw", packageName));

			// both new dictionary and all it's word pairs must be added in one transaction
			database.beginTransaction();
			try {
				// adds dictionary to database and gets its id
				long id = addDict(dictName, SQLiteHelper.DEFAULT_DICT, 0);
				loadWordPairsFromStream(rawDictContent, id);
				database.setTransactionSuccessful();
			} finally {
				database.endTransaction();
			}
		}
		close();
	}
	
	/* Adds new dictionary to database. */
	public static long addDict(String name, int type, long onlineId) {
		ContentValues newDict = new ContentValues();
		newDict.put(SQLiteHelper.COLUMN_NAME, name);
		newDict.put(SQLiteHelper.COLUMN_TYPE, type);
		newDict.put(SQLiteHelper.COLUMN_ONLINE_ID, onlineId);
		long id = database.insert(SQLiteHelper.DICT_TABLE, null, newDict);
		return id;
	}
	
	/* Removes all default dictionaries. */
	public static void removeAllStandartDicts() {
		open();
		database.beginTransaction();
		try {
			Cursor cursor = database.query(SQLiteHelper.DICT_TABLE, 
					SQLiteHelper.DICT_TABLE_COLUMNS,
					SQLiteHelper.COLUMN_TYPE + "=" + SQLiteHelper.DEFAULT_DICT,
					null, null, null, null);
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false) {
				removeDict(cursor.getLong(0));
				cursor.moveToNext();
			}
			cursor.close();
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
		close();
	}
	
	/* Removes dictionary and all it's word pairs from database. */
	public static void removeDict(long id) {
		open();
		try {
			String dictQuery  = SQLiteHelper.COLUMN_ID + "=" + id;
			String topicQuery = SQLiteHelper.COLUMN_DICT_ID + "=" + id;
			database.delete(SQLiteHelper.DICT_TABLE, dictQuery, null);
			database.delete(SQLiteHelper.WORD_PAIRS_TABLE, topicQuery, null);
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
		close();
	}
	
	/*  Renames dictionary */
	public static void renameDict(String newName, long dictId) {
		open();
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_NAME, newName);
		database.beginTransaction();
		try {
			database.update(SQLiteHelper.DICT_TABLE, values,
					SQLiteHelper.COLUMN_ID + "=" + dictId,
					null);
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
		close();
	}
	
	/* Gets list of dicts of specific type from database. */
	public static List<Dictionary> getDicts(int type) {
		open();
		List<Dictionary> dicts = new ArrayList<Dictionary>();
		Cursor cursor = database.query(
				SQLiteHelper.DICT_TABLE,
				SQLiteHelper.DICT_TABLE_COLUMNS,
				SQLiteHelper.COLUMN_TYPE + "=" + type,
				null, null, null, null);
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			dicts.add(getDictFromCursor(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return dicts;
	}
	
	/* Parses and creates Topic from cursor. */
	public static Dictionary getDictFromCursor(Cursor cursor) {
		return new Dictionary(
				cursor.getInt(0),	// ID
				cursor.getString(1) // NAME
		);
	}
	
	/* WORD PAIRS */
	/* Processes and stores word pairs from stream to database. */
	public static boolean loadWordPairsFromStream(InputStream stream, long dictId) throws IOException {
		// all word pairs from stream must be loaded in one transactions
		database.beginTransaction();
		try {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(
						new InputStreamReader(stream, "UTF8"), BUFFER_SIZE);
			} catch (UnsupportedEncodingException e) {}
			// reads first line to check stream status
			String line = reader.readLine();
			// if first line equals "Access denied" -- dicts can't be loaded
			if (line.equals("Access denied")) {
				return false;
			}
			while (line != null) {
				String[] parts = line.split("\\=");
				if (parts.length == 2) {
					String word = parts[0].trim();
					String translation = parts[1].trim();
					addWordPair(word, translation, dictId);
				}
				line = reader.readLine();
			}
			reader.close();
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
		return true;
	}
	
	public static void removePairs(long dictId) {
		database.delete(SQLiteHelper.WORD_PAIRS_TABLE,
				SQLiteHelper.COLUMN_DICT_ID + "=" + dictId,
				null);
	}
	
	/* Checks if dictionary exists in database */
	public static boolean dictExists(long id) {
		Cursor cursor = database.query(SQLiteHelper.DICT_TABLE,
				SQLiteHelper.DICT_TABLE_COLUMNS, 
				SQLiteHelper.COLUMN_ONLINE_ID + "=" + id,
				null, null, null, null);
		cursor.moveToFirst();
		boolean exists = (cursor.getCount() != 0);
		cursor.close();
		return exists;
	}
	
	/* Adds new word pair to database. */
	public static long addWordPair(String word, String translate, long dict) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_WORD, word);
		values.put(SQLiteHelper.COLUMN_TRANSLATE, translate);
		values.put(SQLiteHelper.COLUMN_DICT_ID, dict);
		return database.insert(SQLiteHelper.WORD_PAIRS_TABLE, null, values);
	}
	
	/* Adds new word pair to database and returns it as WordPair. */
	public static WordPair addAndReturnPair(String word, String translate, long dictId) {
		open();
		long insertId = addWordPair(word, translate, dictId);
		Cursor cursor = database.query(
				SQLiteHelper.WORD_PAIRS_TABLE,
				SQLiteHelper.WORD_PAIRS_TABLE_COLUMNS,
				SQLiteHelper.COLUMN_ID + "=" + insertId,
				null, null, null, null);
		cursor.moveToFirst();
		WordPair result = getWordPairFromCursor(cursor);
		cursor.close();
		close();
		return result;
	}
	
	/* Removes word pair from database. */
	public static void removeWordPair(long wordPairId) {
		database.delete(
				SQLiteHelper.WORD_PAIRS_TABLE,
				SQLiteHelper.COLUMN_ID + "=" +  wordPairId,
				null);
	}
	
	/* Retrieves all word pairs form dictionary and returns it as a list of WordPair's. */
	public static List<WordPair> getAllWordPairsFromDict(long dict) {
		open();
		List<WordPair> wordPairs = new ArrayList<WordPair>();
		Cursor cursor = database.query(
				SQLiteHelper.WORD_PAIRS_TABLE,
				SQLiteHelper.WORD_PAIRS_TABLE_COLUMNS,
				SQLiteHelper.COLUMN_DICT_ID + "=" + dict,
				null, null, null, null);
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			wordPairs.add(getWordPairFromCursor(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return wordPairs;
	}
	
	/* Parses and creates WordPair form cursor */
	public static WordPair getWordPairFromCursor(Cursor cursor) {
		return new WordPair(
				cursor.getLong(0),   // ID
				cursor.getString(1), // WORD
				cursor.getString(2), // TRANSLATION
				cursor.getLong(3)    // DICT_ID
		);
	}
	
	/* Counts word pairs in dictionary */
	public static int countWordPairsInDict(long dictId) {
		open();
		Cursor cursor = database.query(SQLiteHelper.WORD_PAIRS_TABLE,
				SQLiteHelper.WORD_PAIRS_TABLE_COLUMNS,
				SQLiteHelper.COLUMN_DICT_ID + "=" + dictId,
				null, null,null, null);
		int result = cursor.getCount();
		cursor.close();
		close();
		return result;
		
	}
	
	/* Changes word pair in database. */
	public static void changeWordPair(String newWord, String newTranslate, long id) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_WORD, newWord);
		values.put(SQLiteHelper.COLUMN_TRANSLATE, newTranslate);
		database.update(SQLiteHelper.WORD_PAIRS_TABLE, values,
				SQLiteHelper.COLUMN_ID + "=" + id,
				null);
	}
}
