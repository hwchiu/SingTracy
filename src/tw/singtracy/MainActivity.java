package tw.singtracy;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import tw.singtracy.utils.GCMRegisterHelper;
import tw.singtracy.utils.PlayList;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.kii.cloud.storage.KiiUser;

public class MainActivity extends Activity implements Observer {
	private static final String TAG = "MainActivity";
	private static final int RESULTCODE_ACCESS_TOKEN = 1;
	private SharedPreferences pref;
	private String token;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		token = pref.getString("access_token", null);
		
		if(token == null)
			startActivityForResult(new Intent(this, LoginActivity.class), RESULTCODE_ACCESS_TOKEN);
		else
			Log.v(TAG, "Use token: " + token);

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
		
		// test video playback
		findViewById(R.id.playback_surface).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playURL("https://api-jp.kii.com/api/x/bkfkd9gkwwc4cdupfn4qkz221");
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
					PlayList.getInstance().addObserver(MainActivity.this);
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
	
	public void playURL(String url){
		MediaPlayer mp = new MediaPlayer();
		mp.setDisplay(((SurfaceView)findViewById(R.id.playback_surface)).getHolder());
		try {
			mp.setDataSource(url);
			mp.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.start();
				}
			});
			mp.prepareAsync();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private boolean isRecording = false;
	private RecordTask task;
	
	public void record(View view) throws IOException{
		if(!this.isRecording) {
			this.startRecording();
		}
		else {
			this.stopRecording();
		}
	}
	
	protected void startRecording() {
		if(!this.isRecording) {

			task = new RecordTask();
			this.isRecording = true;
			((Button) findViewById(R.id.record)).setText("Stop");
			task.execute();
		}
	}
	
	protected void stopRecording(){
		if(this.isRecording) {
			((Button) findViewById(R.id.record)).setText("Record");
			task.stop();
			task.cancel(true);
		}
	}

	@Override
	public void update(Observable observable, Object isMyTurn) {
		if (observable instanceof PlayList) {
			Boolean myturn = (Boolean) isMyTurn;
			if (myturn) {
				Toast.makeText(getApplicationContext(), "myTurn", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "notMyTurn", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
