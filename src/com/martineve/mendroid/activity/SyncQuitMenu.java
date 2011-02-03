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
