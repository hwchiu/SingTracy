package tw.singtracy.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.os.AsyncTask;
import android.util.Log;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.query.KiiClause;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

public class Voter extends Observable{
	private static Voter instance = null;
	private final static String BUCKET_NAME = "Votes";
	private final static String TAG = "Voter";
	private final static int INIT_SCORE = 10;
	private ArrayList<Vote> votes = new ArrayList<Vote>();
	
	public class Vote {
		public int score;
		public String userName;
		public int timestamp;
	}
	
	public void initAfterSignedIn () {
		refresh();
		/*
		// setup subscribe
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				try {
					KiiUser user = KiiUser.getCurrentUser();
					KiiBucket bucket = Kii.bucket("Votes");
					user.pushSubscription().subscribeBucket(bucket);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			protected void onPostExecute(Void result) {
				refresh();
			}
		}.execute();
		*/
	}
	
	public static Voter getInstance() {
        if (instance == null) {
        	instance = new Voter();
        }
		return instance;
	}

	public void voteUp() {
		vote(2);
	}
	
	public void voteDown() {
		vote(-2);
	}
	
	private void vote (final int score) {
		Log.d(TAG, "updating");
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				KiiObject object = Kii.bucket(BUCKET_NAME).object();
				object.set("score", score);
				object.set("userName", KiiUser.getCurrentUser().getUsername());
				object.set("time", (int) (new java.util.Date().getTime() / 1000));
				Log.d(TAG, "sending");
				try {
					object.save();
				} catch (Exception e) {
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
	
	public void clearVotes () {
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				KiiQueryResult<KiiObject> result = null;
				try {
					result = Kii.bucket(BUCKET_NAME)
				          .query(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
				List<KiiObject> objLists = result.getResult();
				for (KiiObject obj : objLists) {
					try {
						obj.delete();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return null;
			}
			
			protected void onPostExecute(Void result) {
				//notifyObservers();
			}
		}.execute();
	}

	public void refresh() {
		Log.d(TAG, "voter needs to refresh");
		votes.clear();
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				KiiQuery query = new KiiQuery();
				query.sortByAsc("time");
				KiiQueryResult<KiiObject> result = null;
				try {
					result = Kii.bucket(BUCKET_NAME)
				          .query(query);
				} catch (Exception e) {
					e.printStackTrace();
				}
				List<KiiObject> objLists = result.getResult();
				int nowScore = INIT_SCORE;
				for (KiiObject obj : objLists) {
					Vote v = new Vote();
					v.score = obj.getInt("score");
					v.timestamp = obj.getInt("time");
					v.userName = obj.getString("userName");
					
					nowScore += v.score;
					v.score = nowScore;
					votes.add(v);
					Log.d(TAG, "vote add: " + v.score);
				}
				return null;
			}
			
			protected void onPostExecute(Void result) {
				notifyObservers();
			}
		}.execute();
	}
	
	@Override
	public boolean hasChanged() {
		return true;
	}

	public ArrayList<Vote> getVotes() {
		return votes;
	}
}
