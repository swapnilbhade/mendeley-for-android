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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.martineve.mendroid.R;

public class AuthSuccess extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.authsuccess);

		// setup the link in the credits box ;)
		TextView credits = (TextView) findViewById(R.id.auth_success_credits);
		credits.setText(Html.fromHtml(getString(R.string.credit_text)));
		credits.setMovementMethod(LinkMovementMethod.getInstance());
		
		
		Button moveNext = (Button) findViewById(R.id.auth_move_next);
		moveNext.setOnClickListener(moveNextListener);
	}
	
	// Create an anonymous implementation of OnClickListener
	private OnClickListener moveNextListener = new OnClickListener() {
	    public void onClick(View v) {
	    	Intent startMain = new Intent();
	    	startMain.setClassName("com.martineve.mendroid", "com.martineve.mendroid.MendeleyForAndroid");
	    	startActivity(startMain);  
	    }
	};

}
