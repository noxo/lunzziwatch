package org.noxo.lunzziwatch;

import java.util.ArrayList;
import java.util.List;

import org.noxo.lunzziwatch.content.DataProvider;
import org.noxo.lunzziwatch.model.Restaurant;
import org.noxo.lunzziwatch.util.RestaurantImporter;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	final String TAG = MainActivity.class.getSimpleName();

	AsyncTask<Void, Void, Void> restaurantImportTask;
	ProgressDialog progressDialog;
	ListView restaurantListView;
	RestaurantAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Log.d(TAG, "onCreate");

		setContentView(R.layout.activity_main);

		restaurantListView = (ListView) findViewById(R.id.restaurantList);

		adapter = new RestaurantAdapter(this);
		restaurantListView.setAdapter(adapter);

		restaurantImportTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				progressDialog = ProgressDialog.show(MainActivity.this, "", MainActivity.this.getText(R.string.loading_restaurant_list));
			}

			@Override
			protected Void doInBackground(Void... params) {
				RestaurantImporter.importMbnet(MainActivity.this);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				progressDialog.hide();
				loadRestaurantsFromDb();
			}
		};

		restaurantImportTask.execute();
	}

	public void loadRestaurantsFromDb()
	{
		Log.d(TAG, "loadRestaurantsFromDb");
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new android.support.v4.content.CursorLoader(this, DataProvider.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

		Log.d(TAG, "load finished");
		List<Restaurant> restaurants = new ArrayList<Restaurant>();

		while (cursor.moveToNext())
		{
			Restaurant r = Restaurant.fromCursor(cursor);
			restaurants.add(r);
		}

		adapter.setRestaurants(restaurants);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// WTF
	}
}
