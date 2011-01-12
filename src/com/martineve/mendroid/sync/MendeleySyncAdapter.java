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
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.martineve.mendroid.common.MendeleyURLs;
import com.martineve.mendroid.data.MendeleyContentProvider;
import com.martineve.mendroid.data.MendeleyDatabase;
import com.martineve.mendroid.task.MendeleyAPITask;
import com.martineve.mendroid.util.MendeleyConnector;


public class MendeleySyncAdapter extends Service {

	private static final String TAG = "MendeleySyncAdapter";

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
				Log.i("com.martineve.mendroid.sync.MendeleySyncAdapter", "Received access token callback.");
				
				// this is where the accesstoken callback lands
				String accessToken = arg0.getResult().getString(AccountManager.KEY_AUTHTOKEN);
				
				Log.i("com.martineve.mendroid.data.MendeleySyncAdapter", "Parsing access token.");
				String[] aTSplit = accessToken.split("/");
				
				// now go through each of the sync items
				MendeleyConnector m_connect = new MendeleyConnector(a_app, aTSplit[0], aTSplit[1]);
				MendeleyAPITask apit = new MendeleyAPITask(m_connect);
				
				// COLLECTIONS
				
				// TODO: firstly, iterate over all database columns with sync_up = true and add to server
				
				Log.i("com.martineve.mendroid.sync.MendeleySyncAdapter", "Asking API for collections.");
				apit.execute(new String[] {MendeleyURLs.getURL(MendeleyURLs.COLLECTIONS)}, a_app);
				try {
					Object o = apit.get()[0];
					JSONArray collections = (JSONArray)o;
					
					int collection_count = collections.length();
					
					// delete all existing collections
					mContentResolver.delete(MendeleyContentProvider.COLLECTIONS_URI, null, null);
					
					// now re-add
					for(int i = 0; i < collection_count; i++)
					{
						JSONObject newCollection = collections.getJSONObject(i);
						int id = newCollection.getInt("id");
						String name = newCollection.getString("name");
						String type = newCollection.getString("type");
						int size = newCollection.getInt("size");
						
						MendeleyDatabase.insertCollection(id, name, type, size, false, mContentResolver);
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
