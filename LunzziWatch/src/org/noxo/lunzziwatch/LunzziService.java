package org.noxo.lunzziwatch;

import org.noxo.lunzziwatch.controls.ControlManagerSmartWatch2;

import android.util.Log;

import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfoHelper;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

public class LunzziService extends ExtensionService {
	
	final String TAG = LunzziService.class.getSimpleName();
	
	 public LunzziService() {
		 super();
	 }
	 
	@Override
	protected RegistrationInformation getRegistrationInformation() {
		return new LunzziRegistrationInformation(getApplicationContext());
	}

	@Override
	protected boolean keepRunningWhenConnected() {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public ControlExtension createControlExtension(String hostAppPackageName) {
        
    	Log.d(TAG, "Service: createControlExtension");
        boolean advancedFeaturesSupported = DeviceInfoHelper.isSmartWatch2ApiAndScreenDetected(
                this, hostAppPackageName);
        
        if (advancedFeaturesSupported) {
            Log.d(TAG,
                    "Service: Advanced features supported, returning SmartWatch2 extension control manager");
            return new ControlManagerSmartWatch2(this, hostAppPackageName);
        } else {
            Log.d(TAG,
                    "Service: Advanced features not supported, exiting");
            throw new IllegalArgumentException("No control for: " + hostAppPackageName);
        }
        
    }
}
