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

	private static String TAG = "com.martineve.mendroid.data.MendeleyCollectionsProvider";
	
	private static String PROVIDER_NAME = "com.martineve.mendroid.data.mendeleycollectionsprovider";

	public static final Uri AUTHOR_URI = Uri.parse("content://" + PROVIDER_NAME + "/author");
	public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/");
	public static final Uri COLLECTIONS_URI = Uri.parse("content://" + PROVIDER_NAME + "/collections");
	public static final Uri COLLECTION_URI = Uri.parse("content://" + PROVIDER_NAME + "/collection");
	public static final Uri DOCUMENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/document");
	public static final Uri COLLECTION_DOCUMENTS_URI = Uri.parse("content://" + PROVIDER_NAME + "/collection/documents");
	public static final Uri COLLECTION_AUTHORS_URI = Uri.parse("content://" + PROVIDER_NAME + "/collection/authors");
	public static final Uri AUTHOR_TO_DOCUMENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/document/author");
	public static final Uri COLLECTION_AUTHOR_DOCUMENTS_URI = Uri.parse("content://" + PROVIDER_NAME + "/collection/author");
	
	public static final Uri TEMP_AUTHOR_URI = Uri.parse("content://" + PROVIDER_NAME + "/tempauthor");
	public static final Uri TEMP_COLLECTIONS_URI = Uri.parse("content://" + PROVIDER_NAME + "/tempcollections");
	public static final Uri TEMP_COLLECTION_URI = Uri.parse("content://" + PROVIDER_NAME + "/tempcollection");
	public static final Uri TEMP_DOCUMENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/tempdocument");
	public static final Uri TEMP_COLLECTION_DOCUMENTS_URI = Uri.parse("content://" + PROVIDER_NAME + "/tempcollection/documents");
	public static final Uri TEMP_COLLECTION_AUTHORS_URI = Uri.parse("content://" + PROVIDER_NAME + "/tempcollection/authors");
	public static final Uri TEMP_AUTHOR_TO_DOCUMENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/tempdocument/author");
	public static final Uri TEMP_COLLECTION_AUTHOR_DOCUMENTS_URI = Uri.parse("content://" + PROVIDER_NAME + "/tempcollection/author");
	
	public static final Uri MOVE_URI = Uri.parse("content://" + PROVIDER_NAME + "/movetemptables");

	private static final int COLLECTIONS = 1;
	private static final int COLLECTION = 2;
	private static final int DOCUMENTS = 3;
	private static final int DOCUMENT = 4;
	private static final int AUTHOR = 5;
	private static final int AUTHOR_ID = 5;
	private static final int AUTHOR_TO_DOCUMENT = 6;
	private static final int AUTHORS_IN_COLLECTION = 7;
	private static final int DOCUMENTS_IN_AUTHORS_IN_COLLECTION = 8;
	private static final int MOVE = 18;
	
	private static final int TEMP_COLLECTIONS = 9;
	private static final int TEMP_COLLECTION = 10;
	private static final int TEMP_DOCUMENTS = 11;
	private static final int TEMP_DOCUMENT = 12;
	private static final int TEMP_AUTHOR = 13;
	private static final int TEMP_AUTHOR_ID = 14;
	private static final int TEMP_AUTHOR_TO_DOCUMENT = 15;
	private static final int TEMP_AUTHORS_IN_COLLECTION = 16;
	private static final int TEMP_DOCUMENTS_IN_AUTHORS_IN_COLLECTION = 17;

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
		uriMatcher.addURI(PROVIDER_NAME, "movetemptables", MOVE);
		uriMatcher.addURI(PROVIDER_NAME, "collections", COLLECTIONS);
		uriMatcher.addURI(PROVIDER_NAME, "collection/#", COLLECTION);
		uriMatcher.addURI(PROVIDER_NAME, "collection/documents", DOCUMENTS);
		uriMatcher.addURI(PROVIDER_NAME, "document/#", DOCUMENT);
		uriMatcher.addURI(PROVIDER_NAME, "author", AUTHOR);
		uriMatcher.addURI(PROVIDER_NAME, "author/*", AUTHOR_ID);
		uriMatcher.addURI(PROVIDER_NAME, "document/author", AUTHOR_TO_DOCUMENT);
		uriMatcher.addURI(PROVIDER_NAME, "collection/authors/#", AUTHORS_IN_COLLECTION);
		uriMatcher.addURI(PROVIDER_NAME, "collection/#/author/#", DOCUMENTS_IN_AUTHORS_IN_COLLECTION);
		
		// temp table entries used by the sync procedure
		uriMatcher.addURI(PROVIDER_NAME, "tempcollections", TEMP_COLLECTIONS);
		uriMatcher.addURI(PROVIDER_NAME, "tempcollection/#", TEMP_COLLECTION);
		uriMatcher.addURI(PROVIDER_NAME, "tempcollection/documents", TEMP_DOCUMENTS);
		uriMatcher.addURI(PROVIDER_NAME, "tempdocument/#", TEMP_DOCUMENT);
		uriMatcher.addURI(PROVIDER_NAME, "tempauthor", TEMP_AUTHOR);
		uriMatcher.addURI(PROVIDER_NAME, "tempauthor/*", TEMP_AUTHOR_ID);
		uriMatcher.addURI(PROVIDER_NAME, "tempdocument/author", TEMP_AUTHOR_TO_DOCUMENT);
		uriMatcher.addURI(PROVIDER_NAME, "tempcollection/authors/#", TEMP_AUTHORS_IN_COLLECTION);
		uriMatcher.addURI(PROVIDER_NAME, "tempcollection/#/author/#", TEMP_DOCUMENTS_IN_AUTHORS_IN_COLLECTION);
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (uriMatcher.match(uri)){
		case MOVE:
			// delete the main tables
			// rename temp tables to main tables
			// recreate temp tables
			
			Log.w(TAG, "Deleting main tables.");
			DB.execSQL("DROP TABLE IF EXISTS collections");
			DB.execSQL("DROP TABLE IF EXISTS documents");
			DB.execSQL("DROP TABLE IF EXISTS authors");
			DB.execSQL("DROP TABLE IF EXISTS documenttoauthors");
			
			Log.w(TAG, "Renaming temp tables.");
			DB.execSQL("ALTER TABLE collections_TEMP RENAME TO collections");
			DB.execSQL("ALTER TABLE documents_TEMP RENAME TO documents");
			DB.execSQL("ALTER TABLE authors_TEMP RENAME TO authors");
			DB.execSQL("ALTER TABLE documenttoauthors_TEMP RENAME TO documenttoauthors");
			
			Log.w(TAG, "Recreating temp tables.");
			MendeleyDatabase.createTempTables(DB);
			return 1;
		case COLLECTIONS:
			// delete all collections
			Log.i(TAG, "Deleting collections table.");
			DB.delete("collections", selection, selectionArgs);
			return 1;
		case DOCUMENTS:
			// delete all documents from a collection
			Log.i(TAG, "Deleting documents.");
			DB.delete("documents", selection, selectionArgs);
			return 1;
		case AUTHOR:
			// delete all collections
			Log.i(TAG, "Deleting authors.");
			DB.delete("authors", selection, selectionArgs);
			return 1;
		case AUTHOR_TO_DOCUMENT:
			// delete all documents from a collection
			Log.i(TAG, "Deleting documents where collection is " + selectionArgs[0] + ".");
			DB.delete("documenttoauthors", selection, selectionArgs);
			return 1;
		case TEMP_COLLECTIONS:
			// delete all collections
			Log.i(TAG, "Deleting temporary collections.");
			DB.delete("collections_temp", selection, selectionArgs);
			return 1;
		case TEMP_DOCUMENTS:
			// delete all documents from a collection
			Log.i(TAG, "Deleting temporary documents.");
			DB.delete("documents_temp", selection, selectionArgs);
			return 1;
		case TEMP_AUTHOR:
			// delete all collections
			Log.i(TAG, "Deleting temporary authors.");
			DB.delete("authors_temp", selection, selectionArgs);
			return 1;
		case TEMP_AUTHOR_TO_DOCUMENT:
			// delete all documents from a collection
			Log.i(TAG, "Deleting temporary document links.");
			DB.delete("documenttoauthors_temp", selection, selectionArgs);
			return 1;
		default:
			IllegalArgumentException e = new IllegalArgumentException("Unsupported URI: " + uri);
			Log.e(TAG, "Unsupported content type requested.");
			throw e;
		} 
	}

	//  
	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)){
		case COLLECTIONS:
			Log.i(TAG, "Returning content type of mendroid.collections.");
			return "vnd.android.cursor.dir/vnd.martineve.mendroid.collections";
		case AUTHOR_ID:
			Log.i(TAG, "Returning content type of mendroid.author.");
			return "vnd.android.cursor.item/vnd.martineve.mendroid.author";
		case AUTHORS_IN_COLLECTION:
			Log.i(TAG, "Returning content type of mendroid.authors.");
			return "vnd.android.cursor.dir/vnd.martineve.mendroid.authors";
		case DOCUMENTS_IN_AUTHORS_IN_COLLECTION:
			Log.i(TAG, "Returning content type of mendroid.documents.");
			return "vnd.android.cursor.dir/vnd.martineve.mendroid.documents";
		case TEMP_COLLECTIONS:
			Log.i(TAG, "Returning content type of mendroid.collections.");
			return "vnd.android.cursor.dir/vnd.martineve.mendroid.collections";
		case TEMP_AUTHOR_ID:
			Log.i(TAG, "Returning content type of mendroid.author.");
			return "vnd.android.cursor.item/vnd.martineve.mendroid.author";
		case TEMP_AUTHORS_IN_COLLECTION:
			Log.i(TAG, "Returning content type of mendroid.authors.");
			return "vnd.android.cursor.dir/vnd.martineve.mendroid.authors";
		case TEMP_DOCUMENTS_IN_AUTHORS_IN_COLLECTION:
			Log.i(TAG, "Returning content type of mendroid.documents.");
			return "vnd.android.cursor.dir/vnd.martineve.mendroid.documents";
		default:
			IllegalArgumentException e = new IllegalArgumentException("Unsupported URI: " + uri);
			Log.e(TAG, "Unsupported content type requested.");
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
		case DOCUMENTS:
			DATABASE_TABLE="DOCUMENTS";
			break;
		case AUTHOR:
			DATABASE_TABLE="AUTHORS";
			break;
		case AUTHOR_TO_DOCUMENT:
			DATABASE_TABLE="DOCUMENTTOAUTHORS";
			break;
		case TEMP_COLLECTIONS:
			DATABASE_TABLE="COLLECTIONS_TEMP";
			break;
		case TEMP_DOCUMENTS:
			DATABASE_TABLE="DOCUMENTS_TEMP";
			break;
		case TEMP_AUTHOR:
			DATABASE_TABLE="AUTHORS_TEMP";
			break;
		case TEMP_AUTHOR_TO_DOCUMENT:
			DATABASE_TABLE="DOCUMENTTOAUTHORS_TEMP";
			break;
		default:
			return null;
		}

		long rowID = DB.insert(
				DATABASE_TABLE, "", values);

		// check if added
		if (rowID>0)
		{
			Uri _uri;
			switch (uriMatcher.match(uri)){
			case COLLECTIONS:
				_uri = ContentUris.withAppendedId(COLLECTION_URI, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);    
				Log.i(TAG, "Inserted row [" + values + "] into " + uri + ".");
				return _uri;
			case DOCUMENTS:
				_uri = ContentUris.withAppendedId(DOCUMENT_URI, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);    
				Log.i(TAG, "Inserted row [" + values + "] into " + uri + ".");
				return _uri;
			case AUTHOR:
				_uri = ContentUris.withAppendedId(AUTHOR_URI, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);    
				Log.i(TAG, "Inserted row [" + values + "] into " + uri + ".");
				return _uri;
			case AUTHOR_TO_DOCUMENT:
				_uri = ContentUris.withAppendedId(AUTHOR_TO_DOCUMENT_URI, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);    
				Log.i(TAG, "Inserted row [" + values + "] into " + uri + ".");
				return _uri;
			case TEMP_COLLECTIONS:
				_uri = ContentUris.withAppendedId(TEMP_COLLECTION_URI, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);    
				Log.i(TAG, "Inserted row [" + values + "] into " + uri + ".");
				return _uri;
			case TEMP_DOCUMENTS:
				_uri = ContentUris.withAppendedId(TEMP_DOCUMENT_URI, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);    
				Log.i(TAG, "Inserted row [" + values + "] into " + uri + ".");
				return _uri;
			case TEMP_AUTHOR:
				_uri = ContentUris.withAppendedId(TEMP_AUTHOR_URI, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);    
				Log.i(TAG, "Inserted row [" + values + "] into " + uri + ".");
				return _uri;
			case TEMP_AUTHOR_TO_DOCUMENT:
				_uri = ContentUris.withAppendedId(TEMP_AUTHOR_TO_DOCUMENT_URI, rowID);
				getContext().getContentResolver().notifyChange(_uri, null);    
				Log.i(TAG, "Inserted row [" + values + "] into " + uri + ".");
				return _uri;
			default:
				return null;
			}
		}        
		
		SQLException e = new SQLException("Failed to insert row into " + uri);
		Log.e(TAG, "Failed to insert row [" + values + "] into Collections table.", e);

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
		
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

		switch (uriMatcher.match(uri)){
		case COLLECTIONS:
			DATABASE_TABLE="COLLECTIONS";
			break;
		case AUTHOR_ID:
			DATABASE_TABLE="AUTHORS";
	        sqlBuilder.appendWhere(MendeleyDatabase.AUTHOR_NAME + " = "); 
			sqlBuilder.appendWhereEscapeString(uri.getPathSegments().get(1));
			break;
		case DOCUMENT:
			DATABASE_TABLE="DOCUMENTS";
	        sqlBuilder.appendWhere(MendeleyDatabase._ID + " = "); 
			sqlBuilder.appendWhere(Long.toString(Long.parseLong(uri.getPathSegments().get(1))));
			break;
		case AUTHORS_IN_COLLECTION:
			/*
			 * SELECT DISTINCT author_name
			 * FROM authors
			 * WHERE _id IN 
			 * (SELECT DISTINCT author_id FROM documenttoauthors WHERE document_id IN 
			 * (SELECT _id FROM documents WHERE collection_id = ?))
			 */
			
			//SELECT DISTINCT author_name
			sqlBuilder.setDistinct(false);
			
			// FROM authors
			DATABASE_TABLE="AUTHORS";
			
			// WHERE _id IN 
	        sqlBuilder.appendWhere(MendeleyDatabase._ID 
	    	        // (SELECT DISTINCT author_id FROM documenttoauthors WHERE document_id IN 
	        		+ " IN (SELECT " + MendeleyDatabase.DOCUMENT_TO_AUTHORS_AUTHOR_ID + " FROM documenttoauthors WHERE "
	        		+ MendeleyDatabase.DOCUMENT_TO_AUTHORS_DOCUMENT_ID + " IN "
	        		// (SELECT _id FROM documents WHERE collection_id = ?))
	        		+ "(SELECT " + MendeleyDatabase._ID + " FROM documents WHERE collection_id = "
	        		// note: long cast gives additional injection protection at little extra cost
	        		+ Long.toString(Long.parseLong(uri.getPathSegments().get(2)))
	        		+ "))");
			
			break;
		case DOCUMENTS_IN_AUTHORS_IN_COLLECTION:
			 /*
			 * SELECT *
			 * FROM documents
			 * WHERE collection_id = ? AND _id IN 
			 * (SELECT DISTINCT document_id FROM documenttoauthors WHERE author_id = ?)
			 */
			
			//SELECT DISTINCT author_name
			sqlBuilder.setDistinct(false);
			
			// FROM documents
			DATABASE_TABLE="DOCUMENTS";
			
			// WHERE collection_id = ? AND _id IN 
	        sqlBuilder.appendWhere(MendeleyDatabase.DOCUMENT_COLLECTION_ID + " = "
	        		+ Long.toString(Long.parseLong(uri.getPathSegments().get(1))) + " AND " + MendeleyDatabase._ID 
	    	        // (SELECT document_id FROM documenttoauthors WHERE author_id = ?) 
	        		+ " IN (SELECT " + MendeleyDatabase.DOCUMENT_TO_AUTHORS_DOCUMENT_ID + " FROM documenttoauthors WHERE "
	        		// author_id = ?)
	        		+ MendeleyDatabase.DOCUMENT_TO_AUTHORS_AUTHOR_ID + " = "
	        		// note: long cast gives additional injection protection at little extra cost
	        		+ Long.toString(Long.parseLong(uri.getPathSegments().get(3)))
	        		+ ")");
			break;
			
		case TEMP_COLLECTIONS:
			DATABASE_TABLE="COLLECTIONS_TEMP";
			break;
		case TEMP_AUTHOR_ID:
			DATABASE_TABLE="AUTHORS_TEMP";
	        sqlBuilder.appendWhere(MendeleyDatabase.AUTHOR_NAME + " = "); 
			sqlBuilder.appendWhereEscapeString(uri.getPathSegments().get(1));
			break;
		case TEMP_DOCUMENT:
			DATABASE_TABLE="DOCUMENTS_TEMP";
	        sqlBuilder.appendWhere(MendeleyDatabase._ID + " = "); 
			sqlBuilder.appendWhere(Long.toString(Long.parseLong(uri.getPathSegments().get(1))));
			break;
		case TEMP_AUTHORS_IN_COLLECTION:
			/*
			 * SELECT DISTINCT author_name
			 * FROM authors
			 * WHERE _id IN 
			 * (SELECT DISTINCT author_id FROM documenttoauthors WHERE document_id IN 
			 * (SELECT _id FROM documents WHERE collection_id = ?))
			 */
			
			//SELECT DISTINCT author_name
			sqlBuilder.setDistinct(false);
			
			// FROM authors
			DATABASE_TABLE="AUTHORS_TEMP";
			
			// WHERE _id IN 
	        sqlBuilder.appendWhere(MendeleyDatabase._ID 
	    	        // (SELECT DISTINCT author_id FROM documenttoauthors WHERE document_id IN 
	        		+ " IN (SELECT " + MendeleyDatabase.DOCUMENT_TO_AUTHORS_AUTHOR_ID + " FROM documenttoauthors_TEMP WHERE "
	        		+ MendeleyDatabase.DOCUMENT_TO_AUTHORS_DOCUMENT_ID + " IN "
	        		// (SELECT _id FROM documents WHERE collection_id = ?))
	        		+ "(SELECT " + MendeleyDatabase._ID + " FROM documents_TEMP WHERE collection_id = "
	        		// note: long cast gives additional injection protection at little extra cost
	        		+ Long.toString(Long.parseLong(uri.getPathSegments().get(2)))
	        		+ "))");
			
			break;
		case TEMP_DOCUMENTS_IN_AUTHORS_IN_COLLECTION:
			 /*
			 * SELECT *
			 * FROM documents
			 * WHERE collection_id = ? AND _id IN 
			 * (SELECT DISTINCT document_id FROM documenttoauthors WHERE author_id = ?)
			 */
			
			//SELECT DISTINCT author_name
			sqlBuilder.setDistinct(false);
			
			// FROM documents
			DATABASE_TABLE="DOCUMENTS_TEMP";
			
			// WHERE collection_id = ? AND _id IN 
	        sqlBuilder.appendWhere(MendeleyDatabase.DOCUMENT_COLLECTION_ID + " = "
	        		+ Long.toString(Long.parseLong(uri.getPathSegments().get(1))) + " AND " + MendeleyDatabase._ID 
	    	        // (SELECT document_id FROM documenttoauthors WHERE author_id = ?) 
	        		+ " IN (SELECT " + MendeleyDatabase.DOCUMENT_TO_AUTHORS_DOCUMENT_ID + " FROM documenttoauthors_TEMP WHERE "
	        		// author_id = ?)
	        		+ MendeleyDatabase.DOCUMENT_TO_AUTHORS_AUTHOR_ID + " = "
	        		// note: long cast gives additional injection protection at little extra cost
	        		+ Long.toString(Long.parseLong(uri.getPathSegments().get(3)))
	        		+ ")");
			break;
		default:
			return null;
		}

		sqlBuilder.setTables(DATABASE_TABLE);


		Cursor c = sqlBuilder.query(
				DB, 
				projection, 
				selection, 
				selectionArgs, 
				null, 
				null, 
				sortOrder);

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
