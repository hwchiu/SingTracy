package tw.singtracy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import tw.singtracy.utils.GCMRegisterHelper;
import tw.singtracy.utils.PlayList;
import tw.singtracy.utils.Voter;
import tw.singtracy.utils.Voter.Vote;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.kii.cloud.storage.KiiUser;

public class MainActivity extends ActionBarActivity implements Observer,TabListener {
	private static final String TAG = "MainActivity";
	private static final int RESULTCODE_ACCESS_TOKEN = 1;
	private SharedPreferences pref;
	private String token;
	private MediaPlayer mp = new MediaPlayer();
	private boolean muted = false;
	private ActionBar bar;

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
		layoutSettings();
		setListeners();
	}
	
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	            Log.d("SimpleActionBarTabsActivity","tab " 
	                    + String.valueOf(tab.getPosition()) + " re-clicked");
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		Log.d("SimpleActionBarTabsActivity","tab " 
	                   + String.valueOf(tab.getPosition()) + " clicked");
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	          Log.d("SimpleActionBarTabsActivity","tab " 
	                  + String.valueOf(tab.getPosition()) + " un-clicked");
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

					Voter.getInstance().addObserver(MainActivity.this);
					Voter.getInstance().initAfterSignedIn();
					initializeGraph();
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
	
	private void playURL (String url) {
		try {
			mp.setDataSource(url);
			mp.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					startRecording();
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
	
	private void layoutSettings() {
		bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    bar.setDisplayShowTitleEnabled(true);
	    bar.setDisplayShowHomeEnabled(true);
	    
	    ActionBar.Tab newTab0 = bar.newTab();
    	newTab0.setText("Tab 0 title");
    	newTab0.setTabListener(this);
    	ActionBar.Tab newTab1 = bar.newTab();
    	newTab1.setText("Tab 1 title");
    	newTab1.setTabListener(this);
    	
    	bar.addTab(newTab0);
    	bar.addTab(newTab1);    
	}
	
	private void setListeners() {
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
		
		findViewById(R.id.playback_surface).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// mute or unmute
				muted = !muted;
				float vol = muted? 0f : 0.5f;
				mp.setVolume(vol, vol);
			}
		});

		findViewById(R.id.btn_votedown).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Voter.getInstance().voteDown();
			}
		});

		findViewById(R.id.btn_voteup).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Voter.getInstance().voteUp();
			}
		});
		
		((Button)findViewById(R.id.btn_voteup)).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				Voter.getInstance().clearVotes();
				return true;
			}
		});
		
		((SurfaceView)findViewById(R.id.playback_surface)).getHolder().addCallback(new SurfaceHolder.Callback() {	
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				mp.setDisplay(holder);
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {
			}
		});
	}
	
	private boolean isRecording = false;
	private RecordTask task;
	
	public void record(View view) {
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
			((Button) findViewById(R.id.record)).setText("Stop");
			this.isRecording = true;
			task.execute();
		}
	}
	
	protected void stopRecording(){
		if(this.isRecording) {
			((Button) findViewById(R.id.record)).setText("Record");
			this.isRecording = false;
			task.stop();
		}
	}
	
	private void initializeGraph () {
		Timer autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                    	Voter.getInstance().refresh();
                    }
                });
            }
        }, 0, 2 * 1000);
		
	}
	
	private void refreshGraph () {
		ArrayList<Vote> votes = Voter.getInstance().getVotes();
		if (votes.size() > 0) {
			GraphViewData[] datas = new GraphViewData[votes.size()];
				int i = 0;
				int now = (int)(new java.util.Date().getTime() / 1000), 
					lasttimestamp = votes.get(0).timestamp - now;
				for (Vote v : votes) {
					int vTimestamp = v.timestamp;
					Log.d(TAG, "V.timestamp: " + vTimestamp + ", now: " + now);
					vTimestamp -= now;
					Log.d(TAG, "vV.timestamp: " + vTimestamp);
					datas[i] = new GraphViewData(vTimestamp, v.score);
					lasttimestamp = vTimestamp;
					i++;
				}
			GraphViewSeries exampleSeries = new GraphViewSeries(datas);
	
			GraphView graphView = new LineGraphView(
			      MainActivity.this // context
			      , "投票結果" // heading
			);
			graphView.addSeries(exampleSeries); // data
			graphView.setViewPort(lasttimestamp - 30, 30);
			graphView.setScrollable(true);
			
			LinearLayout layout = (LinearLayout) findViewById(R.id.graph1);
			layout.removeAllViewsInLayout();
			layout.addView(graphView);
		}
	}

	@Override
	public void update(Observable observable, Object isMyTurn) {
		if (observable instanceof PlayList) {
			Boolean myturn = (Boolean) isMyTurn;
			if (myturn) {
				Toast.makeText(getApplicationContext(), "myTurn", Toast.LENGTH_SHORT).show();
				// Start record mode
				playURL(((PlayList) observable).nowPlaying().url);
			} else {
				Toast.makeText(getApplicationContext(), "notMyTurn", Toast.LENGTH_SHORT).show();
			}
		} else if (observable instanceof Voter) {
			refreshGraph();
		}
	}
}
