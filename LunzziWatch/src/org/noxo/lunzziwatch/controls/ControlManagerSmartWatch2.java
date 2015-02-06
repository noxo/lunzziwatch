package org.noxo.lunzziwatch.controls;

import java.lang.reflect.Constructor;
import java.util.Stack;

import org.noxo.lunzziwatch.R;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlListItem;
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ControlManagerSmartWatch2 extends ControlManagerBase
{
	final String TAG = ControlManagerSmartWatch2.class.getSimpleName();
	
    private Stack<Intent> mControlStack;

    public ControlManagerSmartWatch2(Context context, String packageName) {
        super(context, packageName);
        mControlStack = new Stack<Intent>();
        // Create an initial control extension
        Intent initialListControlIntent = new Intent(mContext, RestaurantListExtension.class);
        mCurrentControl = createControl(initialListControlIntent);
    }
    
    public static int getSupportedControlWidth(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_width);
    }

    public static int getSupportedControlHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_height);
    }
    

    @Override
    public void onRequestListItem(int layoutReference, int listItemPosition) {
        Log.d(TAG, "onRequestListItem");
        if (mCurrentControl != null) {
            mCurrentControl.onRequestListItem(layoutReference, listItemPosition);
        }
    }

    @Override
    public void onListItemClick(ControlListItem listItem, int clickType, int itemLayoutReference) {
        Log.d(TAG, "onListItemClick");
        if (mCurrentControl != null) {
            mCurrentControl.onListItemClick(listItem, clickType, itemLayoutReference);
        }
    }

    @Override
    public void onListItemSelected(ControlListItem listItem) {
        Log.d(TAG, "onListItemSelected");
        if (mCurrentControl != null) {
            mCurrentControl.onListItemSelected(listItem);
        }
    }

    @Override
    public void onListRefreshRequest(int layoutReference) {
        Log.d(TAG, "onListRefreshRequest");
        if (mCurrentControl != null) {
            mCurrentControl.onListRefreshRequest(layoutReference);
        }
    }

    @Override
    public void onObjectClick(ControlObjectClickEvent event) {
        Log.d(TAG, "onObjectClick");
        if (mCurrentControl != null) {
            mCurrentControl.onObjectClick(event);
        }
    }

    @Override
    public void onKey(int action, int keyCode, long timeStamp) {
        Log.d(TAG, "onKey");

        if (action == Control.Intents.KEY_ACTION_RELEASE
                && keyCode == Control.KeyCodes.KEYCODE_BACK) {
            Log.d(TAG, "onKey() - back button intercepted.");
            onBack();
        } else if (mCurrentControl != null) {
            super.onKey(action, keyCode, timeStamp);
        }
    }

    @Override
    public void onMenuItemSelected(int menuItem) {
        Log.d(TAG, "onMenuItemSelected");
        if (mCurrentControl != null) {
            mCurrentControl.onMenuItemSelected(menuItem);
        }
    }

    /**
     * Closes the currently open control extension. If there is a control on the
     * back stack it is opened, otherwise extension is closed.
     */
    public void onBack() {
        Log.d(TAG, "onBack");
        if (!mControlStack.isEmpty()) {
            Intent backControl = mControlStack.pop();
            ControlExtension newControl = createControl(backControl);
            startControl(newControl);
        } else {
            stopRequest();
        }
    }

    /**
     * Start a new control. Any currently running control will be stopped and
     * put on the control extension stack.
     *
     * @param intent the Intent used to create the ManagedControlExtension. The
     *            intent must have the requested ManagedControlExtension as
     *            component, e.g. Intent intent = new Intent(mContext,
     *            CallLogDetailsControl.class);
     */
    public void startControl(Intent intent) {
        addCurrentToControlStack();
        ControlExtension newControl = createControl(intent);
        startControl(newControl);
    }

    public void addCurrentToControlStack() {
        if (mCurrentControl != null && mCurrentControl instanceof ManagedControlExtension) {
            
        	Intent intent = ((ManagedControlExtension) mCurrentControl).getIntent();
            boolean isNoHistory = intent.getBooleanExtra(
                    ManagedControlExtension.EXTENSION_NO_HISTORY,
                    false);
            
            if (isNoHistory) {
                // Not adding this control to history
                Log.d(TAG, "Not adding control to history stack");
            } else {
                Log.d(TAG, "Adding control to history stack");
                mControlStack.add(intent);
            }
            
        } else {
            Log.w(TAG,"ControlManageronly supports ManagedControlExtensions");
        }
    }

    private ControlExtension createControl(Intent intent) {
        
    	ComponentName component = intent.getComponent();
        
        try {
        	
            String className = component.getClassName();
            Log.d(TAG, "Class name:" + className);
            
            Class<?> clazz = Class.forName(className);
            Constructor<?> ctor = clazz.getConstructor(Context.class, String.class,
                    ControlManagerSmartWatch2.class, Intent.class);
            
            if (ctor == null) {
                return null;
            }
            
            Object object = ctor.newInstance(new Object[] {
                    mContext, mHostAppPackageName, this, intent
            });
            
            if (object instanceof ManagedControlExtension) {
                return (ManagedControlExtension) object;
            } else {
                Log.w(TAG,
                        "Created object not a ManagedControlException");
            }

        } catch (Exception e) {
        	Log.e(TAG, e.toString());
        }
        
        return null;
    }

}