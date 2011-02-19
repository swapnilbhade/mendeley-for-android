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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.martineve.mendroid.R;
import com.martineve.mendroid.adapters.SeparatedListAdapter;
import com.martineve.mendroid.data.MendeleyCollectionsProvider;
import com.martineve.mendroid.data.MendeleyDatabase;
import com.martineve.mendroid.sync.MendeleySyncAdapter;

public class CollectionAuthorDocuments extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// determine if a sync is in process
		TextView emptySync = (TextView) findViewById(R.id.emptySync);
		if(MendeleySyncAdapter.preFetchedArray != null)
		{
			emptySync.setText(Html.fromHtml(getString(R.string.sync_in_progress)));
		}
		
		Bundle extra = getIntent().getExtras();
		
		SeparatedListAdapter adapter = new SeparatedListAdapter(this);
		
		Cursor c = managedQuery(Uri.withAppendedPath(Uri.withAppendedPath(MendeleyCollectionsProvider.COLLECTION_URI, extra.getString("collection_id")), "author/" + extra.getString("author_id")), null, null, null, MendeleyDatabase.DOCUMENT_TYPE + " asc, " + MendeleyDatabase.DOCUMENT_NAME + " asc");
		
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

}
