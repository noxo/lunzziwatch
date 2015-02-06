package org.noxo.lunzziwatch.controls;

import java.util.Calendar;
import java.util.List;

import org.noxo.lunzziwatch.R;
import org.noxo.lunzziwatch.content.DataProvider;
import org.noxo.lunzziwatch.model.Food;
import org.noxo.lunzziwatch.model.Restaurant;
import org.noxo.lunzziwatch.util.FoodImporter;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlListItem;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class DailyMenuControl extends ManagedControlExtension {

	final String TAG = DailyMenuControl.class.getSimpleName();

	private AsyncTask<Void, Void, List<Food>> foodImportTask;
	private List<Food> foodList;

	public final static String EXTRA_RESTAURANT_ID = "EXTRA_RESTAURANT_ID";

	int year = 0, month = 0, day = 0;

	public DailyMenuControl(Context context, String hostAppPackageName,
			ControlManagerSmartWatch2 controlManager, Intent intent) {
		super(context, hostAppPackageName, controlManager, intent);
	}

	@Override
	public void onResume() {

		try {

			Log.d(TAG, "onResume");

			showLayout(R.layout.daily_menu, null);

			Calendar cal = Calendar.getInstance();

			year = cal.get(Calendar.YEAR);
			month = cal.get(Calendar.MONTH) + 1;
			day = cal.get(Calendar.DAY_OF_MONTH);

			Log.d(TAG, "onResume, restaurant from db");

			int restaurantId = getIntent().getIntExtra(EXTRA_RESTAURANT_ID, 0);

			final String WHERE = Restaurant.Columns._ID + " = " + restaurantId;

			ContentResolver cr = mContext.getContentResolver();
			Cursor cursor = cr.query(DataProvider.CONTENT_URI, null, WHERE, null, null);

			if (!cursor.moveToNext())
				Log.e(TAG, "onResume, failed to load restaurant from db");

			Log.d(TAG, "onResume, creating food query url");

			Restaurant r = Restaurant.fromCursor(cursor);
			String daysFoodUrl = r.getUrl();
			daysFoodUrl += year;
			daysFoodUrl += "/";
			daysFoodUrl += month;
			daysFoodUrl += "/";
			daysFoodUrl += day;
			daysFoodUrl += "/fi";

			Log.d(TAG, "onResume, querying.. url=" + daysFoodUrl);

			if (foodList == null)
			{
				loadFromNetwork(daysFoodUrl);
			}	
			else
			{
				refreshFoodList();
			}

		} catch (Exception e) {
			Log.e(TAG, "onResume failed", e);
		}

	}

	private void loadFromNetwork(final String url)
	{

		foodImportTask = new AsyncTask<Void, Void, List<Food>>() {

			@Override
			protected void onPreExecute() {
				sendText(R.id.daily_title, "Loading..");
			}

			@Override
			protected List<Food> doInBackground(Void... params) {
				// TODO: have network wakelock
				return FoodImporter.importFood(url);
			}

			@Override
			protected void onPostExecute(List<Food> result) {
				foodList = result;
				refreshFoodList();
			}
		};

		foodImportTask.execute();

	}

	private void refreshFoodList()
	{

		String titleText = mContext.getString(R.string.menu);
		titleText += " " + day + "." + month + "." + year;

		sendText(R.id.daily_title, titleText);
		sendListCount(R.id.listView2, foodList == null ? 1 : foodList.size());

	}

	@Override
	public void onRequestListItem(final int layoutReference, final int listItemPosition) {
		Log.d(TAG, "onRequestListItem() - position " + listItemPosition);
		if (layoutReference != -1 && listItemPosition != -1 && layoutReference == R.id.listView2) {
			ControlListItem item = createControlListItem(listItemPosition);
			if (item != null) {
				sendListItem(item);
			}
		}
	}

	protected ControlListItem createControlListItem(int position) {

		Food food = null;

		if (foodList != null)
			food = foodList.get(position);

		ControlListItem item = new ControlListItem();
		item.layoutReference = R.id.listView2;
		item.dataXmlLayout = R.layout.daily_menu_row;
		item.listItemPosition = position;
		item.listItemId = position;

		Bundle headerBundle = new Bundle();
		Bundle bodyBundle = null;

		if (food == null) 
		{
			headerBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.title);
			headerBundle.putString(Control.Intents.EXTRA_TEXT, mContext.getString(R.string.error));
		}
		else
		{
			headerBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.title);

			String foodCategory = food.getCategory();

			if (foodCategory == null) {
				foodCategory = mContext.getString(R.string.category);
				foodCategory += " " + position;
			}

			headerBundle.putString(Control.Intents.EXTRA_TEXT, foodCategory);
			// Body data
			bodyBundle = new Bundle();
			bodyBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.body);

			String body = food.getTitle() == null ? "?????" : food.getTitle();

			if (food.getPrice() != null)
				body += "\n" + food.getPrice() + "e";
			if (food.getProps() != null)
				body += "\n" + food.getProps();

			bodyBundle.putString(Control.Intents.EXTRA_TEXT, body);

		}

		item.layoutData = new Bundle[3];
		item.layoutData[1] = headerBundle;

		if (bodyBundle != null)
			item.layoutData[2] = bodyBundle;

		return item;
	}

}
