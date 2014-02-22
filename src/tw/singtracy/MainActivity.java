package tw.singtracy;

import java.io.IOException;

import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.BadRequestException;
import com.kii.cloud.storage.exception.app.ConflictException;
import com.kii.cloud.storage.exception.app.ForbiddenException;
import com.kii.cloud.storage.exception.app.NotFoundException;
import com.kii.cloud.storage.exception.app.UnauthorizedException;
import com.kii.cloud.storage.exception.app.UndefinedException;

import tw.singtracy.utils.GCMRegisterHelper;
import tw.singtracy.utils.PlayList;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private static final int RESULTCODE_ACCESS_TOKEN = 1;
	private SharedPreferences pref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		String token = pref.getString("access_token", null);
		
		if(token == null)
			startActivityForResult(new Intent(this, LoginActivity.class), RESULTCODE_ACCESS_TOKEN);
		
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.btn_songs).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), ListSongActivity.class));
			}
		});
		findViewById(R.id.btn_playlist).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), PlayListActivity.class));
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		String token = pref.getString("access_token", null);
		if (token != null) {
			new LoginByTokenTask(){
				protected void onPostExecute(Void result) {
					PlayList.getInstance().refresh();
					GCMRegisterHelper helper = new GCMRegisterHelper();
					helper.initialize(getApplicationContext(), MainActivity.this);
				}
			}.execute();
		}
	}
	
	private class LoginByTokenTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
			String token = pref.getString("access_token", null);
			try {
				KiiUser.loginWithToken(token);
			} catch (Exception e) {
				
			}
			return null;
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == RESULTCODE_ACCESS_TOKEN){
			String token = data.getStringExtra("token");
			Log.v(TAG, "got token: " + token);
			pref.edit().putString("access_token", token).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
