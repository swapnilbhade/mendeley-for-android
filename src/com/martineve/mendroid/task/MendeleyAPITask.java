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

package com.martineve.mendroid.task;

import org.json.JSONArray;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

import com.martineve.mendroid.util.MendeleyConnector;

public class MendeleyAPITask extends AsyncTask<Object, Integer, JSONArray[]> {
	
	private MendeleyConnector m_connect;
	private Dialog d_progress;
	private Context c_context;
	private Exception e_exception;
	
	public MendeleyAPITask(MendeleyConnector connector)
	{
		m_connect = connector;
	}

	@Override
	protected JSONArray[] doInBackground(Object... params) {
		
        return doFetch(params);
	}
	
	public JSONArray[] doFetch(Object... params)
	{
		// TODO: remove this line when ready
		Debug.waitForDebugger();
		
		// first param is a string[]
		String[] urls = (String[])params[0];
		
		// second param is a Context object
		c_context = (Context)params[1];
		
		int count = urls.length;
		JSONArray[] ret = new JSONArray[count];

        
        for (int i = 0; i < count; i++) {
        	try {
        		Log.i("com.martineve.mendroid.task.MendeleyAPITask", "Executing API call: " + urls[i]);
        		
				String strResponse = m_connect.getMendeleyResponse(urls[i]);
				
				if(!strResponse.replace("\n", "").startsWith("["))
				{
					// wrap in JSONArray delimiters
					strResponse = "[" + strResponse + "]";
				}
				
				ret[i] = new JSONArray(strResponse);
				
				Log.i("com.martineve.mendroid.task.MendeleyAPITask", "Succesfully retrieved API call: " + urls[i]);
				
			} catch (Exception e) {
				// TODO: determine if this is due to needing re-auth
				Log.e("com.martineve.mendroid.task.MendeleyAPITask", "Failed to execute API call: " + urls[i], e);
				return null;
			}
            publishProgress((int) ((i / (float) count) * 100));
        }
        
        
        return ret;
	}
	
	
	@Override
	protected void onPostExecute(JSONArray[] result) {
		// invoked on the UI thread
        
        if (result == null)
        {
        	Log.e("com.martineve.mendroid.task.MendeleyAPITask", "Returned NULL; looks like a problem communicating with Mendeley; review stack trace.");
        	
        	// there was an error
        	CharSequence text = "Error communicating with Mendeley.";
        	int duration = Toast.LENGTH_LONG;

        	Toast toast = Toast.makeText(c_context, text, duration);
        	toast.show();
        }
    }

}
