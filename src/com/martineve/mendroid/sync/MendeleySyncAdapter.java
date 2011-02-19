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

import com.martineve.mendroid.R;
import com.martineve.mendroid.activity.CreateMendeleyAccount;
import com.martineve.mendroid.activity.SyncQuitMenu;
import com.martineve.mendroid.common.MendeleyURLs;
import com.martineve.mendroid.data.MendeleyCollectionsProvider;
import com.martineve.mendroid.data.MendeleyDatabase;
import com.martineve.mendroid.task.MendeleyAPITask;
import com.martineve.mendroid.util.MendeleyConnector;


public class MendeleySyncAdapter extends Service {

	private static final String TAG = "com.martineve.mendroid.sync.MendeleySyncAdapter";
	private static SyncAdapterImpl sSyncAdapter = null;
	private static ContentResolver mContentResolver = null;
	public static int failCount = 0;
	
	public static JSONArray preFetchedArray = null;
	public static int resumePosition = 0;

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
				
				if (accessToken == null)
				{
					if (MendeleySyncAdapter.failCount == 1)
					{
						// abort, it's got locked in an oAuth failure cycle
						Log.i(TAG, "OAuth failure cycle detected; aborting sync.");
						MendeleySyncAdapter.failCount = 0;
						return;
					}
					
					Log.i(TAG, "Requesting authorization for an oAuth token.");
					
					MendeleySyncAdapter.failCount = 1;
					
					mContentResolver = a_app.getContentResolver();
					
					AccountManager am = AccountManager.get(a_app);
					
					am.invalidateAuthToken(a_app.getString(R.string.ACCOUNT_TYPE), null);
					
					AccountManagerCB AMC = new AccountManagerCB(a_app); 
					
					Account[] a = am.getAccountsByType(a_app.getString(R.string.ACCOUNT_TYPE));

					if(a.length == 0)
					{
						Log.e(TAG, "No Mendeley accounts found while in sync procedure.");
					} else {
						// start a sync request
						Log.i("com.martineve.mendroid.sync.MendeleyForAndroid", "Retrieving auth token.");
						am.getAuthToken(a[0], "com.martineve.mendroid.account", true, AMC, null);
					}
					
					return;
				}
				
				MendeleySyncAdapter.failCount = 0;
				
				Log.i(TAG, "Parsing access token.");
				String[] aTSplit = accessToken.split("/");
				
				// now go through each of the sync items
				MendeleyConnector m_connect = new MendeleyConnector(a_app, aTSplit[0], aTSplit[1]);
				MendeleyAPITask apit = new MendeleyAPITask(m_connect);
				
				// COLLECTIONS
				
				// TODO: firstly, iterate over all database columns with sync_up = true and add to server
				
				try {
					
					Object o = null;
					
					// check if we are mid-collection
					if (preFetchedArray == null)
					{
						Log.i(TAG, "Asking API for collections.");
						apit.execute(new String[] {MendeleyURLs.getURL(MendeleyURLs.COLLECTIONS)}, a_app);
						
						// delete everything from all temp tables
						mContentResolver.delete(MendeleyCollectionsProvider.TEMP_COLLECTIONS_URI, null, null);
						mContentResolver.delete(MendeleyCollectionsProvider.TEMP_COLLECTION_DOCUMENTS_URI, null, null);
						mContentResolver.delete(MendeleyCollectionsProvider.TEMP_AUTHOR_URI, null, null);
						mContentResolver.delete(MendeleyCollectionsProvider.TEMP_AUTHOR_TO_DOCUMENT_URI, null, null);
						
						o = apit.get()[0];
					}
					else
					{
						Log.i(TAG, "Resuming sync from position " + Integer.toString(resumePosition) + ".");
						o = preFetchedArray;
					}
					
					JSONArray collections = (JSONArray)o;
					
					int collection_count = collections.length();
					
					// now re-add
					for(int i = resumePosition; i < collection_count; i++)
					{					
						
						Log.i(TAG, "Syncing item at position " + Integer.toString(i) + ".");
						
						JSONObject newCollection = collections.getJSONObject(i);
						int collection_id = newCollection.getInt("id");
						String name = newCollection.getString("name");
						String type = newCollection.getString("type");
						int size = newCollection.getInt("size");
						
						MendeleyDatabase.insertCollection(collection_id, name, type, size, false, true, mContentResolver);
						
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
							
							String document_title = "";
							try { document_title = documentInfo.getString("title"); } 
								catch (Exception e) { Log.e(TAG, "Error getting document title."); }
							
							String document_type = "";
							try { document_type = documentInfo.getString("type"); } 
								catch (Exception e) { Log.e(TAG, "Error getting document type."); }
							
								
							String document_year = "";
							try { document_year = documentInfo.getString("year"); } 
								catch (Exception e) { Log.e(TAG, "Error getting document year."); }
							
							String document_abstract = "";
							try { document_abstract = documentInfo.getString("abstract"); } 
								catch (Exception e) { Log.e(TAG, "Error getting document year."); }
							
							Uri documentUri = MendeleyDatabase.insertOrGetDocument(Long.parseLong(document_id), document_title, document_type, document_year, document_abstract, collection_id, false, true, mContentResolver);
							
							JSONArray authors = documentInfo.getJSONArray("authors");
							
							for(int a = 0; a < authors.length(); a++)
							{
								// now insert the authors
								Uri authorUri = MendeleyDatabase.insertOrGetAuthor(authors.getString(a), false, true, mContentResolver);
								
								// insert an author-document link
								MendeleyDatabase.linkDocumentAndAuthor(authorUri, documentUri, false, true, mContentResolver);
							}
							
							resumePosition = i;
							
						}
					}
					
					// end of the collections loop
					// transfer the contents of the temporary tables to the main tables

					// MOVE_URI is a special call that triggers this move from
					// temp tables to main on delete
					mContentResolver.delete(MendeleyCollectionsProvider.MOVE_URI, null, null);
					
					// reset variables that control resume functionality
					resumePosition = 0;
					preFetchedArray = null;
					
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
		
		Log.i(TAG, "Performing sync for account: " + account.toString());
		
		mContentResolver = context.getContentResolver();
		
		AccountManager am = AccountManager.get(app);
		
		AccountManagerCB AMC = new AccountManagerCB(app); 
		
		Log.i(TAG, "Retrieving auth token.");
		am.getAuthToken(account, "com.martineve.mendroid.account", true, AMC, null);
	}
		


}
