package edu.codemonkey.zendroid;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

public class ZendroidApp extends Application {
	private SQLiteDatabase database;
	@Override
	public void onCreate() {
		super.onCreate();
		database = LocalDBHandler.getInstance().setDB(this);
	}
}
