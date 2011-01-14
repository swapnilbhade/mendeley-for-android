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

import com.martineve.mendroid.R;
import com.martineve.mendroid.sync.AccountAuthenticatorService;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	private boolean shouldForceSync = false;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		addPreferencesFromResource(R.xml.preferences_sync);
		findPreference("sync_collections").setOnPreferenceChangeListener(syncToggle);
	}

	@Override
	public void onPause() {
		super.onPause();
		if(shouldForceSync) {
			//AccountAuthenticatorService.resyncAccount(this);
			// some kind of sync here
		}
	}

	Preference.OnPreferenceChangeListener syncToggle = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			shouldForceSync = true;
			return true;
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		
	}
}
