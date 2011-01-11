package com.martineve.mendroid.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class MendeleyDatabase extends SQLiteOpenHelper 
{
	// the database version; increment to call update
	private static final int DATABASE_VERSION = 3;
	
	private SQLiteDatabase mendeleyDB;
	
	public static final String _ID = "_id";
	public static final String SYNC_UP = "sync_up";
	
	// collection fields
	public static final String COLLECTION_NAME = "collection_name";
	public static final String COLLECTION_TYPE = "collection_type";
	public static final String COLLECTION_SIZE = "collection_size";

	
	private static final String DATABASE_NAME = "Mendeley";
	private static final String COLLECTIONS_CREATE =
		"create table collections"+ 
		" (_id integer primary key, "
		+ "collection_name text not null, collection_type text not null, collection_size int not null, sync_up bool not null);";


	MendeleyDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		db.execSQL(COLLECTIONS_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, 
			int newVersion) {
		Log.w("com.martineve.mendroid.data.MendeleyDatabase", 
				"Upgrading database from version " + 
				oldVersion + " to " + newVersion + 
		", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS collections");
		onCreate(db);
	}
	
	public static Uri insertCollection(int id, String name, String type, int size, boolean sync_up, ContentResolver mContentResolver)
	{
		ContentValues values = new ContentValues();
		values.put(MendeleyDatabase._ID, id);
		values.put(MendeleyDatabase.COLLECTION_NAME, name);
		values.put(MendeleyDatabase.COLLECTION_TYPE, type);
		values.put(MendeleyDatabase.COLLECTION_SIZE, size);
		values.put(MendeleyDatabase.SYNC_UP, sync_up);
		
		return mContentResolver.insert(MendeleyContentProvider.COLLECTIONS_URI, values);
	}
}
