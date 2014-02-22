package tw.singtracy;

import java.io.IOException;
import java.util.List;

import tw.singtracy.utils.PlayList;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
		adapter.add(new Song("test", "this is the 1 song"));
		adapter.add(new Song("test1", "this is the 2 song"));
		adapter.add(new Song("test2", "this is the 3 song"));
		adapter.add(new Song("test3", "this is the 4 song"));
		KiiQuery all_query = new KiiQuery();

		try {
		  KiiQueryResult<KiiObject> result = Kii.bucket("Songs")
		          .query(all_query);
		  List<KiiObject> objLists = result.getResult();
		  for (KiiObject obj : objLists) {
			  Log.d(TAG, obj.toString());
		  }
		} catch (IOException e) {
		  // handle error
		} catch (AppException e) {
		  // handle error
		}
		adapter.notifyDataSetChanged();
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
