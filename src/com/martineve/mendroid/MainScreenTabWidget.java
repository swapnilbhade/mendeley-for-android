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

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.Toast;

public class MainScreenTabWidget extends TabActivity {
	
	public void onCreate(Bundle savedInstanceState) {
		//TODO: if no accounts, must redirect to creation page
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.maintabs);
	
	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	
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
	                      res.getDrawable(R.drawable.ic_tab_allfriends))
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
