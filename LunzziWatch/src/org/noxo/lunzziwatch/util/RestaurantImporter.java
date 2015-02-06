package org.noxo.lunzziwatch.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.noxo.lunzziwatch.content.DataProvider;
import org.noxo.lunzziwatch.model.Restaurant;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;

public class RestaurantImporter {

	final static String TAG = RestaurantImporter.class.getSimpleName();

	private static final String SODEXO_BASE_URL = "http://www.sodexo.fi/ruokalistat/output/daily_json/";
	private static final String MBNET_URL = "http://koti.mbnet.fi/noxo/restaurants.txt";

	public static List<Restaurant> importMbnet(Context context)
	{

		Log.v(TAG, "importMbnet");

		BufferedReader reader = null;
		List<Restaurant> result = new ArrayList<Restaurant>();

		try {


			URL url = new URL(MBNET_URL);

			Log.v(TAG, "importMbnet|connecting");
			URLConnection conn = url.openConnection();
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));			

			Log.v(TAG, "importMbnet|reading");

			while (true)
			{
				String line = reader.readLine();

				if (line == null)
					break;

				String parts[] = line.split("\\|");

				if (parts.length != 3)
					continue;

				Restaurant r = new Restaurant();
				r.setId(Integer.valueOf(parts[0]));
				r.setName(parts[1]);
				r.setUrl(parts[2]);
				result.add(r);

				Log.v(TAG, r.toString());
			}

			exportToDb(context, result);

		} catch (Exception e) {
			Log.e(TAG, "importMbnet|error getting restraurant", e);
			return null;
		}
		finally
		{
			try
			{
				reader.close();
			} catch (Exception e)
			{
				Log.w(TAG, e);
			}
		}

		return result;
	}

	public static List<Restaurant> importSodexoJson(Context context)
	{

		Log.v(TAG, "importSodexoJson");

		List<Restaurant> result = new ArrayList<Restaurant>();

		for (int i=8;i<160;i++) // most of restraunts are in this range
		{
			Restaurant r = getNextJSON(i);

			if (r != null)
			{
				result.add(r);
			}
		}

		for (int i=870;i<880;i++) // wtf innopoli I and some random restaurant here
		{
			Restaurant r = getNextJSON(i);

			if (r != null)
			{
				result.add(r);
			}
		}

		exportToSdCard(result);

		return result;
	}



	private static Restaurant getNextJSON(int id)
	{
		Log.v(TAG, "getNextJSON(" + id + ")");

		JsonReader jsonReader = null;

		try {

			Calendar cal = Calendar.getInstance();

			// no entries for weekend
			while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ||
					cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
			{
				cal.add(Calendar.DAY_OF_YEAR, 1);
			}

			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1;
			int day = cal.get(Calendar.DAY_OF_MONTH);

			String requestUrl = SODEXO_BASE_URL;
			requestUrl += (id < 10 ? "" + id : id);
			requestUrl += "/";
			requestUrl += year;
			requestUrl += "/";
			requestUrl += (month < 10 ? "" + month : month);
			requestUrl += "/";
			requestUrl += (day < 10 ? "" + day : day);
			requestUrl += "/fi";

			Log.v(TAG, "requesURI: " + requestUrl);

			URL url = new URL(requestUrl);
			URLConnection conn = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			Log.v(TAG, "parsing JSON");

			jsonReader = new JsonReader(reader);
			jsonReader.beginObject();
			jsonReader.skipValue();

			while (jsonReader.hasNext())
			{
				jsonReader.beginObject();

				while (jsonReader.hasNext())
				{

					String name = jsonReader.nextName();

					if (name.compareTo("ref_title") == 0)
					{
						String restaurantName = jsonReader.nextString();
						Log.v(TAG, "found restaurant=" + restaurantName);
						Restaurant r = new Restaurant();
						r.setId(id);
						r.setName(restaurantName);
						r.setUrl(SODEXO_BASE_URL + (id < 10 ? "" + id : id) +  "/");
						return r;
					}
					else
					{
						jsonReader.skipValue();
					}
				}
			}

			return null;

		} catch (Exception e) {
			Log.e(TAG, "getNextJSON|error getting restraurant");
			return null;
		}
		finally
		{
			try {
				if (jsonReader != null)
					jsonReader.close();
			} catch (Exception e) {
				Log.w(TAG, e);
			}
		}
	}

	private static void exportToDb(Context context, List<Restaurant> restaurants) {

		ContentResolver cr = context.getContentResolver();
		final String WHERE = Restaurant.Columns._ID + "= ?";

		for (Restaurant restaurant : restaurants)
		{

			Cursor c  = cr.query(DataProvider.CONTENT_URI, null, WHERE, new String[] { ""+ restaurant.getId() }, null);

			if (c.getCount() > 0 && c.moveToFirst())
			{				
				Restaurant oldRestaurant = Restaurant.fromCursor(c);
				oldRestaurant.setUrl(restaurant.getUrl());
				oldRestaurant.setName(restaurant.getName());
				cr.update(DataProvider.CONTENT_URI, oldRestaurant.toContentValues(), WHERE, new String[] { ""+ restaurant.getId() });
			}
			else
			{
				cr.insert(DataProvider.CONTENT_URI, restaurant.toContentValues());
			}

		}

	}

	private static void exportToSdCard(List<Restaurant> restaurants) {

		BufferedWriter bw = null;

		try {

			File dir_sdcard = Environment.getExternalStorageDirectory();
			File file = new File(dir_sdcard, "restaurants.txt");

			bw = new BufferedWriter(new FileWriter(file));

			for (Restaurant restaurant : restaurants) {
				bw.write(restaurant.getId() + "|" + restaurant.getName() + "|"
						+ restaurant.getUrl());
				bw.newLine();
			}

			bw.flush();

		} catch (Exception e) {
			Log.e(TAG, "exportToSdCard", e);
		} finally {

			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				Log.w(TAG, e);
			}
		}
	}
}
