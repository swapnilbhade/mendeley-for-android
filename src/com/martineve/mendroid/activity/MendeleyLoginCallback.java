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

package com.martineve.mendroid.activity;

import java.net.URLDecoder;

import oauth.signpost.OAuthConsumer;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.martineve.mendroid.R;
import com.martineve.mendroid.common.Common;
import com.martineve.mendroid.util.MendeleyConnector;

public class MendeleyLoginCallback extends Activity {
	
	AccountAuthenticatorResponse response;
	private boolean inLogin = false;

	/** Redirects the user to Mendeley for login */
	public void doLogin()
	{
		String strAuthURL;
		try {
			Bundle extras = getIntent().getExtras(); 
			String user = extras.getString("user");
			if(extras == null || user == null)
			{
				Common.longToast("There was a problem logging in to Mendley; the Login procedure should only be called by the authenticator.", this);
				Log.e("MendeleyLoginCallback", "Either no Extras were, or no username was, passed to the Intent.");
				
				// TODO: move to fail
				return;
			}
			strAuthURL = Common.login_connector.getAuthenticationURL(extras.getString("user"));

			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(strAuthURL));

			startActivityForResult(i, 0);
		} catch (Exception e) {
			Common.longToast("Got exception while authenticating:\n" + e.getMessage(), this);
			Log.e("MendeleyLoginCallback", "An error occurred getting the authentication URL", e);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		handleLoad();
	}
	
	private void handleLoad()
	{
		android.os.Debug.waitForDebugger();
		
		// see if this onCreate was called by the Authenticator, or was passed back by the Browser		
		AccountAuthenticatorResponse response = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
		
		if(response != null && !inLogin)
		{
			// called by the authenticator
			// store the response object in a static field
			Common.response = response;
			
			inLogin = true;
			// build a Mendeley connector
			Common.login_connector = new MendeleyConnector(getApplication());
			doLogin();
		}
		else
		{
			if(response != null || Common.response == null)
			{
				// duplicate call; ignore
				return;
			}
			
			inLogin = false;
			
			// called by the browser
			// upgrade PIN to token if possible
			upgradePINIfFound();

	
			Uri uri = this.getIntent().getData();
			
			if(uri != null) {
				OAuthConsumer consumer = Common.login_connector.getConsumer();
				String accountName = uri.getQueryParameter("acc_name");
				
				Bundle result = new Bundle();
				result.putString(AccountManager.KEY_ACCOUNT_NAME, URLDecoder.decode(accountName));
				result.putString(AccountManager.KEY_ACCOUNT_TYPE, getString(R.string.ACCOUNT_TYPE));
				result.putString(AccountManager.KEY_AUTHTOKEN, consumer.getToken() + "/" + consumer.getTokenSecret());
				
				Common.response.onResult(result);
				
				// remove the temporary static references
				Common.response = null;
				Common.login_connector = null;
				
				Intent launchMain = new Intent(MendeleyLoginCallback.this, AuthSuccess.class);
				startActivity(launchMain);
				
				finish();
			}
			else
			{
				// no URI?
			}
		
		}

	}
    
	private void upgradePINIfFound()
	{
		if (!Common.login_connector.isConnected())
		{
			// extract the OAUTH access token if it exists
			Uri uri = this.getIntent().getData();
			if(uri != null) {
				ProgressDialog dialog = ProgressDialog.show(this, "", 
						"Logging in to Mendeley, please wait...", true);
	
				String PIN = uri.getQueryParameter("oauth_verifier");
				if (!Common.login_connector.setVerificationCode(PIN))
				{
					Common.shortToast("Error logging in to Mendeley.", this);
					dialog.cancel();
				}
				else
				{
					Common.shortToast("Logged in to Mendeley.", this);
					dialog.cancel();
				}
			}
		}
	}
	
	@Override
	protected void onResume()
	{
		handleLoad();
		
		super.onResume();
	}
}
