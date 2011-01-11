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
import android.accounts.AccountManager;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

import com.martineve.mendroid.activity.CollectionsActivity;
import com.martineve.mendroid.activity.ContactsActivity;
import com.martineve.mendroid.activity.CreateMendeleyAccount;
import com.martineve.mendroid.common.Common;

public class MendeleyForAndroid extends TabActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// if no accounts, redirect to the account creation page
		try
		{
			AccountManager am = AccountManager.get(getApplication());
			Account[] a = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));

			if(a.length == 0)
			{
				Log.i("com.martineve.mendroid.MendeleyForAndroid", "No Mendeley accounts found, redirecting to setup.");
				Intent launchMain = new Intent(MendeleyForAndroid.this, CreateMendeleyAccount.class);
				startActivity(launchMain);
				finish();
			}
		} catch (Exception e)
		{
			Log.e("com.martineve.mendroid.MendeleyForAndroid", "There was an error retrieving the account list.", e);
			Common.longToast("There was an error retrieving the account list. Closing Mendeley for Android to prevent data loss.", this);
			finish();
		}


		setContentView(R.layout.maintabs);

		Resources res = getResources(); // resource object to get Drawables
		TabHost tabHost = getTabHost();  // the activity TabHost
		TabHost.TabSpec spec;  // resusable TabSpec for each tab
		Intent intent;  // reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, ContactsActivity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("contacts").setIndicator("Contacts",
				res.getDrawable(R.drawable.ic_tab_allfriends))
				.setContent(intent);
		tabHost.addTab(spec);


		// Do the same for the other tabs
		intent = new Intent().setClass(this, CollectionsActivity.class);
		spec = tabHost.newTabSpec("collections").setIndicator("Collections",
				res.getDrawable(R.drawable.ic_tab_archive))
				.setContent(intent);
		tabHost.addTab(spec);

		/*
	    intent = new Intent().setClass(this, SongsActivity.class);
	    spec = tabHost.newTabSpec("songs").setIndicator("Songs",
	                      res.getDrawable(R.drawable.ic_tab_songs))
	                  .setContent(intent);
	    tabHost.addTab(spec);*/

		tabHost.setCurrentTab(1);
	}
}
