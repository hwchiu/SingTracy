package tw.singtracy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tw.singtracy.utils.PlayList;
import tw.singtracy.utils.Song;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.exception.app.AppException;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

public class ListSongActivity extends Activity {
	private static final String TAG = "ListSongActivity";
	ListView lv;
	private SongsAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_song);
		findViews();
		grabFromKii();
	}

	private void grabFromKii() {
		new AsyncTask<Void, Void, ArrayList<Song>>() {
			private ProgressDialog dialog;
			
			protected void onPreExecute() {
				dialog = new ProgressDialog(ListSongActivity.this);
				dialog.setMessage("Please wait...");
				dialog.show();
			}
			
			@Override
			protected ArrayList<Song> doInBackground(Void... params) {
				KiiQuery all_query = new KiiQuery();
				ArrayList<Song> songs = new ArrayList<Song>();
				try {
					  KiiQueryResult<KiiObject> result = Kii.bucket("Songs")
					          .query(all_query);
					  List<KiiObject> objLists = result.getResult();
					  for (KiiObject obj : objLists) {
						  songs.add(new Song(obj.toUri().toString(), obj.getString("name")));
					  }
				} catch (IOException e) {
				  // handle error
				} catch (AppException e) {
				  // handle error
				}
				return songs;
			}
			
			protected void onPostExecute(ArrayList<Song> result) {
				dialog.dismiss();
				adapter.addAll(result);
			}
		}.execute();
		
	}
	
	private void findViews () {
		lv = (ListView) findViewById(R.id.lvSongs);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Song s = (Song) lv.getItemAtPosition(position);
				PlayList.getInstance().addLast(s);
				Toast.makeText(getApplicationContext(), "Added into queue", Toast.LENGTH_LONG).show();
			}
		});
		adapter = new SongsAdapter(getApplicationContext());
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
}
