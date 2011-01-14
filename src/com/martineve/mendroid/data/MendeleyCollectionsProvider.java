package com.martineve.mendroid.data;

import java.util.concurrent.ExecutionException;

import com.martineve.mendroid.R;
import com.martineve.mendroid.task.GetWritableDatabaseTask;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class MendeleyCollectionsProvider extends ContentProvider {

	private static String PROVIDER_NAME = "com.martineve.mendroid.data.mendeleycollectionsprovider";

	public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/");
	public static final Uri COLLECTIONS_URI = Uri.parse("content://" + PROVIDER_NAME + "/collections");
	public static final Uri COLLECTION_URI = Uri.parse("content://" + PROVIDER_NAME + "/collection");

	private static final int COLLECTIONS = 1;

	private SQLiteDatabase DB;
	private AsyncTask<SQLiteOpenHelper, Void, SQLiteDatabase> DBFetcher;

	public MendeleyCollectionsProvider()
	{

	}

	private void LoadDB()
	{
		try {
			DB = DBFetcher.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static final UriMatcher uriMatcher;
	static{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "collections", COLLECTIONS);
		//uriMatcher.addURI(PROVIDER_NAME, "collections/#", COLLECTION_ID);      
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (uriMatcher.match(uri)){
		case COLLECTIONS:
			// delete all collections
			Log.i("com.martineve.mendroid.data.MendeleyCollectionsProvider", "Deleting collections table.");
			DB.delete("collections", null, null);
			return 1;
		default:
			IllegalArgumentException e = new IllegalArgumentException("Unsupported URI: " + uri);
			Log.e("com.martineve.mendroid.data.MendeleyCollectionsProvider", "Unsupported content type requested.");
			throw e;
		} 
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)){
		case COLLECTIONS:
			Log.i("com.martineve.mendroid.data.MendeleyCollectionsProvider", "Returning content type of mendroid.collections.");
			return "vnd.android.cursor.dir/vnd.martineve.mendroid.collections";
		default:
			IllegalArgumentException e = new IllegalArgumentException("Unsupported URI: " + uri);
			Log.e("com.martineve.mendroid.data.MendeleyCollectionsProvider", "Unsupported content type requested.");
			throw e;
		}   
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// check the DB is loaded
		LoadDB();

		String DATABASE_TABLE;

		switch (uriMatcher.match(uri)){
		case COLLECTIONS:
			DATABASE_TABLE="COLLECTIONS";
			break;
		default:
			return null;
		}

		long rowID = DB.insert(
				DATABASE_TABLE, "", values);

		// check if added
		if (rowID>0)
		{
			Uri _uri = ContentUris.withAppendedId(COLLECTION_URI, rowID);
			getContext().getContentResolver().notifyChange(_uri, null);    
			Log.i("com.martineve.mendroid.data.MendeleyCollectionsProvider", "Inserted row [" + values + "] into " + uri + ".");
			return _uri;                
		}        
		
		SQLException e = new SQLException("Failed to insert row into " + uri);
		Log.e("com.martineve.mendroid.data.MendeleyCollectionsProvider", "Failed to insert row [" + values + "] into Collections table.", e);

		throw e;
	}

	@Override
	public boolean onCreate() {
		// invoke the SQLLite helper on a background thread
		Context context = getContext();
		DBFetcher = new GetWritableDatabaseTask();
		DBFetcher.execute(new MendeleyDatabase(context));
		return true;	        	
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// check the DB is loaded
		LoadDB();

		String DATABASE_TABLE;

		switch (uriMatcher.match(uri)){
		//---get all collections---
		case COLLECTIONS:
			DATABASE_TABLE="COLLECTIONS";
			break;
		default:
			return null;
		}

		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(DATABASE_TABLE);

		/*if (uriMatcher.match(uri) == BOOK_ID)
	         //---if getting a particular book---
	         sqlBuilder.appendWhere(
	            _ID + " = " + uri.getPathSegments().get(1));*/                

		/*if (sortOrder==null || sortOrder=="")
	         sortOrder = TITLE;*/

		Cursor c = sqlBuilder.query(
				DB, 
				projection, 
				selection, 
				selectionArgs, 
				null, 
				null, 
				sortOrder);

		//---register to watch a content URI for changes---
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
