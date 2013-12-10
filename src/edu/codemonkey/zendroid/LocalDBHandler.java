package edu.codemonkey.zendroid;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDBHandler {

	private LocalDBHandler() {

	}

	private static LocalDBHandler instance = new LocalDBHandler();
	private CustomSQLiteHelper sqlHelper;
	private SQLiteDatabase database;
	private boolean setDB = false;
	public static boolean VERSIONUPDATED = false;
	public ArrayList<ScanObject> scanList = new ArrayList<ScanObject>();


	private String[] allColumns = { CustomSQLiteHelper.ID,
			CustomSQLiteHelper.COMMAND_COLUMN, CustomSQLiteHelper.RESULT_COLUMN};

	public static LocalDBHandler getInstance() {
		return instance;
	}

	public SQLiteDatabase setDB(Context context) {
		if(!setDB) {
			sqlHelper = new CustomSQLiteHelper(context, "zendroid.db", null, 1);
			database = sqlHelper.getWritableDatabase();
			setDB = true;
		}
		fetchScanListfromDB();
		return database;
	}
	
	private void fetchScanListfromDB() {
		Cursor cursor = database.query(false, CustomSQLiteHelper.TABLE, allColumns, null, null, null, null, null, null, null);
		if(cursor.moveToFirst()) {
			do {
				ScanObject scan = new ScanObject(cursor.getInt(0), cursor.getString(1),cursor.getString(2));
				scanList.add(scan);
			}while(cursor.moveToNext());
		}
	}
	
	public int getNextId() {
		String query = "SELECT MAX(" + CustomSQLiteHelper.ID + ") AS max_id FROM " +
				CustomSQLiteHelper.TABLE;
		Cursor cursor = database.rawQuery(query, null);

		int id = 0;     
		if (cursor.moveToFirst())
		{
		    do
		    {           
		        id = cursor.getInt(0);                  
		    } while(cursor.moveToNext());           
		}
		return id+1;
	}

	public boolean saveScan(int id, String fullCommand, String  result) {
		ContentValues values = new ContentValues();
		values.put(CustomSQLiteHelper.ID, id);
		values.put(CustomSQLiteHelper.COMMAND_COLUMN, fullCommand);
		values.put(CustomSQLiteHelper.RESULT_COLUMN, result);
		long insertId = database.insert(CustomSQLiteHelper.TABLE, null,
			        values);
		if (insertId != -1) {
			return true;
		}
		return false;
	}

	public boolean scanUpdate(int id, String command, String result){
		ContentValues values = new ContentValues();
		values.put(CustomSQLiteHelper.COMMAND_COLUMN, command);
		values.put(CustomSQLiteHelper.RESULT_COLUMN, result);
		long insertId = database.update(CustomSQLiteHelper.TABLE, values, CustomSQLiteHelper.ID+" =?", new String[] { ""+id });
		if (insertId != -1) 
			return true;
		return false;
	}

	

	public boolean deleteScan(int id) {
		int rowseffected = database.delete(CustomSQLiteHelper.TABLE, CustomSQLiteHelper.ID+" =?", new String[]{ ""+id });
		if(rowseffected == 0) {
			return false;
		}
		return true;
	}
	
	public void closeDB() {
		database.close();
	}

}

class CustomSQLiteHelper extends SQLiteOpenHelper {

	static final String COMMAND_COLUMN = "command";
	static final String RESULT_COLUMN = "fileLocation";
	static final String ID ="id";
	static final String TABLE = "scandetails";
	private static final String DATABASE_CREATE = "create table "
			+ TABLE + " (" + ID
			+ " integer primary key, " + COMMAND_COLUMN
			+ " text not null, " + 
			RESULT_COLUMN + " text );";
	
	public CustomSQLiteHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
		LocalDBHandler.VERSIONUPDATED = true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE);
		onCreate(db);
	}

}
