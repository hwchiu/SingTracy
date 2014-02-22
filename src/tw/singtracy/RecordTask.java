package tw.singtracy;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class RecordTask extends AsyncTask<Void, Void, Void> {
	private MediaRecorder recorder;
	private Socket senderSocket;
	
	@Override
	protected Void doInBackground(Void... params) {
		Log.d("RecordTask", "doInBackground");
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		// recorder.setOutputFile(file);
		try {
			senderSocket = new Socket();
			senderSocket.connect(new InetSocketAddress(InetAddress.getByName("140.113.214.87"), 8888), 2000);
			ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(senderSocket);
		
			recorder.setOutputFile(pfd.getFileDescriptor());
			recorder.prepare();
		} catch (Exception e) {
			Log.e("doInBackground", "connection problem:" + e.getClass().getName() + ": " + e.getMessage());
		}

		recorder.start();
		return null;
	}
	
	//@Override
	protected void stop()  {
		Log.d("RecordTask", "stop");
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
