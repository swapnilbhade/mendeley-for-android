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

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;



public class ContactsActivity extends Activity {
	ArrayList<HashMap<String,String>> c_list = new ArrayList<HashMap<String,String>>();  
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.contacts);

		if (!OAuth.CONNECTOR.isConnected())
		{
			moveToLogin();
			return;
		}
		
		c_list.clear();
    
    
    
		JSONArray contacts;
		try {
			//new MendeleyAPITask(OAuth.CONNECTOR).execute(MendeleyURLs.getURL(MendeleyURLs.CONTACTS), ContactsActivity.this);
			//new MendeleyAPITask(OAuth.CONNECTOR).doFetch(new String[] {MendeleyURLs.getURL(MendeleyURLs.CONTACTS)}, ContactsActivity.this);
            
			
            /*contacts = m_conn.getCollections();
            for (int i=0; i< collections.length(); i++) {
                    JSONObject collection = collections.getJSONObject(i);
                    
                    HashMap<String,String> item = new HashMap<String,String>();
                    
                    item.put("line1", collection.getString("name"));
                    item.put("line2", collection.getString("size") + " documents");
                    
                    m_list.add(item);
            }*/
	    } catch (Exception e) {
	            Toast.makeText(getApplicationContext(), "\nGot a " + e.getClass().getName() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
	            return;
	    }

	}
	
	/** Moves to the main screen */
	private void moveToLogin()
	{
		Intent launchMain = new Intent(ContactsActivity.this, MendeleyDroidLogin.class);
        
		startActivity(launchMain);
	}  
}
