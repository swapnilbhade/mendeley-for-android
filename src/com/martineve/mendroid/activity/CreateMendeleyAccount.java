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


import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.martineve.mendroid.R;
import com.martineve.mendroid.common.Common;

public class CreateMendeleyAccount extends AccountAuthenticatorActivity implements OnClickListener {

	String request_token;
	
	/** Handles the Login button click */
	public void onClick(View v) {		
		Account account = new Account(((EditText)findViewById(R.id.login_page_identifier)).getText().toString(), "com.martineve.mendroid.account");
		AccountManager am = AccountManager.get(this);
		
		// set the account to automatically synchronize
		// CAN ONLY DO THIS IN FROYO:
        //ContentResolver.addPeriodicSync(account, "com.martineve.mendroid.data.mendeleycollectionsprovider", null, 2*3600);
		ContentResolver.setSyncAutomatically(account, "com.martineve.mendroid.data.mendeleycollectionsprovider" , true);
		
		try{
			boolean accountCreated = am.addAccountExplicitly(account, null, null);
			
			if(!accountCreated)
			{
				Common.longToast("There was an error creating the account. Ensure the identifier is unique and not already in use.", this);
			}
			else
			{			
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
		credits.setText(Html.fromHtml(getString(R.string.credit_text)));
		credits.setMovementMethod(LinkMovementMethod.getInstance());
		
	}

}