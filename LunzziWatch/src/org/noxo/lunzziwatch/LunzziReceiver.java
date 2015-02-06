package org.noxo.lunzziwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LunzziReceiver extends BroadcastReceiver {

	final String TAG = LunzziReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		 intent.setClass(context, LunzziService.class);
	     context.startService(intent);
	}

}
