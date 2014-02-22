package tw.singtracy;

import tw.singtracy.utils.PlayList;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.PushMessageBundleHelper;
import com.kii.cloud.storage.PushMessageBundleHelper.MessageType;
import com.kii.cloud.storage.PushToAppMessage;
import com.kii.cloud.storage.ReceivedMessage;

public class PushNotificationReceiver extends BroadcastReceiver {

	private static final String TAG = "pushNotificationReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		String messageType = gcm.getMessageType(intent);
		if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
	    	//Get the message as a bundle.
	    	Bundle extras = intent.getExtras();
	
	    	// Get ReceiveMessage instance by parsing the bundle.
	    	ReceivedMessage message = PushMessageBundleHelper.parse(extras);
	
	    	// Get the sender of the push message.
	    	KiiUser sender = message.getSender();
	    	Log.d(TAG, sender.toString());
	    	
	    	// Determine the push notification type and start parsing.
	    	MessageType type = message.pushMessageType();
	    	switch (type) {
	    		case PUSH_TO_APP:
	    			PushToAppMessage pam = (PushToAppMessage) message;
	    			// Extract the target bucket and object.
	    			if (pam.containsKiiBucket()) {
	    				dealWithMessage(pam);
	    				// Extract more field values.
	    				long when = extras.getLong("when");
	    				String event_type = extras.getString("type");
	    				// ... and get more field values as needed.
	    			}
	    			break;
	
	    			// cases for PUSH_TO_USER and DIRECT_PUSH will follow...		
	    	}
		}
	}

	private void dealWithMessage(PushToAppMessage pam) {
		KiiBucket bucket = pam.getKiiBucket();
		Log.d(TAG, bucket.toString());
		if (pam.containsKiiObject()) {
			KiiObject obj = pam.getKiiObject();
			Log.d(TAG, obj.toString());
			PlayList.getInstance().refresh();
		}
	}

}
