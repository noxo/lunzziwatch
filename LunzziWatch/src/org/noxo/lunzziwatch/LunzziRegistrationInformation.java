package org.noxo.lunzziwatch;

import java.util.UUID;

import org.noxo.lunzziwatch.controls.ControlManagerSmartWatch2;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

public class LunzziRegistrationInformation extends RegistrationInformation {
	
	final String TAG = LunzziRegistrationInformation.class.getSimpleName();
	
    final Context mContext;
    private String extensionKey;
    private static final String EXTENSION_KEY_PREF = "EXTENSION_KEY_PREF";
    
	protected LunzziRegistrationInformation(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        mContext = context;
    }
	
	@Override
	public int getRequiredNotificationApiVersion() {
		return API_NOT_REQUIRED;
	}

	@Override
	public ContentValues getExtensionRegistrationConfiguration() {
		
		Log.d(TAG, "getExtensionRegistrationConfiguration");
		
        String iconHostapp = ExtensionUtils.getUriString(mContext, R.drawable.ic_launcher);
        String iconExtension = ExtensionUtils.getUriString(mContext, R.drawable.icon_extension);

        ContentValues values = new ContentValues();

        values.put(Registration.ExtensionColumns.CONFIGURATION_ACTIVITY,
                MainActivity.class.getName());
        values.put(Registration.ExtensionColumns.CONFIGURATION_TEXT,
                mContext.getString(R.string.configuration_text));
        values.put(Registration.ExtensionColumns.NAME, mContext.getString(R.string.extension_name));
        values.put(Registration.ExtensionColumns.EXTENSION_KEY, getExtensionKey());
        values.put(Registration.ExtensionColumns.HOST_APP_ICON_URI, iconHostapp);
        values.put(Registration.ExtensionColumns.EXTENSION_ICON_URI, iconExtension);
        values.put(Registration.ExtensionColumns.EXTENSION_48PX_ICON_URI, iconExtension);
        values.put(Registration.ExtensionColumns.NOTIFICATION_API_VERSION,
                getRequiredNotificationApiVersion());
        values.put(Registration.ExtensionColumns.PACKAGE_NAME, mContext.getPackageName());

        return values;
	}

	@Override
	public boolean controlInterceptsBackButton() {
		// Extension has it's own navigation, handles back presses.
	    return true;
	}
	   
	@Override
	public int getRequiredWidgetApiVersion() {
		return API_NOT_REQUIRED;
	}

	@Override
	public int getRequiredControlApiVersion() {
		return 2;
	}

	@Override
	public int getRequiredSensorApiVersion() {
		return API_NOT_REQUIRED;
	}
	
    @Override
    public boolean isDisplaySizeSupported(int width, int height) {
    	boolean supported = ((width == ControlManagerSmartWatch2.getSupportedControlWidth(mContext) && height == ControlManagerSmartWatch2
                .getSupportedControlHeight(mContext)));
        Log.d(TAG,"isDisplaySizeSupported: " + supported);
        return supported;
    }
    
	@Override
	public String getExtensionKey() {
		if (TextUtils.isEmpty(extensionKey)) {
            // Retrieve key from preferences
            SharedPreferences pref = mContext.getSharedPreferences(EXTENSION_KEY_PREF,
                    Context.MODE_PRIVATE);
            extensionKey = pref.getString(EXTENSION_KEY_PREF, null);
            if (TextUtils.isEmpty(extensionKey)) {
                // Generate a random key if not found
                extensionKey = UUID.randomUUID().toString();
                pref.edit().putString(EXTENSION_KEY_PREF, extensionKey).commit();
            }
        }
        return extensionKey;
	}

}
