package tw.singtracy;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;

public class MainActivity extends Activity {
	private static final int RESULTCODE_ACCESS_TOKEN = 1;
	private SharedPreferences pref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		String token = pref.getString("access_token", null);
		
		//if(token == null)
			//startActivityForResult(new Intent(this, LoginActivity.class), RESULTCODE_ACCESS_TOKEN);
		startActivity(new Intent(this, AudioActivity.class));
		setContentView(R.layout.activity_main);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == RESULTCODE_ACCESS_TOKEN){
			String token = data.getStringExtra("token");
			pref.edit().putString("token", token).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
