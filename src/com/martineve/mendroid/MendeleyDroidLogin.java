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


import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MendeleyDroidLogin extends AccountAuthenticatorActivity implements OnClickListener {

	String request_token;
	
	/** Handles the Login button click */
	public void onClick(View v) {		
		Account account = new Account(((EditText)findViewById(R.id.login_page_identifier)).getText().toString(), "com.martineve.mendroid.account");
		AccountManager am = AccountManager.get(this);
		try{
			boolean accountCreated = am.addAccountExplicitly(account, null, null);
			
			if(!accountCreated)
			{
				Common.longToast("There was an error creating the account. Ensure the identifier is unique and not already in use.", this);
			}
			else
			{			
				am.invalidateAuthToken("com.martineve.mendroid.account", "77ba1bf8078fb955bebea33d80c5428804d24b693/19d2753c2f9e42ea91e73c04e59e43db");
				
				am.getAuthToken(account, "com.martineve.mendroid.account", null, this, null, null);
			}
		} catch (Exception e)
		{
			Common.longToast("There was an error creating the account: " + e.getMessage(), this);
			Log.e("MendeleyDroidLogin", "There was an error creating the account.", e);
		}
		
	}

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);

		Button login_button = (Button) findViewById(R.id.login_button);
		login_button.setOnClickListener(this);

		// setup the link in the credits box ;)
		TextView credits = (TextView) findViewById(R.id.login_page_credits);
		credits.setText(Html.fromHtml("Copyright <a href=\"http://www.martineve.com\">Martin Paul Eve</a>, 2011"));
		credits.setMovementMethod(LinkMovementMethod.getInstance());
		
	}

}