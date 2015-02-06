package org.noxo.lunzziwatch.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

public class Restaurant {

	public static final String TABLE_NAME = "Restaurant";

	public static final class Columns implements BaseColumns {
		public static final String NAME = "name";
		public static final String URL = "url";
		public static final String FAVORITED = "favorited";
	}

	int id;
	String name;
	String url;
	int favorited;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isFavorited() {
		return favorited == 1;
	}

	public void setFavorited(boolean favorited) {
		this.favorited = (favorited ? 1 : 0);
	}

	public ContentValues toContentValues()
	{
		ContentValues cv = new ContentValues();
		cv.put(Columns._ID, getId());
		cv.put(Columns.NAME, getName());
		cv.put(Columns.URL, getUrl());
		cv.put(Columns.FAVORITED, isFavorited() ? 1 : 0);
		return cv;
	}

	public static Restaurant fromCursor(Cursor c)
	{
		Restaurant r = new Restaurant();
		r.setId(c.getInt(c.getColumnIndex(Columns._ID)));
		r.setName(c.getString(c.getColumnIndex(Columns.NAME)));
		r.setUrl(c.getString(c.getColumnIndex(Columns.URL)));
		int favo = c.getShort(c.getColumnIndex(Columns.FAVORITED));
		r.setFavorited(favo == 1);
		return r;
	}

	@Override
	public String toString() {
		return id + "|" + name + "|" + url + "|" + favorited;
	}


}
