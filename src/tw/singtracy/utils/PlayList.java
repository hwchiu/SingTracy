package tw.singtracy.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiObject;


public class PlayList {
	private ArrayList<Song> list = new ArrayList<Song>();
	private static PlayList instance = new PlayList();
	
	private PlayList () {
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				KiiObject object = Kii.bucket("Playlist").object();
				JSONArray array = object.getJSONArray("list", new JSONArray());
				for (int i = 0; i < array.length(); i ++) {
					Song s = new Song(array.optJSONObject(i).optString("key"), array.optJSONObject(i).optString("songName"));
					list.add(s);
				}
				return null;
			}
			protected void onPostExecute(Void result) {
				
			}
		}.execute();
	}
	
	public static PlayList getInstance () {
		return instance;
	}
	
	public void addLast (Song song) {
		list.add(song);
		update();
	}
	
	public void addFront (Song song) {
		list.add(0, song);
		update();
	}
	
	public Song play () {
		Song top = list.get(0);
		list.remove(0);
		update();
		return top;
	}
	
	public void insert (int index) {
		Song s = list.get(index);
		list.remove(index);
		list.add(0, s);
		update();
	}
	
	public ArrayList<Song> getList () {
		return list;
	}

	public void delete(int position) {
		list.remove(position);
		update();
	}
	
	private void update () {
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				KiiObject object = Kii.bucket("Playlist").object();
				JSONArray json = new JSONArray();
				for (Song song : list) {
					JSONObject obj = new JSONObject();
					try {
						obj.put("key", song.key);
						obj.putOpt("songName", song.songName);
					} catch (Exception e) {}
					json.put(obj);
				}
				object.set("list", json);
				return null;
			}
			protected void onPostExecute(Void result) {}
		}.execute();
	}
}
