package com.w2mem.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
	public static final int USER_DICT = 1;    // created/downloaded by user dicts
	public static final int PHRASES_DICT = 2; // not used yet
	public static final int DEFAULT_DICT = 3; // default dicts
	
	public static final String DICT_TABLE       = "dicts";
	public static final String COLUMN_ID 		= "_id";
	public static final String COLUMN_NAME 		= "name";
	public static final String COLUMN_TYPE 		= "type";
	public static final String COLUMN_ONLINE_ID = "online_id";
	
	public static final String WORD_PAIRS_TABLE = "word_pairs";
	// + COLUMN_ID
	public static final String COLUMN_WORD      = "word";
	public static final String COLUMN_TRANSLATE = "translation";
	public static final String COLUMN_DICT_ID   = "dict_id";
	
	public static final String[] DICT_TABLE_COLUMNS = { 
		SQLiteHelper.COLUMN_ID,
		SQLiteHelper.COLUMN_NAME,
		SQLiteHelper.COLUMN_TYPE,
		SQLiteHelper.COLUMN_ONLINE_ID };
	
	public static final String[] WORD_PAIRS_TABLE_COLUMNS = { 
		SQLiteHelper.COLUMN_ID,
		SQLiteHelper.COLUMN_WORD, 
		SQLiteHelper.COLUMN_TRANSLATE,
		SQLiteHelper.COLUMN_DICT_ID };

	private static final String DATABASE_NAME = "dicts.db";
	private static final int DATABASE_VERSION = 7;

	// table creation SQL statement
	private static final String CREATE_DICTS_TABLE = 
					"CREATE TABLE "  + DICT_TABLE + "(" + 
					COLUMN_ID   	 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					COLUMN_NAME 	 + " TEXT NOT NULL, " +
					COLUMN_TYPE 	 + " INTEGER, " +
					COLUMN_ONLINE_ID + " INTEGER);";
	
	private static final String CREATE_WORD_PAIRS_TABLE = 
					"CREATE TABLE "  + WORD_PAIRS_TABLE + "(" + 
					COLUMN_ID   	 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					COLUMN_WORD 	 + " TEXT NOT NULL, " +
					COLUMN_TRANSLATE + " TEXT NOT NULL, " +
					COLUMN_DICT_ID   + " INTEGER);";

	
	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_DICTS_TABLE);
		database.execSQL(CREATE_WORD_PAIRS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int _old, int _new) {
		db.execSQL("DROP TABLE IF EXISTS " + DICT_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + WORD_PAIRS_TABLE);
		onCreate(db);
	}

}
