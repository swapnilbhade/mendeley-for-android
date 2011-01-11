package com.martineve.mendroid.task;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

public class GetWritableDatabaseTask extends AsyncTask<SQLiteOpenHelper, Void, SQLiteDatabase> {

	@Override
	protected SQLiteDatabase doInBackground(SQLiteOpenHelper... arg0) {
		Log.i("com.martineve.mendroid.task.GetWritableDatabaseTask", "Opening writable database");
		return arg0[0].getWritableDatabase();
	}

}
