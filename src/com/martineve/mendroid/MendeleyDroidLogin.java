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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

public class MendeleyDroidLogin extends Activity implements OnClickListener {

	String request_token;
	private SharedPreferences s_settings;

	/** Makes a long toast */
	private void longToast(String message)
	{
		CharSequence text = message;
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(this, text, duration);
		toast.show();
	}

	/** Moves to the main screen */
	private void moveToMain()
	{
		Intent launchMain = new Intent(MendeleyDroidLogin.this, MainScreenTabWidget.class);
        
		startActivity(launchMain);
	}  
	
	/** Handles the Login button click */
	public void onClick(View v) {
		
		// save the checkbox state
		SharedPreferences.Editor editor = s_settings.edit();
	    editor.putBoolean("LoginAutomatically", ((CheckedTextView) findViewById(R.id.login_auto)).isChecked());

	    editor.commit();
	    
	    // login
		doLogin();
	}
	
	/** Logs in to Mendeley */
	public void doLogin()
	{
		// get the request token
		ProgressDialog dialog = ProgressDialog.show(this, "", "Communicating with Mendeley, please wait...", true);

		String strAuthURL;
		try {
			strAuthURL = OAuth.CONNECTOR.getAuthenticationURL();
			dialog.cancel();

			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(strAuthURL));

			startActivityForResult(i, 0);
		} catch (Exception e) {
			dialog.cancel();
			longToast("Got exception while authenticating:\n" + e.getMessage());
		}
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		s_settings = getApplication().getSharedPreferences(Settings.PREFS_NAME, 0);
		
		// check if we have some settings already
		if (s_settings.contains("Token") && s_settings.contains("TokenSecret"))
		{
			// instantiate the connector with a context object and a previous token and token secret
			if(OAuth.CONNECTOR == null)
				OAuth.CONNECTOR = new MendeleyConnector(getApplication(), s_settings.getString("Token", ""), s_settings.getString("TokenSecret", ""));
			
			if(s_settings.getBoolean("LoginAutomatically", false))
			{
				shortToast("Attempting to use previous credentials.");
				
				// move to the next intent
				moveToMain();
			}
		}
		else
		{
			// instantiate the connector with a context object
			if(OAuth.CONNECTOR == null)
				OAuth.CONNECTOR = new MendeleyConnector(getApplication());
			
			if(s_settings.getBoolean("LoginAutomatically", false))
			{
				shortToast("Logging in to Mendeley.");
				doLogin();
			}
		}
		
		setContentView(R.layout.main);

		Button login_button = (Button) findViewById(R.id.login_button);
		login_button.setOnClickListener(this);

		// setup the link in the credits box ;)
		TextView credits = (TextView) findViewById(R.id.login_page_credits);
		credits.setText(Html.fromHtml("Copyright <a href=\"http://www.martineve.com\">Martin Paul Eve</a>, 2011"));
		credits.setMovementMethod(LinkMovementMethod.getInstance());
		
		// setup the checkbox
		CheckedTextView chkBox = (CheckedTextView) findViewById(R.id.login_auto);
		chkBox.setChecked(s_settings.getBoolean("LoginAutomatically", true));
	    chkBox.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v)
	        {
	            ((CheckedTextView) v).toggle();
	            SharedPreferences.Editor editor = s_settings.edit();
			    editor.putBoolean("LoginAutomatically", ((CheckedTextView) v).isChecked());

			    editor.commit();
	        }
	    });

	}

	/** Makes a short toast */
	private void shortToast(String message)
	{
		CharSequence text = message;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(this, text, duration);
		toast.show();
	}
}