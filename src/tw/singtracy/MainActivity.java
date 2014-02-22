package tw.singtracy;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {
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
	
	private MediaRecorder recorder;
	private boolean isRecording = false;
	private Socket senderSocket;
	
	public void record(View view) throws IOException{
		if(!this.isRecording) {
			this.startRecording(Environment.getExternalStorageDirectory().toString() + "/haha.3gpp");
		}
		else {
			this.stopRecording();
		}
	}
	
	protected void startRecording(String file) throws IOException{
		if(!this.isRecording) {
			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			// recorder.setOutputFile(file);
			senderSocket = new Socket("127.0.0.1", 8888);
			ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(senderSocket);
			
			recorder.setOutputFile(pfd.getFileDescriptor());
			try {
				recorder.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.isRecording = true;
			recorder.start();
		}
	}
	
	protected void stopRecording(){
		if(this.isRecording) {
			this.isRecording = false;
			try{
				recorder.stop();
			    recorder.release(); 
			    this.senderSocket.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}	

}
