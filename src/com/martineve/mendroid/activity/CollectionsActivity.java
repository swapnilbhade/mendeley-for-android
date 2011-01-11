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
import java.util.HashMap;

import android.app.Activity;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

import com.martineve.mendroid.R;
import com.martineve.mendroid.data.MendeleyContentProvider;
import com.martineve.mendroid.data.MendeleyDatabase;

public class CollectionsActivity extends ListActivity {
	ArrayList<HashMap<String,String>> c_list = new ArrayList<HashMap<String,String>>();  
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.collections);

		Cursor c = managedQuery(MendeleyContentProvider.COLLECTIONS_URI, null, null, null, MendeleyDatabase.COLLECTION_NAME + " asc");

		String[] from = new String[] {MendeleyDatabase.COLLECTION_NAME, MendeleyDatabase.COLLECTION_SIZE};
		
		int[] to = new int[] { R.id.collection_entry, R.id.collection_size };
		
		ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.collection_item, c, from, to);
		this.setListAdapter(adapter);
	}
}
