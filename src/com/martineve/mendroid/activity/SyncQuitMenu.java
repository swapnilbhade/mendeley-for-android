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

import com.martineve.mendroid.MendeleyForAndroid;
import com.martineve.mendroid.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.TabActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SyncQuitMenu extends TabActivity {
	private String TAG = "com.martineve.mendroid.MendeleyForAndroid";
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.mendeley_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.do_sync:
	    	AccountManager am = AccountManager.get(getApplication());
			Account[] a = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));

			if(a.length == 0)
			{
				Log.i(TAG, "No Mendeley accounts found, redirecting to setup.");
				Intent launchMain = new Intent(SyncQuitMenu.this, CreateMendeleyAccount.class);
				startActivity(launchMain);
				finish();
			} else {
				// start an sync request
				// TODO: needs to be called async, apparently
				ContentResolver.requestSync(a[0], "com.martineve.mendroid.data.mendeleycollectionsprovider", new Bundle());
			}
	        return true;
	    case R.id.quit:
	        finish();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
