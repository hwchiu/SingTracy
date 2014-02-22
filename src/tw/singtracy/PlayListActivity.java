package tw.singtracy;

import java.util.Observable;
import java.util.Observer;

import tw.singtracy.utils.PlayList;
import tw.singtracy.utils.Song;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PlayListActivity extends Activity implements Observer {
	private static final String TAG = "PlayListActivity";
	ListView lv;
	private SongsAdapter adapter;
	private ProgressDialog dialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_song);
		findViews();
		
		adapter = new SongsAdapter(getApplicationContext());
		PlayList.getInstance().addObserver(this);	
		if (PlayList.getInstance().getList() == null) {
			dialog  = new ProgressDialog(PlayListActivity.this);
			dialog.setMessage("Please wait...");
			dialog.show();
		} else {
			refreshData();
		}
	}
	
	private void findViews () {
		lv = (ListView) findViewById(R.id.lvSongs);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PlayList.getInstance().delete(position);
			}
		});
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				PlayList.getInstance().insert(position);
				return true;
			}
		});
	}
	
	private void refreshData () {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
		adapter.clear();
		adapter.addAll(PlayList.getInstance().getList());
		lv.setAdapter(adapter);
	}

	private class SongsAdapter extends ArrayAdapter<Song> {
		public SongsAdapter(Context context) {
			super(context, R.layout.song_item, R.id.txtSong);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);

			Song chapter = getItem(position);
			TextView txtChapter = (TextView)v.findViewById(R.id.txtSong);
			txtChapter.setText(chapter.songName);
			return v;
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		Log.d(TAG, "update called");
		refreshData();
	}
}
