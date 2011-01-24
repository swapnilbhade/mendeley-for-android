package com.martineve.mendroid.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class MendeleyDatabase extends SQLiteOpenHelper 
{
	private static final String TAG = "com.martineve.mendroid.data.MendeleyDatabase";
	
	// the database version; increment to call update
	private static final int DATABASE_VERSION = 5;
	
	public static final String _ID = "_id";
	public static final String SYNC_UP = "sync_up";
	
	// author fields
	public static final String AUTHOR_NAME = "author_name";
	
	// collection fields
	public static final String COLLECTION_NAME = "collection_name";
	public static final String COLLECTION_TYPE = "collection_type";
	public static final String COLLECTION_SIZE = "collection_size";

	// document fields
	public static final String DOCUMENT_NAME = "document_title";
	public static final String DOCUMENT_TYPE = "document_type";
	public static final String DOCUMENT_COLLECTION_ID = "collection_id";
	
	// document_to_authors fields
	public static final String DOCUMENT_TO_AUTHORS_DOCUMENT_ID = "document_id";
	public static final String DOCUMENT_TO_AUTHORS_AUTHOR_ID = "author_id";
	
	private static final String DATABASE_NAME = "Mendeley";
	
	// table creation statements
	private static final String COLLECTIONS_CREATE =
		"create table collections"+ 
		" (_id integer primary key, "
		+ "collection_name text not null, collection_type text not null, collection_size int not null, sync_up bool not null);";
	
	private static final String DOCUMENTS_CREATE =
		"create table documents"+ 
		" (_id integer primary key, "
		+ "collection_id int not null, document_title string not null, document_type string not null, sync_up bool not null);";
	
	private static final String AUTHORS_CREATE =
		"create table authors"+ 
		" (_id integer primary key autoincrement, "
		+ "author_name string not null, sync_up bool not null);";

	private static final String DOCUMENT_TO_AUTHORS_CREATE =
		"create table documenttoauthors"+ 
		" (_id integer primary key autoincrement, "
		+ "author_id int not null, document_id int not null, sync_up bool not null);";

	MendeleyDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		db.execSQL(COLLECTIONS_CREATE);
		db.execSQL(DOCUMENTS_CREATE);
		db.execSQL(AUTHORS_CREATE);
		db.execSQL(DOCUMENT_TO_AUTHORS_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, 
			int newVersion) {
		Log.w(TAG, 
				"Upgrading database from version " + 
				oldVersion + " to " + newVersion + 
		", which will destroy all old data.");
		db.execSQL("DROP TABLE IF EXISTS collections");
		db.execSQL("DROP TABLE IF EXISTS documents");
		db.execSQL("DROP TABLE IF EXISTS authors");
		db.execSQL("DROP TABLE IF EXISTS documenttoauthors");
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
		
		return mContentResolver.insert(MendeleyCollectionsProvider.COLLECTIONS_URI, values);
	}
	
	public static Uri insertDocument(long id, String title, String type, int collection, boolean sync_up, ContentResolver mContentResolver)
	{
		ContentValues values = new ContentValues();
		values.put(MendeleyDatabase._ID, id);
		values.put(MendeleyDatabase.DOCUMENT_NAME, title);
		values.put(MendeleyDatabase.DOCUMENT_TYPE, type);
		values.put(MendeleyDatabase.DOCUMENT_COLLECTION_ID, collection);
		values.put(MendeleyDatabase.SYNC_UP, sync_up);
		
		return mContentResolver.insert(MendeleyCollectionsProvider.COLLECTION_DOCUMENTS_URI, values);
	}
	
	public static Uri insertOrGetAuthor(String name, boolean sync_up, ContentResolver mContentResolver)
	{
		ContentValues values = new ContentValues();
		values.put(MendeleyDatabase.AUTHOR_NAME, name);
		values.put(MendeleyDatabase.SYNC_UP, sync_up);
		
		Cursor c = mContentResolver.query(Uri.withAppendedPath(MendeleyCollectionsProvider.AUTHOR_URI, name), null, null, null, null);
		
		if(c.getCount() == 0)
		{
			return mContentResolver.insert(MendeleyCollectionsProvider.AUTHOR_URI, values);
		} else
		{
			c.moveToFirst();
			String ret = Integer.toString(c.getInt(c.getColumnIndex(_ID)));
			c.close();
			
			return Uri.withAppendedPath(MendeleyCollectionsProvider.AUTHOR_URI, ret);
		}
	}
	
	public static Uri linkDocumentAndAuthor(Uri AuthorUri, Uri DocumentUri, boolean sync_up, ContentResolver mContentResolver)
	{
		// extract the various IDs
		long authorID = Long.parseLong(AuthorUri.getPathSegments().get(1));
		long documentID = Long.parseLong(DocumentUri.getPathSegments().get(1));
		
		ContentValues values = new ContentValues();
		values.put(MendeleyDatabase.DOCUMENT_TO_AUTHORS_AUTHOR_ID, authorID);
		values.put(MendeleyDatabase.DOCUMENT_TO_AUTHORS_DOCUMENT_ID, documentID);
		values.put(MendeleyDatabase.SYNC_UP, sync_up);
		
		return mContentResolver.insert(MendeleyCollectionsProvider.AUTHOR_TO_DOCUMENT_URI, values);
	}
}
