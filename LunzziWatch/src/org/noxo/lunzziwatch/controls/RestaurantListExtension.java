package org.noxo.lunzziwatch.controls;

import java.util.ArrayList;
import java.util.List;

import org.noxo.lunzziwatch.MainActivity;
import org.noxo.lunzziwatch.R;
import org.noxo.lunzziwatch.content.DataProvider;
import org.noxo.lunzziwatch.model.Restaurant;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlListItem;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

public class RestaurantListExtension extends ManagedControlExtension {

	private List<Restaurant> restaurants;

	final String TAG = RestaurantListExtension.class.getSimpleName();
	
	private List<Restaurant> favorites;
	Bundle[] menuItems = new Bundle[1];
	
	public RestaurantListExtension(Context context, String hostAppPackageName,
			ControlManagerSmartWatch2 controlManager, Intent intent) {
		super(context, hostAppPackageName, controlManager, intent);
		menuItems[0] = new Bundle();
        menuItems[0].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, 0);
        menuItems[0].putString(Control.Intents.EXTRA_MENU_ITEM_ICON,
                ExtensionUtils.getUriString(mContext, R.drawable.actions_view_in_phone));
	}
	
    @Override
    public void onResume() {
    	Log.v(TAG, "onResume");
        restaurants = getFavoriteRestaurantFromDb();
        showLayout(R.layout.favorite_list, null);
        sendListCount(R.id.listView, restaurants.size());
        
    }
    
    @Override
    public void onRequestListItem(final int layoutReference, final int listItemPosition) {
        Log.d(TAG, "onRequestListItem() - position " + listItemPosition);
        if (layoutReference != -1 && listItemPosition != -1 && layoutReference == R.id.listView) {
            ControlListItem item = createListItem(listItemPosition);
            if (item != null) {
                sendListItem(item);
            }
        }
    }
    
    private List<Restaurant> getFavoriteRestaurantFromDb()
    {
    	final String WHERE = Restaurant.Columns.FAVORITED + " = 1";
    	favorites = new ArrayList<Restaurant>();
    	
    	ContentResolver cr = mContext.getContentResolver();
    	Cursor cursor = cr.query(DataProvider.CONTENT_URI, null, WHERE, null, null);
    	
    	while (cursor.moveToNext())
    	{
    		Restaurant r = Restaurant.fromCursor(cursor);
    		favorites.add(r);
    	}
    	
    	return favorites;
    }
    
    @Override
    public void onKey(int action, int keyCode, long timeStamp) {
    	if (action == Control.Intents.KEY_ACTION_RELEASE
                && keyCode == Control.KeyCodes.KEYCODE_OPTIONS) {
    		showMenu(menuItems);
        }
    }
    
    @Override
    public void onMenuItemSelected(int menuItem) {
    	Intent intent = new Intent(mContext, MainActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	mContext.startActivity(intent);
    }
    
	protected ControlListItem createListItem(int position) {

		Restaurant restaurant = restaurants.get(position);
		
		ControlListItem item = new ControlListItem();
		item.layoutReference = R.id.listView;
		item.dataXmlLayout = R.layout.favorite_row;
		item.listItemPosition = position;
		item.listItemId = position;

		int icon = R.drawable.icon_extension;
		// Icon data
		Bundle iconBundle = new Bundle();
		iconBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE,
				R.id.thumbnail);
		iconBundle.putString(Control.Intents.EXTRA_DATA_URI,
				ExtensionUtils.getUriString(mContext, icon));

		// Header data
		Bundle headerBundle = new Bundle();
		headerBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.title);
		headerBundle.putString(Control.Intents.EXTRA_TEXT, restaurant.getName());

		item.layoutData = new Bundle[3];
		item.layoutData[0] = iconBundle;
		item.layoutData[1] = headerBundle;

		return item;
	}
	
    @Override
    public void onListItemClick(final ControlListItem listItem, final int clickType,
            final int itemLayoutReference) {
        Log.d(TAG, "Item clicked. Position == " + listItem.listItemPosition);

        if (clickType == Control.Intents.CLICK_TYPE_SHORT) {
            Intent intent = new Intent(mContext, DailyMenuControl.class);
            Restaurant selected = favorites.get(listItem.listItemPosition);
            intent.putExtra(DailyMenuControl.EXTRA_RESTAURANT_ID, selected.getId());
            mControlManager.startControl(intent);
        }
    }
}
