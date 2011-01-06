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

package com.martineve.mendroid.util;

import java.net.URL;
import org.json.JSONArray;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

import android.os.AsyncTask;
import android.widget.Toast;

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
		// first param is a string[]
		String[] urls = (String[])params[0];
		
		// second param is a Context object
		c_context = (Context)params[1];
		
		int count = urls.length;
		JSONArray[] ret = new JSONArray[count];
		//((ProgressDialog)d_progress).setMax(count * 100);
        
        for (int i = 0; i < count; i++) {
        	try {
				String strResponse = m_connect.getMendeleyResponse(urls[i]);
				ret[i] = new JSONArray(strResponse);
				publishProgress((int) ((i / (float) count) * 100));
			} catch (Exception e) {
				e_exception = e;
				publishProgress(-1);
				return null;
			}
            publishProgress((int) ((i / (float) count) * 100));
        }
        
        return ret;
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
        if (progress[0] == -1)
        {
        	Toast.makeText(c_context, "\nGot a " + e_exception.getClass().getName() + ": " + e_exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

	
	@Override
	protected void onPreExecute()
	{
		// invoked on the UI thread
		//d_progress = ProgressDialog.show(c_context, "", 
                //"Communicating with Mendeley, please wait...", false);
	}
	
	@Override
	protected void onPostExecute(JSONArray[] result) {
		// invoked on the UI thread
        //d_progress.cancel();
        
        if (result == null)
        {
        	// there was an error
        	CharSequence text = "Error communicating with Mendeley.";
        	int duration = Toast.LENGTH_LONG;

        	Toast toast = Toast.makeText(c_context, text, duration);
        	toast.show();
        }
    }

}
