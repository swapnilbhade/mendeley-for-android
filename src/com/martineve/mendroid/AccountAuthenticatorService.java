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


import android.accounts.AbstractAccountAuthenticator;

import android.accounts.Account;

import android.accounts.AccountAuthenticatorResponse;

import android.accounts.AccountManager;

import android.accounts.NetworkErrorException;

import android.app.ProgressDialog;
import android.app.Service;

import android.content.Context;

import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;

import android.os.IBinder;

import android.util.Log;
import android.widget.EditText;



/**

 * Authenticator service that returns a subclass of AbstractAccountAuthenticator in onBind()

 */

public class AccountAuthenticatorService extends Service {

	private static final String TAG = "AccountAuthenticatorService";

	private static AccountAuthenticatorImpl sAccountAuthenticator = null;

	public AccountAuthenticatorService() {
		super();
	}

	public IBinder onBind(Intent intent) {
		IBinder ret = null;
		if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT))
			ret = getAuthenticator().getIBinder();
		return ret;
	}

	private AccountAuthenticatorImpl getAuthenticator() {
		if (sAccountAuthenticator == null)
			sAccountAuthenticator = new AccountAuthenticatorImpl(this);
		return sAccountAuthenticator;
	}

	private static class AccountAuthenticatorImpl extends AbstractAccountAuthenticator {
		private Context mContext;

		public AccountAuthenticatorImpl(Context context) {
			super(context);
			mContext = context;
		}

		/*
		 *  The user has requested to add a new account to the system.  We return an intent that will launch our login screen if the user has not logged in yet,
		 *  otherwise our activity will just pass the user's credentials on to the account manager.
		 */

		@Override

		public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)

		throws NetworkErrorException {
			Bundle reply = new Bundle();

			Intent i = new Intent(mContext, MendeleyDroidLogin.class);
			i.setAction("com.martineve.mendroid.MendeleyDroidLogin");
			
			i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
			reply.putParcelable(AccountManager.KEY_INTENT, i);
			
			return reply;
		}

		@Override
		public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
			Bundle reply = new Bundle();

			Intent i = new Intent(mContext, MendeleyDroidLogin.class);
			i.setAction("com.martineve.mendroid.MendeleyDroidLogin");
			
			i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
			reply.putParcelable(AccountManager.KEY_INTENT, i);
			
			return reply;
		}

		@Override
		public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
			Bundle reply = new Bundle();

			Intent i = new Intent(mContext, MendeleyDroidLogin.class);
			i.setAction("com.martineve.mendroid.MendeleyDroidLogin");
			
			i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
			reply.putParcelable(AccountManager.KEY_INTENT, i);
			
			return reply;
		}

		@Override
		public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
			// TODO: this should attempt to refresh credentials without asking user, if fails, it should prompt
			AccountManager am = AccountManager.get(mContext);
			String user = account.name;

			Bundle result = new Bundle();
			Intent i = new Intent(mContext, MendeleyLoginCallback.class);
			i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
			i.putExtra("user", user);
			result.putParcelable(AccountManager.KEY_INTENT, i);
			return result;
		}


		@Override
		public String getAuthTokenLabel(String authTokenType) {
			return "Mendeley";
		}

		@Override
		public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
			Bundle reply = new Bundle();

			Intent i = new Intent(mContext, MendeleyDroidLogin.class);
			i.setAction("com.martineve.mendroid.MendeleyDroidLogin");
			
			i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
			reply.putParcelable(AccountManager.KEY_INTENT, i);
			
			return reply;
		}

		@Override
		public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
			Bundle reply = new Bundle();

			Intent i = new Intent(mContext, MendeleyDroidLogin.class);
			i.setAction("com.martineve.mendroid.MendeleyDroidLogin");
			
			i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
			reply.putParcelable(AccountManager.KEY_INTENT, i);
			
			return reply;
		}

	}

}

