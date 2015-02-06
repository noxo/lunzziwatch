package org.noxo.lunzziwatch.content;

import org.noxo.lunzziwatch.model.Restaurant;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class DataProvider extends ContentProvider {

	final static String TAG = DataProvider.class.getSimpleName();

	public static final Uri CONTENT_URI = Uri.parse("content://org.noxo.lunzziwatch");
	public static final String MIME_TYPE_ROWS = "vnd.android.cursor.dir/org.noxo.lunzziwatch";

	final static String DB_NAME = "Favorites";
	final static int DB_VERSION = 1;

	final static String SQL_CREATE_TABLE_FAVORITE = "create table " + Restaurant.TABLE_NAME
			+ "("
			+ Restaurant.Columns._ID + " INTEGER PRIMARY KEY,"
			+ Restaurant.Columns.NAME + " TEXT,"
			+ Restaurant.Columns.URL + " TEXT,"
			+ Restaurant.Columns.FAVORITED + " SHORT"
			+ ")";

	private DbHelper dbHelper;

	private class DbHelper extends SQLiteOpenHelper
	{

		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_TABLE_FAVORITE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onUpgrade(db, oldVersion, newVersion);
		}

	}


	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return MIME_TYPE_ROWS;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long id = db.insert(Restaurant.TABLE_NAME, null, values);
		return Uri.withAppendedPath(uri, Long.toString(id));
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(Restaurant.TABLE_NAME, null, selection, selectionArgs, null, null, null);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int rowsUpdated = db.update(Restaurant.TABLE_NAME, values, selection, selectionArgs);
		return rowsUpdated;
	}

}
