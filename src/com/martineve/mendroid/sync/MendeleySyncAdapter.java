/*
 *  Mendroid: a Mendeley Android client
 *  Copyright 2011 Martin Paul Eve <martin@martineve.com>
 *
 *  This file is part of Mendroid.
 *
 *  Mendroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *   
 *  Mendroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Mendroid.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package com.martineve.mendroid.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Application;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.martineve.mendroid.common.MendeleyURLs;
import com.martineve.mendroid.data.MendeleyCollectionsProvider;
import com.martineve.mendroid.data.MendeleyDatabase;
import com.martineve.mendroid.task.MendeleyAPITask;
import com.martineve.mendroid.util.MendeleyConnector;


public class MendeleySyncAdapter extends Service {

	private static final String TAG = "com.martineve.mendroid.sync.MendeleySyncAdapter";
	private static SyncAdapterImpl sSyncAdapter = null;
	private static ContentResolver mContentResolver = null;

	public MendeleySyncAdapter() {
		super();
	}

	private class SyncAdapterImpl extends AbstractThreadedSyncAdapter {

		private Context mContext;

		public SyncAdapterImpl(Context context) {
			super(context, true);
			mContext = context;
			android.os.Debug.waitForDebugger();
		}

		@Override
		public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
			try {
				MendeleySyncAdapter.performSync(mContext, getApplication(), account, extras, authority, provider, syncResult);
			} catch (OperationCanceledException e) {
			}

		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		IBinder ret = null;
		ret = getSyncAdapter().getSyncAdapterBinder();
		return ret;
	}

	private SyncAdapterImpl getSyncAdapter() {
		if (sSyncAdapter == null)
			sSyncAdapter = new SyncAdapterImpl(this);
		return sSyncAdapter;

	}
	
	public static class AccountManagerCB implements AccountManagerCallback<Bundle>
	{
		private Application a_app;
		
		public AccountManagerCB(Application app)
		{
			a_app = app;
		}
			

		@Override
		public void run(AccountManagerFuture<Bundle> arg0) {
			try {			
				Log.i(TAG, "Received access token callback.");
				
				// this is where the accesstoken callback lands
				String accessToken = arg0.getResult().getString(AccountManager.KEY_AUTHTOKEN);
				
				Log.i(TAG, "Parsing access token.");
				String[] aTSplit = accessToken.split("/");
				
				// now go through each of the sync items
				MendeleyConnector m_connect = new MendeleyConnector(a_app, aTSplit[0], aTSplit[1]);
				MendeleyAPITask apit = new MendeleyAPITask(m_connect);
				
				// COLLECTIONS
				
				// TODO: firstly, iterate over all database columns with sync_up = true and add to server
				// TODO: make this run on temporary tables
				// TODO: the sync functionality needs to support resume; risk of interruption is too high
				
				Log.i(TAG, "Asking API for collections.");
				apit.execute(new String[] {MendeleyURLs.getURL(MendeleyURLs.COLLECTIONS)}, a_app);
				try {
					Object o = apit.get()[0];
					JSONArray collections = (JSONArray)o;
					
					int collection_count = collections.length();
					
					// delete all collections
					mContentResolver.delete(MendeleyCollectionsProvider.COLLECTIONS_URI, null, null);
					
					// now re-add
					for(int i = 0; i < collection_count; i++)
					{					
						
						JSONObject newCollection = collections.getJSONObject(i);
						int collection_id = newCollection.getInt("id");
						String name = newCollection.getString("name");
						String type = newCollection.getString("type");
						int size = newCollection.getInt("size");
						
						MendeleyDatabase.insertCollection(collection_id, name, type, size, false, mContentResolver);
						
						// get all collection items so that all related data can be deleted
						mContentResolver.query(MendeleyCollectionsProvider.COLLECTION_DOCUMENTS_URI, null, "collection_id=? ", new String[] {Integer.toString(collection_id)}, "");
						
						// delete all data associated with this document
						mContentResolver.delete(MendeleyCollectionsProvider.COLLECTION_DOCUMENTS_URI, "collection_id=?", new String[] { Integer.toString(collection_id) });
						
						// request all documents from this collection
						apit = new MendeleyAPITask(m_connect);
						
						Log.i(TAG, "Asking API for all documents in collection " + Integer.toString(collection_id)+ ".");
						apit.execute(new String[] {MendeleyURLs.getURL(MendeleyURLs.COLLECTION_DOCUMENTS + Integer.toString(collection_id) + "/", "items=999999")}, a_app);
						
						o = apit.get()[0];
						
						Log.v(TAG, "Parsing JSONArray from API response (CollectionDocuments)");
						JSONArray CollectionDocuments = (JSONArray)o;
						
						Log.v(TAG, "Parsing JSONObject from CollectionDocuments");
						JSONObject newCollectionDocuments = CollectionDocuments.getJSONObject(0);
						
						// we need the "document_ids" parameter
						Log.v(TAG, "Parsing JSONArray from document_ids parameter");
						JSONArray documentIDs = newCollectionDocuments.getJSONArray("document_ids");
						
						// one more array to parse
						int documents_count = documentIDs.length();
						
						// now loop over documents
						for(int d = 0; d < documents_count; d++)
						{
							String document_id = documentIDs.getString(d);
							
							// now retrieve the document info from the Mendeley server
							// TODO: add flag to toggle whether we update if it exists already
							
							Log.i(TAG, "Asking API for document " + document_id + ".");
							
							apit = new MendeleyAPITask(m_connect);
							apit.execute(new String[] {MendeleyURLs.getURL(MendeleyURLs.DOCUMENT + document_id + "/")}, a_app);
							
							o = apit.get()[0];
							
							Log.v(TAG, "Parsing JSONArray from API response (document)");
							
							JSONArray document = (JSONArray)o;
							
							Log.v(TAG, "Parsing JSONObject from document");
							
							JSONObject documentInfo = document.getJSONObject(0);
							
							// we now have all the information needed to construct a basic document
							// insert query; authors and so forth go in different tables
							
							String document_title = documentInfo.getString("title");
							String document_type = documentInfo.getString("type");
							
							Uri documentUri = MendeleyDatabase.insertDocument(Long.parseLong(document_id), document_title, document_type, collection_id, false, mContentResolver);
							
							JSONArray authors = documentInfo.getJSONArray("authors");
							
							for(int a = 0; a < authors.length(); a++)
							{
								// now insert the authors
								Uri authorUri = MendeleyDatabase.insertOrGetAuthor(authors.getString(a), false, mContentResolver);
								
								// insert an author-document link
								MendeleyDatabase.linkDocumentAndAuthor(authorUri, documentUri, false, mContentResolver);
							}
							
						}
					}
					
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} catch (OperationCanceledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	private static void performSync(Context context, Application app, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)

	throws OperationCanceledException {
		
		Log.i("com.martineve.mendroid.MendeleyForAndroid", "Performing sync for account: " + account.toString());
		
		mContentResolver = context.getContentResolver();
		
		AccountManager am = AccountManager.get(app);
		
		AccountManagerCB AMC = new AccountManagerCB(app); 
		
		Log.i("com.martineve.mendroid.sync.MendeleyForAndroid", "Retrieving auth token.");
		am.getAuthToken(account, "com.martineve.mendroid.account", true, AMC, null);
	}
		


}
