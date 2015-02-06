package org.noxo.lunzziwatch;

import org.noxo.lunzziwatch.content.DataProvider;
import org.noxo.lunzziwatch.model.Restaurant;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class RestaurantAdapter extends ArrayAdapter<Restaurant> {

	final String TAG = RestaurantAdapter.class.getSimpleName();

	Context context;

	private static class ViewHolder {

		public final TextView restaurantNameTextView;
		public final CheckBox restaurantFavoriteCheckBox;

		public ViewHolder(TextView restaurantNameTextView, CheckBox restaurantFavoriteCheckBox) {
			this.restaurantNameTextView = restaurantNameTextView;
			this.restaurantFavoriteCheckBox = restaurantFavoriteCheckBox;
		}

		public TextView getRestaurantNameTextView() {
			return restaurantNameTextView;
		}

		public CheckBox getRestaurantFavoriteCheckBox() {
			return restaurantFavoriteCheckBox;
		}
	}

	List<Restaurant> restaurants;
	LayoutInflater layoutInflater;

	public RestaurantAdapter(Context context) {
		super(context, R.layout.restaurant_row);
		this.context  = context;
		layoutInflater = LayoutInflater.from(context);
	}

	public void setRestaurants(List<Restaurant> restaurants) {
		this.restaurants = restaurants;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return restaurants == null ? 0 : restaurants.size();
	}

	@Override
	public Restaurant getItem(int position) {
		return restaurants.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final TextView restaurantNameTextView;
		final CheckBox restaurantFavoriteCheckBox;

		if (convertView == null)
		{
			convertView = layoutInflater.inflate(R.layout.restaurant_row, parent, false);
			restaurantNameTextView = (TextView) convertView.findViewById(R.id.restaurantNameTextView);
			restaurantFavoriteCheckBox = (CheckBox) convertView.findViewById(R.id.favoriteCheckbox);
			convertView.setTag(new ViewHolder(restaurantNameTextView,restaurantFavoriteCheckBox));
		}
		else
		{
			ViewHolder holder = (ViewHolder) convertView.getTag();
			restaurantNameTextView = holder.getRestaurantNameTextView();
			restaurantFavoriteCheckBox = holder.getRestaurantFavoriteCheckBox();
		}

		final Restaurant restaurant = restaurants.get(position);
		restaurantNameTextView.setText(restaurant.getName());
		restaurantFavoriteCheckBox.setChecked(restaurant.isFavorited());

		restaurantFavoriteCheckBox.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				favorite(restaurant, restaurantFavoriteCheckBox.isChecked());
			}
		});

		return convertView;
	}

	private void favorite(Restaurant r, boolean favorited)
	{
		Log.d(TAG, "Set favorite " + r.toString() + " == " + favorited);

		final String WHERE = Restaurant.Columns._ID + " = ?";
		ContentResolver cr = context.getContentResolver();
		r.setFavorited(favorited);
		cr.update(DataProvider.CONTENT_URI, r.toContentValues(), WHERE, new String[] { "" + r.getId() });
	}
}
