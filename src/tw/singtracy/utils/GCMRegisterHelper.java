package tw.singtracy.utils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import tw.singtracy.MainActivity;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.AppException;
import com.kii.cloud.storage.exception.app.ConflictException;

public class GCMRegisterHelper {
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String TAG = "GCMRegisterHelper";
    
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    String regid;
	private Activity activity;
	
	public void initialize (Context context, Activity activity) {
		this.context = context;
		this.activity = activity;
		
        // Check device for Play Services APK.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(context);
            regid = getRegistrationId(context);

            if (regid.length() == 0) {
                Log.i(TAG, "regid is empty.");
                registerInBackground();
            } else {
            	installPush(regid);
            	Log.i(TAG, "regid is not empty.");
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
	}
	
	private String getRegistrationId(Context context2) {
		final SharedPreferences prefs = getGCMPreferences(context);
	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	    if (registrationId.length() == 0 ) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    /*
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = 1;
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return "";
	    }
	    */
	    return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return activity.getSharedPreferences(MainActivity.class.getSimpleName(),
	            Context.MODE_PRIVATE);
	}

	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(Constants.Sender_id);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    Log.d(TAG, msg);
                    return regid;
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                    Log.d(TAG, msg);
    				return null;
                }
			}
			
			protected void onPostExecute(String regid) {
				if (regid != null)
					installPush(regid);
				else
					Log.e(TAG, "regid == null");
			}
        }.execute();
	}
	
    private void installPush(final String regId) {
    	new AsyncTask<Void, Void, Void>() {
    		@Override
    		protected Void doInBackground(Void... params) {
    	        StringBuilder b = new StringBuilder("Installation ");
    	        try {
    	        	Log.d(TAG, "regid is :" + regId);
    	            KiiUser.pushInstallation().install(regId);
    	            b.append("succeeded.");
    	        } catch (ConflictException e) {
    	            b.append("already exist.");
    	        } catch (IOException e) {
    	            b.append("failed due to ");
    	            b.append(e.getMessage());
    	        } catch (AppException e) {
    	            b.append("failed due to ");
    	            b.append(e.getMessage());
    	        }
    	        Log.d(TAG, b.toString());
    	        return null;
    		}
    	}.execute();
    }
}
