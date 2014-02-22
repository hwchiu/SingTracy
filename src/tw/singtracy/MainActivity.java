package tw.singtracy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
