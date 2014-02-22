package tw.singtracy.utils;

import java.util.ArrayList;

import tw.singtracy.Song;

public class PlayList {
	private ArrayList<Song> list = new ArrayList<Song>();
	private static PlayList instance = new PlayList();
	
	private PlayList () {}
	
	public static PlayList getInstance () {
		return instance;
	}
	
	public void addLast (Song song) {
		list.add(song);
	}
	
	public void addFront (Song song) {
		list.add(0, song);
	}
	
	public Song play () {
		Song top = list.get(0);
		list.remove(0);
		return top;
	}
	
	public void insert (int index) {
		Song s = list.get(index);
		list.remove(index);
		list.add(0, s);
	}
	
	public ArrayList<Song> getList () {
		return list;
	}

	public void delete(int position) {
		list.remove(position);
	}
}
