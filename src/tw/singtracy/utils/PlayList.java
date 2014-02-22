package tw.singtracy.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.BadRequestException;
import com.kii.cloud.storage.exception.app.ConflictException;
import com.kii.cloud.storage.exception.app.ForbiddenException;
import com.kii.cloud.storage.exception.app.NotFoundException;
import com.kii.cloud.storage.exception.app.UnauthorizedException;
import com.kii.cloud.storage.exception.app.UndefinedException;

public class PlayList extends Observable {
	private static final String ID_IN_BUCKET = "1402ec97-d210-4c19-9ecb-3fe04bba0083";
	protected static final String TAG = "PlayList";
	private ArrayList<Song> list = null;
	private static PlayList instance = null;
	private String lastSong = null; 
	
	private PlayList () {
		final KiiUser user = KiiUser.getCurrentUser();
		final KiiBucket bucket = Kii.bucket("Playlist");
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				try {
					user.pushSubscription().subscribeBucket(bucket);
				} catch (BadRequestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnauthorizedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ForbiddenException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ConflictException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UndefinedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}
	
	public static PlayList getInstance () {
		if (instance == null) {
			 instance = new PlayList();
		}
		return instance;
	}
	
	public void addLast (Song song) {
		KiiUser user = KiiUser.getCurrentUser();
		song.userName = user.getUsername();
		list.add(song);
		update();
	}
	
	public void addFront (Song song) {
		KiiUser user = KiiUser.getCurrentUser();
		song.userName = user.getUsername();
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
		if (list.size() >= 2) 
			list.add(1, s);
		update();
	}
	
	public ArrayList<Song> getList () {
		return list;
	}
	
	@Override
	public boolean hasChanged() {
		return true;
	}

	public void refresh() {
		Log.d(TAG, "refreshing");
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				KiiObject object = Kii.bucket("Playlist").object(ID_IN_BUCKET);
				try {
					object.refresh();
					JSONArray array = object.getJSONArray("list", new JSONArray());
					Log.d(TAG, object.toString());
					Log.d(TAG, array.toString());
					list = new ArrayList<Song>();
					for (int i = 0; i < array.length(); i ++) {
						Song s = new Song(
								array.optJSONObject(i).optString("key"),
								array.optJSONObject(i).optString("url"), 
								array.optJSONObject(i).optString("songName"),
								array.optJSONObject(i).optString("userName"));
						list.add(s);
					}
				} catch (BadRequestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnauthorizedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ForbiddenException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ConflictException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UndefinedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			
			protected void onPostExecute(Void result) {
				notifyObservers(itsMyTurn());
				lastSong = nowPlayingKey();
			}
		}.execute();
	}
	
	public Song nowPlaying () {
		return list.size() == 0 ? null : list.get(0);
	}
	
	public String nowPlayingKey () {
		return nowPlaying() == null ? null : nowPlaying().key;
	}
	
	protected boolean queueChanged () {
		return lastSong != nowPlayingKey(); 
	}
	
	public boolean itsMyTurn () {
		return nowPlaying() != null && nowPlaying().userName.equals(KiiUser.getCurrentUser().getUsername());
	}

	public void delete(int position) {
		list.remove(position);
		update();
	}
	
	public void update () {
		Log.d(TAG, "updating");
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				KiiObject object = Kii.bucket("Playlist").object(ID_IN_BUCKET);
				JSONArray json = new JSONArray();
				for (Song song : list) {
					JSONObject obj = new JSONObject();
					try {
						obj.put("key", song.key);
						obj.put("url", song.url);
						obj.put("songName", song.songName);
						obj.put("userName", song.userName);
					} catch (Exception e) {}
					json.put(obj);
				}
				object.set("list", json);
				Log.d(TAG, "updating");
				try {
					object.save();
				} catch (BadRequestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ConflictException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ForbiddenException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnauthorizedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UndefinedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			
			protected void onPostExecute(Void result) {
				//notifyObservers();
			}
		}.execute();
	}
}
