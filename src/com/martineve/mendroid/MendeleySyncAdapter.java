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

package com.martineve.mendroid;

import java.io.IOException;

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
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


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
				String accessToken = arg0.getResult().getString(AccountManager.KEY_AUTHTOKEN);
				AccountManager am = AccountManager.get(a_app);
				am.invalidateAuthToken("com.martineve.mendroid.account", accessToken);
				String s = "thecallback";
				s = "123";
				String y = s;
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
		mContentResolver = context.getContentResolver();
		Log.i(TAG, "performSync: " + account.toString());

		//This is where the magic will happen!		a
		MendeleyConnector m_connect = new MendeleyConnector(app);
		
		AccountManager am = AccountManager.get(app);
		
		am.invalidateAuthToken("com.martineve.mendroid.account", "77ba1bf8078fb955bebea33d80c5428804d24b693/19d2753c2f9e42ea91e73c04e59e43db");
		
		AccountManagerCB AMC = new AccountManagerCB(app); 
		
		am.getAuthToken(account, "com.martineve.mendroid.account", true, AMC, null);

	}
	


}
