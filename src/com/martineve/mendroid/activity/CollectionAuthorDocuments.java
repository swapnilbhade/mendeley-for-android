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

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.martineve.mendroid.R;
import com.martineve.mendroid.adapters.SeparatedListAdapter;
import com.martineve.mendroid.data.MendeleyCollectionsProvider;
import com.martineve.mendroid.data.MendeleyDatabase;
import com.martineve.mendroid.sync.MendeleySyncAdapter;

public class CollectionAuthorDocuments extends ListActivity {

	private static String TAG = "com.martineve.mendroid.activity.CollectionAuthorsDocuments";
	private String author_id;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
	
		Bundle extra = getIntent().getExtras();
		
		SeparatedListAdapter adapter = new SeparatedListAdapter(this);
		
		Cursor c = managedQuery(Uri.withAppendedPath(Uri.withAppendedPath(MendeleyCollectionsProvider.COLLECTION_URI, extra.getString("collection_id")), "author/" + extra.getString("author_id")), null, null, null, MendeleyDatabase.DOCUMENT_TYPE + " asc, " + MendeleyDatabase.DOCUMENT_NAME + " asc");
		
		author_id = extra.getString("author_id");
		
		c.moveToFirst();
		
		String currentType = c.getString(c.getColumnIndex("document_type"));
		
		ArrayList<String> items = new ArrayList<String>();
		
		items.add(c.getString(c.getColumnIndex("document_title")));
		
		while(c.moveToNext())
		{
			String newType = c.getString(c.getColumnIndex("document_type"));
			
			if (!newType.equals(currentType))
			{
				// add the section
				adapter.addSection(currentType + "s", new ArrayAdapter<String>(this,
						R.layout.list_item, items));
				
				items = new ArrayList<String>();
				
				currentType = newType;
			}
			
			items.add(c.getString(c.getColumnIndex("document_title")));
		}
		
		// add the section
		adapter.addSection(currentType + "s", new ArrayAdapter<String>(this,
				R.layout.list_item, items));
		
		items = new ArrayList<String>();
		
		this.setListAdapter(adapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		// gets a Cursor, set to the correct record
		Cursor c = (Cursor)l.getItemAtPosition(position);
		
		// launch the author intent
		Log.v(TAG, "Launching collection authors intent.");
		Intent launchItemView = new Intent(CollectionAuthorDocuments.this, ItemActivity.class);
		launchItemView.putExtra("author_id", author_id);
		launchItemView.putExtra("document_id", c.getString(c.getColumnIndex(MendeleyDatabase._ID)));
		startActivity(launchItemView);
		
		super.onListItemClick(l, v, position, id);
	}

}
